
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
    private int[][] state = new int[Nb][4];

    public AES(byte[] key) throws Exception {
        if (key.length != 16) {
            throw new IllegalArgumentException("Invalid key length, currently only 128 is supported.");
        }

        expandedKey = new ExpandedKey(key);
    }
    
    public byte[] encrypt(byte[] plaintext) {

        for (int i = 0; i < Nb; i++) {
            for (int j = 0; j < 4; j++) {
                state[i][j] = plaintext[4 * i + j] & 0xFF;
            }
        }
        printState();

        //Lets go
        addRoundKey(0);
        printState();
        subBytes();
        printState();
        shiftRows();
        printState();
        mixColumns();
        printState();

        /*for (int i = 0; i < Nr; i++) {
            subBytes();
        }*/
        
        return null;
    }

    private void addRoundKey(int round) {
        for (int i = round * Nk; i < round * Nk + Nk; i++) {
            int w = expandedKey.getKey(i);
            for (int j = 0; j < 4; j++) {
                state[i][j] ^= (w >> 8 * (3 - j)) & 255; // Move the key byte to the first octet, AND it with 8 1's
            }
        }
    }

    private void subBytes() {
        for (int i = 0; i < Nb; i++) {
            for (int j = 0; j < 4; j++) {
                state[i][j] = SBox.subByte(state[i][j]);
            }
        }
    }

    private void shiftRows() {
        int[][] tmp = new int[Nb][4];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < Nb; j++) {
                tmp[j][i] = state[(i + j) % 4][i];
            }
        }
        state = tmp;

        return;
    }

    private void mixColumns() {
        for (int i = 0; i < Nb; i++) {
            int[] tmpCol = state[i].clone();
            state[i][0] = multiplyBytes(0x02, tmpCol[0]) ^ multiplyBytes(0x03, tmpCol[1]) ^ tmpCol[2] ^ tmpCol[3];
            state[i][1] = tmpCol[0] ^ multiplyBytes(0x02, tmpCol[1]) ^ multiplyBytes(0x03, tmpCol[2]) ^ tmpCol[3];
            state[i][2] = tmpCol[0] ^ tmpCol[1] ^ multiplyBytes(0x02, tmpCol[2]) ^ multiplyBytes(0x03, tmpCol[3]);
            state[i][3] = multiplyBytes(0x03, tmpCol[0]) ^ tmpCol[1] ^ tmpCol[2] ^ multiplyBytes(0x02, tmpCol[3]);
        }
    }

    private int multiplyBytes(int exponent, int value) {
        int mask = 1;
        int result = 0;

        for (int i = 0; i < 8; i++) {
            if ((exponent & mask) == 1) {
                int temp = value;
                for (int j = 0; j < i; j++) {
                    temp = multiplyOnce(temp);
                }
                result ^= temp;
            }

            exponent >>= 1;
        }

        return result;
    }

    private int multiplyOnce(int value) {
        int rijandael = 0x11b;
        int highBitMask = 0x80;
        int highBit = value & highBitMask;
        int result = value << 1;

        if (highBit == 0x80) {
            result ^= rijandael;
        }

        return result;
    }

    private void printState() {
        System.out.println("*******************************************");
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < Nb; j++) {
                System.out.print(hexString(state[j][i]) + " ");
            }
            System.out.println("");
        }
    }
    
    private String hexString(int i) {
        return String.format("0x%08X", i);
    }
}
