package org.codehaus.aspectwerkz.annotation;

import java.util.Set;

/**
 * An interface for Annotation which needs to remember the call signature of the member they apply to. This is used by
 * 
 * @Expression and
 * @Before/Around/After to support args() binding.
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur </a>
 */
public interface ParameterizedAnnotationProxy {

    public void addArgument(String argName, String className);

    public Set getArgumentNames();

    public String getArgumentType(String parameterName);

}