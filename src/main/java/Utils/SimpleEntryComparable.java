package utils;

import java.util.AbstractMap;

public class SimpleEntryComparable<K extends Comparable<? super K>, V>
      extends AbstractMap.SimpleEntry<K, V>
      implements Comparable<SimpleEntryComparable<K, V>> {

  public SimpleEntryComparable(K key, V value) {
    super(key, value);
  }

  @Override
  public int compareTo(final SimpleEntryComparable<K, V> other) {
    return getKey().compareTo(other.getKey());
  }
}