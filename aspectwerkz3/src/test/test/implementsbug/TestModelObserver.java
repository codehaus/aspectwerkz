package test.implementsbug;


/**
 * @Aspect perJVM
 */
public class TestModelObserver {
    /**
     * @Implements within(test.implementsbug.TestView)
     */
    Observer observer;

    /**
     * @Implements within(test.implementsbug.TestModel)
     */
    Subject subject;
}
