package bsk.crypto.encrypter;

public enum EncrypterMode {
    ENCRYPTION, DECRYPTION;

    public int intValue() {
        switch (this) {
            case ENCRYPTION:
                return 1;
            case DECRYPTION:
                return 2;
        }
        return -1;
    }
}
