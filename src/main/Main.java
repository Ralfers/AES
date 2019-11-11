
package Main;

import aes.AES;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import static sun.security.krb5.Confounder.bytes;

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

    public static void loadArgs(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("Usage: java -jar AES.jar keyFilePath plaintextFilePath");
        }

        Path keyFilePath = Paths.get(args[0]);
        Path plaintextFilePath = Paths.get(args[1]);

        key =  Files.readAllBytes(keyFilePath);
        plaintext = Files.readAllBytes(plaintextFilePath);

        System.out.println("key: " + javax.xml.bind.DatatypeConverter.printHexBinary(key));
        System.out.println("plaintext: " + javax.xml.bind.DatatypeConverter.printHexBinary(plaintext));
    }
}
