package ac.component;

import ac.constant.ThreadSig;
import ac.pool.point.InitPoint;
import ac.pool.point.KeyPoint;
import ac.pool.point.OneParaKeyPoint;
import ac.pool.point.PointCollector;
import ac.util.AsyncInherit;
import soot.Local;
import soot.SootMethod;
import soot.jimple.Stmt;

public class PointCollectorThread extends PointCollector {

	@Override
	protected KeyPoint newSetUncaughtExceptionHandlerPoint(SootMethod method, Stmt stmt) {
		return KeyPoint.newPoint(method, stmt);
	}

	@Override
	protected OneParaKeyPoint newRejectedExecutionHandlerPoint(SootMethod method, Stmt stmt) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected OneParaKeyPoint newSetFactoryPoint(SootMethod method, Stmt stmt) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected KeyPoint newIsTerminatedPoint(SootMethod method, Stmt stmt) {
		return KeyPoint.newPoint(method, stmt);
	}

	@Override
	protected KeyPoint newShutdownPoint(SootMethod method, Stmt stmt) {
		return null;
	}

	@Override
	protected KeyPoint newShutdownNowPoint(SootMethod method, Stmt stmt) {
		// TODO Auto-generated method stub
		return KeyPoint.newPoint(method, stmt);
	}

	@Override
	protected OneParaKeyPoint newSubmitPoint(SootMethod method, Stmt stmt) {
		int index = getParaIndexByType(ThreadSig.CLASS_RUNNABLE, stmt);
		OneParaKeyPoint point = null;
		if(index == -1) {
			point = new OneParaKeyPoint();
			point.setMethod(method);
			point.setStmt(stmt);
			point.setCaller(OneParaKeyPoint.getBaseCaller(stmt));
			point.setParaLocal((Local) point.getCaller());
		}else {
			point = OneParaKeyPoint.newOneParaKeyPoint(method, stmt, index);
		}
		return point;
	}

	@Override
	protected InitPoint newInitPoint(SootMethod method, Stmt stmt) {
		return InitPoint.newInitPoint(method, stmt);
	}

	@Override
	protected boolean isSetUncaughtExceptionHandlerPoint(Stmt stmt) {
		return ThreadSig.METHOD_SIG_setUncaughtExceptionHandler
						.equals(stmt.getInvokeExpr().getMethod().getSignature());
	}

	@Override
	protected boolean isRejectedExecutionHandlerPoint(Stmt stmt) {
		return false;
	}

	@Override
	protected boolean isSetFactoryPoint(Stmt stmt) {
		return false;
	}

	@Override
	protected boolean isIsTerminatedPoint(Stmt stmt) {
		return ThreadSig.METHOD_SUBSIG_IS_INTERRUPTED.equals(stmt.getInvokeExpr().getMethod().getSubSignature());
	}

	@Override
	protected boolean isShutdownPoint(Stmt stmt) {
		return false;
	}

	@Override
	protected boolean isShutdownNowPoint(Stmt stmt) {
		return ThreadSig.METHOD_SUBSIG_INTERRUPT.equals(stmt.getInvokeExpr().getMethod().getSubSignature())
				|| ThreadSig.METHOD_SUBSIG_INTERRUPT_SAFELY.equals(stmt.getInvokeExpr().getMethod().getSubSignature());
	}

	@Override
	protected boolean isSubmitPoint(Stmt stmt) {
		return isInitPoint(stmt) ;
	}

	@Override
	protected boolean isInitPoint(Stmt stmt) {
		SootMethod invokedMethod = stmt.getInvokeExpr().getMethod();
		return invokedMethod .isConstructor() && AsyncInherit.isInheritedFromThread(invokedMethod.getDeclaringClass());
	}
	
	@Override
	protected KeyPoint newStartPoint(SootMethod method, Stmt stmt) {
		return KeyPoint.newPoint(method, stmt);
	}

	@Override
	protected boolean isStartPoint(Stmt stmt) {
		return ThreadSig.METHOD_SIG_START.equals(stmt.getInvokeExpr().getMethod().getSignature());
	}

}
