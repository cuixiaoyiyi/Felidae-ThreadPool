package ac.constant;


import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Signature {
	
	 

	public static final String[] startMethods = { ExecutorSig.METHOD_SUBSIG_SUBMIT_CALLABLE, ExecutorSig.METHOD_SUBSIG_SUBMIT_RUNNABLE,
			ExecutorSig.METHOD_SUBSIG_SUBMIT_RUNNABLE_T, ExecutorSig.METHOD_SUBSIG_EXECUTE };
	
	public static final Set<String> startMethods_set = new HashSet<>(Arrays.asList(startMethods));

}
