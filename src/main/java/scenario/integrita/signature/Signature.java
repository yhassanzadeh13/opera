package scenario.integrita.signature;

public class Signature {

  public void init(){
  // @TODO generate keys
  }
  public static byte[] sing(String msg, byte[] signature_key) {
    // @TODO sign using the generated keys
    byte[] result = new byte[0];
    return result;
  }
  public static boolean verify(String msg, byte[] signature, byte[] verification_key){
    // @TODO verify using the generated keys
    return true;
  }
}
