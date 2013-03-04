package chash4j;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentNavigableMap;
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

    public Set<String> getTargets(String key, int number) throws CHashException {
        long nodeKey = Hash.getHashForKey(key);
        ConcurrentNavigableMap<Long, String> matchedSubMap = this.nodeMap.tailMap(nodeKey);
        LinkedHashSet<String> results = new LinkedHashSet<String>();
        if (matchedSubMap.isEmpty()) {
            matchedSubMap = this.nodeMap;
        }
        if (number <= 0) {
            number = 1;
        }
        int targetNumber = targetMap.size();
        if (number > targetNumber) {
            number = targetNumber;
        }
        for (Entry<Long, String> matchedEntry : matchedSubMap.entrySet()) {
            results.add(matchedEntry.getValue());
            if (results.size() == number) {
                break;
            }
        }
        if (results.size() < number) {
            for (Entry<Long, String> entry : this.nodeMap.entrySet()) {
                results.add(entry.getValue());
                if (results.size() == number) {
                    break;
                }
            }
        }
        return results;
    }
}
