package test.fieldsetbug;


import org.codehaus.aspectwerkz.joinpoint.JoinPoint;

/**
 * @author Tomasz Mazan (beniamin)
 */
public class AroundAccessorAspect {
    /**
     * @Around set(* test.fieldsetbug.TargetClass.public*) AND within(test.fieldsetbug.*)
     */
    public Object aroundAccessor(JoinPoint jp) throws Throwable {
        if (jp.getCallee() != jp.getCaller()) {
            return null;
        } else {
            return jp.proceed();
        }
    }

}
