package info.ralab.uxdf.instance;

import com.alibaba.fastjson.JSON;
import info.ralab.uxdf.UXDFException;
import info.ralab.uxdf.definition.SdOperateType;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;

import java.util.Date;

@Slf4j
public class EventEntityTest {

    /**
     * 创建一个EventEntity实例
     */
    private EventEntity createEventEntity(
            String eventName,
            String id,
            String leftSd,
            String leftId,
            String rightSd,
            String rightId
    ) {
        EventEntity eventEntity = new EventEntity();
        eventEntity.setOperate(SdOperateType.create);
        eventEntity.set__Sd(eventName);
        eventEntity.set__Id(id);
        eventEntity.set__Left(leftId);
        eventEntity.set__LeftSd(leftSd);
        eventEntity.set__Right(rightId);
        eventEntity.set__RightSd(rightSd);
        return eventEntity;
    }

    /**
     * 测试通过EventEntity的构造函数构造的Event实例是否一致
     */
    @Test
    public void createEventEntityTest() {
        String eventId = IdMaker.next();
        String leftId = IdMaker.next();
        String rightId = IdMaker.next();
        // 构造函数public EventEntity()
        EventEntity eventEntity = createEventEntity(
                "BELONG_TO",
                eventId,
                "User",
                leftId,
                "Department",
                rightId
        );

        // 构造函数public EventEntity(final JSONObject json)
        final String eventEntityStringRight = "{\n" +
                "\t\"$operate\": \"create\",\n" +
                "\t\"__sd\": \"BELONG_TO\",\n" +
                "\t\"__id\": \"" + eventId + "\",\n" +
                "\t\"__leftSd\": \"User\",\n" +
                "\t\"__left\": \"" + leftId + "\",\n" +
                "\t\"__rightSd\": \"Department\",\n" +
                "\t\"__right\": \"" + rightId + "\"\n" +
                "}";
        EventEntity eventEntityRight = new EventEntity(JSON.parseObject(eventEntityStringRight));

        // 通过EventEntity的构造函数构造的EventEntity是否相等
        Assert.assertEquals(eventEntity, eventEntityRight);
    }

    /**
     * 测试EventEntity的getEntityDisplay方法
     */
    @Test
    public void getEntityDisplayTest() {
        String eventId = IdMaker.next();
        String leftId = IdMaker.next();
        String rightId = IdMaker.next();
        // event定义不存在
        EventEntity eventEntity = createEventEntity(
                "BELONG_TO",
                eventId,
                "User",
                leftId,
                "Department",
                rightId
        );
        Assert.assertFalse(eventEntity.getEntityDisplay().isEmpty());

        // event定义存在，event属性display的值不存在
        EventEntity eventEntity1 = createEventEntity(
                "BELONG_TO",
                eventId,
                "User",
                leftId,
                "Department",
                rightId
        );
        eventEntity1.remove(EventEntity.ATTR_ID);
        Assert.assertEquals("null", eventEntity1.getEntityDisplay());

        // event定义存在，event属性display的值存在
        EventEntity eventEntity2 = createEventEntity(
                "BELONG_TO",
                eventId,
                "User",
                leftId,
                "Department",
                rightId
        );
        Assert.assertEquals(eventId, eventEntity2.getEntityDisplay());
    }

