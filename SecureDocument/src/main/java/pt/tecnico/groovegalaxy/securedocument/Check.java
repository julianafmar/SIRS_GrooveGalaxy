package pt.tecnico.groovegalaxy.securedocument;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.security.GeneralSecurityException;
import java.util.Arrays;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import javax.xml.bind.DatatypeConverter;

public class Check {
    private static final String MAC_ALGO = "HmacSHA256";
    private static final int MAC_SIZE = 32;
    private static final int timestampTolerance = 120000; // 2 minutes

    public static void main(String[] args) throws IOException, GeneralSecurityException, Exception {
            
        // Check arguments
        if (args.length < 2) {
            System.err.println("Argument(s) missing!");
            System.err.println("Usage: check [input_file] [key]");
            return;
        }
        check_file(args[0], args[1], true);
    }
    public static void check_file(String input_file, String keyPath, boolean verbose) throws IOException, GeneralSecurityException, Exception {
        try (FileReader fileReader = new FileReader(input_file)) {
            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(fileReader, JsonObject.class);
            check(jsonObject, keyPath, verbose);
        }
    }

    public static boolean check(JsonObject jsonObject, String keyPath, boolean verbose) throws IOException, GeneralSecurityException, Exception{
        SecretKey key = readKey(keyPath, verbose);

        // get mac
        byte[] cipherDigest = DatatypeConverter.parseBase64Binary(jsonObject.get("mac").getAsString());

        // remove the mac
        jsonObject.remove("mac");

        // verify
        boolean result = verifyMAC(jsonObject.toString().getBytes(), cipherDigest, key, verbose);
        if(verbose)
            System.out.println("MAC is " + (result ? "right" : "wrong"));

        // get nonce
        JsonObject nonce = jsonObject.get("nonce").getAsJsonObject();
        boolean result2 = verifyNonce(nonce, verbose);

        if(verbose)
            System.out.println("Nonce is " + (result2 ? "right" : "wrong"));
        
        return result && result2;
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

    /**
	 * Calculates new digest from text and compare it to the to deciphered digest.
	 */
	private static boolean verifyMAC(byte[] file, byte[] cipherDigest, SecretKey key, boolean verbose) throws Exception {
        if(verbose)
            System.out.println("Verifying MAC...");

        Mac mac = Mac.getInstance(MAC_ALGO);
        mac.init(key);
        // calculate new digest from text
        byte[] recomputedMacBytes = mac.doFinal(file);

        // compare digests
        return Arrays.equals(cipherDigest, recomputedMacBytes);
	}

    private static boolean verifyNonce(JsonObject nonce, boolean verbose) throws IOException{
        if(verbose)
            System.out.println("Verifying nonce...");
        int counter = nonce.get("counter").getAsInt();
        File file = new File("currentCounterCheck");
        FileWriter fileWriter;
        if (!file.exists()) {
            fileWriter = new FileWriter("currentCounterCheck");
            fileWriter.write(Integer.toString(0));
            fileWriter.close();
        }
        byte[] counterBytes = Files.readAllBytes(file.toPath());
        int currentCounter = Integer.parseInt(new String(counterBytes));

        if (counter >= currentCounter) {
            fileWriter = new FileWriter("currentCounterCheck");
            fileWriter.write(Integer.toString((counter + 1)));
            fileWriter.close();
        }
        long timestamp = nonce.get("timestamp").getAsLong();
        long currentTimestamp = System.currentTimeMillis();

        return (counter >= currentCounter) && ((currentTimestamp - timestamp) < timestampTolerance);
    }
}
