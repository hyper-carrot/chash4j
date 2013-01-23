package chash4j;

public interface HashRing {

    void build(int shadowNumber) throws CHashException;
    void destroy() throws CHashException;
    HashRingStatus status();
    void Check(NodeCheckMethod nodeCheckMethod) throws CHashException;
    boolean startCheck(NodeCheckMethod nodeCheckMethod, short intervalSeconds) throws CHashException;
    boolean stopCheck() throws CHashException;
    boolean inChecking();
    boolean addTarget(String target) throws CHashException;
    boolean removeTarget(String target) throws CHashException;
    String getTarget(String key) throws CHashException;

}
