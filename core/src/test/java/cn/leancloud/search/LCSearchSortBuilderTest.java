package cn.leancloud.search;

import cn.leancloud.types.LCGeoPoint;
import junit.framework.TestCase;

import java.util.List;

public class LCSearchSortBuilderTest extends TestCase {
    public LCSearchSortBuilderTest(String name) {
        super(name);
    }

    public void testGetterSetter() {
        LCSearchSortBuilder builder = LCSearchSortBuilder.newBuilder();
        builder.orderByAscending("updatedAt");
        builder.whereNear("location", new LCGeoPoint());
        List<Object> sortFields = builder.getSortFields();
        assertTrue(sortFields.size() == 2);
    }
}
