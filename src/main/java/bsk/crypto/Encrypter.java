package bsk.crypto;

import bsk.enums.CipherMode;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class Encrypter {

    private Cipher cipher;

    public Encrypter(SecretKey key, CipherMode cipherMode) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException {
        byte[] iv = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        IvParameterSpec ivspec = new IvParameterSpec(iv);

        String mode = "AES/" + cipherMode.toString() + "/PKCS5PADDING";
        cipher = Cipher.getInstance(mode);
        cipher.init(Cipher.ENCRYPT_MODE, key, ivspec);
    }

    public byte[] encrypt(byte[] data, int offset, int length) {
        return cipher.update(data, offset, length);
    }

    public byte[] end() throws IllegalBlockSizeException, BadPaddingException {
        return cipher.doFinal();
    }
}
