package examples.introduction;

import org.codehaus.aspectwerkz.AspectContext;

import java.io.Serializable;

/**
 * @Aspect perClass
 */
public class IntroductionAspect extends AbstractIntroductionAspect {

    /**
     * @Introduce within(examples.introduction.Target)
     */
    public Serializable serializable;

    /**
     * @Mixin(pointcut="within(@Annotation *..*)", deploymentModel="perInstance")
     */
    public static class MyConcreteImpl extends MyImpl {

        /**
         * The cross-cutting info.
         */
        private final AspectContext m_info;

        /**
         * We are interested in cross-cutting info, therefore we have added a constructor that takes
         * a cross-cutting infor instance as its only parameter.
         *
         * @param info the cross-cutting info
         */
        public MyConcreteImpl(final AspectContext info) {
            m_info = info;
            System.out.println("--Accessing mixin target instance from the mixin <init>...");
            sayHello2();
            System.out.println("--..<init> done");
        }

        public String sayHello2() {
            return "Hello World! Hello World!";
        }
    }
}