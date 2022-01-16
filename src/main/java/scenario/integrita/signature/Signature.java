package scenario.integrita.signature;

/**
 * signature implementation.
 */
public class Signature {

  public static byte[][] keyGen() {
    // @TODO generate keys
    byte[][] result =  new byte[2][];
    return result;
  }

  public static byte[] sign(String msg, byte[] signature_key) {
    // @TODO sign using the generated keys
    byte[] result = new byte[0];
    return result;
  }

  public static boolean verify(String msg, byte[] signature, byte[] verification_key) {
    // @TODO verify using the generated keys
    return true;
  }
}
