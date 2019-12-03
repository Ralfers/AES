
package aes;

import static aes.AES.Nk;
import static aes.AES.Nr;

/**
 *
 * @author Ralfs
 */
public class ExpandedKey {

    private static final int[] roundConsts = {
        0x01000000, 0x02000000, 0x04000000, 0x08000000, 0x10000000,
        0x20000000, 0x40000000, 0x80000000, 0x1b000000, 0x36000000
    };

    private int w[] = new int[44];
    private int key[] = new int[16];

    public ExpandedKey(byte[] key) {
        for (int i = 0; i < key.length; i++) {
            this.key[i] = key[i] & 0xFF;
        }

        expandKey();
    }

    private void expandKey() {
        //w0-w3
        for (int i = 0; i < Nk; i++) {
            w[i] = 0x00000000;
            w[i] |= key[i * 4] << 24;
            w[i] |= key[i * 4 + 1] << 16;
            w[i] |= key[i * 4 + 2] << 8;
            w[i] |= key[i * 4 + 3];

            //System.out.println("w" + i + ": " + hexString(w[i]));
        }
        //w4-w43
        for (int i = 1; i <= Nr; i++) {
            for (int j = 0; j < Nk; j++) {
                int number = i * Nk + j;
                int temp = w[number - 1];
                //System.out.print("w" + number + ": temp = " + hexString(temp));

                if (j == 0) {
                    temp = Integer.rotateLeft(temp, 8);
                  //  System.out.print(", RotWord() = " + hexString(temp));
                    temp = SBox.subWord(temp);
                    //System.out.print(", SubWord() = " + hexString(temp));
                    //System.out.print(", Rcon[" + number + "/4]: " + hexString(roundConsts[number / Nk - 1]));
                    temp ^= roundConsts[number / Nk - 1];
                    //System.out.print(", After XOR with Rcon: " + hexString(temp));
                }

                int prevRoundW = w[number - Nk];
                //System.out.print(", w[" + number + "-4]: " + hexString(prevRoundW));
                w[number] = temp ^ prevRoundW;
                //System.out.println(", w" + number + ": " + hexString(w[number]));
            }
        }
    }

    String hexString(int i) {
        return String.format("0x%08X", i);
    }
    
    int getKey(int i) {
        return w[i];
    }
}
