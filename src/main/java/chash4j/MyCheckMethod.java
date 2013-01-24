package chash4j;

import java.util.Map;
import java.util.SortedMap;

public class MyCheckMethod implements CheckMethod {

    private SortedMap<Long, String> nodeMap = null;

    private Map<String, long[]> targetMap = null;

    private Map<String, long[]> invalidTargetMap = null;

    private NodeCheckMethod nodeCheckMethod = null;

    private MyLogger logger = MyLoggerFactory.getLogger(this.getClass().getName());

    public MyCheckMethod(
            SortedMap<Long, String> nodeMap,
            Map<String, long[]> targetMap,
            Map<String, long[]> invalidTargetMap,
            NodeCheckMethod nodeCheckMethod) {
        this.nodeMap = nodeMap;
        this.targetMap = targetMap;
        this.invalidTargetMap = invalidTargetMap;
        this.nodeCheckMethod = nodeCheckMethod;
    }

    public void check() {
        for (Map.Entry<String, long[]> entry : targetMap.entrySet()) {
            String target = entry.getKey();
            if (!nodeCheckMethod.check(target)) {
                logger.info("Removing invalid target '" + target + "'...");
                long[] nodeKeys = entry.getValue();
                for (long nodeKey : nodeKeys) {
                    this.nodeMap.remove(nodeKey);
                }
                this.invalidTargetMap.put(target, nodeKeys);
                this.targetMap.remove(target);
            }
        }
        for (Map.Entry<String, long[]> entry : invalidTargetMap.entrySet()) {
            String target = entry.getKey();
            if (nodeCheckMethod.check(target)) {
                logger.info("Adding valid target '" + target + "'...");
                long[] nodeKeys = entry.getValue();
                for (long nodeKey : nodeKeys) {
                    this.nodeMap.put(nodeKey, target);
                }
                this.targetMap.put(target, nodeKeys);
                this.invalidTargetMap.remove(target);
            }
        }
    }

}
