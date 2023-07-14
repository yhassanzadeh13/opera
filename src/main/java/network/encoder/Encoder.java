package network.encoder;

import network.packets.Event;

import java.io.UncheckedIOException;

/**
 * Encoder is an interface for encoding and decoding events into byte arrays and vice versa. It is used by the
 * networking layer to encode and decode events into byte arrays and vice versa.
 */
public interface Encoder {
  /**
   * Encodes an event into a byte array.
   *
   * @param event the event to encode.
   * @return the encoded event.
   * @throws UncheckedIOException if an I/O error occurs.
   */
  byte[] encode(Event event) throws UncheckedIOException;

  /**
   * Decodes a byte array into an event.
   *
   * @param bytes the byte array to decode.
   * @return the decoded event.
   * @throws IllegalStateException if the decoded object is not an instance of Event.
   * @throws UncheckedIOException  if an I/O error occurs.
   */
  Event decode(byte[] bytes) throws IllegalStateException, UncheckedIOException;
}
