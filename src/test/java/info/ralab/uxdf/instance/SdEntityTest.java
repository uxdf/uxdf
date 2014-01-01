package info.ralab.uxdf.instance;

import info.ralab.uxdf.UXDFException;
import info.ralab.uxdf.utils.UXDFValueConvert;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

@Slf4j
public class SdEntityTest {

    /**
     * 测试SdEntity的getBaseString方法
     */
    @Test
    public void getBaseStringTest() {

        // 初始化转换器
        UXDFValueConvert<String> UXDFValueConvertString = (Object object) -> {
            if (object instanceof String) {
                return object.toString() + "~";
            } else {
                return object.toString() + "!";
            }
        };

        // 当是null对象时
        Assert.assertNull(SdEntity.getBaseString(null));
        Assert.assertNull(SdEntity.getBaseString(null, UXDFValueConvertString));

        // 当是字符串对象时
        String objectString = "test";
        Assert.assertEquals(
                objectString,
                SdEntity.getBaseString(objectString)
        );
        Assert.assertEquals(
                objectString + "~",
                SdEntity.getBaseString(objectString, UXDFValueConvertString)
        );

        // 当是其他对象类型时（例如为Integer）
        Integer objectInteger = 10;
        Assert.assertEquals(
                String.valueOf(objectInteger),
                SdEntity.getBaseString(objectInteger)
        );
        Assert.assertEquals(
                objectInteger.toString() + "!",
                SdEntity.getBaseString(objectInteger, UXDFValueConvertString)
        );
    }

    /**
     * 测试SdEntity的getBaseInteger方法
     */
    @Test
    public void getBaseIntegerTest() {

        // 初始化转换器
        UXDFValueConvert<Long> UXDFValueConvertInteger = (Object object) -> {
            if (object instanceof Long) {
                return (Long) object + 1L;
            } else if (object instanceof Integer) {
                return Long.valueOf((Integer) object) + 10L;
            } else if (object instanceof BigDecimal) {
                return ((BigDecimal) object).longValue() + 100L;
            } else if (object instanceof String) {
                return Double.valueOf(object.toString()).longValue() + 1000L;
            } else {
                return null;
            }
        };

        // 当是null对象时
        Assert.assertNull(SdEntity.getBaseInteger(null));
        Assert.assertNull(SdEntity.getBaseInteger(null, UXDFValueConvertInteger));

        // 当是Long类型时
        Long objectLong = 10L;
        Assert.assertEquals(
                objectLong,
                SdEntity.getBaseInteger(objectLong)
        );
        Assert.assertEquals(
                objectLong.longValue() + 1,
                SdEntity.getBaseInteger(objectLong, UXDFValueConvertInteger).longValue()
        );

        // 当是Integer类型时
        Integer objectInteger = 100;
        Assert.assertEquals(
                objectInteger.longValue(),
                SdEntity.getBaseInteger(objectInteger).longValue()
        );
        Assert.assertEquals(
                objectInteger.longValue() + 10,
                SdEntity.getBaseInteger(objectInteger, UXDFValueConvertInteger).longValue()
        );

        // 当是BigDecimal类型时
        BigDecimal objectBigDecimal = new BigDecimal(1000);
        Assert.assertEquals(
                objectBigDecimal.longValue(),
                SdEntity.getBaseInteger(objectBigDecimal).longValue()
        );
        Assert.assertEquals(
                objectBigDecimal.longValue() + 100,
                SdEntity.getBaseInteger(objectBigDecimal, UXDFValueConvertInteger).longValue()
        );

        // 当是String类型时
        String objectString = "10000";
        Assert.assertEquals(
                Long.parseLong(objectString),
                SdEntity.getBaseInteger(objectString).longValue()
        );
        Assert.assertEquals(
                Long.parseLong(objectString) + 1000L,
                SdEntity.getBaseInteger(objectString, UXDFValueConvertInteger).longValue()
        );

        // 当是Date类型时
        Date date = new Date();
        Assert.assertNull(SdEntity.getBaseInteger(date));
        Assert.assertNull(SdEntity.getBaseInteger(date, UXDFValueConvertInteger));
    }

