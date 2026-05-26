package com.richard.app;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Unit test for BatchEvictionCacheImplTest
 */
public class BatchEvictionCacheImplTest {


	@Test
	public void testCacheForMultipleThread() throws InterruptedException {
		// This is a simple return myself solution
		ICache<Integer,Integer> cache = new BatchEvictionCacheImpl<>(k->k, 3, 2);
		Thread t1 = new Thread(()->{
			assertEquals(1,cache.get(1));
		}, "testCacheForMultipleThread-t1");
		
		Thread t2 = new Thread(()->{
			assertEquals(1,cache.get(1));
		}, "testCacheForMultipleThread-t2");
		
		t1.start();
		t2.start();
		t1.join();
		t2.join();
		
	}
	
	@Test
	public void testEvict() {
		// This is a simple return myself solution
		ICache<Integer,Integer> cache = new BatchEvictionCacheImpl<>(k->k, 3, 2);

		cache.get(1);
		assert cache.hasKey(1);
		cache.get(2);
		assert cache.hasKey(1) && cache.hasKey(2);
		cache.get(3);
		assert cache.hasKey(1) && cache.hasKey(2) && cache.hasKey(3);
		cache.get(4);
		assert !cache.hasKey(1);
		assert !cache.hasKey(2);
		assert cache.hasKey(3) && cache.hasKey(4);
		
		assert cache.currentSize() == 2;
	}
	
	@Test
	public void testEvictMultiThread() throws InterruptedException {
		ICache<Integer,Integer> cache = new BatchEvictionCacheImpl<>(k->k, 3, 2);
		Thread t1 = new Thread(()->{
			assertEquals(1,cache.get(1));
		}, "testEvictMultiThread-t1");
		
		Thread t2 = new Thread(()->{
			assertEquals(2,cache.get(2));
		}, "testEvictMultiThread-t2");
		
		Thread t3 = new Thread(()->{
			assertEquals(3,cache.get(3));
		}, "testEvictMultiThread-t3");
		
		Thread t4 = new Thread(()->{
			assertEquals(4,cache.get(4));
		}, "testEvictMultiThread-t4");
		
		t1.start();
		t2.start();
		t3.start();
		t4.start();		
		
		t1.join();
		t2.join();
		t3.join();
		t4.join();
		
		assert cache.currentSize() == 2;
	}
	
	
}
