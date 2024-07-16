package ac.pool.point;

import java.util.Set;

import soot.Local;
import soot.PointsToSet;
import soot.SootMethod;
import soot.Type;
import soot.jimple.Stmt;

public class OneParaKeyPoint extends KeyPoint{

	Local paraLocal = null;
	
	public void setParaLocal(Local paraLocal) {
		this.paraLocal = paraLocal;
	}
	
	public Local getParaLocal() {
		return paraLocal;
	}
	
	public Set<Type> getParaLocalPossiableTypes() {
		PointsToSet pts = pta.reachingObjects(paraLocal);
		return pts.possibleTypes();
	}
	
	public static OneParaKeyPoint newOneParaKeyPoint(SootMethod method, Stmt stmt, int index) {
		OneParaKeyPoint point = new OneParaKeyPoint();
		point.method = method;
		point.stmt = stmt;
		point.setCaller(getBaseCaller(stmt));
		point.paraLocal = (Local) point.getParameter(index);
		return point;
	}
	
}
