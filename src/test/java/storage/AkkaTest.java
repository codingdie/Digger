package storage;

import junit.framework.TestCase;

/**
 * Created by xupeng on 2017/5/17.
 */
public class AkkaTest extends TestCase {

    public static class A {
        private String a;

    }

    public static class B extends A {
        private String a;
    }

    public void testA() {
        System.out.println(new B() instanceof A);
        System.out.println(new A() instanceof B);

    }
}
