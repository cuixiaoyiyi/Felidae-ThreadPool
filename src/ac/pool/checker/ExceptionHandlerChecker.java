package ac.pool.checker;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ac.constant.AsyncTaskSig;
import ac.constant.ExecutorSig;
import ac.constant.ThreadSig;
import ac.pool.point.InitPoint;
import ac.pool.point.KeyPoint;
import ac.pool.point.OneParaKeyPoint;
import ac.util.Log;
import soot.Body;
import soot.Local;
import soot.RefType;
import soot.SootClass;
import soot.SootMethod;
import soot.Trap;
import soot.Type;
import soot.Unit;
import soot.jimple.DefinitionStmt;
import soot.jimple.ReturnStmt;
import soot.jimple.ThisRef;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.util.Chain;

public class ExceptionHandlerChecker {
	

	public static boolean hasMisuse(InitPoint initPoint, Set<OneParaKeyPoint> setFactoryPoints,
			Set<OneParaKeyPoint> submitPoints, Set<KeyPoint> setUncaughtExceptionHandlerPoints) {
		return !hasSubmitHandler(initPoint, submitPoints, setUncaughtExceptionHandlerPoints)
				&& !hasFactoryHandler(initPoint, setFactoryPoints, setUncaughtExceptionHandlerPoints);
	}

