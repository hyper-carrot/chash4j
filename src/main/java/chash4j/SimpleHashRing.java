package chash4j;

import chash4j.CHashException;
import chash4j.HashRingStatus;
import chash4j.NodeCheckMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public final class SimpleHashRing implements HashRing {
    
	public static final int DEFAULT_SHADOW_NUMBER = 1000;

    private TreeMap<Long, String> nodeMap = null;

    private Lock lock = null;

    public SimpleHashRing() {
    	this.nodeMap = new TreeMap<Long, String>();
        this.lock = new ReentrantLock();
    }

    public void build(int shadowNumber) throws CHashException {

    }

    public void destroy() throws CHashException {

    }

    public HashRingStatus status() {
        //TODO
        return HashRingStatus.UNINITIALIZED;
    }

    public void Check(NodeCheckMethod nodeCheckMethod) throws CHashException {

    }

    public boolean startCheck(
            NodeCheckMethod nodeCheckMethod,
            short intervalSeconds) throws CHashException {
        return false;
    }

    public boolean stopCheck() throws CHashException {
        return false;
    }

    public boolean inChecking() {
        return false;
    }

    public boolean addTarget(String target) throws CHashException {
        return false;
    }

    public boolean removeTarget(String target) throws CHashException {
        return false;
    }

    public String getTarget(String key) throws CHashException {
        return null;
    }
}
