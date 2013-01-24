package chash4j;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import java.util.concurrent.atomic.AtomicLong;

@RunWith(JUnit4.class)
public class CycleCheckerTest {

    @Test
    public void testCycleChecker() {
        Short intervalSeconds = 1;
        System.out.printf("The intervalSeconds is %d.\n", intervalSeconds);
        CycleChecker checker = new CycleChecker((short) 1);
        final AtomicLong count = new AtomicLong(0);
        System.out.printf("The count is %d\n", count.get());
        CheckMethod checkMethod = new CheckMethod() {

            public void check() {
                long currentCount = count.addAndGet(1);
                System.out.printf("The count is %d.\n", currentCount);
            }
        };
        boolean result = false;
        long timeoutSeconds = 3;
        String failingMsg = null;
        try {
            System.out.println("Start the checker...");
            result = checker.start(checkMethod);
            if (!result) {
                failingMsg = "The result is starting checker is FALSE! ";
                System.err.println(failingMsg);
                fail(failingMsg);
            }
            if (!checker.inChecking()) {
                failingMsg = "The Checker is not successful running! ";
                System.err.println(failingMsg);
                fail(failingMsg);
            }
            System.out.println("The checker is started.");
            while (count.get() < timeoutSeconds) {
                Thread.sleep(1);
            }
            assertTrue(result);
            System.out.println("Stop the checker...");
            result = checker.stop();
            if (!result) {
                failingMsg = "The result is stopping checker is FALSE! ";
                System.err.println(failingMsg);
                fail(failingMsg);
            }
            if (checker.inChecking()) {
                failingMsg = "The Checker is still running! ";
                System.err.println(failingMsg);
                fail(failingMsg);
            }
            System.out.println("The checker is stopped.");
            if (count.get() != timeoutSeconds) {
                failingMsg = "The count '" + count + "' should equals timeoutSeconds '" + timeoutSeconds + "'.";
                System.err.println(failingMsg);
                fail(failingMsg);
            }
        } catch (Exception e) {
            String errorMsg = "Error: " + e.getMessage() +".";
            System.err.println(errorMsg);
            fail(errorMsg);
        }
        System.out.printf("The count is %d. It's OK.", count.get());

    }

}
