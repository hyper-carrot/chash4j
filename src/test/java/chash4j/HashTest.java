package chash4j;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;


@RunWith(JUnit4.class)
public class HashTest {

    @Test
    public void testGetHashBytes() {
        String content = "127.0.0.1:8080";
        byte[] bytes = Hash.getHashBytes(content);
        assertNotNull(bytes);
        assertEquals(20, bytes.length);
        byte[] expectedBytes =
                new byte[]{
                        (byte) 86, (byte) 133, (byte) 42, (byte) 84, (byte) 86,
                        (byte) 209, (byte) 176, (byte) 158, (byte) 30, (byte) 177,
                        (byte) 28, (byte) 12, (byte) 163, (byte) 157, (byte) 143,
                        (byte) 188, (byte) 230, (byte) 72, (byte) 1, (byte) 6};
        assertArrayEquals(expectedBytes, bytes);
    }

    @Test
    public void testGetKetamaNumbers() {
        String content = "127.0.0.1:8080";
        long[] ketamaNumbers = Hash.getKetamaNumbers(content);
        assertNotNull(ketamaNumbers);
        long[] expectedKetamaNumbers = new long[]{1412072790, 2662388054L, 203206942, 3163528611L};
        assertArrayEquals(expectedKetamaNumbers, ketamaNumbers);
    }

    @Test
    public void testGetHashForKey() {
        String key = "abc";
        long keyHash = Hash.getHashForKey(key);
        long expectedKeyHash = 1604963272L;
        assertEquals(expectedKeyHash, keyHash);
    }
}
