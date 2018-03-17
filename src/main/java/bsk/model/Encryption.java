package bsk.model;


import bsk.crypto.encrypter.CipherMode;
import lombok.Data;

import java.util.Map;

@Data
public class Encryption {
    private final String algorithm;
    private final int keySize;
    private final int blockSize;
    private final CipherMode cipherMode;
    private final String padding;
    private final byte[] initialVector;
    private final byte[] encryptedExtension;
    private final Map<String, byte[]> recipientsKeys;
}
