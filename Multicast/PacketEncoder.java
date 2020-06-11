import java.nio.*;
import java.io.*;
import java.security.Security;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.*;

public class PacketEncoder {
    byte[] buffer;
    byte[] seqBytes;
    byte[] userBytes;
    private static final String ENCRYPTION_ALGORITHM = "ARCFOUR";

    PacketEncoder(int user, int seq, byte[] buffer, byte[] key) {
        try {
            //initialize secret key for the encryption algorithm provided
            SecretKey secretKey = new SecretKeySpec(key, ENCRYPTION_ALGORITHM);
            //initialize the cipher for the encryption
            Cipher rc4 = Cipher.getInstance(ENCRYPTION_ALGORITHM);
            //set the mode of the cipher to encryption
            rc4.init(Cipher.ENCRYPT_MODE, secretKey);
            /*
             * convert user id and sequence number to byte array
             ***/
            userBytes = ByteBuffer.allocate(4).putInt(user).array();
            seqBytes = ByteBuffer.allocate(4).putInt(seq).array();
            ByteArrayOutputStream BOS = new ByteArrayOutputStream();

            //write user byte and sequence number to byte array output
            BOS.write(userBytes);
            BOS.write(seqBytes);
            //append encrypted buffer to byte array
            BOS.write(rc4.doFinal(buffer));

            //set the final encrypted payload to the buffer
            this.buffer = BOS.toByteArray();

        } catch (Exception E) {
            E.printStackTrace();
        }
    }
}
