package chash4j;

import chash4j.CHashException;
import chash4j.HashRingStatus;
import chash4j.NodeCheckMethod;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.Callable;

public final class SimpleHashRing implements HashRing {
    
	public static final short DEFAULT_SHADOW_NUMBER = 1000;

    private ConcurrentSkipListMap<Long, String> nodeMap = null;

    private ConcurrentHashMap<String, long[]> targetMap = null;

    private ConcurrentHashMap<String, long[]> invalidTargetMap = null;

    private Lock lock = null;

    private HashRingStatus status = HashRingStatus.UNINITIALIZED;

    private short shadowNumber = 1;

    private Checker checker = null;

    private MyLogger logger = MyLoggerFactory.getLogger(this.getClass().getName());

    public SimpleHashRing() {
        initialize();
    }

    private void initialize() {
        this.nodeMap = new ConcurrentSkipListMap<Long, String>();
        this.targetMap = new ConcurrentHashMap<String, long[]>();
        this.invalidTargetMap = new ConcurrentHashMap<String, long[]>();
        this.lock = new ReentrantLock();
        this.shadowNumber = DEFAULT_SHADOW_NUMBER;
        this.status = HashRingStatus.INITIALIZED;
    }

    public void build(short shadowNumber) throws CHashException {
        switch (this.status) {
            case UNINITIALIZED:
            case DESTROYED:
                initialize();
            case INITIALIZED:
                if (shadowNumber > 0) {
                    this.shadowNumber = shadowNumber;
                }
                this.status = HashRingStatus.BUILDED;
                break;
            default:
                String errorMsg = "Please destroy hash ring before rebuilding.";
                logger.error(errorMsg);
                throw new CHashException(errorMsg);
        }
    }

    public void destroy() throws CHashException {
        switch (this.status) {
            case INITIALIZED:
            case BUILDED:
                this.nodeMap = null;
                this.targetMap = null;
                this.invalidTargetMap = null;
                this.lock = null;
                this.shadowNumber = 1;
                this.stopCheck();
                this.status = HashRingStatus.DESTROYED;
            default:
                String warningMsg = "The hash ring were not builded. IGNORE the destroy operation.";
                logger.warn(warningMsg);
        }
    }

    public HashRingStatus status() {
        return this.status;
    }

    public void check(NodeCheckMethod nodeCheckMethod) throws CHashException {
        MyCheckMethod myCheckMethod =
                new MyCheckMethod(this.nodeMap, this.targetMap, this.invalidTargetMap, nodeCheckMethod);
        myCheckMethod.check();
    }

    public boolean startCheck(
            final NodeCheckMethod nodeCheckMethod,
            short intervalSeconds) throws CHashException {
        if (this.status != HashRingStatus.BUILDED) {
            logger.warn("The hash ring were not builded. IGNORE the checker startup.");
            return false;
        }
        MyCheckMethod myCheckMethod =
                new MyCheckMethod(this.nodeMap, this.targetMap, this.invalidTargetMap, nodeCheckMethod);
        this.checker = new CycleChecker();
        return this.checker.start(myCheckMethod);
    }

    public boolean stopCheck() throws CHashException {
        if (this.checker == null) {
            return false;
        }
        return this.checker.stop();
    }

    public boolean inChecking() {
        if (this.checker == null) {
            return false;
        }
        return this.checker.inChecking();
    }

    public boolean addTarget(String target) throws CHashException {
        int currentShadowNumber = (int) this.shadowNumber;
        String[] targetShadows = new String[currentShadowNumber];
        for (int i = 0; i < currentShadowNumber; i++) {
            targetShadows[i] = target + "-" + i;
        }
        int total = (currentShadowNumber * Hash.KETAMA_NUMBERS_LENGTH);
        Map<Long, String> nodeAll = new HashMap<Long, String>();
        long[] nodeKeyAll = new long[total];
        int count = 0;
        for (String targetShadow : targetShadows) {
            long[] nodeKeys = Hash.getKetamaNumbers(targetShadow);
            for (long nodeKey : nodeKeys) {
                nodeAll.put(nodeKey, target);
                nodeKeyAll[count] = nodeKey;
                count++;
            }
        }
        this.nodeMap.putAll(nodeAll);
        this.targetMap.put(target, nodeKeyAll);
        return true;
    }

    public boolean removeTarget(String target) throws CHashException {
        long[] nodeKeys = this.targetMap.get(target);
        if (nodeKeys == null) {
            nodeKeys = this.invalidTargetMap.get(target);
        }
        if (nodeKeys == null || nodeKeys.length == 0) {
            return false;
        }
        for (long nodeKey: nodeKeys) {
            this.nodeMap.remove(nodeKey);
        }
        this.targetMap.remove(target);
        this.invalidTargetMap.remove(target);
        return true;
    }

    public String getTarget(String key) throws CHashException {
        long nodeKey = Hash.getHashForKey(key);
        Entry<Long, String> matchedEntry = this.nodeMap.ceilingEntry(nodeKey);
        if (matchedEntry != null) {
            return matchedEntry.getValue();
        }
        matchedEntry = this.nodeMap.firstEntry();
        if (matchedEntry != null) {
            return matchedEntry.getValue();
        }
        return null;
    }
}
