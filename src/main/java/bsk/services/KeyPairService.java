package bsk.services;

import bsk.model.User;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.*;

public class KeyPairService {

    private FileOutputStream fileOutputStream;
    private final KeyPairGenerator kpg;
    private final String pubKeyDirectory = "src/main/resources/pki/public/";
    private final String pvtKeyDirectory = "src/main/resources/pki/private/";
    private final String pubKeyExtension = ".key";
    private final String pvtKeyExtension = ".pub";


    public KeyPairService() throws NoSuchAlgorithmException {
        kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
    }

    public void generateKeyPair(User user){
        KeyPair kp = kpg.generateKeyPair();

        //public key
        Key pub = kp.getPublic();
        savePubKeyToFile(user, pub);

        //add path and format to user xml
        user.setPubKeyLocation(pubKeyDirectory + user.getLogin() + pubKeyExtension);
        user.setPubKeyFormat(pub.getFormat());

        //private key
        Key pvt = kp.getPrivate();
        savePvtKeyToFile(user, pvt);

        //add path and format to user xml
        user.setPvtKeyLocation(pubKeyDirectory + user.getLogin() + pvtKeyExtension);
        user.setPvtKeyFormat(pvt.getFormat());
    }
    private void savePubKeyToFile(User user, Key key){
        try {
            fileOutputStream = new FileOutputStream(pubKeyDirectory + user.getLogin()
                    + pubKeyExtension);
            fileOutputStream.write(key.getEncoded());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void savePvtKeyToFile(User user, Key key){
        try {
            fileOutputStream = new FileOutputStream(pvtKeyDirectory + user.getLogin()
                    + pvtKeyExtension);
            fileOutputStream.write(key.getEncoded());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
