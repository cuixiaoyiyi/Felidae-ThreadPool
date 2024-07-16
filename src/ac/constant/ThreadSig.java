package ac.constant;

public class ThreadSig {

	public static final String METHOD_SUBSIG_RUN = "void run()";
	
	public final static String METHOD_JOIN_PREFIX = "<java.lang.Thread: void join(";
	public final static String METHOD_SUBSIG_START = "void start()";
	public final static String METHOD_SIG_START = "<java.lang.Thread: void start()>";
	public final static String METHOD_SUBSIG_INTERRUPT = "boolean stop()";
	public final static String METHOD_SUBSIG_INTERRUPT_SAFELY = "void interrupt()";
	public final static String METHOD_SUBSIG_IS_INTERRUPTED = "boolean isInterrupted()";
	public final static String METHOD_SUBSIG_NEWTHREAD = "java.lang.Thread newThread(java.lang.Runnable)";
	public final static String METHOD_SIG_SET_NAME = "<java.lang.Thread setName(java.lang.String)>";
	
	public static final String METHOD_SIG_setUncaughtExceptionHandler = "<java.lang.Thread: void setUncaughtExceptionHandler(java.lang.Thread$UncaughtExceptionHandler)>";
	
	public static final String CLASS_OBJECT = "java.lang.Object";
	public static final String CLASS_THREAD = "java.lang.Thread";
	public static final String CLASS_RUNNABLE = "java.lang.Runnable";
}
