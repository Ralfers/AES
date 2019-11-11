
package aes;

import java.util.Arrays;

/**
 *
 * @author Ralfs
 */
public class AES {

    static final int Nk = 4; //The columns of the key
    static final int Nr = 10; //The amount of rounds
    static final int Nb = 4; //The columns of the state

    private ExpandedKey expandedKey;
    private int[][] state = new int[4][Nb];

    public AES(byte[] key) throws Exception {
        if (key.length != 16) {
            throw new IllegalArgumentException("Invalid key length, currently only 128 is supported.");
        }

        expandedKey = new ExpandedKey(key);
    }
    
    public byte[] encrypt(byte[] plaintext) {
        for (int i = 0; i < Nk; i++) {
            for (int j = 0; j < 4; j++) {
                state[i][j] = plaintext[4 * i + j] & 0xFF;
            }
        }
        
        //Lets go
        addRoundKey(0);
        printState();

        for (int i = 1; i <= Nr; i++) {
            
        }
        
        
        return null;
    }
    
    private void addRoundKey(int round) {
        for (int i = round * Nk; i < round * Nk + 4; i++) {
            int w = expandedKey.getKey(i);
            for (int j = 0; j < Nk; j++) {
                state[i][j] ^= (w >> 8 * (Nk - 1 - j)) & 255;
            } 
        }
    }
    
    private void printState() {
        System.out.println("*******************************************");
        for (int i = 0; i < Nk; i++) {
            for (int j = 0; j < 4; j++) {
                System.out.print(hexString(state[i][j]) + " ");
            }
            System.out.println("");
        }
        System.out.println("*******************************************");
    }
    
    private String hexString(int i) {
        return String.format("0x%08X", i);
    }
}