	private static boolean hasFactoryHandler(InitPoint initPoint, Set<OneParaKeyPoint> setFactoryPoints,
			Set<KeyPoint> setUncaughtExceptionHandlerPoints) {
		for (OneParaKeyPoint setFactoryPoint : setFactoryPoints) {
			if (initPoint.isAliasCaller(setFactoryPoint)) {
				Set<Local> coreThreads = getCoreThreadsFromThreadFactory(setFactoryPoint.getParaLocal());
				for (Local coreThread : coreThreads) {
					Log.e("hasFactoryHandler#####", hasBackgroundMethodHandler(coreThread, setUncaughtExceptionHandlerPoints));
					if (!hasBackgroundMethodHandler(coreThread, setUncaughtExceptionHandlerPoints)) {
						return false;
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

	private static boolean hasSubmitHandler(InitPoint initPoint, Set<OneParaKeyPoint> submitPoints,
			Set<KeyPoint> setUncaughtExceptionHandlerPoints) {
		for (OneParaKeyPoint submitPoint : submitPoints) {
			if (initPoint.isAliasCaller(submitPoint)) {
				Local task = submitPoint.getParaLocal();
				if (!hasBackgroundMethodHandler(task, setUncaughtExceptionHandlerPoints)) {
					Log.e("hasSubmitHandler#####", hasBackgroundMethodHandler(task, setUncaughtExceptionHandlerPoints));
					return false;
				}
			}
		}
		return true;
	}

	private static boolean hasBackgroundMethodHandler(Local task, Set<KeyPoint> setUncaughtExceptionHandlerPoints) {
		for (KeyPoint keyPoint : setUncaughtExceptionHandlerPoints) { // thread.setUncaughtExceptionHandler();
			if (keyPoint.isAliasCaller(task)) {  
				/** / fkkk, soot has a bug:
				 * 
				 *  $r5 = new cn.ac.ios.PoolTest$2;

        specialinvoke $r5.<cn.ac.ios.PoolTest$2: void <init>(cn.ac.ios.PoolTest)>(r3);  //inner Runnable

        virtualinvoke r1.<java.util.concurrent.ThreadPoolExecutor: java.util.concurrent.Future submit(java.lang.Runnable)>($r5);

        $r7 = <cn.ac.ios.PoolTest$A: cn.ac.ios.PoolTest$A B>;

        if r6 != $r7 goto label1;

        $r10 = <java.lang.System: java.io.PrintStream out>;

        virtualinvoke $r10.<java.io.PrintStream: void print(java.lang.String)>("1");

     label1:
        $r8 = staticinvoke <java.lang.Thread: java.lang.Thread currentThread()>();
        
        
				 *  then $r8 is  $r5 ??
				 * */ 
				Log.e(keyPoint.getStmt());
				Log.e(task);
				Log.e("keyPoint.isAliasCaller(task)");
				return true;
			}
		}
		if (KeyPoint.pta.reachingObjects(task).isEmpty()) {
			return false;
		}
		for (Type taskPossiableType : KeyPoint.pta.reachingObjects(task).possibleTypes()) {
			if(taskPossiableType instanceof RefType) {
				SootMethod method = getBackgroundMethod((RefType) taskPossiableType);
				if (method!=null && !hasExceptionHandler(method)) {
					return false;
				}
			}
			
		}
		return true;
	}

	static SootMethod getBackgroundMethod(RefType taskPossiableType) {
		SootClass sootClass = taskPossiableType.getSootClass();
		SootMethod runMethod = sootClass.getMethodUnsafe(ThreadSig.METHOD_SUBSIG_RUN);
		if(runMethod == null) {
			runMethod = getMethod(sootClass,ExecutorSig.METHOD_NAME_CALL);
		}
		if(runMethod == null) {
			runMethod = getMethod(sootClass, AsyncTaskSig.DO_IN_BACKGROUND_NAME);
		}
		return null;
	}

	private static boolean hasExceptionHandler(SootMethod sootMethod) {
		if (!sootMethod.hasActiveBody()) {
			return false;
		}
		Body body = sootMethod.getActiveBody();
		Chain<Trap> traps = body.getTraps();
		ExceptionalUnitGraph unitGraph = new ExceptionalUnitGraph(body);
		List<Unit> heads = new ArrayList<Unit>();
		if (unitGraph.getHeads().isEmpty()) {
			return true;
		}
		for (Unit head : unitGraph.getHeads()) {
			boolean thisDefinitionStmt = false;
			if (head instanceof DefinitionStmt) {
				DefinitionStmt definitionStmt = (DefinitionStmt) head;
				thisDefinitionStmt = definitionStmt.getRightOp() instanceof ThisRef;
				if (thisDefinitionStmt && unitGraph.getHeads().size() == 1) {
					return true;
				}
			}
			if (thisDefinitionStmt) {
				heads.addAll(unitGraph.getSuccsOf(head));
			} else {
				heads.add(head);
			}
		}

		for (Unit head : heads) {
			boolean hasExceptionHandler = false;
			for (Trap trap : traps) {
				Log.i("############");
				Log.i(trap);
				if (head.equals(trap.getBeginUnit())) {
					hasExceptionHandler = true;
					break;
				}
			}
			if (!hasExceptionHandler) {
				return false;
			}
		} // all heads is contained in the try-catch (Exception Handler) block
		Log.i("#####unitGraph.getHeads()#######");
		Log.i(unitGraph.getHeads());
		return true;
	}
	
	private static SootMethod getMethod(SootClass sootClass, String methodName) {
		if (sootClass == null) {
			return null;
		}
		SootMethod resultMethod = null;
		List<SootMethod> methods = new ArrayList<SootMethod>(sootClass.getMethods());
		for (SootMethod sootMethod : methods) {
			if (sootMethod.getName().equals(methodName)
					&& sootMethod.getDeclaration().contains("transient")
					&& !sootMethod.getDeclaration().contains("volatile")) {
				return sootMethod;
			}
			if (sootMethod.getName().equals(methodName)
					&& sootMethod.getDeclaration().contains("transient")) {
				resultMethod = sootMethod;
				continue;
			}
			if (sootMethod.getName().equals(methodName) && resultMethod == null) {
				resultMethod = sootMethod;
			}
		}

		if (resultMethod == null && sootClass.hasSuperclass()) {
			SootClass superclass = sootClass.getSuperclass();
			return getMethod(superclass, methodName);
		}

		return resultMethod;
	}
}
