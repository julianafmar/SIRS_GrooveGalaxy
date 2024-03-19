package pt.tecnico.groovegalaxy.securedocument;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.security.GeneralSecurityException;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.crypto.spec.IvParameterSpec;

import javax.xml.bind.DatatypeConverter;


public class Unprotect {

    private static final String SYM_CIPHER = "AES/CTR/NoPadding";
    private static final int SYM_IV_SIZE = 16;
    private static final int MAC_SIZE = 32;

    public static void main(String[] args) throws IOException, GeneralSecurityException, Exception {
        // Check arguments
        if (args.length < 3) {
            System.err.println("Argument(s) missing!");
            System.err.println("Usage: unprotect [input_file] [output_file] [key]");
            return;
        }
        unprotect_file(args[0], args[1], args[2], true);
    }

    public static void unprotect_file(String input_file, String output_file, String keyPath, boolean verbose) throws IOException, GeneralSecurityException, Exception {
        // decrypt file
        try (FileReader fileReader = new FileReader(input_file)) {
            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(fileReader, JsonObject.class);
            byte[] file = unprotect(jsonObject, keyPath, verbose);

            System.out.println("File: " + new String(file));
            writeJsonFile(file, output_file, verbose);
        }
    }

    public static byte[] unprotect(JsonObject jsonObject, String keyPath, boolean verbose) throws IOException, GeneralSecurityException, Exception {
        // check MAC
        boolean sameFile = Check.check(jsonObject, keyPath, verbose);

        if(!sameFile)
            System.out.println("I am sorry, something went wrong :(\nTry again later.");

        // read key
        SecretKey key = readKey(keyPath, verbose);

        // get iv and encrypted file
        byte[] iv = DatatypeConverter.parseBase64Binary(jsonObject.get("iv").getAsString());
        byte[] encryptedFile = DatatypeConverter.parseBase64Binary(jsonObject.get("encryptedFile").getAsString());
        byte[] file;
        file = decrypt(encryptedFile, iv, key, verbose);
        return file;
    }

    public static byte[] unprotectBlock(JsonObject jsonObject, String keyPath, boolean verbose, int index) throws IOException, GeneralSecurityException, Exception {
        // check MAC
        boolean sameFile = Check.check(jsonObject, keyPath, verbose);

        if(!sameFile)
            System.out.println("I am sorry, something went wrong :(\nTry again later.");

        // read key
        SecretKey key = readKey(keyPath, verbose);

        // get iv and encrypted file
        byte[] iv = DatatypeConverter.parseBase64Binary(jsonObject.get("iv").getAsString());
        byte[] encryptedFile = DatatypeConverter.parseBase64Binary(jsonObject.get("encryptedFile").getAsString());
        byte[] file;
        file = decryptBlock(encryptedFile, iv, key, index, verbose);
        return file;
    }

    public static SecretKey readKey(String keyPath, boolean verbose) throws GeneralSecurityException, IOException {
        if(verbose)
            System.out.println("Reading key from file " + keyPath + " ...");
        FileInputStream fis = new FileInputStream(keyPath);
        byte[] encoded = new byte[fis.available()];
        fis.read(encoded);
        fis.close();

        return new SecretKeySpec(encoded, 0, 16, "AES");
    }

    private static byte[] decrypt(byte[] encryptedFile, byte[] iv, SecretKey key, boolean verbose) throws Exception {
        IvParameterSpec ivspec = new IvParameterSpec(iv);

        // get a AES cipher object
        Cipher cipher = Cipher.getInstance(SYM_CIPHER);
        
        // decrypt using the key and the byte array
        if(verbose)
            System.out.println("Decrypting...");
        cipher.init(Cipher.DECRYPT_MODE, key, ivspec);
        return cipher.doFinal(encryptedFile);
    }

    private static byte[] decryptBlock(byte[] encryptedFile, byte[] iv, SecretKey key, int index, boolean verbose) throws Exception {
    
        // get a AES cipher object
        Cipher cipher = Cipher.getInstance(SYM_CIPHER);
        if(verbose)
            System.out.println("Decrypting...");

        // increment IV

        for (int i = 0; i < index; i++) {
            iv = incrementIV(iv);
        }
        IvParameterSpec ivspec = new IvParameterSpec(iv);
        

        cipher.init(Cipher.DECRYPT_MODE, key, ivspec);

        int blocoTamanho = cipher.getBlockSize();
        int len = encryptedFile.length - index * blocoTamanho;
        byte[] textoCifradoParcial = new byte[len];
        System.arraycopy(encryptedFile, index * blocoTamanho, textoCifradoParcial, 0, len);

        // Desencripta apenas uma parte do texto cifrado
        return cipher.doFinal(textoCifradoParcial);
    }

    private static byte[] incrementIV(byte[] iv) {
        for (int i = iv.length - 1; i >= 0; i--) {
            if (iv[i] == Byte.MAX_VALUE) {
                iv[i] = 0;
            } else {
                iv[i]++;
                break;
            }
        }
        return iv;
    }

    private static void writeJsonFile(byte[] bytes, String filename, boolean verbose) throws IOException {
        // convert to json
        if(verbose)
            System.out.println("Converting to JSON...");
        String str = new String(bytes);
        JsonObject rootJson = JsonParser.parseString(str).getAsJsonObject();

        // write to a file
        if(verbose)
            System.out.println("Writing to file...");
        try (FileWriter fileWriter = new FileWriter(filename)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(rootJson, fileWriter);
        }
    }
}
