package awbench.jasco;

import awbench.Run;
import awbench.method.Execution;

class MethodExecutionAspect {

	hook BeforeHook {
		BeforeHook(method(..args)) {
			execute(method);
		}
		before() {
			Run.ADVICE_HIT++;
		}
	}

	hook BeforeHookSJP {
		BeforeHookSJP(method(..args)) {
			execute(method);
		}
		before() {
			calledmethod.getFullName();
			Run.ADVICE_HIT++;
		}
	}

	hook BeforeHookJP {
		BeforeHookJP(method(..args)) {
			execute(method);
		}
		before() {
			Object target = calledmethod.getCalledObject();
			Run.ADVICE_HIT++;
		}
	}

	hook BeforeWithPrimitiveArgsHook {
		BeforeWithPrimitiveArgsHook(method(int i)) {
			execute(method);
		}
		before() {
			int j = i;
			Run.ADVICE_HIT++;
		}
	}

	hook BeforeWithWrappedArgsHook {
		BeforeWithWrappedArgsHook(method(Integer i)) {
			execute(method);
		}
		before() {
			Integer j = i;
			Run.ADVICE_HIT++;
		}
	}

	hook BeforeWithArgsAndTargetHook {
		BeforeWithArgsAndTargetHook(method(int i)) {
			execute(method) && target(awbench.method.Execution);
		}
		before() {
			int j = i;
			Execution u = calledobject;
			Run.ADVICE_HIT++;
		}
	}

	hook AfterHook {
		AfterHook(method(..args)) {
			execute(method);
		}
		after() {
			Run.ADVICE_HIT++;
		}
	}

	hook AfterReturningHook {
		AfterReturningHook(method(..args)) {
			execute(method);
		}
		after returning(String returnvalue) {
			String value = returnvalue;
			Run.ADVICE_HIT++;
		}
	}

	hook AfterThrowingHook {
		AfterThrowingHook(method(..args)) {
			execute(method);
		}
		after throwing(RuntimeException ex) {
			RuntimeException exception = ex;
			Run.ADVICE_HIT++;
		}
	}

	hook AroundHook {
		AroundHook(method(..args)) {
			execute(method);
		}
		replace() {
			Run.ADVICE_HIT++;
			return proceed();
		}
	}

	hook AroundHookSJP {
		AroundHookSJP(method(..args)) {
			execute(method);
		}
		replace() {
			Run.ADVICE_HIT++;
			Object o = calledmethod.getFullName();
			return proceed();
		}
	}

	hook AroundHookJP {
		AroundHookJP(method(..args)) {
			execute(method);
		}
		replace() {
			Run.ADVICE_HIT++;
			Object object = calledmethod.getCalledObject();
			return proceed();
		}
	}


	hook AroundStackedWithArgAndTargetHook {
		AroundStackedWithArgAndTargetHook(method(int i)) {
			execute(method) && target(Execution);
		}
		replace() {
			int j = i;
			Execution u = calledobject;
			Run.ADVICE_HIT++;
			return proceed();
		}
	}

}
