package org.codehaus.aspectwerkz.annotation;

import org.codehaus.aspectwerkz.DeploymentModelEnum;

import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Target({ElementType.METHOD,ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Around {
    DeploymentModelEnum value() default DeploymentModelEnum.PER_JVM;
}
