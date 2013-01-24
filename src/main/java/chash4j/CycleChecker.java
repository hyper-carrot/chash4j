package chash4j;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.Executors;

public class CycleChecker implements Checker {

    public final static short DEFAULT_INTERVAL_SECONDS = 2;

    private short intervalSeconds = 0;

    private final AtomicBoolean checkingTag = new AtomicBoolean();

    private final AtomicBoolean stopSign = new AtomicBoolean();

    private final AtomicLong count = new AtomicLong();

    private Future<String> checkFuture = null;

    private MyLogger logger = MyLoggerFactory.getLogger(this.getClass().getName());

    public CycleChecker() {
        this(DEFAULT_INTERVAL_SECONDS);
    }

    public CycleChecker(short intervalSeconds) {
        if (intervalSeconds > 0) {
            this.intervalSeconds = intervalSeconds;
        }
    }

    public boolean start(final CheckMethod checkMethod) throws CHashException {
        if (this.checkingTag.get()) {
            logger.warn("Please stop before restart.");
            return false;
        }
        if (this.intervalSeconds <= 0) {
            this.intervalSeconds = DEFAULT_INTERVAL_SECONDS;
        }
        this.stopSign.set(false);
        this.count.set(0);
        final long intervalMs = this.intervalSeconds * 1000;
        Callable<String> checkTask = new  Callable<String>() {

            public String call() throws Exception {
                String errorMsg = null;
                while (true) {
                    if (!stopSign.get()) {
                        logger.info("The checker will be stop. (count=" + count.get() + ")");
                        break;
                    }
                    try {
                        checkMethod.check();
                        Thread.sleep(intervalMs);
                    } catch (InterruptedException ie) {
                        errorMsg = "Sleep Error: " + ie.getMessage();
                        logger.error(errorMsg);
                    } catch (Exception e) {
                        errorMsg = "Unexpected Error: " + e.getMessage();
                        logger.error(errorMsg);
                    }
                }
                return errorMsg;
            }

        };
        ExecutorService executor = Executors.newFixedThreadPool(1);
        this.checkFuture = executor.submit(checkTask);
        this.checkingTag.set(true);
        return true;
    }

    public boolean stop() throws CHashException {
        if (!this.checkingTag.get()) {
            logger.warn("The checker were not started.");
            return false;
        }
        this.checkingTag.set(false);
        this.stopSign.set(true);
        return true;
    }

    public boolean inChecking(){
        return this.checkingTag.get();
    }

}