    /**
     * 测试SdEntity的getBaseFloat方法
     */
    @Test
    public void getBaseFloatTest() {

        // 初始化转换器
        UXDFValueConvert<Double> UXDFValueConvertFloat = (Object object) -> {
            if (object instanceof Double) {
                return (Double) object + 1;
            } else if (object instanceof Float) {
                return Double.valueOf((Float) object) + 10;
            } else if (object instanceof Integer) {
                return Double.valueOf((Integer) object) + 100;
            } else if (object instanceof BigDecimal) {
                return ((BigDecimal) object).doubleValue() + 1000;
            } else if (object instanceof String) {
                return Double.valueOf(object.toString()) + 10000;
            } else {
                return null;
            }
        };

        // 当是null对象时
        Assert.assertNull(SdEntity.getBaseFloat(null));
        Assert.assertNull(SdEntity.getBaseFloat(null, UXDFValueConvertFloat));

        // 当是Double类型时
        Double objectDouble = 1.0;
        Assert.assertEquals(
                objectDouble,
                SdEntity.getBaseFloat(objectDouble)
        );
        Assert.assertEquals(
                Double.valueOf(objectDouble + 1),
                SdEntity.getBaseFloat(objectDouble, UXDFValueConvertFloat)
        );

        // 当是Float类型时
        Float objectFloat = 10.0f;
        Assert.assertEquals(
                Double.valueOf(objectFloat),
                SdEntity.getBaseFloat(objectFloat)
        );
        Assert.assertEquals(
                Double.valueOf(objectFloat + 10),
                SdEntity.getBaseFloat(objectFloat, UXDFValueConvertFloat)
        );

        // 当是Integer类型时
        Integer objectInteger = 100;
        Assert.assertEquals(
                Double.valueOf(objectInteger),
                SdEntity.getBaseFloat(objectInteger)
        );
        Assert.assertEquals(
                Double.valueOf(objectInteger + 100),
                SdEntity.getBaseFloat(objectInteger, UXDFValueConvertFloat)
        );

        // 当是BigDecimal类型时
        BigDecimal objectBigDecimal = new BigDecimal(1000);
        Assert.assertEquals(
                objectBigDecimal.longValue(),
                SdEntity.getBaseFloat(objectBigDecimal).longValue()
        );
        Assert.assertEquals(
                Double.valueOf(objectBigDecimal.add(new BigDecimal(1000)).toString()),
                SdEntity.getBaseFloat(objectBigDecimal, UXDFValueConvertFloat)
        );

        // 当是String类型时
        String objectString = "10000";
        Assert.assertEquals(
                Double.valueOf(objectString),
                SdEntity.getBaseFloat(objectString)
        );
        Assert.assertEquals(
                (Double) (Double.parseDouble(objectString) + 10000),
                SdEntity.getBaseFloat(objectString, UXDFValueConvertFloat)
        );

        // 当是Date类型时
        Date date = new Date();
        Assert.assertNull(SdEntity.getBaseFloat(date));
        Assert.assertNull(SdEntity.getBaseFloat(date, UXDFValueConvertFloat));
    }

    /**
     * 测试SdEntity的getBaseBoolean方法
     */
    @Test
    public void getBaseBooleanTest() {

        // 初始化转换器
        UXDFValueConvert<Boolean> UXDFValueConvertBoolean = (Object object) -> {
            if (object instanceof Boolean) {
                return !(Boolean) object;
            } else if (object instanceof String) {
                return !Boolean.valueOf(object.toString());
            } else if (object instanceof Number) {
                return ((Number) object).doubleValue() == 0.0;
            } else {
                return null;
            }
        };

        // 当是null类型时
        Assert.assertNull(SdEntity.getBaseBoolean(null));
        Assert.assertNull(SdEntity.getBaseBoolean(null, UXDFValueConvertBoolean));

        // 当是Boolean类型时
        Boolean objectBoolean = true;
        Assert.assertEquals(
                objectBoolean,
                SdEntity.getBaseBoolean(objectBoolean)
        );
        Assert.assertEquals(
                !objectBoolean,
                SdEntity.getBaseBoolean(objectBoolean, UXDFValueConvertBoolean)
        );

        // 当是String类型时
        String objectString = "true";
        Assert.assertEquals(
                Boolean.valueOf(objectString),
                SdEntity.getBaseBoolean(objectString)
        );
        Assert.assertEquals(
                !Boolean.valueOf(objectString),
                SdEntity.getBaseBoolean(objectString, UXDFValueConvertBoolean)
        );

        // 当是Number类型时
        Number objectNumber = 0.0;
        Assert.assertEquals(
                false,
                SdEntity.getBaseBoolean(objectNumber)
        );
        Assert.assertEquals(
                true,
                SdEntity.getBaseBoolean(objectNumber, UXDFValueConvertBoolean)
        );

        // 当是Date类型时
        Date date = new Date();
        Assert.assertNull(SdEntity.getBaseBoolean(date));
        Assert.assertNull(SdEntity.getBaseBoolean(date, UXDFValueConvertBoolean));
    }

