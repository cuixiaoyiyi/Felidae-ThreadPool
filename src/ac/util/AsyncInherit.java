package ac.util;

import ac.constant.AsyncTaskSig;
import ac.constant.ExecutorSig;
import ac.constant.ThreadSig;
import ac.util.InheritanceProcess.MatchType;
import soot.RefType;
import soot.SootClass;
import soot.Type;

public class AsyncInherit {
	
	public static boolean isInheritedFromExecutor(SootClass theClass) {
		return InheritanceProcess.isInheritedFromGivenClass(theClass, ExecutorSig.CLASS_EXECUTOR);
	}

	public static boolean isInheritedFromExecutor(Type type) {
		if (type instanceof RefType) {
			return isInheritedFromExecutor(((RefType) type).getSootClass());
		}
		return false;
	}
	
	public static boolean isInheritedCallerRunsPolicy(Type type) {
		if (type instanceof RefType) {
			return InheritanceProcess.isInheritedFromGivenClass(((RefType) type).getSootClass(), ExecutorSig.CLASS_CallerRunsPolicy);
		}
		return false;
	}
	
	public static boolean isInheritedFromThread(SootClass theClass) {
		return InheritanceProcess.isInheritedFromGivenClass(theClass, ThreadSig.CLASS_THREAD);
	}


	public static boolean isInheritedFromExecutorService(SootClass theClass) {
		return InheritanceProcess.isInheritedFromGivenClass(theClass, ExecutorSig.CLASS_EXECUTOR_SERVICE);
	}

	public static boolean isInheritedFromExecutorService(Type type) {
		if (type instanceof RefType) {
			return isInheritedFromExecutorService(((RefType) type).getSootClass());
		}
		return false;
	}

	public static boolean isInheritedFromRunnable(SootClass theClass) {
		return InheritanceProcess.isInheritedFromGivenClass(theClass, ThreadSig.CLASS_RUNNABLE);
	}

	public static boolean isInheritedFromRunnable(Type type) {
		if (type instanceof RefType) {
			return isInheritedFromRunnable(((RefType) type).getSootClass());
		}
		return false;
	}

	public static boolean isInheritedFromCallable(SootClass theClass) {
		return InheritanceProcess.isInheritedFromGivenClass(theClass, ExecutorSig.CLASS_CALLABLE);
	}

	public static boolean isInheritedFromCallable(Type type) {
		if (type instanceof RefType) {
			return isInheritedFromCallable(((RefType) type).getSootClass());
		}
		return false;
	}
	


	public static boolean isInheritedFromActivity(SootClass theClass) {
		return InheritanceProcess.isInheritedFromGivenClass(theClass, AsyncTaskSig.CLASS_ACTIVITY, MatchType.equal);
	}

	public static boolean isInheritedFromView(SootClass theClass) {
		return InheritanceProcess.isInheritedFromGivenClass(theClass, AsyncTaskSig.CLASS_VIEW, MatchType.equal);
	}
	

	public static boolean isInheritedFromFragment(SootClass theClass) {
		return InheritanceProcess.isInheritedFromGivenClass(theClass, AsyncTaskSig.CLASS_FRAGMENT, MatchType.equal)
				|| InheritanceProcess.isInheritedFromGivenClass(theClass, AsyncTaskSig.CLASS_SUPPORT_FRAGMENT, MatchType.equal)
				|| InheritanceProcess.isInheritedFromGivenClass(theClass, AsyncTaskSig.CLASS_SUPPORT_FRAGMENT_V7, MatchType.equal);
	}

	public static boolean isInheritedFromAsyncTask(SootClass theClass) {
		return InheritanceProcess.isInheritedFromGivenClass(theClass, AsyncTaskSig.ASYNC_TASK, MatchType.equal);
	}

}
