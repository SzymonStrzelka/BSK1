package bsk.services.encryption.reader;

import bsk.crypto.encrypter.CipherMode;
import bsk.model.Encryption;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class EncryptionReader {

    private XMLInputFactory factory = XMLInputFactory.newInstance();
    private XMLStreamReader reader;
    private FileInputStream inputStream;

    public Encryption readHeader(File inputFile) throws EncryptionReadingException {
        try {
            this.inputStream = new FileInputStream(inputFile);
            reader = factory.createXMLStreamReader(new FileInputStream(inputFile));
            nextUntil("EncryptionHeader");
            return parseHeader();
        } catch (Exception e) {
            throw new EncryptionReadingException("Could not read header", e);
        }
    }

    public int readData(byte[] data) throws IOException {
        int bytesRead = inputStream.read(data);

        if (bytesRead == -1)
            return bytesRead;
        String tags = "</EncryptionData>\n</Encryption>";
        int tagsLength = tags.length();
        byte[] tagsBytes = Arrays.copyOfRange(data, bytesRead - tagsLength, bytesRead);
        if (tags.equals(new String(tagsBytes, StandardCharsets.UTF_8)))
            bytesRead -= tagsLength;

        return bytesRead;
    }

    public void finish() throws IOException {
        inputStream.close();
    }

    private Encryption parseHeader() throws XMLStreamException, IOException {
        reader.next();

        String algorithm = parseNode();
        int keySize = Integer.valueOf(parseNode());
        int blockSize = Integer.valueOf(parseNode());
        CipherMode cipherMode = CipherMode.valueOf(parseNode());
        String padding = parseNode();
        String ivString = parseNode();
        byte[] initialVector = ivString.isEmpty() ? null : Base64.getDecoder().decode(ivString);
        String extString = parseNode();
        byte[] ext = extString.isEmpty() ? null : Base64.getDecoder().decode(extString);
        Map<String, byte[]> recipientsKeys = parseRecipients();

        setInputStream();
        return new Encryption(algorithm, keySize, blockSize, cipherMode, padding, initialVector, ext, recipientsKeys);
    }

    private void nextUntil(String name) throws XMLStreamException {
        while (reader.hasNext()) {
            reader.next();
            if (reader.getEventType() == XMLStreamConstants.START_ELEMENT && reader.getName().equals(QName.valueOf(name)))
                break;
        }
    }

    private String parseNode() throws XMLStreamException {
        reader.next();
        String val = reader.getElementText();
        reader.next();
        return val;
    }

    private Map<String, byte[]> parseRecipients() throws XMLStreamException {
        Map<String, byte[]> recipientsKeys = new HashMap<>();
        nextUntil("Recipient");

        while (true) {
            switch (reader.getEventType()) {
                case XMLStreamReader.START_ELEMENT:
                    reader.next();
                    String key = parseNode();
                    byte[] val = Base64.getDecoder().decode(parseNode());
                    recipientsKeys.put(key, val);
                    break;
                case XMLStreamReader.END_ELEMENT:
                    if (reader.getName().equals(QName.valueOf("Recipients")))
                        return recipientsKeys;
            }
            reader.next();
        }

    }

    private void setInputStream() throws XMLStreamException, IOException {
        nextUntil("EncryptionData");
        long dataOffset = reader.getLocation().getCharacterOffset() - 8;    //Zajebiście działa ten offset
        inputStream.skip(dataOffset);
    }
}
