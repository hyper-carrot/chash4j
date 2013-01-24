package chash4j;

import java.security.MessageDigest;
import java.util.List;
import java.util.ArrayList;

public final class Hash {

    public final static String DEFAULT_CHARSET_NAME = "UTF-8";
    public final static int KETAMA_NUMBERS_LENGTH = 4;

    public static byte[] getHashBytes(final String content) {
        if (content == null || content.length() == 0) {
            return null;
        }
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.reset();
            md.update(content.getBytes(DEFAULT_CHARSET_NAME));
            return md.digest();
        } catch (final Exception e) {
            throw new RuntimeException("Hash Error: ", e);
        }
    }

    public static long[] getKetamaNumbers(String content) {
        byte[] bytes = getHashBytes(content);
        long[] numbers = new long[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            numbers[i] = bytes[i];
        }
        long[] ketamaNumbers = new long[4];
        long temp = -1;
        for (int i = 0; i < KETAMA_NUMBERS_LENGTH; i++) {
            temp = (numbers[3+i*4]&0xFF)<<24 | (numbers[2+i*4]&0xFF)<<16 | (numbers[1+i*4]&0xFF)<<8 | numbers[i*4]&0xFF;
            ketamaNumbers[i] = temp;
        }
        return ketamaNumbers;
    }

    public static long getHashForKey(String content) {
        long[] hashNumbers = getKetamaNumbers(content);
        long hash = 0;
        for (long n : hashNumbers) {
            hash += n;
        }
        return hash / hashNumbers.length;
    }
}