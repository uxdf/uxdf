package info.ralab.uxdf.model;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;

@Slf4j
public class SdDataQueryRequestTest {

    @Test
    public void testJSONToRequest() {
        final String json = "{\n" +
                "\t\tchains: ['User-BELONG_TO>UserGroup'],\n" +
                "\t\tparams: {\n" +
                "\t\t\tUser: [{\n" +
                "\t\t\t\tproperty: 'name',\n" +
                "\t\t\t\tlogic: 'EQ',\n" +
                "\t\t\t\tvalue: 'foo'\n" +
                "\t\t\t}]\n" +
                "\t\t},\n" +
                "\t\tmain: {\n" +
                "\t\t\talias: 'UserGroup',\n" +
                "\t\t\torders: [{\n" +
                "\t\t\t\tproperty: 'name',\n" +
                "\t\t\t\ttype: 'ASC'\n" +
                "\t\t\t}],\n" +
                "\t\t\tpage: {\n" +
                "\t\t\t\tstart: 0,\n" +
                "\t\t\t\tlimit: 10\n" +
                "\t\t\t}\n" +
                "\t\t},\n" +
                "\t\trepository: 'metatdata',\n" +
                "\t\tbranch: 'current',\n" +
                "\t\treturns: ['UserGroup']\n" +
                "\t}";

        SdDataQueryRequest request = JSON.parseObject(json, SdDataQueryRequest.class);

        Assert.assertNotNull(request);
        Assert.assertTrue(request.hasParams());
        Assert.assertTrue(request.hasOrder());
        Assert.assertTrue(request.hasPage());

        Assert.assertNotNull(request.getChains());
        Assert.assertEquals(1, request.getChains().size());

        Assert.assertNotNull(request.getParams());
        Assert.assertEquals(1, request.getParams().size());

        Assert.assertNotNull(request.getMain());
        Assert.assertNotNull(request.getMain().getAlias());

        Assert.assertNotNull(request.getMain().getOrders());
        Assert.assertEquals(1, request.getMain().getOrders().size());

        Assert.assertNotNull(request.getMain().getPage());
        Assert.assertEquals(0, request.getMain().getPage().getStart());
        Assert.assertEquals(10, request.getMain().getPage().getLimit());
    }
}
