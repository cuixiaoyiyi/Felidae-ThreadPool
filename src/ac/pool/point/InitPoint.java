package ac.pool.point;

import java.util.HashSet;
import java.util.Set;

import soot.Local;
import soot.SootMethod;
import soot.jimple.DefinitionStmt;
import soot.jimple.Stmt;

public class InitPoint extends KeyPoint{
	
	Set<Local> taskLocals = new HashSet<>();
	
	Set<Local> coreThreadLocals = new HashSet<>();
	
	public void addTask(Local taskLocal) {
		taskLocals.add(taskLocal);
	}
	
	public void addCoreThread(Local coreThreadLocal) {
		coreThreadLocals.add(coreThreadLocal);
	}
	
	public Set<Local> getCoreThreadLocals() {
		return coreThreadLocals;
	}
	
	public Set<Local> getTaskLocals() {
		return taskLocals;
	}
	
	public static InitPoint newInitPoint(SootMethod method, Stmt stmt) {
		InitPoint point = new InitPoint();
		point.method = method;
		point.stmt = stmt;
		point.setCaller(getBaseCaller(stmt));
		if(point.getCaller() == null && stmt instanceof DefinitionStmt) { // r0 = Executors.newxxx()
			DefinitionStmt definitionStmt = (DefinitionStmt) stmt;
			point.setCaller(definitionStmt.getLeftOp());		
		}
		return point;
	}

}
