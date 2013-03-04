package chash4j;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Map;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

public final class SimpleHashRing implements HashRing {
    
	public static final short DEFAULT_SHADOW_NUMBER = 1000;

    private ConcurrentSkipListMap<Long, String> nodeMap = null;

    private ConcurrentHashMap<String, long[]> targetMap = null;

    private ConcurrentHashMap<String, long[]> invalidTargetMap = null;

    private HashRingStatus status = HashRingStatus.UNINITIALIZED;

    private short shadowNumber = 1;

    private Checker checker = null;

    private MyLogger logger = MyLoggerFactory.getLogger(this.getClass().getName());

    public SimpleHashRing() {
        //
    }

    private void initialize() {
        this.nodeMap = new ConcurrentSkipListMap<Long, String>();
        this.targetMap = new ConcurrentHashMap<String, long[]>();
        this.invalidTargetMap = new ConcurrentHashMap<String, long[]>();
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
                this.shadowNumber = 1;
                this.stopCheck();
                this.status = HashRingStatus.DESTROYED;
                break;
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
        return this.checker != null && this.checker.stop();
    }

    public boolean inChecking() {
        return this.checker != null && this.checker.inChecking();
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
        List<String> results = getTargets(key, 1);
        if (results.isEmpty()) {
            return null;
        } else {
            return results.get(0);
        }
    }

    public List<String> getTargets(String key, int number) throws CHashException {
        List<String> results = new ArrayList<String>();
        if (key == null || key.length() == 0) {
            return results;
        }
        if (number <= 0) {
            number = 1;
        }
        int targetNumber = targetMap.size();
        if (number > targetNumber) {
            number = targetNumber;
        }
        long keyHash = Hash.getHashForKey(key);
        long currentKeyHash = keyHash;
        String tempTarget = null;
        while (results.size() < number) {
            Entry<Long, String> matchedEntry = this.nodeMap.ceilingEntry(currentKeyHash);
            if (matchedEntry == null) {
                matchedEntry = this.nodeMap.firstEntry();
            }
            tempTarget = matchedEntry.getValue();
            if (!results.contains(tempTarget)) {
                results.add(tempTarget);
            }
            currentKeyHash = matchedEntry.getKey() + 1;
        }
        return results;
    }
}
