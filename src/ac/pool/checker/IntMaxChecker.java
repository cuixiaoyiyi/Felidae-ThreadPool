package ac.pool.checker;

import java.util.List;

import ac.pool.point.OneParaValueKeyPoint;
import soot.Local;
import soot.Unit;
import soot.jimple.DefinitionStmt;
import soot.jimple.IntConstant;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.SimpleLocalDefs;

public class IntMaxChecker {
	
	public static boolean hasMaxIntegerSizeMisuse(OneParaValueKeyPoint point){
		if(point.getParaValue() instanceof IntConstant) {
			IntConstant intConstant = (IntConstant) point.getParaValue();
			if(intConstant.value == Integer.MAX_VALUE) {
				return true;
			}
		}else if(point.getParaValue() instanceof Local) {
			Local local = (Local) point.getParaValue();
			UnitGraph graph = new BriefUnitGraph(point.getMethod().getActiveBody());
			SimpleLocalDefs defs = new SimpleLocalDefs(graph);
			List<Unit> defsOfHandlerLocal = defs.getDefsOf(local);
			for (Unit unit : defsOfHandlerLocal) {
				if(unit instanceof DefinitionStmt) {
					DefinitionStmt definitionStmt = (DefinitionStmt) unit;
					if(definitionStmt.getRightOp() instanceof IntConstant) {
						IntConstant intConstant = (IntConstant) definitionStmt.getRightOp();
						if(intConstant.value == Integer.MAX_VALUE) {
							return true;
						}
					}
				}
			}

		}
		return false;
	}

}
