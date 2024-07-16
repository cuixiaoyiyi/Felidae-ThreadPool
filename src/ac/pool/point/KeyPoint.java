package ac.pool.point;

import java.util.Set;

import soot.Local;
import soot.PointsToAnalysis;
import soot.PointsToSet;
import soot.Scene;
import soot.SootMethod;
import soot.Type;
import soot.Value;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.spark.sets.EmptyPointsToSet;

public class KeyPoint {

	public static final PointsToAnalysis pta = Scene.v().getPointsToAnalysis();

	SootMethod method = null;
	Stmt stmt = null;
	private Value caller = null;

	protected Value getParameter(int index) {
		
		return stmt.getInvokeExpr().getArg(index);
	}
	
	protected PointsToSet reachingObjects(Value value) {
		if(value instanceof Local) {
			return  pta.reachingObjects((Local) value);
		}
		return EmptyPointsToSet.v();
		
	}
	
	public PointsToSet getCallerPointsToSet() {
		return reachingObjects(getCaller());
	}
	
	public Set<Type> getCallerPossibleType() {
		PointsToSet pts = getCallerPointsToSet();
		return pts.possibleTypes();
	}
	
	public boolean isAliasCaller(KeyPoint otherKeyPoint) {
		return getCallerPointsToSet().hasNonEmptyIntersection(otherKeyPoint.getCallerPointsToSet());
	}
	
	public boolean isAliasCaller(Local local) {
		return getCallerPointsToSet().hasNonEmptyIntersection(pta.reachingObjects(local));
	}

	public SootMethod getMethod() {
		return method;
	}
	
	public void setMethod(SootMethod method) {
		this.method = method;
	}
	
	public void setStmt(Stmt stmt) {
		this.stmt = stmt;
	}

	public Value getCaller() {
		return caller;
	}

	public Stmt getStmt() {
		return stmt;
	}
	
	public static KeyPoint newPoint(SootMethod method, Stmt stmt) {
		KeyPoint keyPoint = new KeyPoint();
		keyPoint.method = method;
		keyPoint.stmt = stmt;
		keyPoint.setCaller(getBaseCaller(stmt));
		return keyPoint;
	}
	
	public static Value getBaseCaller(Stmt stmt) {
		return getBaseCaller(stmt.getInvokeExpr());
	}
	
	public static Value getBaseCaller(InvokeExpr invokeExpr) {
		if(invokeExpr instanceof InstanceInvokeExpr) {
			InstanceInvokeExpr instanceInvokeExpr = (InstanceInvokeExpr) invokeExpr;
			return instanceInvokeExpr.getBase();
		}
		return null;
	}

	public void setCaller(Value caller) {
		this.caller = caller;
	}

}