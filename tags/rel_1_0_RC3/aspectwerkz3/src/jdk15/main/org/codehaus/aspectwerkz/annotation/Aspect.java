package org.codehaus.aspectwerkz.annotation;

import org.codehaus.aspectwerkz.DeploymentModelEnum;

import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Aspect {
    String value() default "perJVM";
}
