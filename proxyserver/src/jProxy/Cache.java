package jProxy;

import java.util.*;

public class Cache {
	
	private static Cache instance;
	public static final int CAPACITY = 100000;
	
    public class DListNode{
        public String key;
        public String value;
        public long timeStamp;
        public DListNode next;
        public DListNode pre;
        public DListNode(String key, String value){
            this.key = key;
            this.value = value;
            pre = null;
            next = null;
            timeStamp = (new java.util.Date()).getTime();
        }
    }
    
    public static HashSet<String> set;
    
    private Map<String, DListNode> map;
    private DListNode head;
    private DListNode tail;
    private int capacity;
    private int size;
    
    private Cache(int capacity) {
        if(capacity<1)
            return;
        this.capacity = capacity;
        map = new HashMap<String, DListNode>();
        size = 0;
        head = new DListNode(null, null);
        tail = new DListNode(null, null);
        head.next = tail;
        tail.pre = head;
    }
    
    public static Cache getInstance(){
    	if(instance == null){
    		instance = new Cache(CAPACITY);
    	}
    	return instance;
    }
    
    public void insertTail(DListNode node){
        node.pre.next = node.next;
        node.next.pre = node.pre;
        node.pre = tail.pre;
        node.next = tail;
        tail.pre.next = node;
        tail.pre = node;
    }
    public void removeFirst(){
        map.remove(head.next.key);
        size -= head.next.value.length();
        head.next = head.next.next;
        head.next.pre = head;
    }
    
    public String get(String key) {
        if(!map.containsKey(key))
            return null;
        DListNode node = map.get(key);
        insertTail(node);
        return node.value;
    }
    
    public void set(String key, String value) {
        if(map.containsKey(key)){
            DListNode node = map.get(key);
            insertTail(node);
            node.value = value;
        }else{
        	if(value.length() > capacity){
        		return;
        	}
            while(size + value.length() > capacity){
                removeFirst();
            }
            DListNode node = new DListNode(key, value);
            map.put(key, node);
            node.pre = tail.pre;
            node.next = tail;
            tail.pre.next = node;
            tail.pre = node;
            size += value.length();
        }
    }
    
    public boolean isTimeOut(String key){
    	if(!map.containsKey(key)){
    		return false;
    	}
    	DListNode node = map.get(key);
    	if((new java.util.Date()).getTime() - node.timeStamp > 50000){
    		return true;
    	}else{
    		return false;
    	}
    }
    
    public boolean isContains(String key){
    	return map.containsKey(key);
    }
    
    public void remove(String key){
    	DListNode node = map.get(key);
    	size -= node.value.length();
    	node.pre.next = node.next;
    	node.next.pre = node.pre;
    	node.pre = null;
    	node.next = null;
    	map.remove(key);
    }
}
