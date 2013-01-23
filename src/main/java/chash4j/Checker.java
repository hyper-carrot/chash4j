package chash4j;

public interface Checker {

    boolean start(CheckMethod checkMethod) throws CHashException;
    boolean stop() throws CHashException;
    boolean inChecking();

}
