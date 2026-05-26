package com.richard.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Unit test for simple App.
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
	
}
