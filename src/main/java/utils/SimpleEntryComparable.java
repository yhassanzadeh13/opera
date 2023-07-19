package utils;

import java.util.AbstractMap;

/**
 * Can be used to construct entries that comparable.
 *
 * @param <K> Type of the key
 * @param <V> Type of the Value
 */
public class SimpleEntryComparable<K extends Comparable<? super K>, V> extends AbstractMap.SimpleEntry<K, V> implements Comparable<SimpleEntryComparable<K, V>> {
  public SimpleEntryComparable(K key, V value) {
    super(key, value);
  }

  @Override
  public int compareTo(final SimpleEntryComparable<K, V> other) {
    return getKey().compareTo(other.getKey());
  }
}