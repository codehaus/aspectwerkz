package test;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @version $Id: TestException.java,v 1.2 2003-06-09 07:04:13 jboner Exp $
 */
public class TestException extends Exception {
    public TestException(String message) {
        super(message);
    }
}
