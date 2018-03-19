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
    private final KeyPairGenerator kpg;
    private final Encrypter encrypter;
    private final String pubKeyDirectory = "src/main/resources/pki/public/";
    private final String pvtKeyDirectory = "src/main/resources/pki/private/";
    private final String pubKeyExtension = ".key";
    private final String pvtKeyExtension = ".pub";


    public RsaKeysService() throws NoSuchAlgorithmException {
        kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
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
        saveKeyToFile(pvtLocation, encyptKey(pvt, user.getPassword()));

        user.setPvtKeyLocation(pvtLocation);
        user.setPvtKeyFormat(pvt.getFormat());
    }

    private void saveKeyToFile(String path, byte[] key) throws IOException {
        fileOutputStream = new FileOutputStream(path);
        fileOutputStream.write(key);
    }

    public byte[] getKeyFromFile(String path, String format) throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {
        if (format.equals("X.509"))
            return getX509KeyFromFile(path, format);
        else if (format.equals("PKCS#8"))
            return getPKCS8KeyFromFile(path, format);
        throw new InvalidKeySpecException("Unsupported key format");
    }

    private byte[] getPKCS8KeyFromFile(String path, String format) throws InvalidKeySpecException, NoSuchAlgorithmException, IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));

        /* Generate private key. */
        PKCS8EncodedKeySpec ks = new PKCS8EncodedKeySpec(bytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(ks).getEncoded();
    }

    private byte[] getX509KeyFromFile(String path, String format) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));

        /* Generate public key. */
        X509EncodedKeySpec ks = new X509EncodedKeySpec(bytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(ks).getEncoded();
    }

    private byte[] decryptKey(byte[] encryptedString, byte[] symetricKey) throws EncrypterInitializationException, EncryptionException {
        SecretKeySpec keySpec = new SecretKeySpec(symetricKey, "AES");
        encrypter.init(keySpec, "AES", CipherMode.ECB, "PKCS5Padding",
                null, EncrypterMode.DECRYPTION);
        return encrypter.process(encryptedString);
    }
    private byte[] encyptKey(Key key, byte[] symetricKey) throws EncrypterInitializationException, EncryptionException {
        SecretKeySpec keySpec = new SecretKeySpec(symetricKey, "AES");
        encrypter.init(keySpec, "AES", CipherMode.ECB, "PKCS5Padding",
                null, EncrypterMode.ENCRYPTION);
        return encrypter.process(key.getEncoded());

    }
}
