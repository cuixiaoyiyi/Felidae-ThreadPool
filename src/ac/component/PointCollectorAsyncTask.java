package ac.component;

import ac.constant.AsyncTaskSig;
import ac.pool.point.InitPoint;
import ac.pool.point.KeyPoint;
import ac.pool.point.OneParaKeyPoint;
import ac.pool.point.OneParaValueKeyPoint;
import ac.pool.point.PointCollector;
import ac.util.AsyncInherit;
import soot.Local;
import soot.SootMethod;
import soot.jimple.Stmt;

public class PointCollectorAsyncTask extends PointCollector {

	@Override
	protected KeyPoint newSetUncaughtExceptionHandlerPoint(SootMethod method, Stmt stmt) {
		return null;
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
		OneParaKeyPoint point = new OneParaKeyPoint();
		point.setMethod(method);
		point.setStmt(stmt);
		point.setCaller(OneParaKeyPoint.getBaseCaller(stmt));
		point.setParaLocal((Local) point.getCaller());
		return point;
	}

	@Override
	protected InitPoint newInitPoint(SootMethod method, Stmt stmt) {
		return InitPoint.newInitPoint(method, stmt);
	}

	@Override
	protected boolean isSetUncaughtExceptionHandlerPoint(Stmt stmt) {
		return false;
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
		return AsyncTaskSig.IS_CANCELLED_SIGNATURE.equals(stmt.getInvokeExpr().getMethod().getSignature());
	}

	@Override
	protected boolean isShutdownPoint(Stmt stmt) {
		return false;
	}

	@Override
	protected boolean isShutdownNowPoint(Stmt stmt) {
		return AsyncTaskSig.CANCEL_SIGNATURE.equals(stmt.getInvokeExpr().getMethod().getSignature());
	}

	@Override
	protected boolean isSubmitPoint(Stmt stmt) {
		return  AsyncTaskSig.EXECUTE_ON_EXECUTOR.equals(stmt.getInvokeExpr().getMethod().getSignature()) ||  AsyncTaskSig.EXECUTE_SIGNATURE.equals(stmt.getInvokeExpr().getMethod().getSignature());
	}

	@Override
	protected boolean isInitPoint(Stmt stmt) {
		SootMethod invokedMethod = stmt.getInvokeExpr().getMethod();
		return invokedMethod .isConstructor() && AsyncInherit.isInheritedFromAsyncTask(invokedMethod.getDeclaringClass());
	}
	
	@Override
	protected KeyPoint newStartPoint(SootMethod method, Stmt stmt) {
		return KeyPoint.newPoint(method, stmt);
	}

	@Override
	protected boolean isStartPoint(Stmt stmt) {
		return isSubmitPoint(stmt);
	}

	@Override
	protected OneParaValueKeyPoint newSetMaxThreadSizePoint(SootMethod method, Stmt stmt) {
		return null;
	}

	@Override
	protected OneParaValueKeyPoint newSetCoreThreadSizePoint(SootMethod method, Stmt stmt) {
		return null;
	}

	@Override
	protected boolean isSetMaxThreadSizePoint(Stmt stmt) {
		return false;
	}

	@Override
	protected boolean isSetCoreThreadSizePoint(Stmt stmt) {
		return false;
	}

}
