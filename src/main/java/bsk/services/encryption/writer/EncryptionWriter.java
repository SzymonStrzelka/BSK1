package bsk.services.encryption.writer;

import bsk.model.Encryption;
import com.sun.xml.internal.txw2.output.IndentingXMLStreamWriter;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

public class EncryptionWriter {

    private XMLOutputFactory factory = XMLOutputFactory.newInstance();
    private XMLStreamWriter writer;
    private FileOutputStream outputStream;

    public void writeHeader(File outputFile, Encryption encryption) throws EncryptionWritingException {
        try {

            XMLStreamWriter noIndentationWriter = factory.createXMLStreamWriter(new FileOutputStream(outputFile));
            writer = new IndentingXMLStreamWriter(noIndentationWriter);
            writer.writeStartDocument();
            writer.writeStartElement("Encryption");
            writer.writeStartElement("EncryptionHeader");
            writeNode("Algorithm", encryption.getAlgorithm());
            writeNode("KeySize", String.valueOf(encryption.getKeySize()));
            writeNode("BlockSize", String.valueOf(encryption.getBlockSize()));
            writeNode("CipherMode", encryption.getCipherMode().name());
            writeNode("Padding", encryption.getPadding());
            String iv = encryption.getInitialVector() == null ? "" : Base64.getEncoder().encodeToString(encryption.getInitialVector());
            writeNode("InitialVector", iv);
            String ext = encryption.getEncryptedExtension() == null ? "" : Base64.getEncoder().encodeToString(encryption.getEncryptedExtension());
            writeNode("Extension", ext);
            writer.writeStartElement("Recipients");
            for (Map.Entry<String, byte[]> recipientKey : encryption.getRecipientsKeys().entrySet()) {
                writer.writeStartElement("Recipient");
                writeNode("Login", recipientKey.getKey());
                writeNode("Key", Base64.getEncoder().encodeToString(recipientKey.getValue()));
                writer.writeEndElement();
            }
            writer.writeEndElement();
            writer.writeEndElement();
            writer.writeStartElement("EncryptionData");
            writer.close();
            outputStream = new FileOutputStream(outputFile, true);
            outputStream.write(">".getBytes(StandardCharsets.UTF_8));

        } catch (Exception e) {
            throw new EncryptionWritingException("Could not finish writing EncryptionHeader", e);
        }
    }

    private void writeNode(String name, String value) throws XMLStreamException {
        writer.writeStartElement(name);
        writer.writeCharacters(value);
        writer.writeEndElement();
    }

    public void writeData(byte[] data) throws EncryptionWritingException {
        try {
            outputStream.write(data);
        } catch (IOException e) {
            throw new EncryptionWritingException("Could not write encrypted data", e);
        }
    }

    public void finish() throws EncryptionWritingException {
        try {
            String endingTags = "</EncryptionData>\n</Encryption>";
            outputStream.write(endingTags.getBytes(StandardCharsets.UTF_8));
            outputStream.close();
        } catch (IOException e) {
            throw new EncryptionWritingException("Could finish", e);
        }
    }
}
