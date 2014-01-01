package info.ralab.uxdf.model;

import com.alibaba.fastjson.annotation.JSONField;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * {@link info.ralab.uxdf.SdData}查询请求
 */
@Data
public class SdDataQueryRequest {

    public SdDataQueryRequest() {
    }

    public SdDataQueryRequest(List<String> chains) {
        this.chains = chains;
    }

    /**
     * 查询链，可以转换为{@link info.ralab.uxdf.chain.UXDFChain}
     */
    private List<String> chains = Lists.newArrayList();

    /**
     * 针对查询链中的查询条件集合，其key对应查询链中的别名。
     * <br />
     * 每个别面对应一组{@link SdDataQueryParam}。
     */
    private Map<String, List<SdDataQueryParam>> params = Maps.newHashMap();

    /**
     * 分页和排序参数{@link PageOrder}
     */
    private PageOrder main;
    /**
     * 通过指定查询链中的别名，设置需要返回的SD类型。
     */
    private Set<String> returns = Sets.newHashSet();

    /**
     * 维护一个排序索引。可以通过Sd别名和属性名获取对应的排序参数{@link SdDataQueryOrder}。
     */
    @JSONField(serialize = false)
    private Map<String, Map<String, Set<SdDataQueryOrder>>> orderIndex = Maps.newConcurrentMap();

    /**
     * 当前查询请求{@link SdDataQueryRequest}是否拥有排序条件{@link SdDataQueryOrder}
     *
     * @return 是否拥有排序条件
     */
    @JSONField(serialize = false, deserialize = false)
    public boolean hasOrder() {
        return this.main != null
                && this.main.alias != null
                && this.main.orders != null
                && !this.main.orders.isEmpty();
    }

    /**
     * 当前查询请求{@link SdDataQueryRequest}是否拥有分页条件{@link SdDataQueryPage}
     *
     * @return 是否拥有分页条件
     */
    @JSONField(serialize = false, deserialize = false)
    public boolean hasPage() {
        return this.main != null
                && this.main.alias != null
                && this.main.page != null;
    }

    /**
     * 当前查询请求{@link SdDataQueryRequest}是否有查询参数{@link SdDataQueryParam}
     *
     * @return 是否有查询参数
     */
    @JSONField(serialize = false, deserialize = false)
    public boolean hasParams() {
        return this.params != null && !this.params.isEmpty();
    }

    /**
     * 分页和排序参数
     */
    @Data
    public static class PageOrder {
        private String alias;
        private List<SdDataQueryOrder> orders;
        private SdDataQueryPage page;
    }

    /**
     * 只查询主数据。不关联其它内容。
     */
    public boolean onlyMain;
}
