package ac.pool.checker;

import java.util.HashSet;
import java.util.Set;
import ac.constant.ThreadSig;
import ac.pool.point.InitPoint;
import ac.pool.point.KeyPoint;
import ac.pool.point.OneParaKeyPoint;
import soot.Local;
import soot.RefType;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.jimple.ReturnStmt;

public class SetThreadNameChecker {

	public static boolean hasMisuse(InitPoint initPoint, Set<OneParaKeyPoint> setFactoryPoints,
			Set<KeyPoint> setNameKeyPoints) {
		for (KeyPoint setNameKeyPoint : setNameKeyPoints) {
			if (setNameKeyPoint.isAliasCaller(initPoint)) {
				return false;
			}
		}
		for (OneParaKeyPoint setFactoryPoint : setFactoryPoints) {
			if (setFactoryPoint.isAliasCaller(initPoint)) {
				Set<Local> coreThreads = getCoreThreadsFromThreadFactory(setFactoryPoint.getParaLocal());
				for (Local coreThread : coreThreads) {
					for (KeyPoint setNameKeyPoint : setNameKeyPoints) {
						if (setNameKeyPoint.isAliasCaller(coreThread)) {
							return false;
						}
					}
				}
			}
		}
		return true;
	}

	private static Set<Local> getCoreThreadsFromThreadFactory(Local threadFactoryLocal) {
		Set<Local> locals = new HashSet<>();
		for (Type taskPossiableType : KeyPoint.pta.reachingObjects(threadFactoryLocal).possibleTypes()) {
			RefType refType = (RefType) taskPossiableType;
			SootMethod newThreadMethod = refType.getSootClass().getMethodUnsafe(ThreadSig.METHOD_SUBSIG_NEWTHREAD);
			if (newThreadMethod != null && newThreadMethod.hasActiveBody()) {
				for (Unit unit : newThreadMethod.getActiveBody().getUnits()) {
					if (unit instanceof ReturnStmt) {
						ReturnStmt returnStmt = (ReturnStmt) unit;
						if (returnStmt.getOp() instanceof Local) {
							locals.add((Local) returnStmt.getOp());
						}
					}
				}
			}
		}
		return locals;
	}

}
