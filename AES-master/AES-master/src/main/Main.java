
package main;

import aes.AES;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *
 * @author Ralfs
 */
public class Main {

    private static byte[] key = null;
    private static byte[] text = null;
    private static byte[] iv = null;
    private static char mode;

    public static void main(String[] args) throws Exception {
        loadArgs(args);
        AES aes = new AES(key);
        byte[] result;

        if (mode == 'e') {
            result = aes.encrypt(text, iv);
            try (FileOutputStream fos = new FileOutputStream(args[2] + ".enc")) {
                fos.write(result);
            }
        } else {
            result = aes.decrypt(text);
            try (FileOutputStream fos = new FileOutputStream(args[2] + ".dec")) {
                fos.write(result);
            }
        }
    }

    private static void loadArgs(String[] args) throws Exception {
        if (args.length < 3) {
            printUsage();
        }

        String modeStr = args[0];
        if (modeStr.length() != 1 || (modeStr.charAt(0) != 'e' && modeStr.charAt(0) != 'd')) {
            printUsage();
        }
        mode = modeStr.charAt(0);

        Path keyFilePath = Paths.get(args[1]);
        Path plaintextFilePath = Paths.get(args[2]);
        key =  Files.readAllBytes(keyFilePath);
        text = Files.readAllBytes(plaintextFilePath);

        if (args.length > 3) {
            Path ivFilePath = Paths.get(args[3]);
            iv = Files.readAllBytes(ivFilePath);
        }

        System.out.println("key: " + bytesToHex(key));
        System.out.println("plaintext: " + bytesToHex(text));
    }

    private static void printUsage() {
        System.out.println("Usage: java -jar AES.jar [e/d] [keyFilePath] [plaintextFilePath]");
    }

    private static String bytesToHex(byte[] in) {
        final StringBuilder builder = new StringBuilder();
        for(byte b : in) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }
}
