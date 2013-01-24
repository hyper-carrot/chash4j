package chash4j;

import java.util.concurrent.Callable;

public interface Checker {

    boolean start(CheckMethod checkMethod) throws CHashException;
    boolean stop() throws CHashException;
    boolean inChecking();

}
