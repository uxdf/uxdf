package info.ralab.uxdf.instance;

import com.alibaba.fastjson.JSON;
import info.ralab.uxdf.SdData;
import info.ralab.uxdf.chain.UXDFChain;
import info.ralab.uxdf.definition.SdOperateType;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;

import java.util.Date;

@Slf4j
public class NodeEntityTest {

    /**
     * 创建一个NodeEntity实例
     */
    private NodeEntity createNodeEntity() {
        NodeEntity nodeEntity = new NodeEntity("User", IdMaker.next());
        nodeEntity.setOperate(SdOperateType.create);
        nodeEntity.put("name", "name");
        nodeEntity.put("userName", "userName");
        return nodeEntity;
    }

    /**
     * 测试通过NodeEntity的构造函数构造的Node实例是否一致
     */
    @Test
    public void createNodeEntityTest() {
        // 构造函数public NodeEntity(final String sd, final long id)
        NodeEntity nodeEntity = createNodeEntity();

        // 构造函数public NodeEntity(final JSONObject json)
        final String nodeStringRight = "{\n" +
                "\t\"$operate\": \"create\",\n" +
                "\t\"__id\": \"" + nodeEntity.get__Id() + "\",\n" +
                "\t\"name\": \"name\",\n" +
                "\t\"userName\": \"userName\",\n" +
                "\t\"__sd\": \"User\"\n" +
                "}";
        NodeEntity nodeEntityRight = new NodeEntity(JSON.parseObject(nodeStringRight));

        // 两个构造函数构造的Node实例是否一致
        Assert.assertEquals(nodeEntity, nodeEntityRight);
    }

    /**
     * 测试NodeEntity的getEntityDisplay方法
     */
    @Test
    public void getEntityDisplayTest() {

        // node定义不存在
        final String nodeName1 = "Test";
        Assert.assertTrue(new NodeEntity(nodeName1, IdMaker.next()).getEntityDisplay().isEmpty());

        // node定义存在，node属性display的值不存在
        final String nodeName2 = "User";
        Assert.assertEquals("null", new NodeEntity(nodeName2, IdMaker.next()).getEntityDisplay());

        // node定义存在，node属性display的值存在
        final String nodeName3 = "User";
        NodeEntity nodeEntity3 = new NodeEntity(nodeName3, IdMaker.next());
        nodeEntity3.put("name", "test");
        Assert.assertFalse(nodeEntity3.getEntityDisplay().isEmpty());
    }

