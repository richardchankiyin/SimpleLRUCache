package com.richard.app;

public interface ICache<K,V> {
	
	public V get(K key);
	
	public boolean hasKey(K key);

	public int currentSize();
}
