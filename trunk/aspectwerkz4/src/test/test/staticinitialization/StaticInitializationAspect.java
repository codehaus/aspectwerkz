/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test.staticinitialization;

import org.codehaus.aspectwerkz.definition.Pointcut;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.joinpoint.StaticJoinPoint;


/**
 * Aspect on staticinitialization pointcut.
 * 
 * @author <a href="mailto:the_mindstorm@evolva.ro">Alex Popescu</a>
 * 
 * @Aspect("perClass")
 */
public class StaticInitializationAspect {
	/**
	 * @Expression staticinitialization(test.staticinitialization.ClinitTarget) 
	 */
	Pointcut staticInitialization;
	
	/**
	 * @Before staticInitialization
	 */
	public void beforeStaticInitializer() {
		StaticInitializationTest.s_messages.add(StaticInitializationTest.BEFORE_EXPECTED_MESSAGES[0]);	                                                                                   
	}
	
	/**
	 * @Before staticInitialization
	 */
	public void beforeStaticInitialization(StaticJoinPoint sjp) {
		StaticInitializationTest.s_staticJoinPoints.add(sjp);
	}
	
	/**
	 * @Before staticInitialization
	 */
	public void beforeStaticInitialization(JoinPoint jp) {
		StaticInitializationTest.s_joinPoints.add(jp);
	}
	
	/**
	 * @Around staticInitialization
	 */
	public Object aroundStaticInitialization(StaticJoinPoint sjp) throws Throwable {
		StaticInitializationTest.s_messages.add(StaticInitializationTest.BEFORE_EXPECTED_MESSAGES[1]);
		StaticInitializationTest.s_staticJoinPoints.add(sjp);
		
		return sjp.proceed();
	}
	
	/**
	 * @Around staticInitialization
	 */
	public Object aroundStaticInitialization(JoinPoint jp) throws Throwable {
		StaticInitializationTest.s_messages.add(StaticInitializationTest.BEFORE_EXPECTED_MESSAGES[2]);
		StaticInitializationTest.s_joinPoints.add(jp);
		
		return jp.proceed();
	}
	
	/**
	 * @AfterReturning staticInitialization
	 */
	public void afterReturningStaticInitializer() {
		StaticInitializationTest.s_messages.add(StaticInitializationTest.AFTER_EXPECTED_MESSAGES[0]);
	}

	/**
	 * @AfterReturning staticInitialization
	 */
	public void afterReturningStaticInitializer(StaticJoinPoint sjp) {
		StaticInitializationTest.s_staticJoinPoints.add(sjp);
	}
	
	/**
	 * @AfterReturning staticInitialization
	 */
	public void afterReturningStaticInitializer(JoinPoint jp) {
		StaticInitializationTest.s_joinPoints.add(jp);
	}
	
	/**
	 * @After staticInitialization
	 */
	public void afterStaticInitializer() {
		StaticInitializationTest.s_messages.add(StaticInitializationTest.AFTER_EXPECTED_MESSAGES[1]);
	}
	
	/**
	 * @After staticInitialization
	 */
	public void afterStaticInitializer(StaticJoinPoint sjp) {
		StaticInitializationTest.s_staticJoinPoints.add(sjp);
	}
	
	/**
	 * @After staticInitialization
	 */
	public void afterStaticInitializer(JoinPoint jp) {
		StaticInitializationTest.s_joinPoints.add(jp);
	}
}
