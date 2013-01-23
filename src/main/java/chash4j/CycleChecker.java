package chash4j;

public class CycleChecker implements Checker {

    public boolean start(CheckMethod checkMethod) throws CHashException {
        return false;
    }

    public boolean stop() throws CHashException {
        return false;
    }

    public boolean inChecking(){
        return false;
    }

}
