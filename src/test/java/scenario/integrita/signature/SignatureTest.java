package scenario.integrita.signature;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class SignatureTest {

  @Test
  public void correctnessTest() {
    byte[][] keys = Signature.keyGen();
    String msg = "test message";
    byte[] sign = Signature.sign(msg, keys[0]);
    boolean verification = Signature.verify(msg, sign, keys[1]);
    assertTrue(verification);
  }

}
