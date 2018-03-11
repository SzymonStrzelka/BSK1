package bsk;


import bsk.crypto.Encryptor;
import bsk.crypto.SymetricKeyGenerator;
import org.junit.Before;
import org.junit.Test;

import javax.crypto.SecretKey;

import static junit.framework.Assert.assertEquals;

public class EncryptorTest {
    private Encryptor encryptor;
    private SymetricKeyGenerator keyGenerator;
    private String initVector;

    @Before
    public void setup(){
        encryptor = new Encryptor();
        keyGenerator = new SymetricKeyGenerator();
        initVector = "RandomInitVector";
    }

    @Test
    public void testEncryptWith128BitKey(){
        SecretKey key = keyGenerator.generate128BitKey();
        testEncypt(key);
    }

    @Test
    public void testEncryptWith192BitKey(){
        SecretKey key = keyGenerator.generate192BitKey();
        testEncypt(key);
    }

    @Test
    public void testEncryptWith256BitKey(){
        SecretKey key = keyGenerator.generate256BitKey();
        testEncypt(key);
    }

    private void testEncypt(SecretKey key){
        String originalString = "Hello World";

        byte[] encrypted = encryptor.encrypt(key.getEncoded(), initVector, originalString.getBytes());
        byte[] decrypted = encryptor.decrypt(key.getEncoded(), initVector, encrypted);

        String decryptedString = new String(decrypted);

        assertEquals(originalString, decryptedString);
    }
}
