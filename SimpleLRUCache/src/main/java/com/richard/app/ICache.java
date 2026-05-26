package com.richard.app;

public interface ICache<K,V> {
	
	public V get(K key);

}
