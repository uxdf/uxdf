package info.ralab.uxdf.chain;

import com.google.common.collect.Lists;
import info.ralab.uxdf.SdData;
import info.ralab.uxdf.UXDFLoader;
import info.ralab.uxdf.instance.EventEntity;
import info.ralab.uxdf.instance.NodeEntity;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;
import java.util.List;

public class UXDFChainTest {

    @Before
    public void before() {
        UXDFLoader.reloadForced();
    }

    @Test
    public void testSimple() {
        final String chainString = "A-E1>B<E2-C";
        UXDFChain chain = UXDFChain.getInstance(chainString);
        Assert.assertNotNull(chain);
        Assert.assertFalse(chain.getLabelSdMapping().isEmpty());
        Assert.assertEquals(5, chain.getLabelSdMapping().size());
    }

    /**
     * 测试关系链双向通配
     */
    @Test
    public void testBothPath() {
        // 通配关系链测试
        final String chainString = "Signifier-AS_MEMBER-Signifier";

        UXDFChain chain = UXDFChain.getInstance(chainString);
        Assert.assertNotNull(chain);

        Iterator<List<UXDFChainItem>> chainIterator = chain.iterator();

        // 取出第一个关系链
        Assert.assertTrue(chainIterator.hasNext());

        List<UXDFChainItem> chainItemList = chainIterator.next();
        Assert.assertNotNull(chainItemList);
        Assert.assertFalse(chainItemList.isEmpty());
        Assert.assertEquals(1, chainItemList.size());

        UXDFChainItem chainItemOne = chainItemList.get(0);
        System.out.println(chainItemOne.toString());
        Assert.assertEquals(chainItemOne.getChainPath(), ChainPath.LEFT);

        // 取出第二个关系链
        Assert.assertTrue(chainIterator.hasNext());

        chainItemList = chainIterator.next();
        Assert.assertNotNull(chainItemList);
        Assert.assertFalse(chainItemList.isEmpty());
        Assert.assertEquals(1, chainItemList.size());

        UXDFChainItem chainItemTwo = chainItemList.get(0);
        System.out.println(chainItemTwo.toString());
        Assert.assertEquals(chainItemTwo.getChainPath(), ChainPath.RIGHT);
    }

    private void getChildSd(SdData chainData, NodeEntity mainNode) {
        List<NodeEntity> nextGetNodes = Lists.newArrayList();

        List<EventEntity> eventEntities = chainData.getDetachedEvent(mainNode);
        eventEntities.forEach(eventEntity -> {
            NodeEntity leftNode = chainData.getNodeByLogicId(eventEntity.leftLogicId());
            NodeEntity rightNode = chainData.getNodeByLogicId(eventEntity.rightLogicId());

            String eventChain = String.format(
                    "%s-%s>%s",
                    eventEntity.get__LeftSd(),
                    eventEntity.get__Sd(),
                    eventEntity.get__RightSd()
            );

            if (!leftNode.equals(mainNode) && !leftNode.containsKey(eventChain)) {
                System.out.println(leftNode);
                leftNode.put(eventChain, true);
                eventEntity.put(eventChain, true);
                mainNode.put(eventChain, true);

                nextGetNodes.add(leftNode);
            }

            if (!rightNode.equals(mainNode) && !rightNode.containsKey(eventChain)) {
                System.out.println(rightNode);
                rightNode.put(eventChain, true);
                eventEntity.put(eventChain, true);
                mainNode.put(eventChain, true);

                nextGetNodes.add(rightNode);
            }

        });

        nextGetNodes.forEach(nodeEntity -> getChildSd(chainData, nodeEntity));
    }

}
