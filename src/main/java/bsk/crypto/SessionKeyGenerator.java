package bsk.crypto;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class SessionKeyGenerator {

    public SecretKey generate128BitKey(){
        return generateKey(128);
    }

    public SecretKey generate192BitKey(){
        return generateKey(192);
    }

    public SecretKey generate256BitKey(){
        return generateKey(256);
    }

    private SecretKey generateKey(int bits){
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            byte[] seed = ByteBuffer.allocate(Long.BYTES).putLong(System.currentTimeMillis()).array();
            SecureRandom random = new SecureRandom(seed);
            keyGen.init(bits, random);
            return keyGen.generateKey();
        }catch (NoSuchAlgorithmException e){
            e.printStackTrace();
        }
        return null;
    }
}
