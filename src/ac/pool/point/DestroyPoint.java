package ac.pool.point;

import soot.SootField;
import soot.SootMethod;
import soot.jimple.Stmt;

public class DestroyPoint extends KeyPoint {
	SootField htrField = null;

	public SootField getHTRField() {
		return htrField;
	}

	public static DestroyPoint newDestroyPoint(SootMethod method, Stmt stmt, SootField htrField) {

		DestroyPoint point = new DestroyPoint();
		point.method = method;
		point.stmt = stmt;
		point.htrField = htrField;
		point.setCaller(getBaseCaller(stmt));
		return point;
	
	}
}