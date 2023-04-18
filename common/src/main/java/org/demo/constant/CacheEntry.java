package org.demo.constant;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CacheEntry<K, V> {

    public K key;
    public V value;
}