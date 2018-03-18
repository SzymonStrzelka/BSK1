package bsk.crypto.encrypter;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public class Encrypter {

    private Cipher cipher;

    public void init(SecretKey key, String algorithm, CipherMode cipherMode, String padding,
                     byte[] initialVector, EncrypterMode mode) throws EncrypterInitializationException {
        try {
            IvParameterSpec ivSpec = null;
            if (!cipherMode.equals(CipherMode.ECB)) {
                ivSpec = new IvParameterSpec(initialVector);
            }
            String settings = String.format("%s/%s/%s", algorithm, cipherMode, padding);
            cipher = Cipher.getInstance(settings);
            if (ivSpec == null) {
                cipher.init(mode.intValue(), key);
            } else {
                cipher.init(mode.intValue(), key, ivSpec);
            }
        } catch (Exception ex) {
            throw new EncrypterInitializationException("Initialization failed", ex);
        }
    }

    public byte[] process(byte[] data) throws EncryptionException {
        try {
            return cipher.doFinal(data);
        } catch (Exception e) {
            throw new EncryptionException("Error processing data", e);
        }
    }

    public byte[] step(byte[] data, int offset, int length) throws EncryptionException {
        try {
            return cipher.update(data, offset, length);
        } catch (Exception e) {
            throw new EncryptionException("Error processing data", e);
        }
    }

    public byte[] finish() throws EncryptionException {
        try {
            return cipher.doFinal();
        } catch (Exception e) {
            throw new EncryptionException("Error while trying to finish processing", e);
        }
    }
}
