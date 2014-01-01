package info.ralab.uxdf.instance;


import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 数据权限
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SdDataAuthority {

    /**
     * 当前Sd实例是否不可编辑
     */
    private boolean editDisabled = false;

    /**
     * 当前Sd实例是否不可删除
     */
    private boolean deleteDisabled = false;

    /**
     * 当前SD不可编辑的属性
     */
    private List<String> propertiesDisabled = Lists.newArrayList();
}
