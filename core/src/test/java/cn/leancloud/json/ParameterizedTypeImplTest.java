package cn.leancloud.json;

import junit.framework.TestCase;

import java.lang.reflect.Type;

public class ParameterizedTypeImplTest extends TestCase {
    public ParameterizedTypeImplTest(String name) {
        super(name);
    }

    public void testGetters() throws Exception {
        Type[] arguments1 = new Type[]{};
        Type owner1 = String.class.getGenericSuperclass();
        Type raw1 = String.class.getGenericSuperclass();
        ParameterizedTypeImpl impl = new ParameterizedTypeImpl(arguments1, owner1, raw1);
        assertEquals(arguments1, impl.getActualTypeArguments());
        assertEquals(owner1, impl.getOwnerType());
        assertEquals(raw1, impl.getRawType());
        assertEquals(false,impl.equals(""));
        assertTrue(impl.hashCode() > 0);
    }
}
