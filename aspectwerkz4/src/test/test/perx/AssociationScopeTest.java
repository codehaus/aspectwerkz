/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test.perx;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.aspectwerkz.aspect.management.Aspects;
import org.codehaus.aspectwerkz.aspect.management.NoAspectBoundException;
import org.codehaus.aspectwerkz.joinpoint.management.JoinPointType;

import test.proceedinnewthread.ProceedTest;
import junit.framework.TestCase;


/**
 * perX test
 * 
 * FIXME: not working
 * java.lang.VerifyError: (class: test/perx/AssociationScopeTest, method: aw$original$_AW_$suite$_AW_$test_perx_AssociationScopeTest signature: ()Ljunit/framework/Test;) 
 * Unable to pop operand off an empty stack
 * 
 * @author <a href='mailto:the_mindstorm@evolva.ro'>Alexandru Popescu</a>
 */
public class AssociationScopeTest extends TestCase {
	public static String SCOPE_LOG = "";
	public static List	 JOINPOINTTYPES = new ArrayList();
	
	private static final String[] JPT_NAMES = {
		JoinPointType.METHOD_CALL.toString(),
		JoinPointType.METHOD_EXECUTION.toString(),
		JoinPointType.METHOD_EXECUTION.toString(),
		JoinPointType.CONSTRUCTOR_CALL.toString(),
		JoinPointType.METHOD_CALL.toString()
	};
	
	public void testPerThis() {
		A a = new A();
		a.m();
		
		assertEquals("perTarget perTarget perThis perThis perThis ",
		             SCOPE_LOG
		);
		
		assertEquals(JPT_NAMES.length,
		             JOINPOINTTYPES.size()
		);
		
		for(int i = 0; i < JOINPOINTTYPES.size(); i++) {
			assertEquals(JPT_NAMES[i],
			             JOINPOINTTYPES.get(i)
			);
		}
	}

	
	public void testAspectsOf() {
		A a = new A();
        try {
            Object aspectInstance = Aspects.aspectOf("tests/perThis", a);
            fail("perX aspect is not yet available");
        } catch(NoAspectBoundException nabe) {
            ;
        }

		a.m();
		
        try {
            Object aspectInstance = Aspects.aspectOf("tests/perThis", a);
        } catch(NoAspectBoundException nabe) {
            fail("perX aspect should be available now");
        }
	}
	
    // -- JUnit
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static junit.framework.Test suite() {
        return new junit.framework.TestSuite(AssociationScopeTest.class);
    }
}