    /**
     * 测试EventEntity的makeBaseProperties、isEffective、equal、generateUUID方法
     */
    @Test
    public void eventEntityTest() {
        String eventId = IdMaker.next();
        String leftId = IdMaker.next();
        String rightId = IdMaker.next();
        try {
            // 准备测试数据
            // normal
            EventEntity eventEntity = createEventEntity(
                    "BELONG_TO",
                    eventId,
                    "User",
                    leftId,
                    "Department",
                    rightId
            );
            // 无ID
            EventEntity eventEntityNoId = eventEntity.clone();
            eventEntityNoId.remove(EventEntity.ATTR_ID);
            // 无SD
            EventEntity eventEntityNoSd = eventEntity.clone();
            eventEntityNoSd.remove(EventEntity.ATTR_SD);
            // 无left
            EventEntity eventEntityNoLeft = eventEntity.clone();
            eventEntityNoLeft.remove(EventEntity.ATTR_LEFT);
            eventEntityNoLeft.remove(EventEntity.ATTR_LEFT_SD);
            // 无right
            EventEntity eventEntityNoRight = eventEntity.clone();
            eventEntityNoRight.remove(EventEntity.ATTR_RIGHT);
            eventEntityNoRight.remove(EventEntity.ATTR_RIGHT_SD);

            // normal
            final String eventStringNormal = "{\n" +
                    "\t\"$operate\": \"create\",\n" +
                    "\t\"__rightSd\": \"Department\",\n" +
                    "\t\"__left\": \"" + leftId + "\",\n" +
                    "\t\"__leftSd\": \"User\",\n" +
                    "\t\"__id\": \"" + eventId + "\",\n" +
                    "\t\"__right\": \"" + rightId + "\",\n" +
                    "\t\"__sd\": \"BELONG_TO\"\n" +
                    "}";
            EventEntity eventEntityNormal = new EventEntity(JSON.parseObject(eventStringNormal));

            // 更新id
            EventEntity eventEntityUpdateId = eventEntity.clone();
            eventEntityUpdateId.set__Id(IdMaker.next());

            // 更新sd
            EventEntity eventEntityUpdateSd = eventEntity.clone();
            eventEntityUpdateSd.set__Sd("TEST");

            // 更新left
            EventEntity eventEntityUpdateLeft = eventEntity.clone();
            eventEntityUpdateLeft.set__Left(IdMaker.next());
            eventEntityUpdateLeft.set__LeftSd("TestRight");

            // 更新right
            EventEntity eventEntityUpdateRight = eventEntity.clone();
            eventEntityUpdateRight.set__Left(IdMaker.next());
            eventEntityUpdateRight.set__LeftSd("TestRight");

            // 测试makeBaseProperties方法
            StringBuilder baseProperties = new StringBuilder();
            baseProperties.append(eventEntity.get__Sd())
                    .append(eventEntity.get__Left())
                    .append(eventEntity.get__LeftSd())
                    .append(eventEntity.get__Right())
                    .append(eventEntity.get__RightSd());
            Assert.assertEquals(baseProperties.toString(), eventEntity.makeBaseProperties().toString());
            Assert.assertEquals(baseProperties.toString(), eventEntityNoId.makeBaseProperties().toString());
            Assert.assertNull(eventEntityNoSd.makeBaseProperties());
            Assert.assertNull(eventEntityNoLeft.makeBaseProperties());
            Assert.assertNull(eventEntityNoRight.makeBaseProperties());

            // 测试isEffective方法
            Assert.assertTrue(eventEntity.isEffective());
            Assert.assertFalse(eventEntityNoId.isEffective());
            Assert.assertFalse(eventEntityNoSd.isEffective());
            Assert.assertFalse(eventEntityNoLeft.isEffective());
            Assert.assertFalse(eventEntityNoRight.isEffective());

            // 测试equal方法
            Assert.assertEquals(eventEntity, eventEntityNormal);
            Assert.assertNotEquals(eventEntity, eventEntityUpdateId);
            Assert.assertNotEquals(eventEntity, eventEntityUpdateSd);
            Assert.assertNotEquals(eventEntity, eventEntityUpdateLeft);
            Assert.assertNotEquals(eventEntity, eventEntityUpdateRight);

            // 测试generateUUID方法
            Assert.assertNotNull(eventEntity.generateUUID());
            Assert.assertNotNull(eventEntityUpdateSd.generateUUID());
            /*try {
                Assert.fail(eventEntityNoBranch.generateUUID());
            } catch (Exception e) {
                log.error(e.getLocalizedMessage(), e);
            }*/
        } catch (UXDFException e) {
            log.error(e.getLocalizedMessage());
        }
    }

    /**
     * 测试EventEntity的merge方法
     */
    @Test
    public void mergeTest() {
        String eventId = IdMaker.next();
        String leftId = IdMaker.next();
        String rightId = IdMaker.next();
        EventEntity eventEntityOld = createEventEntity(
                "BELONG_TO",
                eventId,
                "User",
                leftId,
                "Department",
                rightId
        );

        EventEntity eventEntityNew = new EventEntity();
        eventEntityNew.set__Sd("testSd");
        eventEntityNew.set__Id(eventId);
        eventEntityNew.setOperate(SdOperateType.update);
        Date createTime = new Date();
        Date updateTime = new Date();
        eventEntityNew.set__CreateTime(createTime);
        eventEntityNew.set__UpdateTime(updateTime);
        eventEntityNew.set__Right(rightId);
        eventEntityNew.set__RightSd("rightSd");
        eventEntityNew.set__Left(leftId);
        eventEntityNew.set__LeftSd("leftSd");
        eventEntityNew.put("test", "test");

        eventEntityNew.merge(eventEntityOld);

        // 断言是否跳过了不合并属性
        Assert.assertEquals("testSd", eventEntityNew.get__Sd());
        Assert.assertEquals(eventId, eventEntityNew.get__Id());
        Assert.assertEquals(SdOperateType.update, eventEntityNew.getOperate());
        Assert.assertEquals(createTime, eventEntityNew.get__CreateTime());
        Assert.assertEquals(updateTime, eventEntityNew.get__UpdateTime());
        Assert.assertEquals(rightId, eventEntityNew.get__Right());
        Assert.assertEquals("rightSd", eventEntityNew.get__RightSd());
        Assert.assertEquals(leftId, eventEntityNew.get__Left());
        Assert.assertEquals("leftSd", eventEntityNew.get__LeftSd());

        // 断言是否合并了需要合并的属性
        Assert.assertEquals("test", eventEntityNew.getString("test"));
    }

    /**
     * 测试EventEntity的clone方法
     */
    @Test
    public void cloneTest() {
        String eventId = IdMaker.next();
        String leftId = IdMaker.next();
        String rightId = IdMaker.next();
        EventEntity eventEntityOld = createEventEntity(
                "BELONG_TO",
                eventId,
                "User",
                leftId,
                "Department",
                rightId
        );
        EventEntity eventEntityNew = eventEntityOld.clone();
        eventEntityOld.remove(NodeEntity.ATTR_UUID);
        Assert.assertEquals(eventEntityOld, eventEntityNew);
    }
}
