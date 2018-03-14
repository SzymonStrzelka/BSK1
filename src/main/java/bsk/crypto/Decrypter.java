package bsk.crypto;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class Decrypter {
    private Cipher cipher;

    public Decrypter(SecretKey key) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException {
        byte[] iv = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        IvParameterSpec ivspec = new IvParameterSpec(iv);
        cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, key, ivspec);
    }

    public byte[] decrypt(byte[] data, int offset, int length) {
        return cipher.update(data, offset, length);
    }

    public byte[] end() throws IllegalBlockSizeException, BadPaddingException {
        return cipher.doFinal();
    }
}
