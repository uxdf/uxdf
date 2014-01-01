package info.ralab.uxdf.chain;

import info.ralab.uxdf.UXDFLoader;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Set;

public class UXDFChainItemTest {

    @Before
    public void before() {
        UXDFLoader.reloadForced();
    }

    @Test
    public void testChainItem() {
        // 初始化ChainItem
        final String nodeName = "Signifier";
        final String eventName = "AS_MEMBER";

        UXDFChainItem chainItem = new UXDFChainItem();
        Assert.assertFalse(chainItem.addSd("S1:" + nodeName));
        chainItem.addPath(UXDFChain.PATH_LINE);
        Assert.assertFalse(chainItem.addSd(eventName));
        chainItem.addPath(UXDFChain.PATH_LINE);
        Assert.assertTrue(chainItem.addSd("S2:" + nodeName));

        System.out.println(chainItem.toString());

        // 检查基本属性是否正确
        Assert.assertEquals(ChainPath.BOTH, chainItem.getChainPath());

        Assert.assertTrue(chainItem.hasWildcards());

        Set<UXDFChainItem> chainItems = chainItem.createItemsByWildcards();
        Assert.assertNotNull(chainItems);
        Assert.assertFalse(chainItems.isEmpty());
        Assert.assertEquals(2, chainItems.size());

        chainItems.forEach(System.out::println);

    }

}
