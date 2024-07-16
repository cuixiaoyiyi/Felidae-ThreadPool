package ac.pool.point;

import java.util.HashSet;
import java.util.Set;

import soot.Local;
import soot.PointsToSet;
import soot.RefType;
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
	
	public Set<RefType> getParaLocalPossiableTypes() {
		PointsToSet pts = pta.reachingObjects(paraLocal);
		Set<RefType> set = new HashSet<RefType>();
		for(Type type: pts.possibleTypes()) {
			if(type instanceof RefType) {
				set.add((RefType) type);
			}
		}
		return set;
	}
	
	public static OneParaKeyPoint newOneParaKeyPoint(SootMethod method, Stmt stmt, int index) {
		OneParaKeyPoint point = new OneParaKeyPoint();
		point.method = method;
		point.stmt = stmt;
		point.setCaller(getBaseCaller(stmt));
		if (point.getParameter(index) instanceof Local) {
			point.paraLocal = (Local) point.getParameter(index);
		} else {
			return null;
		}

		return point;
	}
	
}
