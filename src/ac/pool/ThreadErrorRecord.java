package ac.pool;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import ac.pool.point.InitPoint;
import ac.pool.point.KeyPoint;
import soot.SootClass;


public class ThreadErrorRecord {

	private static final String Path_FOLDER = PoolMain.getOutputPath("path") + File.separator;
	private static final String ERROR_FOLDER = PoolMain.getOutputPath("error") + File.separator;
	private static final String EXCEPTION_FOLDER = PoolMain.getOutputPath("") + File.separator;

//	public static Set<KeyPoint> HTRPointSet = new HashSet<>();
//	public static Set<String> HTRSet = new HashSet<>();
//	public static Set<String> NTTSet = new HashSet<>();
//	public static Set<String> INRSet = new HashSet<>();

	public static Set<KeyPoint> errorInstanceSet = new HashSet<>();
	public static Set<KeyPoint> rightInstanceSet = new HashSet<>();

	public synchronized static void recordHTR(String component, KeyPoint newPoint, SootClass sootClass) {
//		HTRPointSet.add(newPoint);
//		if (!needRecord(HTRSet, newPoint, sootClass)) {
//			return;
//		}

		String filePath = ERROR_FOLDER + component + "HTR-sum.txt";
		recordSum(component, newPoint, sootClass, filePath);
	}

	public synchronized static void recordNTT(String component, KeyPoint newPoint) {
//		if (!needRecord(NTTSet, newPoint, null))
//			return;
		String filePath = ERROR_FOLDER + component + "NTT-sum.txt";
		recordSum(component, newPoint, null, filePath);
		return;

	}
	
	public synchronized static void recordNT(String component, KeyPoint newPoint) {
//		if (!needRecord(NTTSet, newPoint, null))
//			return;
		String filePath = ERROR_FOLDER + component + "NT-sum.txt";
		recordSum(component, newPoint, null, filePath);
		return;

	}

	public synchronized static void recordINR(String component, KeyPoint newPoint, SootClass sootClass) {
//		if (!needRecord(INRSet, newPoint, null))
//			return;

		String filePath = ERROR_FOLDER + component + "INR-sum.txt";
		recordSum(component, newPoint, sootClass, filePath);

	}
	

	public synchronized static void recordIL(String component, KeyPoint newPoint) {
	//	if (!needRecord(ILSet, newPoint, null)) {
	//		return;
	//	}
	
		String filePath = ERROR_FOLDER + component + "IL-sum.txt";
		recordSum(component, newPoint, null, filePath);
	}
	
	public static void recordCallerRunsChecker(String component, KeyPoint point) {
		String filePath = ERROR_FOLDER + "CallerRuns-sum.txt";
		recordSum(component, point, null, filePath);
	}
	
	public static void recordNonExceptionHandlerChecker(String component, InitPoint point) {
		String filePath = ERROR_FOLDER + component + "NonExceptionHandler-sum.txt";
		recordSum(component, point, null, filePath);
	}
	
	
	
	public static void recordRCTP(String component, InitPoint point) {
		String filePath = ERROR_FOLDER + component + "RCTP-sum.txt";
		recordSum(component, point, null, filePath);
	}
	
	
	
	public static void recordUTL(String component, InitPoint point) {
		String filePath = ERROR_FOLDER + component + "UTL-sum.txt";
		recordSum(component, point, null, filePath);
	}
	
	public static void recordUBNT(String component, InitPoint point) {
		String filePath = ERROR_FOLDER + component + "UBNT-sum.txt";
		recordSum(component, point, null, filePath);
	}
	
	
	public static void recordUNT(String component, InitPoint point) {
		String filePath = ERROR_FOLDER + component + "UNT-sum.txt";
		recordSum(component, point, null, filePath);
	}
	
	


	public static String getObjectKey(String component, KeyPoint newPoint, SootClass sootClass) {
		return component + newPoint.getMethod() + ";" + newPoint.getStmt() + ";" + sootClass;
	}

	public synchronized static void recordTime(String component, String source, long androlicStartTime, long sootStartTime) {
		String logFilePath = Path_FOLDER + component +  "log.txt";

		String content = source + "\n" + "sootTime: " + (androlicStartTime - sootStartTime) + "\n" + "FelidaeTime: "
				+ (System.currentTimeMillis() - androlicStartTime) + "\n";
//		String content = source + Thread.currentThread().getName() + " sootTime: " + (androlicStartTime - sootStartTime) + "\n" + "androlic time: "
//				+ (System.currentTimeMillis() - androlicStartTime) + "\n";
		record(content, logFilePath);
	}

	public synchronized static void recordRightInstance() {
		String rightInstanceFilePath = Path_FOLDER + "right-async.txt";
		FileWriter errorWriter = null;
		try {
			errorWriter = new FileWriter(rightInstanceFilePath, true);
			for (KeyPoint newPoint : rightInstanceSet) {
				errorWriter.write(newPoint + "\n");
			}
		} catch (Exception e) {
			saveException(e);
		} finally {
			try {
				errorWriter.close();
			} catch (IOException e) {
				saveException(e);
			}
		}
	}

	private static void recordSum(String component, KeyPoint newPoint, SootClass sootClass, String filePath) {
		String content = getObjectKey(component, newPoint, sootClass) + "\n";
		errorInstanceSet.add(newPoint);
		rightInstanceSet.remove(newPoint);
		record(content, filePath);

		String errorInstanceFilePath = ERROR_FOLDER + component + "error-async.txt";
		String key = getObjectKey(component, newPoint, sootClass);
		record(key + "\n", errorInstanceFilePath);
	}

	static long currentTime = PoolMain.preprocessStartTime;

	public synchronized static void recordWorkingData(String component, int arg) {
		String filePath = ERROR_FOLDER + component + "workingData.txt";
		String content = (System.currentTimeMillis() - PoolMain.startTime) + ";" + arg + "\n";
		record(content, filePath);
	}

	public static void record(String content, String filePath) {
		FileWriter errorWriter = null;
		try {
			errorWriter = new FileWriter(filePath, true);
			errorWriter.write(content);
		} catch (Exception e) {
			e.printStackTrace();
//			saveException(e);
		} finally {
			try {
				errorWriter.close();
			} catch (Exception | Error e) {
				e.printStackTrace();
			}
		}
	}

	public synchronized static void saveException(Throwable exception) {
		String filePath = EXCEPTION_FOLDER + File.separator + "exception.txt";

		exception.printStackTrace();

		StringBuffer out = new StringBuffer();
		out.append(PoolMain.getApkFullPath() + "\r\n");
		out.append(exception + "\r\n");
		for (StackTraceElement element : exception.getStackTrace()) {
			out.append("	" + element + "\r\n");
		}
		out.append("------------------------------\r\n\n");
		record(out.toString(), filePath);
	}

}