package info.ralab.uxdf;

import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

@Slf4j
public class UXDFLoaderTest {

    @Before
    public void before() {
        UXDFLoader.clear();
    }

    @After
    public void after() {
        UXDFLoader.clear();
    }

    /**
     * 测试缓存状态
     */
    @Test
    public void testCacheState() throws IOException {
        Assert.assertFalse(UXDFLoader.isCached());
        UXDFLoader.reloadForced();

        System.out.println(UXDFLoader.getEvents());

        Assert.assertTrue(UXDFLoader.isCached());
    }

}
