
package aes;

import static aes.AES.Nb;
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

    private int w[] = new int[Nb * (Nr + 1)];
    private int key[] = new int[Nk * 4];

    public ExpandedKey(byte[] key) {
        for (int i = 0; i < key.length; i++) {
            this.key[i] = key[i] & 0xFF;
        }

        expandKey();
    }

    private void expandKey() {
        for (int i = 0; i < Nk; i++) {
            w[i] = 0x00000000;
            w[i] |= key[i * 4] << 24;
            w[i] |= key[i * 4 + 1] << 16;
            w[i] |= key[i * 4 + 2] << 8;
            w[i] |= key[i * 4 + 3];

            System.out.println("w" + i + ": " + hexString(w[i]));
        }

        for (int i = Nk; i < Nb * (Nr + 1); i++) {
            int temp = w[i - 1];
            System.out.print("w" + i + ": temp = " + hexString(temp));

            if (i % Nk == 0) {
                temp = Integer.rotateLeft(temp, 8);
                System.out.print(", RotWord() = " + hexString(temp));
                temp = SBox.subWord(temp);
                System.out.print(", SubWord() = " + hexString(temp));
                System.out.print(", Rcon[" + i + "/4]: " + hexString(roundConsts[i / Nk - 1]));
                temp ^= roundConsts[i / Nk - 1];
                System.out.print(", After XOR with Rcon: " + hexString(temp));
            } else if (Nk == 8 && i % Nk == 4) {
                temp = SBox.subWord(temp);
                System.out.print(", SubWord() = " + hexString(temp));
            }

            int prevRoundW = w[i - Nk];
            System.out.print(", w[" + i + "-4]: " + hexString(prevRoundW));
            w[i] = temp ^ prevRoundW;
            System.out.println(", w" + i + ": " + hexString(w[i]));
        }
    }

    String hexString(int i) {
        return String.format("0x%08X", i);
    }
    
    int getKey(int i) {
        return w[i];
    }
}
