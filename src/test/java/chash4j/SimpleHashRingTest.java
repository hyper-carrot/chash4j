package chash4j;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class SimpleHashRingTest {

    @Test
    public void testSimpleHashRing() {
        String[] servers = new String[]{"10.11.156.71:2181", "10.11.5.145:2181", "10.11.5.164:2181", "192.168.106.63:2181", "192.168.106.64:2181"};

        String failingMsg = null;
        try {
            SimpleHashRing shr = new SimpleHashRing();
            if (shr.status() != HashRingStatus.UNINITIALIZED) {
                failingMsg = "The status '" + shr.status() + "' should '" + HashRingStatus.UNINITIALIZED + "'. ";
                System.err.println(failingMsg);
                fail(failingMsg);
            }
            // begin - test about init & build & add target
            shr.build((short) 500);
            System.out.println("The hash ring is builded.");
            if (shr.status() != HashRingStatus.BUILDED) {
                failingMsg = "The status '" + shr.status() + "' should '" + HashRingStatus.BUILDED + "'. ";
                System.err.println(failingMsg);
                fail(failingMsg);
            }
            StringBuilder msgBuffer = new StringBuilder();
            msgBuffer.append("[");
            for (int i = 0; i < servers.length; i++) {
                msgBuffer.append(servers[i]);
                if (i < servers.length - 1) {
                    msgBuffer.append(",");
                }
            }
            msgBuffer.append("]");
            System.out.printf("Add servers (%s)...\n", msgBuffer.toString());
            boolean result = false;
            for (String server : servers) {
                result = shr.addTarget(server);
                if (!result) {
                    failingMsg = "Adding server '" + server + "' is FAILING. ";
                    System.err.println(failingMsg);
                    fail(failingMsg);
                }
            }
            // end - test about init & build & add target
            // begin - test about check
            NodeCheckMethod nodeCheckMethod = new NodeCheckMethod() {

                public boolean check(String target) {
                    if (target == null || target.length() == 0) {
                        return false;
                    }
                    return true;
                }
            };
            shr.check(nodeCheckMethod);
            System.out.println("Check is OK.");
            result = shr.startCheck(nodeCheckMethod, (short) 1);
            if (!result) {
                failingMsg = "Starting Checker is FAILING. ";
                System.err.println(failingMsg);
                fail(failingMsg);
            }
            System.out.println("The checker is started.");
            if (!shr.inChecking()) {
                failingMsg = "The checker should be started.";
                System.err.println(failingMsg);
                fail(failingMsg);
            }
            Thread.sleep(3 * 1000);
            result = shr.stopCheck();
            if (shr.inChecking()) {
                failingMsg = "The checker should be stopped.";
                System.err.println(failingMsg);
                fail(failingMsg);
            }
            System.out.println("The checker is stopped.");
            // end - test about check
            // begin - test about get target & remove target
            String key = "chash_test";
            String target = shr.getTarget(key);
            String expectedTarget = "192.168.106.64:2181";
            System.out.printf("The target of '%s' (1st): %s\n", key, target);
            if (!expectedTarget.equals(target))  {
                failingMsg = "The target '" + target + "' of key '" + key + "' should be '" + expectedTarget + "'.";
                System.err.println(failingMsg);
                fail(failingMsg);
            }
            result = shr.removeTarget(target);
            if (!result) {
                failingMsg = "Removing target '" + target + "' is FAILING.";
                System.err.println(failingMsg);
                fail(failingMsg);
            }
            System.out.printf("Removed target : %s\n", target);
            target = shr.getTarget(key);
            expectedTarget = "10.11.5.145:2181";
            System.out.printf("The target of '%s' (2nd): %s\n", key, target);
            if (!expectedTarget.equals(target))  {
                failingMsg = "The target '" + target + "' of key '" + key + "' should be '" + expectedTarget + "'.";
                System.err.println(failingMsg);
                fail(failingMsg);
            }
            // end - test about get target & remove target
            // begin - test about destroy
            shr.destroy();
            System.out.println("The hash ring is destroyed.");
            if (shr.status() != HashRingStatus.DESTROYED) {
                failingMsg = "The status '" + shr.status() + "' should '" + HashRingStatus.DESTROYED + "'. ";
                System.err.println(failingMsg);
                fail(failingMsg);
            }
            // end - test about destroy
        } catch (Exception e) {
            String errorMsg = "Error: " + e.getMessage() +".";
            System.err.println(errorMsg);
            fail(errorMsg);
        }
    }

}
