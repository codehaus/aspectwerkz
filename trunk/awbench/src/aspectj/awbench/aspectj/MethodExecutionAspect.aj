/**************************************************************************************
 * Copyright (c) Jonas Bon?r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package awbench.aspectj;

import awbench.method.Execution;

/**
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public aspect MethodExecutionAspect {


    public static int s_count = 0;


    before() :
    execution(* awbench.method.Execution.before()) {
        s_count++;
    }

     
    before() :
    execution(* awbench.method.Execution.beforeSjp()) {
        // TODO - we don't make use of sjp / jp so a very lazy impl could hide its weakness
        // but we want it comparable to before() advice
        s_count++;
    }

    before() :
    execution(* awbench.method.Execution.beforeJp()) {
        // TODO - we don't make use of sjp / jp so a very lazy impl could hide its weakness
        // but we want it comparable to before() advice
    	// f.e. AJ
    	
    	// when removing this line, AJ is faster than AW
    	// else slower but we need to add RTTI in AW to expose the same feature set. 
    	Object target = thisJoinPoint.getTarget();
    	
        s_count++;
    }

    before(int i) :
    execution(* awbench.method.Execution.withPrimitiveArgs(int)) && args(i) {
        int j = i;
        s_count++;
    }

    before(Integer i) :
    execution(* awbench.method.Execution.withWrappedArgs(java.lang.Integer)) && args(i) {
        Integer j = i;
        s_count++;
	}

	before() :
	execution(* awbench.method.Execution.beforeAfter()) {
		s_count++;
	}
	after() :
	execution(* awbench.method.Execution.beforeAfter()) {
		s_count++;
	}
	
	// around gets inlined if thisJoinPoint is not used and thus way faster.
	Object around() :
	execution(* awbench.method.Execution.aroundJP()) {
		s_count++;
		Object o = thisJoinPoint.getTarget();//Signature();
	    return proceed();
	}

	// around gets inlined if thisJoinPoint is not used and thus way faster.
	Object around() :
	execution(* awbench.method.Execution.aroundSJP()) {
		s_count++;
		Object o = thisJoinPointStaticPart.getSignature();
	    return proceed();
	}

    //TODO: add Rtti around

	before(int i, Execution t) :
	execution(* awbench.method.Execution.withArgsAndTarget(int)) && args(i) && target(t) {
        int j = i;
        Execution u = t;
        s_count++;
    }

	
}
