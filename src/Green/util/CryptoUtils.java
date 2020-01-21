package Green.util;



import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;

/**
 * A utility class that encrypts or decrypts a file.
 *
 * @author www.codejava.net
 *
 */
public class CryptoUtils {

    private static final int KEY_SIZE = 128;
    private static final int ITERATION_COUNT = 10000;
    private static final String IV = "F27D545647726BCEFE7810B1BDD3D537";
    private static final String SALT = "3FF78C019C627B955225DE2f511A01B6985FE84C95A70EB132882F88C0A59A55";
    private static final String KEY_PHARASE = "CUH jbcp passpharase2 aes encoding algorithm";

    private final Cipher cipher;
    private final SecretKey key;

    public CryptoUtils() throws Exception {
//                this.keySize = keySize;
//
//            this.iterationCount = iterationCount;

        try {

            cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            key = generateKey();

        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {

            throw fail(e);

        }

    }

    public String encrypt(String plaintext) throws Exception {

        try {

        //  SecretKey key = generateKey();
            byte[] encrypted = doFinal(Cipher.ENCRYPT_MODE, plaintext.getBytes("UTF-8"));

            return base64(encrypted);

        } catch (UnsupportedEncodingException e) {

            throw fail(e);

        }

    }

    public String decrypt(String ciphertext) throws Exception {

        try {

            byte[] decrypted = doFinal(Cipher.DECRYPT_MODE, base64(ciphertext));

            return new String(decrypted, "UTF-8");

        } catch (UnsupportedEncodingException e) {

            throw fail(e);

        }

    }

    private byte[] doFinal(int encryptMode, byte[] bytes) throws InvalidAlgorithmParameterException, Exception {

        try {

            cipher.init(encryptMode, key, new IvParameterSpec(hex(IV)));

            return cipher.doFinal(bytes);

        } catch (InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {

            throw fail(e);

        }

    }

    private SecretKey generateKey() throws Exception {

        try {

            SecretKeyFactory factory = SecretKeyFactory
                    .getInstance("PBKDF2WithHmacSHA1");

            KeySpec spec = new PBEKeySpec(KEY_PHARASE.toCharArray(),
                    hex(SALT), ITERATION_COUNT, KEY_SIZE);

            SecretKey key = new SecretKeySpec(factory.generateSecret(spec)
                    .getEncoded(), "AES");

            return key;

        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {

            throw fail(e);

        }

    }

    public static String random(int length) {

        byte[] salt = new byte[length];

        new SecureRandom().nextBytes(salt);

        return hex(salt);

    }

    public static String base64(byte[] bytes) {

        return Base64.encodeBase64String(bytes);

    }

    public static byte[] base64(String str) {

        return Base64.decodeBase64(str);

    }

    public static String hex(byte[] bytes) {

        return Hex.encodeHexString(bytes);

    }

    public static byte[] hex(String str) {

        try {

            return Hex.decodeHex(str.toCharArray());

        } catch (DecoderException e) {

            throw new IllegalStateException(e);

        }

    }

    private IllegalStateException fail(Exception e) {

        return new IllegalStateException(e);

    }

    public static void main(String[] args) {
        CryptoUtils util = null;
        try {
            util = new CryptoUtils();

            String txt = "utf-8";
            String encrypt = util.encrypt(txt);

            String decrypt = util.decrypt(encrypt);

            System.out.println("문자열 : " + txt);

            System.out.println("암호화 : " + encrypt);

            System.out.println("복호화 : " + decrypt);
        } catch (Exception ex) {
            Logger.getLogger(CryptoUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
