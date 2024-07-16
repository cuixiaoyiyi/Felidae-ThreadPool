package ac.component;

import ac.constant.ExecutorSig;
import ac.constant.ThreadSig;
import ac.pool.point.InitPoint;
import ac.pool.point.KeyPoint;
import ac.pool.point.OneParaKeyPoint;
import ac.pool.point.PointCollector;
import ac.util.AsyncInherit;
import soot.SootMethod;
import soot.jimple.DefinitionStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;

public class PointCollectorExecutor extends PointCollector {

	@Override
	protected KeyPoint newSetUncaughtExceptionHandlerPoint(SootMethod method, Stmt stmt) {
		return KeyPoint.newPoint(method, stmt);
	}

	@Override
	protected OneParaKeyPoint newRejectedExecutionHandlerPoint(SootMethod method, Stmt stmt) {
		int index = getParaIndexByType(ExecutorSig.CLASS_RejectedExecutionHandler, stmt);
		return OneParaKeyPoint.newOneParaKeyPoint(method, stmt, index);
	}

	@Override
	protected OneParaKeyPoint newSetFactoryPoint(SootMethod method, Stmt stmt) {
		int index = getParaIndexByType(ExecutorSig.CLASS_ThreadFactory, stmt);
		return OneParaKeyPoint.newOneParaKeyPoint(method, stmt, index);
	}

	@Override
	protected KeyPoint newIsTerminatedPoint(SootMethod method, Stmt stmt) {
		return KeyPoint.newPoint(method, stmt);
	}

	@Override
	protected KeyPoint newShutdownPoint(SootMethod method, Stmt stmt) {
		return KeyPoint.newPoint(method, stmt);
	}

	@Override
	protected KeyPoint newShutdownNowPoint(SootMethod method, Stmt stmt) {
		return KeyPoint.newPoint(method, stmt);
	}

	@Override
	protected OneParaKeyPoint newSubmitPoint(SootMethod method, Stmt stmt) {
		return OneParaKeyPoint.newOneParaKeyPoint(method, stmt, 0);
	}

	@Override
	protected InitPoint newInitPoint(SootMethod method, Stmt stmt) {
		return InitPoint.newInitPoint(method, stmt);
	}
	
	@Override
	protected KeyPoint newStartPoint(SootMethod method, Stmt stmt) {
		return KeyPoint.newPoint(method, stmt);
	}

	@Override
	protected boolean isSetUncaughtExceptionHandlerPoint(Stmt stmt) {
		return (isInitPoint(stmt) && stmt.getInvokeExpr().getMethod().getParameterTypes().toString()
				.contains(ThreadSig.METHOD_SIG_setUncaughtExceptionHandler))
				|| ThreadSig.METHOD_SIG_setUncaughtExceptionHandler
						.equals(stmt.getInvokeExpr().getMethod().getSignature());
	}

	@Override
	protected boolean isRejectedExecutionHandlerPoint(Stmt stmt) {
		return (isInitPoint(stmt) && stmt.getInvokeExpr().getMethod().getParameterTypes().toString()
				.contains(ExecutorSig.METHOD_SIG_setRejectedExecutionHandler))
				|| ExecutorSig.METHOD_SIG_setRejectedExecutionHandler
						.equals(stmt.getInvokeExpr().getMethod().getSignature());
	}

	@Override
	protected boolean isSetFactoryPoint(Stmt stmt) {
		return (isInitPoint(stmt) && stmt.getInvokeExpr().getMethod().getParameterTypes().toString()
				.contains(ExecutorSig.METHOD_SIG_setThreadFactory))
				|| ExecutorSig.METHOD_SIG_setThreadFactory.equals(stmt.getInvokeExpr().getMethod().getSignature());
	}

	@Override
	protected boolean isIsTerminatedPoint(Stmt stmt) {
		return ExecutorSig.METHOD_SUBSIG_IS_SHUT_DOWN.equals(stmt.getInvokeExpr().getMethod().getSubSignature())
				|| ExecutorSig.METHOD_SUBSIG_IS_TERMINATED.equals(stmt.getInvokeExpr().getMethod().getSubSignature());
	}

	@Override
	protected boolean isShutdownPoint(Stmt stmt) {
		return ExecutorSig.METHOD_SUBSIG_SHUT_DOWN.equals(stmt.getInvokeExpr().getMethod().getSubSignature());
	}

	@Override
	protected boolean isShutdownNowPoint(Stmt stmt) {
		return ExecutorSig.METHOD_SUBSIG_SHUT_DOWN_NOW.equals(stmt.getInvokeExpr().getMethod().getSubSignature());
	}

	@Override
	protected boolean isSubmitPoint(Stmt stmt) {
		String subSig = stmt.getInvokeExpr().getMethod().getSubSignature();
		return ExecutorSig.METHOD_SUBSIG_SUBMIT_CALLABLE.equals(subSig)
				|| ExecutorSig.METHOD_SUBSIG_SUBMIT_RUNNABLE.equals(subSig)
				|| ExecutorSig.METHOD_SUBSIG_SUBMIT_RUNNABLE_T.equals(subSig);
	}

	@Override
	protected boolean isInitPoint(Stmt stmt) {
		InvokeExpr invokeExpr = stmt.getInvokeExpr();
		SootMethod invokedMethod = invokeExpr.getMethod();
		if (invokedMethod.isConstructor() && AsyncInherit.isInheritedFromExecutor(invokedMethod.getDeclaringClass())) {
			return true;
		} else if (invokedMethod.isStatic()) {
			if (stmt instanceof DefinitionStmt) {
				DefinitionStmt definitionStmt = (DefinitionStmt) stmt;
				if (AsyncInherit.isInheritedFromExecutor(definitionStmt.getLeftOp().getType())) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	protected boolean isStartPoint(Stmt stmt) {
		return isSubmitPoint(stmt);
	}

}
