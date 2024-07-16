package ac.pool.point;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ac.pool.checker.HTRChecker;
import ac.util.InheritanceProcess;
import ac.util.Log;
import destructible.DestructibleIdentify;
import soot.Body;
import soot.Local;
import soot.PointsToAnalysis;
import soot.PointsToSet;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.Stmt;

public abstract class PointCollector {

	protected Set<InitPoint> initialPoints = new HashSet<>();

	protected Set<OneParaKeyPoint> submitPoints = new HashSet<>();

	protected Set<KeyPoint> shutDownPoints = new HashSet<>();

	protected Set<KeyPoint> shutDownNowPoints = new HashSet<>();
	
	protected Set<KeyPoint> startPoints = new HashSet<>();

	static protected Set<DestroyPoint> destroyPoints = new HashSet<>();

	protected Set<OneParaKeyPoint> setThreadFactoryPoints = new HashSet<>();

	protected Set<KeyPoint> isTerminatedPoints = new HashSet<>();

	protected Set<OneParaKeyPoint> setRejectedExecutionHandlerPoints = new HashSet<>();

	protected Set<KeyPoint> setUncaughtExceptionHandlerPoints = new HashSet<>();

	public void start(Collection<SootClass> classes) {

		Log.i("## start PointCollector: classes.size() = ", classes.size());

		for (SootClass sootClass : classes) {
			List<SootMethod> methods = new ArrayList<>(sootClass.getMethods());
			for (SootMethod method : methods) {
//				if (!Scene.v().getReachableMethods().contains(method)) {
//					continue;
//				}
				if (method.hasActiveBody()) {
					Body body = method.getActiveBody();
					for (Unit unit : body.getUnits()) {
						Stmt stmt = (Stmt) unit;
						if (stmt.containsInvokeExpr()) {
							findKeyPoint(stmt, method);
						}
					}
				}
			}
		}
	}
	
	protected int getParaIndexByType(String type, Stmt stmt) {
		for (int i = 0; i < stmt.getInvokeExpr().getMethod().getParameterCount(); i++) {
			if (type.equals(stmt.getInvokeExpr().getMethod().getParameterType(i).toString())) {
				return i;
			}
		}
		return -1;
	}

	protected void findKeyPoint(Stmt stmt, SootMethod method) {
		if (!stmt.containsInvokeExpr()) {
			return;
		}
		if (isInitPoint(stmt)) {
			InitPoint point = newInitPoint(method, stmt);
			initialPoints.add(point);
		}
		if (isSubmitPoint(stmt)) {
			OneParaKeyPoint point = newSubmitPoint(method, stmt);
			submitPoints.add(point);
		}
		if (isDestroyPoint(stmt)) {
			DestroyPoint point = newDestroyPoint(method, stmt);
			if (point != null) {
				destroyPoints.add(point);
			}

		}
		if (isShutdownNowPoint(stmt)) {
			KeyPoint point = newShutdownNowPoint(method, stmt);
			shutDownNowPoints.add(point);
		}
		if (isShutdownPoint(stmt)) {
			KeyPoint point = newShutdownPoint(method, stmt);
			shutDownPoints.add(point);
		}
		if (isStartPoint(stmt)) {
			KeyPoint point = newStartPoint(method, stmt);
			startPoints.add(point);
		}
		if (isIsTerminatedPoint(stmt)) {
			KeyPoint point = newIsTerminatedPoint(method, stmt);
			isTerminatedPoints.add(point);
		}
		if (isSetFactoryPoint(stmt)) {
			OneParaKeyPoint point = newSetFactoryPoint(method, stmt);
			setThreadFactoryPoints.add(point);
		}
		if (isRejectedExecutionHandlerPoint(stmt)) {
			OneParaKeyPoint point = newRejectedExecutionHandlerPoint(method, stmt);
			setRejectedExecutionHandlerPoints.add(point);
		}
		if (isSetUncaughtExceptionHandlerPoint(stmt)) {
			KeyPoint point = newSetUncaughtExceptionHandlerPoint(method, stmt);
			setUncaughtExceptionHandlerPoints.add(point);
		}
	}
	

