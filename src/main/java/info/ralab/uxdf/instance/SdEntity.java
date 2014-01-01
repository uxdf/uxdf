package info.ralab.uxdf.instance;

import com.alibaba.fastjson.JSONObject;
import info.ralab.uxdf.UXDFException;
import info.ralab.uxdf.definition.SdDefinition;
import info.ralab.uxdf.definition.SdOperateType;
import info.ralab.uxdf.utils.UXDFBinaryFileInfo;
import info.ralab.uxdf.utils.UXDFBinaryFileInfos;
import info.ralab.uxdf.utils.UXDFValueConvert;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
public abstract class SdEntity extends JSONObject {

    /**
     * 唯一标识
     */
    public final static String ATTR_ID = "__id";
    /**
     *
     */
    public final static String ATTR_UUID = "__uuid";
    /**
     * 类型
     */
    public final static String ATTR_SD = "__sd";
    /**
     * 创建时间
     */
    public final static String ATTR_CREATE_TIME = "__createTime";
    /**
     * 更新时间
     */
    public final static String ATTR_UPDATE_TIME = "__updateTime";
    /**
     * 操作标识
     */
    public final static String DYNA_OPERATE = "$operate";
    /**
     * 强制删除行为
     */
    public final static String DYNA_OPERATE_DELETE_ENFORCE = "$operate_delete_enforce";
    /**
     * 创建使用原始ID
     */
    public final static String DYNA_OPERATE_CREATE_ORIGINAL_ID = "$operate_create_original_id";
    /**
     * 权限行为
     */
    public final static String DYNA_AUTHORITY = "$authority";
    /**
     * 同步锁
     */
    public final static String DYNA_SYNC_LOCK = "$syncLock";

    /**
     * 日期格式化模板
     */
    public final static String PATTERN_DATE = "yyyy-MM-dd";
    public final static String PATTERN_DATE_ISO = "yyyy-MM-ddX";
    public final static String PATTERN_DATE_MINUTE = "yyyy-MM-dd'T'HH:mm";
    public final static String PATTERN_DATE_SECOND = "yyyy-MM-dd'T'HH:mm:ss";
    public final static String PATTERN_DATE_SECOND_ISO = "yyyy-MM-dd'T'HH:mm:ssX";
    public final static String PATTERN_DATE_MILLISECOND = "yyyy-MM-dd'T'HH:mm:ss.SSS";
    public final static String PATTERN_DATE_MILLISECOND_ISO = "yyyy-MM-dd'T'HH:mm:ss.SSSX";

    /**
     * 日期格式化
     */
    public final static DateFormat DATE_FORMAT_DATE = new SimpleDateFormat(PATTERN_DATE);
    public final static DateFormat DATE_FORMAT_DATE_ISO = new SimpleDateFormat(PATTERN_DATE_ISO);
    public final static DateFormat DATE_FORMAT_MINUTE = new SimpleDateFormat(PATTERN_DATE_MINUTE);
    public final static DateFormat DATE_FORMAT_SECOND = new SimpleDateFormat(PATTERN_DATE_SECOND);
    public final static DateFormat DATE_FORMAT_SECOND_ISO = new SimpleDateFormat(PATTERN_DATE_SECOND_ISO);
    public final static DateFormat DATE_FORMAT_MILLISECOND = new SimpleDateFormat(PATTERN_DATE_MILLISECOND);
    public final static DateFormat DATE_FORMAT_ISO = new SimpleDateFormat(PATTERN_DATE_MILLISECOND_ISO);

    public SdEntity() {
        super();
    }

    public SdEntity(final String sd, final String id) {
        this();
        this.set__Sd(sd);
        this.set__Id(id);
    }

    /**
     * 转换获取基本类型String
     *
     * @param value 原始值
     * @return 转换后的值
     */
    public static String getBaseString(final Object value, final UXDFValueConvert<String>... converts) {
        if (value == null) {
            return null;
        }
        String result = null;
        if (converts != null) {
            for (UXDFValueConvert<String> convert : converts) {
                if ((result = convert.convert(value)) != null) {
                    break;
                }
            }
        }
        if (result == null) {
            if (value instanceof String) {
                result = (String) value;
            } else {
                result = value.toString();
            }
        }
        return result;
    }

    /**
     * 转换获取基本类型Integer
     *
     * @param value 原始值
     * @return 转换后的值
     */
    public static Long getBaseInteger(final Object value, final UXDFValueConvert<Long>... converts) {
        if (value == null) {
            return null;
        }
        Long result = null;
        if (converts != null) {
            for (UXDFValueConvert<Long> convert : converts) {
                if ((result = convert.convert(value)) != null) {
                    break;
                }
            }
        }
        if (result == null) {
            if (value instanceof Long) {
                result = (Long) value;
            } else if (value instanceof Integer) {
                result = Long.valueOf((Integer) value);
            } else if (value instanceof BigDecimal) {
                result = ((BigDecimal) value).longValue();
            } else if (value instanceof String) {
                result = Double.valueOf(value.toString()).longValue();
            }
        }
        return result;
    }

