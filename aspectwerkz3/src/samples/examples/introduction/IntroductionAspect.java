package examples.introduction;

import org.codehaus.aspectwerkz.CrossCuttingInfo;


/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 * @Aspect perClass
 */
public class IntroductionAspect extends AbstractIntroductionAspect {

    /**
     * @Introduce within(examples.introduction.Target) deploymentModel=perInstance
     */
    public static class MyConcreteImpl extends MyImpl {

        /**
         * The cross-cutting info.
         */
        private final CrossCuttingInfo m_info;

        /**
         * We are interested in cross-cutting info, therefore we have added a constructor that takes a cross-cutting
         * infor instance as its only parameter.
         *
         * @param info the cross-cutting info
         */
        public MyConcreteImpl(final CrossCuttingInfo info) {
            m_info = info;
        }

        public String sayHello2() {
            System.out.println("mixin target class: " + m_info.getMixinTargetClass(this));
            System.out.println("mixin target instance: " + m_info.getMixinTargetInstance(this));
            return "Hello World! Hello World!";
        }
    }
}
