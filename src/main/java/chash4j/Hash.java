package chash4j;

import java.security.MessageDigest;
import java.util.List;
import java.util.ArrayList;

public enum Hash {
    INSTANCE;

    public final static String DEFAULT_CHARSET_NAME = "UTF-8";
    public final static int KETAMA_NUMBERS_LENGTH = 4;

    public byte[] getHashBytes(final String content) {
        if (content == null || content.length() == 0) {
            return null;
        }
        try {
            MessageDigest md = MessageDigest.getInstance("SH1");
            md.reset();
            md.update(content.getBytes(DEFAULT_CHARSET_NAME));
            return md.digest();
        } catch (final Exception e) {
            throw new RuntimeException("Hash Error: ", e);
        }
    }

    public List<Long> getKetamaNumbers(String content) {
        byte[] bytes = getHashBytes(content);
        long[] numbers = new long[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            numbers[i] = bytes[i];
        }
        List<Long> ketamaNumbers = new ArrayList<Long>();
        long temp = -1;
        for (int i = 0; i < KETAMA_NUMBERS_LENGTH; i++) {
            temp = (numbers[3+i*4]&0xFF)<<24 | (numbers[2+i*4]&0xFF)<<16 | (numbers[1+i*4]&0xFF)<<8 | numbers[i*4]&0xFF;
            ketamaNumbers.add(temp);
        }
        return ketamaNumbers;
    }

    public long getHashForKey(String content) {
        List<Long> hashNumbers = getKetamaNumbers(content);
        long hash = 0;
        for (long n : hashNumbers) {
            hash += n;
        }
        return hash / hashNumbers.size();
    }
}
