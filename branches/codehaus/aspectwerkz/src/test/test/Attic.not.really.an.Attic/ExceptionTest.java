package test;

import junit.framework.TestCase;

import org.codehaus.aspectwerkz.AspectWerkz;
import org.codehaus.aspectwerkz.exception.DefinitionException;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;

public class ExceptionTest extends TestCase {

    public void testWrappedRuntimeException() {
        DefinitionException exception = new DefinitionException("definition not found");
        try {
            try {
                throw exception;
            }
            catch (DefinitionException e) {
                throw new WrappedRuntimeException(e);
            }
        }
        catch (WrappedRuntimeException e) {
            assertEquals(exception.getMessage(), e.getMessage());
            assertEquals(exception.getLocalizedMessage(), e.getLocalizedMessage());
            assertEquals(exception.toString(), e.toString());
            assertTrue(e.getCause() instanceof DefinitionException);
        }
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static junit.framework.Test suite() {
        return new junit.framework.TestSuite(ExceptionTest.class);
    }

    public ExceptionTest(String name) {
        super(name);
        AspectWerkz.initialize();
    }
}