    /**
     * 测试NodeEntity的makeBaseProperties、isEffective、equal、generateUUID方法
     */
    @Test
    public void nodeEntityTest() {
        // 准备测试数据
        // normal
        NodeEntity nodeEntity = createNodeEntity();
        // 无ID
        NodeEntity nodeEntityNoId = nodeEntity.clone();
        nodeEntityNoId.remove(NodeEntity.ATTR_ID);
        // 无SD
        NodeEntity nodeEntityNoSd = nodeEntity.clone();
        nodeEntityNoSd.remove(NodeEntity.ATTR_SD);
        // normal
        final String nodeStringRight = "{\n" +
                "\t\"$operate\": \"create\",\n" +
                "\t\"__id\": \"" + nodeEntity.get__Id() + "\",\n" +
                "\t\"name\": \"name\",\n" +
                "\t\"userName\": \"userName\",\n" +
                "\t\"__sd\": \"User\"\n" +
                "}";
        NodeEntity nodeEntityRight = new NodeEntity(JSON.parseObject(nodeStringRight));
        // 更新id
        final String nodeStringUpdateId = "{\n" +
                "\t\"$operate\": \"create\",\n" +
                "\t\"__id\": \"" + IdMaker.next() + "\",\n" +
                "\t\"name\": \"name\",\n" +
                "\t\"userName\": \"userName\",\n" +
                "\t\"__sd\": \"User\"\n" +
                "}";
        NodeEntity nodeEntityUpdateId = new NodeEntity(JSON.parseObject(nodeStringUpdateId));
        // 更新sd
        final String nodeStringUpdateSd = "{\n" +
                "\t\"$operate\": \"create\",\n" +
                "\t\"__id\": \"" + nodeEntity.get__Id() + "\",\n" +
                "\t\"name\": \"name\",\n" +
                "\t\"userName\": \"userName\",\n" +
                "\t\"__sd\": \"Test\"\n" +
                "}";
        NodeEntity nodeEntityUpdateSd = new NodeEntity(JSON.parseObject(nodeStringUpdateSd));

        // 测试makeBaseProperties方法
        StringBuilder baseProperties = new StringBuilder();
        Assert.assertEquals(
                baseProperties.append(nodeEntity.get__Sd())
                        .toString(),
                nodeEntity.makeBaseProperties().toString()
        );
        Assert.assertNull(nodeEntityNoSd.makeBaseProperties());
        baseProperties = new StringBuilder();
        Assert.assertEquals(
                baseProperties.append(nodeEntityNoId.get__Sd())
                        .toString(),
                nodeEntityNoId.makeBaseProperties().toString()
        );

        // 测试isEffective方法
        Assert.assertTrue(nodeEntity.isEffective());
        Assert.assertFalse(nodeEntityNoId.isEffective());
        Assert.assertFalse(nodeEntityNoSd.isEffective());

        // 测试equal方法
        Assert.assertEquals(nodeEntity, nodeEntityRight);
        Assert.assertNotEquals(nodeEntity, nodeEntityUpdateId);
        Assert.assertNotEquals(nodeEntity, nodeEntityUpdateSd);

        // 测试generateUUID方法
        Assert.assertNotNull(nodeEntity.generateUUID());
        Assert.assertNotNull(nodeEntityUpdateSd.generateUUID());
        // 唯一属性没有值的情况
        NodeEntity nodeEntityNoUserName = createNodeEntity().clone();
        nodeEntityNoUserName.remove("userName");
        Assert.assertNull(nodeEntityNoUserName.generateUUID());
        /*try {
            Assert.assertNull(nodeEntityNoBranch.generateUUID());
        } catch (Exception e) {
            log.error(e.getLocalizedMessage(), e);
        }*/
    }

    /**
     * 测试NodeEntity的merge方法
     */
    @Test
    public void mergeTest() {
        String nodeId = IdMaker.next();
        NodeEntity nodeEntity = new NodeEntity();
        nodeEntity.set__Sd("test");
        nodeEntity.set__Id(nodeId);
        nodeEntity.setOperate(SdOperateType.update);
        Date createTime = new Date();
        Date updateTime = new Date();
        nodeEntity.set__CreateTime(createTime);
        nodeEntity.set__UpdateTime(updateTime);
        nodeEntity.put("name", "test");
        nodeEntity.put("userName", "test");

        nodeEntity.merge(createNodeEntity());

        // 断言是否跳过了不合并属性
        Assert.assertEquals("test", nodeEntity.get__Sd());
        Assert.assertEquals(nodeId, nodeEntity.get__Id());
        Assert.assertEquals(SdOperateType.update, nodeEntity.getOperate());
        Assert.assertEquals(createTime, nodeEntity.get__CreateTime());
        Assert.assertEquals(updateTime, nodeEntity.get__UpdateTime());

        // 断言是否合并了需要合并的属性
        Assert.assertEquals("name", nodeEntity.getString("name"));
        Assert.assertEquals("userName", nodeEntity.getString("userName"));
    }

    /**
     * 测试NodeEntity的clone方法
     */
    @Test
    public void cloneTest() {
        NodeEntity nodeEntity = createNodeEntity();
        NodeEntity nodeEntity1 = nodeEntity.clone();
        nodeEntity.remove(NodeEntity.ATTR_UUID);
        Assert.assertEquals(nodeEntity, nodeEntity1);
    }

