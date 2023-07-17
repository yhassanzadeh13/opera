package scenario.integrita.signature;

/**
 * signature implementation.
 */
public class Signature {

  /**
   * generates signature and verification keys.
   */
  public static byte[][] keyGen() {
    // @TODO generate keys
    byte[][] result = new byte[2][];
    return result;
  }

  /**
   * signs the message msg using provided signatureKey.
   */
  public static byte[] sign(String msg, byte[] signatureKey) {
    // @TODO sign using the generated keys
    byte[] result = new byte[0];
    return result;
  }

  /**
   * verifies the signature against the supplied msg and the verificationKey.
   */
  public static boolean verify(String msg, byte[] signature, byte[] verificationKey) {
    // @TODO verify using the generated keys
    return true;
  }
}
