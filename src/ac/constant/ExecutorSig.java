package ac.constant;

import ac.util.AsyncInherit;
import ac.util.InheritanceProcess;
import soot.RefType;
import soot.Type;
import soot.Value;
import soot.jimple.DefinitionStmt;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.NewExpr;
import soot.jimple.Stmt;

public class ExecutorSig {
	
	// for ExecutorService
	public final static String METHOD_SUBSIG_SHUT_DOWN_NOW = "void shutdownNow()";
	public final static String METHOD_SUBSIG_SHUT_DOWN = "void shutdown()";
	public final static String METHOD_SUBSIG_IS_SHUT_DOWN = "boolean isShutdown()";
	public final static String METHOD_SUBSIG_IS_TERMINATED = "boolean isTerminated()";
	public final static String METHOD_SUBSIG_SUBMIT_CALLABLE = "java.util.concurrent.Future submit(java.util.concurrent.Callable)";
	public final static String METHOD_SUBSIG_SUBMIT_RUNNABLE = "java.util.concurrent.Future submit(java.lang.Runnable)";
	public final static String METHOD_SUBSIG_SUBMIT_RUNNABLE_T = "java.util.concurrent.Future submit(java.lang.Runnable,java.lang.Object)";
	public final static String METHOD_SUBSIG_EXECUTE = "void execute(java.lang.Runnable)";
	public final static String METHOD_SIG_setRejectedExecutionHandler = "<java.util.concurrent.ThreadPoolExecutor: void setRejectedExecutionHandler(java.util.concurrent.RejectedExecutionHandler)>";
	public final static String METHOD_SIG_setThreadFactory = "<java.util.concurrent.ThreadPoolExecutor: void setThreadFactory(java.util.concurrent.ThreadFactory)>";
	public static final String METHOD_SUBSIG_awaitTermination = "boolean awaitTermination(long,java.util.concurrent.TimeUnit)";
	
	public static final String METHOD_NAME_CALL = "call";
	
	public static final String CLASS_CALLABLE = "java.util.concurrent.Callable";
	public static final String CLASS_EXECUTOR_SERVICE = "java.util.concurrent.ExecutorService";
	public static final String CLASS_EXECUTORS = "java.util.concurrent.Executors";
	public static final String CLASS_EXECUTOR = "java.util.concurrent.Executor";
	public static final String CLASS_CallerRunsPolicy = "java.util.concurrent.ThreadPoolExecutor$CallerRunsPolicy";
	public static final String CLASS_RejectedExecutionHandler = "java.util.concurrent.RejectedExecutionHandler";
	public static final String CLASS_ThreadFactory = "java.util.concurrent.ThreadFactory";
	
	// for ExecutorService create
	public static Value getNewThreadPoolValue(Stmt stmt) {
		if (stmt.containsInvokeExpr()) {
			InvokeExpr invokeExpr = stmt.getInvokeExpr();
			// executorService.init(...);
			if (invokeExpr instanceof InstanceInvokeExpr) {
				InstanceInvokeExpr instanceInvokeExpr = (InstanceInvokeExpr) invokeExpr;
				if (InheritanceProcess.isInheritedFromGivenClass(instanceInvokeExpr.getMethod().getDeclaringClass(),
						ExecutorSig.CLASS_EXECUTOR_SERVICE) && instanceInvokeExpr.getMethod().isConstructor()) {
					return instanceInvokeExpr.getBase();
				}
			}
			// executorService = Executors.newXXX(...);
			if (stmt instanceof DefinitionStmt) {
				DefinitionStmt definitionStmt = (DefinitionStmt) stmt;
				Type type = invokeExpr.getMethod().getReturnType();
				if (type instanceof RefType) {
					RefType returnRefType = (RefType) type;
					if (InheritanceProcess.isInheritedFromGivenClass(returnRefType.getSootClass(),
							ExecutorSig.CLASS_EXECUTOR_SERVICE)) {
						return definitionStmt.getLeftOp();
					}
				}
			}
		} else {
			if (stmt instanceof DefinitionStmt) {
				DefinitionStmt definitionStmt = (DefinitionStmt) stmt;
				Value rightOP = definitionStmt.getRightOp();
				if (rightOP instanceof NewExpr && AsyncInherit.isInheritedFromExecutor(rightOP.getType())) {
					return definitionStmt.getLeftOp();
				}
			}
		}
		return null;
	}
	

}