    @Test
    public void testBasic() {
        String chainString = "a:A-b:B>ttt:C";
        UXDFChain chain = UXDFChain.getInstance(chainString);
        chain.forEach(uxdfChainItems -> {
            uxdfChainItems.forEach(chainItem -> {
                System.out.println(chainItem);
                Assert.assertEquals(chainItem.getFirstNode(), "A");
                Assert.assertEquals(chainItem.getFirstLabel(), "a");
                Assert.assertEquals(chainItem.getEvent(), "B");
                Assert.assertEquals(chainItem.getEventLabel(), "b");
                Assert.assertEquals(chainItem.getLastNode(), "C");
                Assert.assertEquals(chainItem.getLastLabel(), "ttt");
            });
        });
    }

    @Test
    public void getUUIDTest() {
        String dataSourceId = IdMaker.next();
        NodeEntity dataSource = new NodeEntity("DataSource", dataSourceId);
        dataSource.put("databaseName", "m数据源名称");
        dataSource.setOperate(SdOperateType.create);

        String businessSystemId = IdMaker.next();
        NodeEntity businessSystem = new NodeEntity("BusinessSystem", businessSystemId);
        businessSystem.put("name", "m名字");
        businessSystem.setOperate(SdOperateType.create);

        EventEntity belong_to4 = new EventEntity();
        belong_to4.set__Left(dataSourceId);
        belong_to4.set__LeftSd("DataSource");
        belong_to4.set__Right(businessSystemId);
        belong_to4.set__RightSd("BusinessSystem");
        belong_to4.set__Id(IdMaker.next());
        belong_to4.set__Sd("BELONG_TO");
        belong_to4.setOperate(SdOperateType.create);

        SdData sdData = new SdData();
        sdData.addNodeIfAbsent(dataSource);
        sdData.addNodeIfAbsent(businessSystem);

        sdData.addEventIfAbsent(belong_to4);


        //测试是否可以从获取UUID
        String uuid = dataSource.getUUID();

        //测试metadataField中生成的UUID和他相关的event中的node  uuid是否相同
        String leftLogicId = sdData.getEvent(belong_to4).leftLogicId();
        String uuid1 = sdData.getNodeByLogicId(leftLogicId).getUUID();

        System.out.println(uuid + "---------" + uuid1);
        Assert.assertEquals(uuid, uuid1);
    }
    /*
    测试NodeEntity的getUUIDTest方法 表达式可以有多重

    @Test
    public void getUUIDTest(){
        NodeEntity metadataField = new NodeEntity("MetadataField",-1);
        metadataField.set__Repository("m");
        metadataField.set__Branch("1.0");
        metadataField.set__Version("v1");
        metadataField.put("metadataTableId",-2L);
        metadataField.put("fieldName","m字段名称");
        metadataField.setOperate(SdOperateType.create);

        NodeEntity metadataTable = new NodeEntity("MetadataTable",-2);
        metadataTable.set__Repository("m");
        metadataTable.set__Branch("1.0");
        metadataTable.set__Version("v1");
        metadataTable.put("schemaId",-3L);
        metadataTable.put("tableName","m表名");
        metadataTable.setOperate(SdOperateType.create);

        NodeEntity schema = new NodeEntity("Schema",-3);
        schema.set__Repository("m");
        schema.set__Branch("1.0");
        schema.set__Version("v1");
        schema.put("dataSourceId",-4L);
        schema.put("name","m模式名称");
        schema.setOperate(SdOperateType.create);

        NodeEntity dataSource = new NodeEntity("DataSource",-4);
        dataSource.set__Repository("m");
        dataSource.set__Branch("1.0");
        dataSource.set__Version("v1");
        dataSource.put("databaseName","m数据源名称");
        dataSource.setOperate(SdOperateType.create);

        NodeEntity businessSystem = new NodeEntity("BusinessSystem",-5);
        businessSystem.set__Repository("m");
        businessSystem.set__Branch("1.0");
        businessSystem.set__Version("v1");
        businessSystem.put("name","m名字");
        businessSystem.setOperate(SdOperateType.create);


        EventEntity belong_to1 = new EventEntity();
        belong_to1.set__Left(-1L);
        belong_to1.set__LeftSd("MetadataField");
        belong_to1.set__LeftRepository("m");
        belong_to1.set__LeftBranch("1.0");
        belong_to1.set__LeftVersion("v1");
        belong_to1.set__Right(-2L);
        belong_to1.set__RightSd("MetadataTable");
        belong_to1.set__RightRepository("m");
        belong_to1.set__RightBranch("1.0");
        belong_to1.set__RightVersion("v1");
        belong_to1.set__Id(-11L);
        belong_to1.set__Sd("BELONG_TO");
        belong_to1.set__Repository("m");
        belong_to1.set__Branch("1.0");
        belong_to1.set__Version("v1");
        belong_to1.setOperate(SdOperateType.create);

        EventEntity belong_to2 = new EventEntity();
        belong_to2.set__Left(-2L);
        belong_to2.set__LeftSd("MetadataTable");
        belong_to2.set__LeftRepository("m");
        belong_to2.set__LeftBranch("1.0");
        belong_to2.set__LeftVersion("v1");
        belong_to2.set__Right(-3L);
        belong_to2.set__RightSd("Schema");
        belong_to2.set__RightRepository("m");
        belong_to2.set__RightBranch("1.0");
        belong_to2.set__RightVersion("v1");
        belong_to2.set__Id(-12L);
        belong_to2.set__Sd("BELONG_TO");
        belong_to2.set__Repository("m");
        belong_to2.set__Branch("1.0");
        belong_to2.set__Version("v1");
        belong_to2.setOperate(SdOperateType.create);

        EventEntity belong_to3 = new EventEntity();
        belong_to3.set__Left(-3L);
        belong_to3.set__LeftSd("Schema");
        belong_to3.set__LeftRepository("m");
        belong_to3.set__LeftBranch("1.0");
        belong_to3.set__LeftVersion("v1");
        belong_to3.set__Right(-4L);
        belong_to3.set__RightSd("DataSource");
        belong_to3.set__RightRepository("m");
        belong_to3.set__RightBranch("1.0");
        belong_to3.set__RightVersion("v1");
        belong_to3.set__Id(-13L);
        belong_to3.set__Sd("BELONG_TO");
        belong_to3.set__Repository("m");
        belong_to3.set__Branch("1.0");
        belong_to3.set__Version("v1");
        belong_to3.setOperate(SdOperateType.create);

        EventEntity belong_to4 = new EventEntity();
        belong_to4.set__Left(-4L);
        belong_to4.set__LeftSd("DataSource");
        belong_to4.set__LeftRepository("m");
        belong_to4.set__LeftBranch("1.0");
        belong_to4.set__LeftVersion("v1");
        belong_to4.set__Right(-5L);
        belong_to4.set__RightSd("BusinessSystem");
        belong_to4.set__RightRepository("m");
        belong_to4.set__RightBranch("1.0");
        belong_to4.set__RightVersion("v1");
        belong_to4.set__Id(-14L);
        belong_to4.set__Sd("BELONG_TO");
        belong_to4.set__Repository("m");
        belong_to4.set__Branch("1.0");
        belong_to4.set__Version("v1");
        belong_to4.setOperate(SdOperateType.create);

        SdData sdData = new SdData();
        sdData.addNodeIfAbsent(metadataField);
        sdData.addNodeIfAbsent(metadataTable);
        sdData.addNodeIfAbsent(schema);
        sdData.addNodeIfAbsent(dataSource);
        sdData.addNodeIfAbsent(businessSystem);

        sdData.addEventIfAbsent(belong_to1);
        sdData.addEventIfAbsent(belong_to2);
        sdData.addEventIfAbsent(belong_to3);
        sdData.addEventIfAbsent(belong_to4);

        //测试是否可以从获取UUID
        String uuid = metadataField.getUUID(sdData);

        //测试metadataField中生成的UUID和他相关的event中的node  uuid是否相同
        String leftLogicId = sdData.getEvent(belong_to1).leftLogicId();
        String uuid1 = sdData.getNodeByLogicId(leftLogicId).getUUID(sdData);

        System.out.println(uuid+"---------"+uuid1);
        Assert.assertEquals(uuid,uuid1);

    }
    */

}