package ac.pool.checker;

import java.util.List;
import java.util.Set;

import ac.pool.point.InitPoint;
import ac.pool.point.OneParaKeyPoint;
import ac.util.AsyncInherit;
import soot.Local;
import soot.Unit;
import soot.jimple.DefinitionStmt;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.SimpleLocalDefs;

public class CallerRunsChecker {
	
	public static boolean hasMisuse(InitPoint initPoint, Set<OneParaKeyPoint> set) {
		for(OneParaKeyPoint setRejectedExecutionHandlerPoint: set) {
			if(initPoint.isAliasCaller(setRejectedExecutionHandlerPoint) && isSetCallerRunsHandlerMisuse(setRejectedExecutionHandlerPoint)) {
				return true;
			}
		}
		return false;
	}
	
	private static boolean isSetCallerRunsHandlerMisuse(OneParaKeyPoint setRejectedExecutionHandlerPoint) {
		UnitGraph graph = new BriefUnitGraph(setRejectedExecutionHandlerPoint.getMethod().getActiveBody());
		Local handlerLocal = setRejectedExecutionHandlerPoint.getParaLocal();
		SimpleLocalDefs defs = new SimpleLocalDefs(graph);
		List<Unit> defsOfHandlerLocal = defs.getDefsOf(handlerLocal);
		for (Unit unit : defsOfHandlerLocal) {
			if(unit instanceof DefinitionStmt) {
				DefinitionStmt definitionStmt = (DefinitionStmt) unit;
				if(AsyncInherit.isInheritedCallerRunsPolicy(definitionStmt.getRightOp().getType())) {
					return true;
				}
			}
		}
		return false;
	}

}