    /**
     * 测试SdEntity的getBaseDate方法
     */
    @Test
    public void getBaseDateTest() throws ParseException {

        // 初始化转换器
        UXDFValueConvert<Date> UXDFValueConvertDate = (Object object) -> {
            Pattern DATE_PATTERN = Pattern.compile("[0-9]{4}年[0-9]{1,2}月[0-9]{1,2}日");
            if (!DATE_PATTERN.matcher(object.toString()).matches()) {
                return null;
            }
            String[] values = object.toString().split("年|月|日");
            String newValue = String.join("-",
                    String.format("%04d", Integer.parseInt(values[0])),
                    String.format("%02d", Integer.parseInt(values[1])),
                    String.format("%02d", Integer.parseInt(values[2]))
            );
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            try {
                return dateFormat.parse(newValue);
            } catch (ParseException e) {
                log.error(e.getLocalizedMessage(), e);
                throw new UXDFException(e.getLocalizedMessage());
            }
        };

        // 当是null类型时
        Assert.assertNull(SdEntity.getBaseDate(null));
        Assert.assertNull(SdEntity.getBaseDate(null, UXDFValueConvertDate));

        // 当是Date类型时
        Assert.assertEquals(new Date(), SdEntity.getBaseDate(new Date()));

        // 当是Long类型时
        Assert.assertEquals(new Date(), SdEntity.getBaseDate(new Date().getTime()));

        // 当是其他类型时（例如Double）
        Double objectDouble = 10.0;
        Assert.assertNull(SdEntity.getBaseDate(objectDouble));

        // 当是String类型时
        DateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd");
        DateFormat dateFormat2 = new SimpleDateFormat("yyyy-MM-ddX");
        DateFormat dateFormat3 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
        DateFormat dateFormat4 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        DateFormat dateFormat5 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
        DateFormat dateFormat6 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");
        DateFormat dateFormat7 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");

        String date1 = "2018-08-23";
        Assert.assertEquals(dateFormat1.parse(date1), SdEntity.getBaseDate(date1));
        String date2 = "2018-08-23+11:11";
        Assert.assertEquals(dateFormat2.parse(date2), SdEntity.getBaseDate(date2));
        String date3 = "2018-08-23T11:11";
        Assert.assertEquals(dateFormat3.parse(date3), SdEntity.getBaseDate(date3));
        String date4 = "2018-08-23T11:11:11";
        Assert.assertEquals(dateFormat4.parse(date4), SdEntity.getBaseDate(date4));
        String date5 = "2018-08-23T11:11:11.111";
        Assert.assertEquals(dateFormat5.parse(date5), SdEntity.getBaseDate(date5));
        String date6 = "2018-08-23T11:11:11+11:11";
        Assert.assertEquals(dateFormat6.parse(date6), SdEntity.getBaseDate(date6));
        String date7 = "2018-08-23T11:11:11.111+11:11";
        Assert.assertEquals(dateFormat7.parse(date7), SdEntity.getBaseDate(date7));
        String date8 = "2018-08-23T11:11:11.111Z";
        Assert.assertEquals(dateFormat7.parse(date8), SdEntity.getBaseDate(date8));

        String date9 = "2018年08月23日";
        Assert.assertEquals(dateFormat1.parse(date1), SdEntity.getBaseDate(date9, UXDFValueConvertDate));

        // 当是String类型时，但不满足已有的时间格式时
        /*try {
            String date10 = "20188年111年22日";
            Assert.assertNull(SdEntity.getBaseDate(date10));
        } catch (Exception e) {
            log.error(e.getLocalizedMessage(), e);
        }*/
    }
}
