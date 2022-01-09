package scenario.integrita.signature;

public class Signature {

  public void init(){
  // @TODO generate keys
  }
  public byte[] sing(byte[] msg) {
    // @TODO sign using the generated keys
    byte[] result = new byte[0];
    return result;
  }
  public boolean verify(){
    // @TODO verify using the generated keys
    return true;
  }
}
