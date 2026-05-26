package com.richard.app;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class BatchEvictionCacheImpl<K,V> implements ICache<K,V>{

	private static final int DEFAULT_CAPACITY = 10;
	private static final int DEFAULT_EVICTBATCHSIZE = 2;
	private Function<K,V> valueComputingProcess;
	private int capacity;
	private int evictbatchsize;
	private ConcurrentHashMap<K,CacheValue<V>> cache;
	
	public BatchEvictionCacheImpl(Function<K,V> func, int capacity, int evictbatchsize) {
		this.valueComputingProcess=func;
		this.capacity=capacity;
		this.evictbatchsize=evictbatchsize;
		
		// verification of capacity and evictbatchsize
		if (this.capacity < 0) {
			this.capacity = DEFAULT_CAPACITY;
		}
		if (this.evictbatchsize < 0 || this.evictbatchsize > this.capacity) {
			this.evictbatchsize = DEFAULT_EVICTBATCHSIZE;
		}
		// just create as what we want to see and with load factor 1.0 to prevent expansion to happen
		this.cache = new ConcurrentHashMap<>(this.capacity,1.0f);
	}
	
	
	@Override
	public V get(K key) {
		// TODO Auto-generated method stub
		CacheValue<V> item = cache.get(key);
		
		
		return null;
	}
	
	private static class CacheValue<V> {
		final V val;
		volatile long lastAccessTimeInNanosec;
		CacheValue(V initval, long creationTimeInNanosec) {
			this.val = initval;
			this.lastAccessTimeInNanosec = creationTimeInNanosec;
		}
		V getVal() {
			return this.val;
		}
		long getLastAccessTimeInNanosec() {
			return this.lastAccessTimeInNanosec;
		}
		void setLastAccessTimeInNanosec(long updatetime) {
			this.lastAccessTimeInNanosec = updatetime;
		}
	}

	
	
}
