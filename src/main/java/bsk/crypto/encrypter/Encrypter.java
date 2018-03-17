package bsk.crypto.encrypter;

import bsk.model.Encryption;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public class Encrypter {

    private Cipher cipher;

    public void init(SecretKey key, Encryption e, EncrypterMode mode) throws EncrypterInitializationException {
        try {
            IvParameterSpec ivSpec = new IvParameterSpec(e.getInitialVector());
            String settings = String.format("%s/%s/%s", e.getAlgorithm(), e.getCipherMode(), e.getPadding());
            cipher = Cipher.getInstance(settings);
            cipher.init(mode.toCipherMode(), key, ivSpec);
        } catch (Exception ex) {
            throw new EncrypterInitializationException("Initialization failed", ex);
        }
    }

    public byte[] process(byte[] data, int offset, int length) throws EncryptionException {
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
            throw new EncryptionException("Error while trying to finish", e);
        }
    }
}
