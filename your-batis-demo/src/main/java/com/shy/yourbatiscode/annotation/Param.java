package com.shy.yourbatiscode.annotation;

import java.lang.annotation.*;

/**
* Param注解
* */
@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Param {
    String value();
}