	protected abstract KeyPoint newStartPoint(SootMethod method, Stmt stmt);

	protected abstract KeyPoint newSetUncaughtExceptionHandlerPoint(SootMethod method, Stmt stmt);

	protected abstract OneParaKeyPoint newRejectedExecutionHandlerPoint(SootMethod method, Stmt stmt);

	protected abstract OneParaKeyPoint newSetFactoryPoint(SootMethod method, Stmt stmt);

	protected abstract KeyPoint newIsTerminatedPoint(SootMethod method, Stmt stmt);

	protected abstract KeyPoint newShutdownPoint(SootMethod method, Stmt stmt);

	protected abstract KeyPoint newShutdownNowPoint(SootMethod method, Stmt stmt);

	protected DestroyPoint newDestroyPoint(SootMethod method, Stmt stmt) {
		Value caller = KeyPoint.getBaseCaller(stmt);
		if (caller instanceof Local) {
			PointsToAnalysis pointsToAnalysis = Scene.v().getPointsToAnalysis();
			for (SootField sootField : HTRChecker.destructibleSootFields) {
				if (InheritanceProcess.isInheritedFromGivenClass(sootField.getType(), caller.getType())
						|| InheritanceProcess.isInheritedFromGivenClass(caller.getType(), sootField.getType())) {
					PointsToSet aliaSet = pointsToAnalysis.reachingObjects((Local) caller, sootField);
					if (!aliaSet.isEmpty()) {
						DestroyPoint point = DestroyPoint.newDestroyPoint(method, stmt, sootField);
						destroyPoints.add(point);
					}
				}
			}
		}
		return null;
	}

	protected abstract boolean isStartPoint(Stmt stmt);

	protected boolean isDestroyPoint(Stmt stmt) {
		return DestructibleIdentify.isDestroyMethod(stmt.getInvokeExpr().getMethod());
	}

	protected abstract OneParaKeyPoint newSubmitPoint(SootMethod method, Stmt stmt);

	protected abstract InitPoint newInitPoint(SootMethod method, Stmt stmt);

	protected abstract boolean isSetUncaughtExceptionHandlerPoint(Stmt stmt);

	protected abstract boolean isRejectedExecutionHandlerPoint(Stmt stmt);

	protected abstract boolean isSetFactoryPoint(Stmt stmt);

	protected abstract boolean isIsTerminatedPoint(Stmt stmt);

	protected abstract boolean isShutdownPoint(Stmt stmt);

	protected abstract boolean isShutdownNowPoint(Stmt stmt);

	protected abstract boolean isSubmitPoint(Stmt stmt);

	protected abstract boolean isInitPoint(Stmt stmt);

	public Set<DestroyPoint> getDestroyPoints() {
		return destroyPoints;
	}

	public Set<InitPoint> getInitialPoints() {
		return initialPoints;
	}

	public Set<KeyPoint> getIsTerminatedPoints() {
		return isTerminatedPoints;
	}

	public Set<OneParaKeyPoint> getSetRejectedExecutionHandlerPoints() {
		return setRejectedExecutionHandlerPoints;
	}

	public Set<OneParaKeyPoint> getSetThreadFactoryPoints() {
		return setThreadFactoryPoints;
	}

	public Set<KeyPoint> getSetUncaughtExceptionHandlerPoints() {
		return setUncaughtExceptionHandlerPoints;
	}

	public Set<KeyPoint> getShutDownNowPoints() {
		return shutDownNowPoints;
	}

	public Set<KeyPoint> getShutDownPoints() {
		return shutDownPoints;
	}

	public Set<OneParaKeyPoint> getSubmitPoints() {
		return submitPoints;
	}
	
	public Set<KeyPoint> getStartPoints() {
		return startPoints;
	}

}
