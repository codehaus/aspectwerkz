/**************************************************************************************
 * Copyright (c) Jonas Bon?r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package awbench.extension.aspectj;

import awbench.method.Execution;
import awbench.Run;

/**
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public aspect MethodExecutionAspect {

    before() :
    execution(* awbench.method.Execution.before()) {
        Run.ADVICE_HIT++;
    }

     
    before() :
    execution(* awbench.method.Execution.beforeSJP()) {
        // TODO - we don't make use of sjp / jp so a very lazy impl could hide its weakness
        // but we want it comparable to before() advice
        Run.ADVICE_HIT++;
    }

/*
FIXME thisJoinPoint argument
    before() :
    execution(* awbench.method.Execution.beforeJP()) {
        // TODO - we don't make use of sjp / jp so a very lazy impl could hide its weakness
        // but we want it comparable to before() advice
    	// f.e. AJ
    	
    	// when removing this line, AJ is faster than AW
    	// else slower but we need to add RTTI in AW to expose the same feature set. 
    	Object target = thisJoinPoint.getTarget();
    	
        Run.ADVICE_HIT++;
    }
*/

	before() :
	execution(* awbench.method.Execution.beforeAfter()) {
		Run.ADVICE_HIT++;
	}
	
	after() :
	execution(* awbench.method.Execution.beforeAfter()) {
		Run.ADVICE_HIT++;
	}

    after() returning(String s) :
    execution(* awbench.method.Execution.afterReturningString()) {
        String returnValue = s;
        Run.ADVICE_HIT++;
    }

    after() throwing(RuntimeException e) :
    execution(* awbench.method.Execution.afterThrowingRTE()) {
        RuntimeException rte = e;
        Run.ADVICE_HIT++;
    }
                                                    
/*
FIXME thisJoinPoint argument
	// around gets inlined if thisJoinPoint is not used and thus way faster.
	Object around() :
	execution(* awbench.method.Execution.aroundJP()) {
		Run.ADVICE_HIT++;
		Object o = thisJoinPoint.getTarget();//Signature();
	    return proceed();
	}
*/

	// around gets inlined if thisJoinPoint is not used and thus way faster.
	Object around() :
	execution(* awbench.method.Execution.aroundSJP()) {
		Run.ADVICE_HIT++;
	    return proceed();
	}
}