    /**
     * 转换获取基本类型Float
     *
     * @param value 原始值
     * @return 转换后的值
     */
    public static Double getBaseFloat(final Object value, final UXDFValueConvert<Double>... converts) {
        if (value == null) {
            return null;
        }
        Double result = null;
        if (converts != null) {
            for (UXDFValueConvert<Double> convert : converts) {
                if ((result = convert.convert(value)) != null) {
                    break;
                }
            }
        }
        if (result == null) {
            if (value instanceof Double) {
                result = (Double) value;
            } else if (value instanceof Float) {
                result = Double.valueOf((Float) value);
            } else if (value instanceof Integer) {
                result = Double.valueOf((Integer) value);
            } else if (value instanceof BigDecimal) {
                result = ((BigDecimal) value).doubleValue();
            } else if (value instanceof String) {
                result = Double.valueOf(value.toString());
            }
        }
        return result;
    }

    /**
     * 转换获取基本类型Boolean
     *
     * @param value 原始值
     * @return 转换后的值
     */
    public static Boolean getBaseBoolean(final Object value, final UXDFValueConvert<Boolean>... converts) {
        if (value == null) {
            return null;
        }
        Boolean result = null;
        if (converts != null) {
            for (UXDFValueConvert<Boolean> convert : converts) {
                if ((result = convert.convert(value)) != null) {
                    break;
                }
            }
        }
        if (result == null) {
            if (value instanceof Boolean) {
                result = (Boolean) value;
            } else if (value instanceof String) {
                result = Boolean.valueOf(value.toString());
            } else if (value instanceof Number) {
                result = ((Number) value).doubleValue() != 0.0;
            }
        }
        return result;
    }

