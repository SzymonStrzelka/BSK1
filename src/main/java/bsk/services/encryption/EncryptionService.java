package bsk.services.encryption;

import bsk.crypto.encrypter.CipherMode;
import bsk.crypto.encrypter.Encrypter;
import bsk.crypto.encrypter.EncrypterMode;
import bsk.crypto.key.SessionKeyGenerator;
import bsk.model.Encryption;
import bsk.model.User;
import bsk.services.encryption.reader.EncryptionReader;
import bsk.services.encryption.writer.EncryptionWriter;
import io.reactivex.Observable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler;
import io.reactivex.schedulers.Schedulers;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class EncryptionService {
    private final Encrypter encrypter;
    private final SessionKeyGenerator keyGenerator;
    private final EncryptionWriter encryptionWriter;
    private final EncryptionReader encryptionReader;
    private static final int BLOCK_SIZE = 128;

    public EncryptionService() {
        this.encrypter = new Encrypter();
        this.keyGenerator = new SessionKeyGenerator();
        this.encryptionWriter = new EncryptionWriter();
        this.encryptionReader = new EncryptionReader();
    }


    public void encrypt(File inputFile,
                        File outputFile,
                        CipherMode cipherMode,
                        List<String> recipients,
                        Consumer<Double> onProgressChanged,
                        Consumer<Throwable> onError,
                        Action onCompleted) {

        byte[] initialVector = null;
        if (cipherMode != CipherMode.ECB) {
            Random random = new SecureRandom();
            initialVector = new byte[16];
            random.nextBytes(initialVector);
        }
        SecretKey key = keyGenerator.generate128BitKey();

        Map<String, byte[]> recipientsKeys = new HashMap<>();
        recipients.forEach(login -> recipientsKeys.put(login, key.getEncoded()));

        Encryption encryption = new Encryption("AES", 128, BLOCK_SIZE, cipherMode, "PKCS5Padding", initialVector, recipientsKeys);

        Observable
                .<Double>create(emitter -> {
                    try (FileInputStream inputStream = new FileInputStream(inputFile)) {

                        encrypter.init(key, encryption, EncrypterMode.ENCRYPTION);
                        encryptionWriter.writeHeader(outputFile, encryption);

                        long totalBytesRead = 0;
                        int readBytes;
                        byte[] inputBytes = new byte[BLOCK_SIZE];

                        while ((readBytes = inputStream.read(inputBytes)) != -1) {
                            totalBytesRead += readBytes;

                            byte[] outputBytes = encrypter.process(inputBytes, 0, readBytes);
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

    public void decrypt(File inputFile,
                        File outputFile,
                        User currentUser,
                        Consumer<Double> onProgressChanged,
                        Consumer<Throwable> onError,
                        Action onCompleted) {
        Observable
                .<Double>create(emitter -> {
                    try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {

                        Encryption encryption = encryptionReader.readHeader(inputFile);

                        byte[] key = encryption.getRecipientsKeys().get(currentUser.getLogin());
                        SecretKeySpec keySpec = new SecretKeySpec(key, encryption.getAlgorithm());
                        encrypter.init(keySpec, encryption, EncrypterMode.DECRYPTION);

                        long totalBytesRead = 0;
                        int readBytes;
                        byte[] inputBytes = new byte[BLOCK_SIZE];

                        while ((readBytes = encryptionReader.readData(inputBytes)) != -1) {
                            totalBytesRead += readBytes;

                            byte[] outputBytes = encrypter.process(inputBytes, 0, readBytes);
                            if (outputBytes != null)
                                outputStream.write(outputBytes);

                            double progress = (double) totalBytesRead / inputFile.length();
                            emitter.onNext(progress);
                        }

                        byte[] outputBytes = encrypter.finish();
                        if (outputBytes != null)
                            outputStream.write(outputBytes);

                        emitter.onComplete();
                    } catch (Exception e) {
                        emitter.onError(e);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(JavaFxScheduler.platform())
                .subscribe(onProgressChanged, onError, onCompleted);
    }
}
