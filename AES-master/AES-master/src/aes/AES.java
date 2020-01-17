
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

        for (int i = 1; i <= Nr; i++) {
            subBytes();
            shiftRows();
            if (i != Nr) {
                mixColumns();
            }
            addRoundKey(i);
        }
        printState();
        
        byte[] output = new byte[Nb * 4];
        for (int i = 0; i < Nb; i++) {
            for (int j = 0; j < 4; j++) {
                output[i * Nb + j] = (byte) state[i][j];
            }
        }

        return output;
    }

    public byte[] decrypt(byte[] cyphertext) {
        for (int i = 0; i < Nb; i++) {
            for (int j = 0; j < 4; j++) {
                state[i][j] = cyphertext[4 * i + j] & 0xFF;
            }
        }
        printState();

        //Lets go
        addRoundKey(Nr);

        for (int i = Nr; i >= 1; i--) {
            invShiftRows();
            invSubBytes();
            addRoundKey(i - 1);
            if (i != 1) {
                invMixColumns();
            }
        }
        printState();

        byte[] output = new byte[Nb * 4];
        for (int i = 0; i < Nb; i++) {
            for (int j = 0; j < 4; j++) {
                output[i * Nb + j] = (byte) state[i][j];
            }
        }

        return output;
    }

    private void addRoundKey(int round) {
        for (int i = 0; i < Nk; i++) {
            int w = expandedKey.getKey(i + round * Nk);
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

    private void invSubBytes() {
        for (int i = 0; i < Nb; i++) {
            for (int j = 0; j < 4; j++) {
                state[i][j] = SBox.invSubByte(state[i][j]);
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
    }

    private void invShiftRows() {
        int[][] tmp = new int[Nb][4];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < Nb; j++) {
                tmp[j][i] = state[(4 - (i - j)) % 4][i];
            }
        }

        state = tmp;
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

    private void invMixColumns() {
        for (int i = 0; i < Nb; i++) {
            int[] tmpCol = state[i].clone();
            state[i][0] = multiplyBytes(0x0e, tmpCol[0]) ^ multiplyBytes(0x0b, tmpCol[1]) ^ multiplyBytes(0x0d, tmpCol[2]) ^ multiplyBytes(0x09, tmpCol[3]);
            state[i][1] = multiplyBytes(0x09, tmpCol[0]) ^ multiplyBytes(0x0e, tmpCol[1]) ^ multiplyBytes(0x0b, tmpCol[2]) ^ multiplyBytes(0x0d, tmpCol[3]);
            state[i][2] = multiplyBytes(0x0d, tmpCol[0]) ^ multiplyBytes(0x09, tmpCol[1]) ^ multiplyBytes(0x0e, tmpCol[2]) ^ multiplyBytes(0x0b, tmpCol[3]);
            state[i][3] = multiplyBytes(0x0b, tmpCol[0]) ^ multiplyBytes(0x0d, tmpCol[1]) ^ multiplyBytes(0x09, tmpCol[2]) ^ multiplyBytes(0x0e, tmpCol[3]);
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
