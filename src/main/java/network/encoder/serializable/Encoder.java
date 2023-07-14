package network.encoder.serializable;

import network.packets.Event;

import java.io.*;

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
