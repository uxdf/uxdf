package info.ralab.uxdf.definition;

import info.ralab.uxdf.instance.SdEntity;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.regex.Pattern;

/**
 * Sd属性校验规则。
 * <p>
 * 根据当前规则设置检查目标{@link SdPropertyValidRuleTarget}和检查类型{@link SdPropertyValidRuleType}，
 * 对{@link SdProperty}的值进行检查。
 * </p>
 */
@Data
@Slf4j
public class SdPropertyValidRule {
    private SdPropertyValidRuleTarget target;
    private SdPropertyValidRuleType type;
    private Object value;
    private String message;

    private Pattern regexPattern = null;

    /**
     * 根据{@link SdBaseType}检查数据有效性
     *
     * @param baseType   Sd基础类型
     * @param checkValue 被检查的数据
     * @return 是否符合检查
     */
    public boolean check(final SdBaseType baseType, final Object checkValue) {
        switch (type) {
            case regex: // 正则
                if (checkValue == null) {
                    return false;
                }
                if (regexPattern == null) {
                    regexPattern = Pattern.compile(this.value.toString());
                }
                return regexPattern.matcher(checkValue.toString()).matches();
            case eq: // 等于
                return checkEqual(baseType, checkValue);
            case ne: // 不等于
                return !checkEqual(baseType, checkValue);
            case ge: // 大于等于
                return !checkLess(baseType, checkValue);
            case le: // 小于等于
                return !checkGreater(baseType, checkValue);
            case gt: // 大于
                return checkGreater(baseType, checkValue);
            case lt: // 小于
                return checkLess(baseType, checkValue);
        }
        return false;
    }

    /**
     * 大于逻辑判断检查
     *
     * @param baseType   Sd基础类型
     * @param checkValue 被检查的数据
     * @return 是否符合检查
     */
    private boolean checkGreater(final SdBaseType baseType, final Object checkValue) {
        Integer compareCode = this.compare(baseType, checkValue);

        return compareCode != null && compareCode > 0;
    }

    /**
     * 小于逻辑判断检查
     *
     * @param baseType   Sd基础类型
     * @param checkValue 被检查的数据
     * @return 是否符合检查
     */
    private boolean checkLess(final SdBaseType baseType, final Object checkValue) {
        Integer compareCode = this.compare(baseType, checkValue);

        return compareCode != null && compareCode < 0;
    }

    /**
     * 等于逻辑判断
     *
     * @param baseType   Sd基础类型
     * @param checkValue 被检查的数据
     * @return 是否符合检查
     */
    private boolean checkEqual(final SdBaseType baseType, final Object checkValue) {
        if (checkValue == null) {
            return false;
        }
        switch (baseType) {
            case Datetime:
                // 日期必须是ISO日期格式，或一个时间戳
                Date checkDate = SdEntity.getBaseDate(checkValue);
                Date valueDate = SdEntity.getBaseDate(this.value);

                if (checkDate == null || valueDate == null) {
                    return false;
                }

                return checkDate.equals(valueDate);
            case Binary:
            case Boolean:
            case Float:
            case Integer:
            case String:
                return checkValue.equals(this.value);
        }
        return false;
    }

    /**
     * 比较数据值和检查值得大小。<br />
     * <ul>
     * <li>数据值小于检查值：返回{@code -1}</li>
     * <li>数据值等于检查值：返回{@code 0}</li>
     * <li>数据值大于检查值：返回{@code 1}</li>
     * <li>数据值或检查值为Null：返回Null</li>
     * </ul>
     *
     * @param baseType   Sd基础类型
     * @param checkValue 被检查的数据
     * @return 是否符合检查
     */
    private Integer compare(final SdBaseType baseType, final Object checkValue) {
        if (checkValue == null || this.value == null) {
            return null;
        }
        switch (baseType) {
            case String:
                switch (this.target) {
                    case value:
                        return checkValue.toString().compareTo(this.value.toString());
                    case length:
                    case size:
                        return checkValue.toString().length() - this.value.toString().length();
                }
            case Integer:
                Long checkLong = SdEntity.getBaseInteger(checkValue);
                Long valueLong = SdEntity.getBaseInteger(this.value);

                if (checkLong == null || valueLong == null) {
                    return null;
                }

                return checkLong.compareTo(valueLong);
            case Float:
                Double checkDouble = SdEntity.getBaseFloat(checkValue);
                Double valueDouble = SdEntity.getBaseFloat(this.value);

                if (checkDouble == null || valueDouble == null) {
                    return null;
                }

                return checkDouble.compareTo(valueDouble);
            case Boolean:
                return null;
            case Datetime:
                // 日期必须是ISO日期格式，或一个时间戳
                Date checkDate = SdEntity.getBaseDate(checkValue);
                Date valueDate = SdEntity.getBaseDate(this.value);

                if (checkDate == null || valueDate == null) {
                    return null;
                }

                return checkDate.compareTo(valueDate);
            case Binary:
                return checkValue.toString().length() - this.value.toString().length();
            default:
                return null;
        }
    }
}
