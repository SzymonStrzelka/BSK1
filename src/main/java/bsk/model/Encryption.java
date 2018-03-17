package bsk.model;


import lombok.Data;

import java.util.Map;

@Data
public class Encryption {
    private final String algorithm;
    private final int keySize;
    private final int blockSize;
    private final String cipherMode;
    private final String padding;
    private final byte[] initialVector;
    private final Map<String, byte[]> recipientsKeys;
}
