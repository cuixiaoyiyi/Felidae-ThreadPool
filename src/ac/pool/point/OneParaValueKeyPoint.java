package ac.pool.point;

import soot.SootMethod;
import soot.Value;
import soot.jimple.Stmt;

public class OneParaValueKeyPoint extends KeyPoint{

	Value paraValue = null;
	
	public void setParaValue(Value paraValue) {
		this.paraValue = paraValue;
	}
	
	public Value getParaValue() {
		return paraValue;
	}
	
	public static OneParaValueKeyPoint newOneParaValueKeyPoint(SootMethod method, Stmt stmt, int index) {
		OneParaValueKeyPoint point = new OneParaValueKeyPoint();
		point.method = method;
		point.stmt = stmt;
		point.setCaller(getBaseCaller(stmt));
		point.paraValue =  point.getParameter(index);
		return point;
	}
	
}

