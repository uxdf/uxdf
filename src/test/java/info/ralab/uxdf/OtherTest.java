package info.ralab.uxdf;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class OtherTest {

    @Test
    public void testMap() {
        String key = "foo";
        String value = "bar";
        Map<String, String> map = Maps.newHashMap();
        map.put(key, value);
        map.values().remove(value);
        Assert.assertFalse(map.containsKey(key));

        map.clear();
        Assert.assertNotNull(map.values());

        Assert.assertEquals(value, map.computeIfAbsent(key, currentKey -> "bar"));
    }

    @Test
    public void testSet() {
        Set<String> set = Sets.newHashSet();
        set.add(null);
        Assert.assertFalse(set.isEmpty());
    }

    @Test
    public void testByte() throws IOException {
        byte[] bytes = "{\"num\":123}".getBytes(StandardCharsets.UTF_8);
        bytes = Arrays.copyOf(bytes, 2048);

        Arrays.fill(bytes, 1024, 2047, (byte) 0);
        System.out.println(bytes.length);

        InputStream inputStream = new ByteArrayInputStream(bytes);
        byte[] jsonBytes = new byte[1024];
        System.out.println(inputStream.read(jsonBytes));
        System.out.println(new String(jsonBytes, StandardCharsets.UTF_8).trim());
        System.out.println(inputStream.read(jsonBytes));
        System.out.println(new String(jsonBytes, StandardCharsets.UTF_8));
    }

    @Test
    public void testRegex() {
        Pattern pattern = Pattern.compile("^unique(\\w+)Id$");
        Matcher matcher = pattern.matcher("uniqueUserId");
        if (matcher.find()) {
            System.out.println(matcher.group(1));
            Assert.assertEquals("User", matcher.group(1));
        }
    }

    public static void main(String[] args) {
        System.out.println(10 / 4);
        System.out.println(Integer.parseInt("w", 32));
    }
}
