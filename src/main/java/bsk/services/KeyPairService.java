package bsk.services;

import bsk.crypto.encrypter.*;
import bsk.model.User;

import javax.crypto.spec.SecretKeySpec;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

public class KeyPairService {

    private FileOutputStream fileOutputStream;
    private final KeyPairGenerator kpg;
    private final Encrypter encrypter;
    private final String pubKeyDirectory = "src/main/resources/pki/public/";
    private final String pvtKeyDirectory = "src/main/resources/pki/private/";
    private final String pubKeyExtension = ".key";
    private final String pvtKeyExtension = ".pub";


    public KeyPairService() throws NoSuchAlgorithmException {
        kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        encrypter = new Encrypter();
    }

    public void generateKeyPair(User user) throws EncrypterInitializationException, EncryptionException, IOException {
        KeyPair kp = kpg.generateKeyPair();

        SecretKeySpec keySpec = new SecretKeySpec(user.getPassword(), "AES");
        encrypter.init(keySpec, "AES", CipherMode.ECB, "PKCS5Padding", null, EncrypterMode.ENCRYPTION);

        Key pub = kp.getPublic();
        String pubLocation = pubKeyDirectory + user.getLogin() + pubKeyExtension;
        saveKeyToFile(pubLocation, pub.getEncoded());

        user.setPubKeyLocation(pubLocation);
        user.setPubKeyFormat(pub.getFormat());

        Key pvt = kp.getPrivate();
        byte[] encPvt = encrypter.process(pvt.getEncoded());
        String pvtLocation = pvtKeyDirectory + user.getLogin() + pvtKeyExtension;
        saveKeyToFile(pvtLocation, encPvt);

        user.setPvtKeyLocation(pvtLocation);
        user.setPvtKeyFormat(pvt.getFormat());
    }

    private void saveKeyToFile(String path, byte[] key) throws IOException {
        fileOutputStream = new FileOutputStream(path);
        fileOutputStream.write(key);
    }
}
