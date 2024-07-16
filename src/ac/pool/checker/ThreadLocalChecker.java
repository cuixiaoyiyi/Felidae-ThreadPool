package ac.pool.checker;

import ac.pool.point.InitPoint;
import ac.pool.point.OneParaKeyPoint;
import soot.RefType;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.jimple.Stmt;

public class ThreadLocalChecker {

	public static  boolean hasThreadLocalMisuse(InitPoint point, OneParaKeyPoint submitPoint) {
		for (Type type : submitPoint.getParaLocalPossiableTypes()) {
			RefType refType = (RefType) type;
			SootMethod backgroundMethod = ExceptionHandlerChecker.getBackgroundMethod(refType);
			if (hasThreadLocalInThreadPool(backgroundMethod)) {
				return true;
			}
		}
		return false;
	}

	private  static  boolean hasThreadLocalInThreadPool(SootMethod sootMethod) {
		if (sootMethod != null && sootMethod.hasActiveBody()) {
			for (Unit unit : sootMethod.getActiveBody().getUnits()) {
				if (unit instanceof Stmt) {
					Stmt stmt = (Stmt) unit;
					if (stmt.containsInvokeExpr()) {
						if ("java.lang.ThreadLocal"
								.equals(stmt.getInvokeExpr().getMethod().getDeclaringClass().toString())) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

}
