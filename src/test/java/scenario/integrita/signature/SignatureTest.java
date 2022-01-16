package scenario.integrita.signature;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class SignatureTest {

  @Test
  public void correctnessTest() {
    byte[][] keys = Signature.keyGen();
    String msg = "test message";
    byte[] sign = Signature.sign(msg,keys[0]);
    boolean verification =  Signature.verify(msg,sign,keys[1]);
    assertTrue(verification);
  }

}
