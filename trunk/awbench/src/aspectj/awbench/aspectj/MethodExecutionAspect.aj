/**************************************************************************************
 * Copyright (c) Jonas Bon?r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package awbench.aspectj;

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
        Object sig = thisJoinPointStaticPart.getSignature();
        Run.ADVICE_HIT++;
    }

    before() :
    execution(* awbench.method.Execution.beforeJP()) {
    	Object target = thisJoinPoint.getTarget();
        Run.ADVICE_HIT++;
    }

    before(int i) :
    execution(* awbench.method.Execution.withPrimitiveArgs(int)) && args(i) {
        int j = i;
        Run.ADVICE_HIT++;
    }

    before(Integer i) :
    execution(* awbench.method.Execution.withWrappedArgs(java.lang.Integer)) && args(i) {
        Integer j = i;
        Run.ADVICE_HIT++;
	}

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
	
	// around gets inlined if thisJoinPoint is not used and thus way faster.
	Object around() :
	execution(* awbench.method.Execution.around_()) {
		Run.ADVICE_HIT++;
	    return proceed();
	}

	Object around() :
	execution(* awbench.method.Execution.aroundSJP()) {
		Run.ADVICE_HIT++;
		Object o = thisJoinPointStaticPart.getSignature();
	    return proceed();
	}

	Object around() :
	execution(* awbench.method.Execution.aroundJP()) {
		Run.ADVICE_HIT++;
		Object o = thisJoinPoint.getTarget();
	    return proceed();
	}

    //TODO: add Rtti around

	before(int i, Execution t) :
	execution(* awbench.method.Execution.withArgsAndTarget(int)) && args(i) && target(t) {
        int j = i;
        Execution u = t;
        Run.ADVICE_HIT++;
    }

	Object around(int i, Execution t) :
	execution(* awbench.method.Execution.aroundStackedWithArgAndTarget(int)) && args(i) && target(t) {
        int j = i;
        Execution u = t;
        Run.ADVICE_HIT++;
        return proceed(j, u);
    }

	Object around(int i, Execution t) :
	execution(* awbench.method.Execution.aroundStackedWithArgAndTarget(int)) && args(i) && target(t) {
        int j = i;
        Execution u = t;
        Run.ADVICE_HIT++;
        return proceed(j, u);
    }

}
