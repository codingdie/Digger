package com.digger.storage.anomation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Created by xupeng on 17-8-10.
 */
@Target(ElementType.FIELD)
public @interface Field {
    int length() default 1;

    int index() default Integer.MAX_VALUE;
}
