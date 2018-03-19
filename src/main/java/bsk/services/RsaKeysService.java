package bsk.services;

import bsk.crypto.encrypter.*;
import bsk.model.User;
import jdk.nashorn.internal.runtime.ECMAException;

import javax.crypto.spec.SecretKeySpec;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class RsaKeysService {

    private FileOutputStream fileOutputStream;
    private KeyPairGenerator kpg;
    private final Encrypter encrypter;
    private final String pubKeyDirectory = "src/main/resources/pki/public/";
    private final String pvtKeyDirectory = "src/main/resources/pki/private/";
    private final String pubKeyExtension = ".key";
    private final String pvtKeyExtension = ".pub";


    public RsaKeysService() {
        try {
            kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(2048);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        encrypter = new Encrypter();
    }

    public void generateKeyPair(User user) throws EncrypterInitializationException, EncryptionException, IOException {
        KeyPair kp = kpg.generateKeyPair();

        Key pub = kp.getPublic();
        String pubLocation = pubKeyDirectory + user.getLogin() + pubKeyExtension;
        saveKeyToFile(pubLocation, pub.getEncoded());

        user.setPubKeyLocation(pubLocation);
        user.setPubKeyFormat(pub.getFormat());

        Key pvt = kp.getPrivate();
        String pvtLocation = pvtKeyDirectory + user.getLogin() + pvtKeyExtension;
        saveKeyToFile(pvtLocation, encryptKey(pvt, user.getPassword()));

        user.setPvtKeyLocation(pvtLocation);
        user.setPvtKeyFormat(pvt.getFormat());
    }

    private void saveKeyToFile(String path, byte[] key) throws IOException {
        fileOutputStream = new FileOutputStream(path);
        fileOutputStream.write(key);
    }

    public Key getKeyFromFile(String path, String format, byte[] hash) throws KeyException, InvalidKeySpecException {
        if (format.equals("X.509"))
            return getX509KeyFromFile(path, format);
        else if (format.equals("PKCS#8"))
            return getPKCS8KeyFromFile(path, format, hash);
        throw new InvalidKeySpecException("Unsupported key format");
    }

    private Key getPKCS8KeyFromFile(String path, String format, byte[] hash) throws KeyException {
        try {
            byte[] bytes = Files.readAllBytes(Paths.get(path));
            byte[] decryptedKey = decryptKey(bytes, hash);
            PKCS8EncodedKeySpec ks = new PKCS8EncodedKeySpec(decryptedKey);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePrivate(ks);
        } catch (Exception e){
            throw new KeyException("Cannot read key from file!");
        }
    }

    private Key getX509KeyFromFile(String path, String format) throws KeyException {
        try {
            byte[] bytes = Files.readAllBytes(Paths.get(path));

        /* Generate public key. */
            X509EncodedKeySpec ks = new X509EncodedKeySpec(bytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePublic(ks);
        } catch (Exception e){
            throw new KeyException("Cannot read key from file!");
        }
    }

    private byte[] decryptKey(byte[] encryptedString, byte[] symetricKey) throws EncrypterInitializationException, EncryptionException {
        SecretKeySpec keySpec = new SecretKeySpec(symetricKey, "AES");
        encrypter.init(keySpec, "AES", CipherMode.ECB, "PKCS5Padding",
                null, EncrypterMode.DECRYPTION);
        return encrypter.process(encryptedString);
    }
    private byte[] encryptKey(Key key, byte[] symetricKey) throws EncrypterInitializationException, EncryptionException {
        SecretKeySpec keySpec = new SecretKeySpec(symetricKey, "AES");
        encrypter.init(keySpec, "AES", CipherMode.ECB, "PKCS5Padding",
                null, EncrypterMode.ENCRYPTION);
        return encrypter.process(key.getEncoded());

    }
}
