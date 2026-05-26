SimpleLRUCache
=============

This implementation assumes multiple threads can read cache with high performance manner yet only single thread will be able to perform compute if it is not available.

Also this is a LRU (Least Recently Used) cache to perform evict when cache size reaches its capacity. This is a batch based eviction algorithm with less performance
solution. It is because we assume preconfigure not reaching the capacity should be ideal for reading cache. If we are at a very restricted memory environment, this
solution should be replaced by a more aggressive indexing of cache age (internally double-linked-list node of KV solution for ordering), or going ahead to Caffeine
- Google ConcurrentLinkedHashMap. However there will be additional overhead for each read.

Build
-------
mvn clean install
