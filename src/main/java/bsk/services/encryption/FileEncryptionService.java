package bsk.services.encryption;

import bsk.crypto.encrypter.CipherMode;
import bsk.crypto.encrypter.Encrypter;
import bsk.crypto.encrypter.EncrypterMode;
import bsk.crypto.encrypter.EncryptionException;
import bsk.crypto.key.SessionKeyGenerator;
import bsk.model.Encryption;
import bsk.model.User;
import bsk.services.RsaKeysService;
import bsk.services.encryption.reader.EncryptionReader;
import bsk.services.encryption.writer.EncryptionWriter;
import io.reactivex.Observable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler;
import io.reactivex.schedulers.Schedulers;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class FileEncryptionService {
    private final Encrypter encrypter;
    private final SessionKeyGenerator keyGenerator;
    private final EncryptionWriter encryptionWriter;
    private final EncryptionReader encryptionReader;
    private final RsaKeysService rsaKeysService;
    private static final int BLOCK_SIZE = 128;
    private static final String ALGORITHM = "AES";
    private static final int KEY_SIZE = 128;
    private static final String PADDING = "PKCS5Padding";

    public FileEncryptionService() {
        this.encrypter = new Encrypter();
        this.keyGenerator = new SessionKeyGenerator();
        this.encryptionWriter = new EncryptionWriter();
        this.encryptionReader = new EncryptionReader();
        this.rsaKeysService = new RsaKeysService();
    }


    public void encrypt(File inputFile,
                        File outputFile,
                        CipherMode cipherMode,
                        List<User> recipients,
                        Consumer<Double> onProgressChanged,
                        Consumer<Throwable> onError,
                        Action onCompleted) {

        SecretKey key = keyGenerator.generate128BitKey();

        Map<String, byte[]> recipientsKeys = new HashMap<>();
       //encrypt session key with each recipient public key
        for (User user : recipients) {
            try {
                Cipher cipher = Cipher.getInstance("RSA");
                Key publicKey = rsaKeysService.getKeyFromFile(user.getPubKeyLocation(),
                        user.getPubKeyFormat(), user.getPassword());
                cipher.init(Cipher.ENCRYPT_MODE, publicKey);
                recipientsKeys.put(user.getLogin(), cipher.doFinal(key.getEncoded()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Observable
                .<Double>create(emitter -> {
                    try (FileInputStream inputStream = new FileInputStream(inputFile)) {

                        byte[] initialVector = generateInitialVector(cipherMode);
                        encrypter.init(key, ALGORITHM, cipherMode, PADDING, initialVector, EncrypterMode.ENCRYPTION);

                        byte[] ext = encryptExtension(inputFile.getName());

                        Encryption encryption = new Encryption(ALGORITHM, KEY_SIZE, BLOCK_SIZE, cipherMode, PADDING,
                                initialVector, ext, recipientsKeys);

                        encryptionWriter.writeHeader(outputFile, encryption);

                        long totalBytesRead = 0;
                        int readBytes;
                        byte[] inputBytes = new byte[BLOCK_SIZE];

                        while ((readBytes = inputStream.read(inputBytes)) != -1) {
                            totalBytesRead += readBytes;

                            byte[] outputBytes = encrypter.step(inputBytes, 0, readBytes);
                            if (outputBytes != null)
                                encryptionWriter.writeData(outputBytes);

                            double progress = (double) totalBytesRead / inputFile.length();
                            emitter.onNext(progress);
                        }

                        byte[] outputBytes = encrypter.finish();
                        if (outputBytes != null)
                            encryptionWriter.writeData(outputBytes);

                        encryptionWriter.finish();
                        emitter.onComplete();
                    } catch (Exception e) {
                        emitter.onError(e);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(JavaFxScheduler.platform())
                .subscribe(onProgressChanged, onError, onCompleted);

    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void decrypt(File inputFile,
                        File outputFile,
                        User currentUser,
                        Consumer<Double> onProgressChanged,
                        Consumer<Throwable> onError,
                        Action onCompleted) {
        Observable
                .<Double>create(emitter -> {
                    try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {

                        Encryption e = encryptionReader.readHeader(inputFile);

                        //decrypt session key with user private key
                        byte[] encryptedKey = e.getRecipientsKeys().get(currentUser.getLogin());
                        boolean isAuthorized = encryptedKey != null;
                        SecretKey key;
                        if (isAuthorized) {
                            final Cipher cipher = Cipher.getInstance("RSA");
                            Key privateKey = rsaKeysService.getKeyFromFile(currentUser.getPvtKeyLocation(),
                                    currentUser.getPvtKeyFormat(), currentUser.getPassword());
                            cipher.init(Cipher.DECRYPT_MODE, privateKey);
                            byte[] keyBytes = cipher.doFinal(encryptedKey);
                            key = new SecretKeySpec(keyBytes, e.getAlgorithm());

                        } else {
                            //this key is generated only to init encrypter, it won't be used anywhere
                            key = keyGenerator.generate128BitKey();
                        }

                        encrypter.init(key, e.getAlgorithm(), e.getCipherMode(), e.getPadding(),
                                e.getInitialVector(), EncrypterMode.DECRYPTION);

                        long totalBytesRead = 0;
                        int readBytes;
                        byte[] inputBytes = new byte[BLOCK_SIZE];
                        byte[] outputBytes;
                        Random random = new Random();

                        while ((readBytes = encryptionReader.readData(inputBytes)) != -1) {
                            totalBytesRead += readBytes;
                            if (isAuthorized) {
                                outputBytes = encrypter.step(inputBytes, 0, readBytes);
                            } else {
                                //unauthorized user won't know if encryption was indeed successful
                                outputBytes = new byte[readBytes];
                                random.nextBytes(outputBytes);
                            }
                            if (outputBytes != null)
                                outputStream.write(outputBytes);

                            double progress = (double) totalBytesRead / inputFile.length();
                            emitter.onNext(progress);
                        }

                        encryptionReader.finish();
                        if (isAuthorized) {
                            outputBytes = encrypter.finish();
                            if (outputBytes != null)
                                outputStream.write(outputBytes);
                            outputStream.close();

                            byte[] encryptedExt = e.getEncryptedExtension();
                            if (encryptedExt != null) {
                                String fileExtension = decryptExtension(encryptedExt);
                                String newFileName = outputFile + "." + fileExtension;
                                outputFile.renameTo(new File(newFileName));
                            }
                        }

                        emitter.onComplete();
                    } catch (Exception e) {
                        emitter.onError(e);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(JavaFxScheduler.platform())
                .subscribe(onProgressChanged, onError, onCompleted);
    }

    private String getFileExtension(String fileName) {
        int i = fileName.lastIndexOf('.');
        if (i > 0) {
            return fileName.substring(i + 1);
        }
        return null;
    }

    private byte[] encryptExtension(String filename) throws EncryptionException {
        String ext = getFileExtension(filename);
        if (ext == null) {
            return null;
        } else {
            byte[] extBytes = ext.getBytes(StandardCharsets.UTF_8);
            return encrypter.process(extBytes);
        }
    }

    private String decryptExtension(byte[] encryptedExt) throws EncryptionException {
        if (encryptedExt == null)
            return "";

        byte[] decryptedExt = encrypter.process(encryptedExt);
        return new String(decryptedExt, StandardCharsets.UTF_8);
    }

    private byte[] generateInitialVector(CipherMode cipherMode) {
        byte[] initialVector = null;
        if (cipherMode != CipherMode.ECB) {
            Random random = new SecureRandom();
            initialVector = new byte[16];
            random.nextBytes(initialVector);
        }
        return initialVector;
    }
}
