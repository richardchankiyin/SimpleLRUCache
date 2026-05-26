package com.richard.app;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BatchEvictionCacheImpl<K,V> implements ICache<K,V>{
	private static final Logger log = LoggerFactory.getLogger(BatchEvictionCacheImpl.class);

	private static final int DEFAULT_CAPACITY = 10;
	private static final int DEFAULT_EVICTBATCHSIZE = 2;
	private Function<K,V> valueComputingProcess;
	private int capacity;
	private int evictbatchsize;
	private ConcurrentHashMap<K,CacheValue<V>> cache;
	// this is to ensure only single thread will get into eviction
	private final ReentrantLock evictionLock = new ReentrantLock();
	
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
		
		log.debug("capacity: {}", this.capacity);
		
		// just create as what we want to see and with load factor 1.0 to prevent expansion to happen
		this.cache = new ConcurrentHashMap<>(this.capacity);
	}
	
	
	@Override
	public V get(K key) {
		CacheValue<V> item = this.cache.get(key);
		// this is a happy cached solution
		if (item != null) {
			item.setLastAccessTimeInNanosec(System.nanoTime());
			return item.getVal();
		}
		
		// otherwise will perform a computeIfAbsent, using concurrenthashmap key locking solution
		CacheValue<V> calcEntry = this.cache.computeIfAbsent(key, k->{
			log.debug("going to compute for key: {}", k);
			CacheValue<V> result = new CacheValue<>(this.valueComputingProcess.apply(key), System.nanoTime());
			log.debug("Cache Size: {} vs Capacity: {}", this.cache.size(), this.capacity);
			if (this.cache.size() >= this.capacity) {
				log.debug("going to perform evict...");
				evict();
			}
			return result;
		});
		
		log.debug("cache after accessing: {}", this.cache, key);
		
		return calcEntry.getVal();
	}
	
	
	private void evict() {
		if (evictionLock.tryLock()) {
			if (this.cache.size() >= this.capacity) {
				List<Map.Entry<K, CacheValue<V>>> entries = new ArrayList<>(cache.entrySet());
				// perform sorting here
				entries.sort((e1, e2) -> Long.compare(e1.getValue().getLastAccessTimeInNanosec(), e2.getValue().getLastAccessTimeInNanosec()));
				
				// this is to play safe we wont get ArrayIndexOutOfBoundException
				int itemsToEvict = Math.min(this.evictbatchsize, entries.size());
                for (int i = 0; i < itemsToEvict; i++) {
                	K key = entries.get(i).getKey();
                	this.cache.remove(key);
                	log.debug("evict key: {}", key);
                }
                
			} else {
				// clean up already, leaving
				log.debug("giving up evict as cache size is lowered");
			}
			
		} else {
			// give up eviction, another thread is doing so.
			log.debug("giving up evict as another thread is doing so");
		}
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
		public String toString() {
			return "[Val:" + this.val + "|time:" + this.lastAccessTimeInNanosec + "]"; 
		}
	}


	@Override
	public boolean hasKey(K key) {
		return this.cache.containsKey(key);
	}


	@Override
	public int currentSize() {
		return this.cache.size();
	}
	
}