    /**
     * 转换获取基本类型Date
     *
     * @param value 原始值
     * @return 转换后的值
     */
    public static Date getBaseDate(final Object value, final UXDFValueConvert<Date>... converts) {
        if (value == null) {
            return null;
        }
        Date result = null;
        if (converts != null) {
            for (UXDFValueConvert<Date> convert : converts) {
                if ((result = convert.convert(value)) != null) {
                    break;
                }
            }
        }
        if (result == null) {
            if (value instanceof String) {

                try {
                    // 根据格式进行不同转换
                    String valueString = value.toString();
                    if (valueString.trim().isEmpty()) {
                        result = null;
                    } else if (valueString.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}")) {
                        result = SdEntity.DATE_FORMAT_DATE.parse(valueString);
                    } else if (valueString.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}\\+[0-9]{2}:?[0-9]{2}")) {
                        result = SdEntity.DATE_FORMAT_DATE_ISO.parse(valueString);
                    } else if (valueString.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}")) {
                        result = SdEntity.DATE_FORMAT_MINUTE.parse(valueString);
                    } else if (valueString.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}")) {
                        result = SdEntity.DATE_FORMAT_SECOND.parse(valueString);
                    } else if (valueString.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}Z")) {
                        result = SdEntity.DATE_FORMAT_SECOND_ISO.parse(valueString);
                    } else if (valueString.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}.[0-9]{3}")) {
                        result = SdEntity.DATE_FORMAT_MILLISECOND.parse(valueString);
                    } else if (valueString.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}\\+[0-9]{2}:?[0-9]{2}")) {
                        result = SdEntity.DATE_FORMAT_SECOND_ISO.parse(valueString);
                    } else if (valueString.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}.[0-9]{3}\\+[0-9]{2}:?[0-9]{2}")) {
                        result = SdEntity.DATE_FORMAT_ISO.parse(valueString);
                    } else if (valueString.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}.[0-9]{3}Z")) {
                        result = SdEntity.DATE_FORMAT_ISO.parse(valueString);
                    } else {
                        throw new ParseException("date format error.", 0);
                    }
                } catch (ParseException e) {
                    log.error(e.getLocalizedMessage(), e);
                    throw new UXDFException(e);
                }
            } else if (value instanceof Long) {
                result = new Date((Long) value);
            } else if (value instanceof Date) {
                result = (Date) value;
            }
        }
        return result;
    }

    /**
     * 获取基本二进制数据
     *
     * @param value 原始值
     * @return 转换后的值
     */
    public static UXDFBinaryFileInfo getBaseBinary(
            final Object value,
            final SdEntity sdEntity,
            final UXDFValueConvert<UXDFBinaryFileInfo>... converts
    ) {
        if (value == null) {
            return null;
        }

        UXDFBinaryFileInfo fileInfo = null;

        // 使用外部转换器
        if (converts != null) {
            for (UXDFValueConvert<UXDFBinaryFileInfo> convert : converts) {
                if ((fileInfo = convert.convert(value)) != null) {
                    break;
                }
            }
        }
        if (fileInfo == null) {
            // 根据数据类型获取二进制输入流
            if (value instanceof InputStream) {
                fileInfo = UXDFBinaryFileInfos.make((InputStream) value);
            } else if (value instanceof byte[]) {
                fileInfo = UXDFBinaryFileInfos.make((byte[]) value);
            }
        }
        return fileInfo;
    }

    public String get__Id() {
        return this.getString(ATTR_ID);
    }

    public void set__Id(final String id) {
        this.put(ATTR_ID, id);
    }

    public String get__Uuid() {
        return this.getString(ATTR_UUID);
    }

    public void set__Uuid(String uuid) {
        this.put(ATTR_UUID, uuid);
    }

    public String get__Sd() {
        return this.getString(ATTR_SD);
    }

    public void set__Sd(final String sd) {
        this.put(ATTR_SD, sd);
    }

    public Date get__CreateTime() {
        return this.getDate(ATTR_CREATE_TIME);
    }

    public void set__CreateTime(final Date createTime) {
        this.put(ATTR_CREATE_TIME, createTime);
    }

    public Date get__UpdateTime() {
        return this.getDate(ATTR_UPDATE_TIME);
    }

    public void set__UpdateTime(final Date updateTime) {
        this.put(ATTR_UPDATE_TIME, updateTime);
    }

    public SdOperateType getOperate() {
        final String operate = this.getString(DYNA_OPERATE);
        try {
            return SdOperateType.valueOf(operate);
        } catch (Exception e) {
            return null;
        }
    }

    public void setOperate(final SdOperateType operate) {
        this.put(DYNA_OPERATE, operate.toString());
    }

    public Boolean getOperateDeleteEnforce() {
        return this.getBoolean(DYNA_OPERATE_DELETE_ENFORCE);
    }

    public void setOperateDeleteEnforce(Boolean sysOperateDeleteEnforce) {
        this.put(DYNA_OPERATE_DELETE_ENFORCE, sysOperateDeleteEnforce);
    }

    public Boolean isCreateOriginalId() {
        Boolean isCreateOriginalId = this.getBoolean(DYNA_OPERATE_CREATE_ORIGINAL_ID);
        return isCreateOriginalId == null ? false : isCreateOriginalId;
    }

    public void setCreateOriginalId(Boolean createOriginalId) {
        this.put(DYNA_OPERATE_CREATE_ORIGINAL_ID, createOriginalId);
    }

    /**
     * 是否强制删除关联Event和Node
     *
     * @return 是否强制删除
     */
    public boolean isDeleteEnforce() {
        return this.containsKey(DYNA_OPERATE_DELETE_ENFORCE) && this.getBoolean(DYNA_OPERATE_DELETE_ENFORCE);
    }

    /**
     * 移除动态属性
     */
    public void removeDynamicAttr() {
        this.remove(DYNA_OPERATE);
        this.remove(DYNA_OPERATE_DELETE_ENFORCE);
        this.remove(DYNA_AUTHORITY);
        this.remove(DYNA_SYNC_LOCK);
        this.remove(DYNA_OPERATE_CREATE_ORIGINAL_ID);
    }

    /**
     * 获取实例标题信息
     *
     * @return 标题
     */
    public abstract String getEntityDisplay();

    protected String makeEntityDisplay(final SdDefinition sdDefinition) {
        StringBuilder displayValue = new StringBuilder();
        if (sdDefinition != null && sdDefinition.getDisplay() != null) {
            String[] displays = sdDefinition.getDisplay();
            for (String display : displays) {
                displayValue.append(this.get(display));
            }
        }
        return displayValue.toString();
    }

    public abstract <T extends SdEntity> T id(final String id);

    public abstract <T extends SdEntity> T sd(final String sd);

    public abstract <T extends SdEntity> T merge(final SdEntity sdEntity);

    public StringBuilder makeBaseProperties() {
        if (this.get__Sd() != null) {
            StringBuilder builder = new StringBuilder();
            builder.append(this.get__Sd());
            return builder;
        }
        return null;
    }

    /**
     * 是否是一个有效的SdEntity，包含最基本的属性
     *
     * @return 是否有效
     */
    public boolean isEffective() {
        return this.get__Id() != null && makeBaseProperties() != null;
    }

    /**
     * 生成UUID
     *
     * @return UUID
     */
    public abstract String generateUUID();

    public abstract String getUUID();

    public abstract String getLogicId();
}
