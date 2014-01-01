package info.ralab.uxdf;

import com.alibaba.fastjson.JSON;
import info.ralab.uxdf.instance.EventEntity;
import info.ralab.uxdf.instance.IdMaker;
import info.ralab.uxdf.instance.NodeEntity;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;

@Slf4j
public class SdDataTest {

    /**
     * 测试{@link NodeEntity}在{@link SdData}中的存取逻辑是否正常。
     */
    @Test
    public void testNode() {
        final String nodeName = "Test";
        final String nodeId = IdMaker.next();
        final NodeEntity nodeByInit = new NodeEntity(nodeName, nodeId);

        // 空的SdData
        SdData data = new SdData();
        Assert.assertTrue(data.isEmpty());
        Assert.assertTrue(data.isNodeEmpty());
        Assert.assertTrue(data.isEventEmpty());

        // 加入Node，断言状态是否正确
        data.addNodeIfAbsent(nodeByInit);
        Assert.assertFalse(data.isEmpty());
        Assert.assertFalse(data.isNodeEmpty());
        Assert.assertTrue(data.isEventEmpty());

        // 基于新的NodeEntity进行查询，断言可以获取
        final NodeEntity nodeByQuery = new NodeEntity(nodeName, nodeId);
        Assert.assertTrue(data.containsNode(nodeByQuery));
        NodeEntity nodeByReturn = data.getNode(nodeByQuery);
        Assert.assertNotNull(nodeByReturn);
    }

    /**
     * 测试未定义Sd加入SdData是否正确
     */
    @Test
    public void testUnDefinitionSd() {
        String nodeNameA = "NodeA";
        NodeEntity nodeOne = new NodeEntity(nodeNameA, IdMaker.next());

        NodeEntity nodeTwo = new NodeEntity(nodeNameA, IdMaker.next());

        String eventName = "Event";
        EventEntity eventEntity = new EventEntity();
        eventEntity.sd(eventName)
                .id(IdMaker.next())
                .leftNode(nodeOne)
                .rightNode(nodeTwo);


        SdData sdData = new SdData();

        Assert.assertTrue(sdData.addNodeIfAbsent(nodeOne));
        Assert.assertTrue(sdData.addNodeIfAbsent(nodeTwo));
        Assert.assertTrue(sdData.addEventIfAbsent(eventEntity));

        System.out.println(JSON.toJSONString(sdData));
    }

    /**
     * 测试Node和Event的更新是否正常
     */
    @Test
    public void testUpdate() {

        String nodeNameA = "NodeA";
        NodeEntity nodeOne = new NodeEntity(nodeNameA, IdMaker.next());

        NodeEntity nodeTwo = new NodeEntity(nodeNameA, IdMaker.next());

        String eventName = "Event";
        EventEntity eventEntityOne = new EventEntity();
        eventEntityOne.sd(eventName)
                .id(IdMaker.next())
                .leftNode(nodeOne)
                .rightNode(nodeTwo);


        SdData sdData = new SdData();

        Assert.assertTrue(sdData.addNodeIfAbsent(nodeOne));
        Assert.assertTrue(sdData.addNodeIfAbsent(nodeTwo));
        Assert.assertTrue(sdData.addEventIfAbsent(eventEntityOne));

        System.out.println(JSON.toJSONString(sdData));

        // 更新Node
        NodeEntity nodeThree = nodeOne.clone();
        nodeThree.id(IdMaker.next());

        sdData.updateNode(nodeOne.getLogicId(), nodeOne.get__Id(), nodeThree);

        Assert.assertEquals(1, sdData.getDetachedEvent(nodeOne).size());
        Assert.assertEquals(1, sdData.getDetachedEvent(nodeThree).size());
        Assert.assertNotNull(sdData.getNodeByLogicId(nodeThree.getLogicId()));
        Assert.assertNotNull(sdData.getNodeByLogicId(nodeOne.getLogicId()));
        Assert.assertEquals(nodeOne, nodeThree);

        System.out.println(JSON.toJSONString(sdData));

        // 更新Event
        sdData.addNodeIfAbsent(nodeOne);

        EventEntity eventEntityTwo = eventEntityOne.clone();
        eventEntityTwo.leftNode(nodeOne);
        sdData.updateEvent(eventEntityOne.getLogicId(), eventEntityOne.get__Id(), eventEntityTwo);
        Assert.assertEquals(1, sdData.getDetachedEvent(nodeOne).size());
        Assert.assertEquals(1, sdData.getDetachedEvent(nodeThree).size());
        Assert.assertEquals(eventEntityOne, eventEntityTwo);
        Assert.assertNotNull(sdData.getEvent(eventEntityOne.getUUID()));

        System.out.println(JSON.toJSONString(sdData));
    }
}
