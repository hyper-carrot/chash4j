package chash4j;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Random;

import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class SimpleHashRingBenchmark {

    @Test
    public void benchmarkSimpleHashRing() {
        boolean debugTag = false;
        String[] servers =
                new String[]{"10.11.156.71:2181", "10.11.5.145:2181", "10.11.5.164:2181", "192.168.106.63:2181", "192.168.106.64:2181"};
        boolean result = false;
        String failingMsg = null;
        SimpleHashRing shr = null;
        try {
            shr = new SimpleHashRing();
            if (shr.status() != HashRingStatus.UNINITIALIZED) {
                failingMsg = "The status '" + shr.status() + "' should '" + HashRingStatus.UNINITIALIZED + "'. ";
                System.err.println(failingMsg);
                fail(failingMsg);
            }
            shr.build((short) 500);
            System.out.println("The hash ring is builded.");
            if (shr.status() != HashRingStatus.BUILDED) {
                failingMsg = "The status '" + shr.status() + "' should '" + HashRingStatus.BUILDED + "'. ";
                System.err.println(failingMsg);
                fail(failingMsg);
            }
            for (String server : servers) {
                result = shr.addTarget(server);
                if (!result) {
                    failingMsg = "Adding server '" + server + "' is FAILING. ";
                    System.err.println(failingMsg);
                    fail(failingMsg);
                }
            }
        } catch (Exception e) {
            String errorMsg = "Hash Ring Build Error: " + e.getMessage() +".";
            System.err.println(errorMsg);
            fail(errorMsg);
        }
        int[] loopNumbers = new int[]{10000, 20000, 50000, 100000, 200000, 300000, 400000, 500000};
        for (int loopNumber : loopNumbers) {
            String[] keys = new String[loopNumber];
            for (int i = 0; i < loopNumber; i++) {
                keys[i] = getRandomKey();
            }
            try {
                String key = null;
                String target = null;
                long ns1 = System.nanoTime();
                for (int i = 0; i < loopNumber; i++) {
                    key = keys[i];
                    target = shr.getTarget(key);
                    if (debugTag) {
                        System.out.printf("The target of key '%s' is %s. (%d)\n", key, target, i);
                    }
                    assertNotNull(target);
                }
                long ns2 = System.nanoTime();
                long totalCostNs = ns2 - ns1;
                float totalCost = totalCostNs / 1000f;
                float eachCost = totalCost / (float) loopNumber;
                System.out.printf("Benchmark Result (loopNumber=%d) - Total cost (microsecond): %f, Each cost (microsecond): %f.\n", loopNumber, totalCost, eachCost);
            } catch (Exception e) {
                String errorMsg = "Error: " + e.getMessage() +".";
                System.err.println(errorMsg);
                fail(errorMsg);
            }
        }
        try {
            shr.destroy();
            System.out.println("The hash ring is destroyed.");
            if (shr.status() != HashRingStatus.DESTROYED) {
                failingMsg = "The status '" + shr.status() + "' should '" + HashRingStatus.DESTROYED + "'. ";
                System.err.println(failingMsg);
                fail(failingMsg);
            }
        } catch (Exception e) {
            String errorMsg = "Destroy Error: " + e.getMessage() +".";
            System.err.println(errorMsg);
            fail(errorMsg);
        }
    }

    private String getRandomKey() {
        String chars = "abcdefghijklmnopqrstuvwxyz-_#=+ABCDEFJHIJKLMNOPQRSTUVWXYZ";
        int mode = chars.length();
        int keyLength = 10;
        Random random = new Random();
        StringBuilder buffer = new StringBuilder();
        int index = 0;
        for (int i = 0; i < keyLength; i++) {
            index = Math.abs(random.nextInt()) % mode;
            buffer.append(chars.charAt(index));
        }
        return buffer.toString();
    }
}
