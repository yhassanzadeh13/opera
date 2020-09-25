package Utils;


import java.io.*;

/**
 * Contains various static helper methods to be used by the UDP underlay implementation.
 */
public class Serialization {

    /**
     * Converts the given serializable object into a byte array
     * @param obj object to serialize.
     * @return byte array representation of the object.
     */
    public static byte[] serialize(Serializable obj) {
        ByteArrayOutputStream bos;
        ObjectOutputStream oos;
        // Create the streams.
        try {
            bos = new ByteArrayOutputStream();
            // Object output stream will write to the byte array output stream.
            oos = new ObjectOutputStream(bos);
            // Write the object to the object output stream.
            oos.flush();
            oos.writeObject(obj);
            oos.flush();
        } catch (IOException e) {
            System.err.println("[SimulatorUtils] Could not serialize.");
            e.printStackTrace();
            return null;
        }
        // Acquire the bytes from the byte array output stream.
        byte[] bytes = bos.toByteArray();
        // Close the streams.
        try {
            oos.close();
            bos.close();
        } catch (IOException e) {
            System.err.println("[SimulatorUtils] Could not close the streams.");
            e.printStackTrace();
        }
        return bytes;
    }

    /**
     * Converts the given byte array into its object representation.
     * @param bytes the byte array to convert.
     * @return the object representation.
     */
    public static Object deserialize(byte[] bytes) {
        ByteArrayInputStream bis;
        ObjectInputStream ois;
        Object obj;
        try {
            // Create the streams.
            bis = new ByteArrayInputStream(bytes);
            // Object input stream will read from the byte array input stream.
            ois = new ObjectInputStream(bis);
            // Read the object from the object input stream.
            obj = ois.readObject();
        } catch (Exception e) {
            System.err.println("[SimulatorUtils] Could not deserialize.");
            e.printStackTrace();
            return null;
        }
        // Close the streams.
        try {
            ois.close();
            bis.close();
        } catch (IOException e) {
            System.err.println("[SimulatorUtils] Could not close the streams.");
            e.printStackTrace();
        }
        return obj;
    }
}