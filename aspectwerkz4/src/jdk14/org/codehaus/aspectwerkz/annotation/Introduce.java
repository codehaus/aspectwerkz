package org.codehaus.aspectwerkz.annotation;

/**
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public interface Introduce {
    public String value();
    public String deploymentModel();
    public String isTransient();
    //FIXME
    // since @Introduced was move to Aspect class we have to have this crap:
    public String[] introducedInterfaces();// not exposed to user
    public String innerClassName();//not exposed to user
}
