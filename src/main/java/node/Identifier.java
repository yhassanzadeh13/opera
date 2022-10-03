package node;

import java.io.Serializable;
import java.util.Arrays;

import io.ipfs.multibase.Multibase;

/**
 * Represents a 32-byte unique identifier.
 */
public class Identifier implements Serializable, Comparable<Identifier> {
  public static final int Size = 32;
  private final byte[] value;

  public Identifier(byte[] value) {
    this.value = value.clone();
  }

  /**
   * Returns if objects equal.
   *
   * @param o an identifier object.
   * @return true if objcets equal.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Identifier that = (Identifier) o;
    return Arrays.equals(value, that.value);
  }

  /**
   * Return the hashCode.
   *
   * @return hashCode.
   */
  @Override
  public int hashCode() {
    return Arrays.hashCode(value);
  }

  public byte[] getBytes() {
    return this.value.clone();
  }

  /**
   * Returns string representation of identifier in Base58BTC.
   *
   * @return string representation of identifier in Base58BTC.
   */
  public String toString() {
    return pretty(this.value);
  }

  /**
   * Converts identifier from its byte representation to Base58BTC.
   *
   * @param identifier input identifier in byte representation.
   * @return Base58BTC representation of identifier.
   */
  private static String pretty(byte[] identifier) {
    return Multibase.encode(Multibase.Base.Base58BTC, identifier);
  }

  /**
   * Compares this identifier with the other identifier.
   *
   * @param other represents other identifier to compared to.
   * @return 0 if two identifiers are equal, 1 if this identifier is greater than other,
   * -1 if other identifier is greater than this.
   */
  public int comparedTo(Identifier other) {
    int result = Arrays.compare(this.value, other.value);
    return Integer.compare(result, 0);
  }
}