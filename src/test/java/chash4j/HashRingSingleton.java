package chash4j;

import static org.junit.Assert.fail;

public enum HashRingSingleton {
    INSTANCE;

    private SimpleHashRing hashRing = null;

    HashRingSingleton() {
        String[] servers =
                new String[]{"10.11.156.71:2181", "10.11.5.145:2181", "10.11.5.164:2181", "192.168.106.63:2181", "192.168.106.64:2181"};
        boolean result = false;
        String failingMsg = null;
        try {
            this.hashRing = new SimpleHashRing();
            if (hashRing.status() != HashRingStatus.UNINITIALIZED) {
                failingMsg = "The status '" + hashRing.status() + "' should '" + HashRingStatus.UNINITIALIZED + "'. ";
                System.err.println(failingMsg);
                fail(failingMsg);
            }
            hashRing.build((short) 500);
            System.out.println("The hash ring is builded.");
            if (hashRing.status() != HashRingStatus.BUILDED) {
                failingMsg = "The status '" + hashRing.status() + "' should '" + HashRingStatus.BUILDED + "'. ";
                System.err.println(failingMsg);
                fail(failingMsg);
            }
            for (String server : servers) {
                result = hashRing.addTarget(server);
                if (!result) {
                    failingMsg = "Adding server '" + server + "' is FAILING. ";
                    System.err.println(failingMsg);
                    fail(failingMsg);
                }
            }
        } catch (Exception e) {
            String errorMsg = "Error: " + e.getMessage() +".";
            System.err.println(errorMsg);
            fail(errorMsg);
        }
    }

    public SimpleHashRing getHashRing() {
        return this.hashRing;
    }
}
