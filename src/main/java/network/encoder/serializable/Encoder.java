package network.encoder.serializable;

import java.io.*;

import network.model.Event;

/**
 * Encoder is an implementation of the Encoder interface which uses Java's built-in object serialization and
 * deserialization to convert events to byte arrays and back. This encoder is used by default by the networking layer
 * to encode and decode events. Note that this encoder assumes that Event is actually a class, not an interface,
 * because the Serializable interface doesn't define any methods. Also note that Java's built-in serialization has
 * various shortcomings and should not be used for production code unless you're
 * aware of its limitations and potential issues.
 */
public class Encoder implements network.encoder.Encoder {
  /**
   * Encodes an event into a byte array.
   *
   * @param event the event to encode.
   * @return the encoded event.
   * @throws UncheckedIOException if an I/O error occurs.
   */
  public byte[] encode(Event event) throws UncheckedIOException {
    try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
         ObjectOutputStream out = new ObjectOutputStream(bos)) {
      out.writeObject(event);
      return bos.toByteArray();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  /**
   * Decodes a byte array into an event.
   *
   * @param bytes the byte array to decode.
   * @return the decoded event.
   * @throws IllegalStateException if the decoded object is not an instance of Event.
   */
  public Event decode(byte[] bytes) throws IllegalStateException, UncheckedIOException {
    try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
         ObjectInputStream in = new ObjectInputStream(bis)) {
      return (Event) in.readObject();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    } catch (ClassNotFoundException e) {
      throw new IllegalStateException("Decoded object is not an instance of Event", e);
    }
  }
}
