
package main;

import aes.AES;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *
 * @author Ralfs
 */
public class Main {

    private static byte[] key;
    private static byte[] plaintext;

    public static void main(String[] args) throws Exception {
        loadArgs(args);

        AES aes = new AES(key);
        aes.encrypt(plaintext);
    }

    public static String bytesToHex(byte[] in) {
        final StringBuilder builder = new StringBuilder();
        for(byte b : in) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }

    public static void loadArgs(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("Usage: java -jar AES.jar keyFilePath plaintextFilePath");
        }

        Path keyFilePath = Paths.get(args[0]);
        Path plaintextFilePath = Paths.get(args[1]);

        key =  Files.readAllBytes(keyFilePath);
        plaintext = Files.readAllBytes(plaintextFilePath);

        System.out.println("key: " + bytesToHex(key));
        System.out.println("plaintext: " + bytesToHex(plaintext));
    }
}
