package pt.tecnico.groovegalaxy.securedocument;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.security.GeneralSecurityException;
import java.security.Key;

import static javax.xml.bind.DatatypeConverter.printHexBinary;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.crypto.spec.IvParameterSpec;
import java.security.SecureRandom;

import javax.xml.bind.DatatypeConverter;


public class Protect {
    private static final String MAC_ALGO = "HmacSHA256";
    private static final String SYM_CIPHER = "AES/CTR/NoPadding";
    /** Symmetric IV size. */
    private static final int SYM_IV_SIZE = 16;

    
    public static void main(String[] args) throws IOException, GeneralSecurityException, Exception{

        // Check arguments
        if (args.length < 3) {
            System.err.println("Argument(s) missing!");
            System.err.println("Usage: protect [input_file] [output_file] [key]");
            return;
        }
        final String input_file = args[0];
        final String output_file = args[1];
        final String keyPath = args[2];

        protect_file(input_file, output_file, keyPath, true);
        
    }

    public static void protect_file(String input_file, String output_file, String keyPath, boolean verbose) throws IOException, GeneralSecurityException, Exception{
        try (FileReader fileReader = new FileReader(input_file)) {
            Gson gson = new Gson();
            JsonObject originalFile = gson.fromJson(fileReader, JsonObject.class);
            byte[] originalFileByte = originalFile.toString().getBytes();
            JsonObject finalFile = protect(originalFileByte, keyPath, verbose);

            // Write JSON object to file
            try (FileWriter fileWriter = new FileWriter(output_file)) {
                Gson finalGson = new GsonBuilder().setPrettyPrinting().create();
                gson.toJson(finalFile, fileWriter);
            }
        }
    }

    public static JsonObject protect(byte[] originalFile, String keyPath, boolean verbose) throws IOException, GeneralSecurityException, Exception{
        JsonObject finalFile = new JsonObject();
        SecretKey key = readKey(keyPath, verbose);

        // make IV
        SecureRandom random = new SecureRandom();
        byte[] iv = new byte[SYM_IV_SIZE];
        random.nextBytes(iv);
        finalFile.addProperty("iv", DatatypeConverter.printBase64Binary(iv));

        // encrypt file
        byte[] encryptedFile = encrypt(originalFile, iv, key, verbose);
        finalFile.addProperty("encryptedFile", DatatypeConverter.printBase64Binary(encryptedFile));

        // make nonce
        JsonObject nonce = createNonce();
        finalFile.add("nonce", nonce);

        byte[] bytesToMAC = finalFile.toString().getBytes();
        // make MAC
        byte[] cipherDigest = makeMAC(bytesToMAC, key);
        finalFile.addProperty("mac", DatatypeConverter.printBase64Binary(cipherDigest));
        return finalFile;
    }

    public static SecretKey readKey(String keyPath, boolean verbose) throws GeneralSecurityException, IOException {
        if (verbose)
            System.out.println("Reading key from file " + keyPath + " ...");
        FileInputStream fis = new FileInputStream(keyPath);
        byte[] encoded = new byte[fis.available()];
        fis.read(encoded);
        fis.close();

        return new SecretKeySpec(encoded, 0, 16, "AES");
    }

    public static void writeKey(String keyPath, byte[] key) throws GeneralSecurityException, IOException {
        FileOutputStream fos = new FileOutputStream(keyPath);
        fos.write(key);
        fos.close();
    }   

    public static byte[] generateKey(String keyPath) throws GeneralSecurityException, IOException {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES", "SunJCE");
        keyGen.init(128);
        Key key = keyGen.generateKey();
        byte[] encoded = key.getEncoded();

        FileOutputStream fos = new FileOutputStream(keyPath);
        fos.write(encoded);
        fos.close();
        return encoded;
    }

    private static JsonObject createNonce() throws IOException{
        JsonObject nonce = new JsonObject();
        // counter
        File file = new File("currentCounterProtect");
        FileWriter fileWriter;
        if (!file.exists()) {
            fileWriter = new FileWriter("currentCounterProtect");
            fileWriter.write(Integer.toString(0));
            fileWriter.close();
        }
        byte[] counterBytes = Files.readAllBytes(file.toPath());
        int counter = Integer.parseInt(new String(counterBytes));

        nonce.addProperty("counter", counter);

        fileWriter = new FileWriter("currentCounterProtect");
        fileWriter.write(Integer.toString((counter+1)));
        fileWriter.close();

        // timestamp
        long timestamp = System.currentTimeMillis();
        
        nonce.addProperty("timestamp", timestamp);

        return nonce;
    }

    private static byte[] encrypt(byte[] bytes, byte[] iv, SecretKey key, boolean verbose) throws Exception {
        // generate a random IV using block size (possibly use SecureRandom())
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        
        // get a AES cipher object
        Cipher cipher = Cipher.getInstance(SYM_CIPHER);

        if(verbose)
            System.out.println("Ciphering...");
        // initialize the cipher with the key and IV
        cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);

        // encrypt the data
        byte[] cipherBytes = cipher.doFinal(bytes);

        return cipherBytes;
    }

    /** Makes a message authentication code. */
	private static byte[] makeMAC(byte[] bytes, SecretKey key) throws Exception {
		Mac mac = Mac.getInstance(MAC_ALGO);
		mac.init(key);
		byte[] macBytes = mac.doFinal(bytes);

		return macBytes;
	}
}