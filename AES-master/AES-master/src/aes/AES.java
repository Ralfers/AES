
package aes;

import java.io.ByteArrayOutputStream;
import java.security.SecureRandom;
import java.util.Arrays;

/**
 *
 * @author Ralfs
 */
public class AES {

    static final int BLOCK_SIZE = 16; //Constant for all AES types
    static final int IV_SIZE = 16; //Constant for all AES types
    static int Nk; //The columns of the key
    static int Nr; //The amount of rounds
    static int Nb = 4; //The columns of the state

    private ExpandedKey expandedKey;
    private int[][] state = new int[Nb][4];

    public AES(byte[] key) throws Exception {
        int keyLength = key.length;
        if (keyLength == 16) {
            Nk = 4;
            Nr = 10;
        } else if (keyLength == 24) {
            Nk = 6;
            Nr = 12;
        } else if (keyLength == 32) {
            Nk = 8;
            Nr = 14;
        } else {
            throw new IllegalArgumentException("Invalid key length, must be 16, 24 or 32 bytes");
        }

        expandedKey = new ExpandedKey(key);
    }

    public byte[] encrypt(byte[] plaintext, byte[] iv) throws Exception {
        byte[] fullPlaintext;
        int missingBytes = BLOCK_SIZE - plaintext.length % BLOCK_SIZE;
        if (missingBytes < BLOCK_SIZE) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            out.write(plaintext);
            for (int i = 0; i < missingBytes; i++) {
                out.write(0x05);
            }
            fullPlaintext = out.toByteArray();
        } else {
            fullPlaintext = plaintext;
        }

        if (fullPlaintext.length == BLOCK_SIZE) {
            return encryptBlock(fullPlaintext);
        } else {
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            if (iv == null) {
                SecureRandom randomSecureRandom = SecureRandom.getInstance("SHA1PRNG");
                iv = new byte[IV_SIZE];
                randomSecureRandom.nextBytes(iv);
            } else if (iv.length != IV_SIZE) {
                throw new Exception("IV must be " + IV_SIZE + " bytes long");
            }

            out.write(iv);

            byte[] previousBlock = null;
            for (int i = 0; i < fullPlaintext.length; i += 16) {
                byte[] part = Arrays.copyOfRange(fullPlaintext, i, i + 16);
                if (previousBlock == null) {
                    previousBlock = iv;
                }
                part = xorByteArrays(previousBlock, part);
                previousBlock = encryptBlock(part);
                out.write(previousBlock);
            }
            return out.toByteArray();
        }
    }

    public byte[] decrypt(byte[] cyphertext) throws Exception {
        if (cyphertext.length % BLOCK_SIZE != 0) {
            throw new Exception("Cyphertext is not in the right length");
        }
        if (cyphertext.length == BLOCK_SIZE) {
            return decryptBlock(cyphertext);
        } else {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] iv = Arrays.copyOfRange(cyphertext, 0, IV_SIZE);

            byte[] previousBlock = null;
            for (int i = 16; i < cyphertext.length; i += 16) {
                byte[] part = Arrays.copyOfRange(cyphertext, i, i + 16);
                byte[] tmp = decryptBlock(part);
                if (previousBlock == null) {
                    previousBlock = iv;
                }
                tmp = xorByteArrays(previousBlock, tmp);
                previousBlock = part;
                out.write(tmp);
            }
            return out.toByteArray();
        }
    }

    private static byte[] xorByteArrays(byte[] a, byte[] b) {
        byte[] result = new byte[Math.min(a.length, b.length)];
        for (int i = 0; i < result.length; i++) {
            int xor = (a[i] & 0xff) ^ (b[i] & 0xff);
            result[i] = (byte) xor;
        }
        return result;
    }

    public byte[] encryptBlock(byte[] plaintext) {
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

    public byte[] decryptBlock(byte[] cyphertext) {
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
        for (int i = 0; i < Nb; i++) {
            int w = expandedKey.getKey(i + round * Nb);
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
