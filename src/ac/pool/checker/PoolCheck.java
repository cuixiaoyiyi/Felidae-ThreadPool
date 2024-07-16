package ac.pool.checker;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ac.pool.ThreadErrorRecord;
import ac.pool.point.InitPoint;
import ac.pool.point.KeyPoint;
import ac.pool.point.OneParaKeyPoint;
import ac.pool.point.PointCollector;
import ac.util.Log;
import soot.RefType;
import soot.Type;

public class PoolCheck implements ICheck{

	protected static ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()/2);
	
	protected PointCollector pointCollector = null;
	
	protected String component = null; 
	
	protected Set<String> iNRClasses = null; 
	
	public PoolCheck(PointCollector pointCollector, String component, Set<String> iNRClasses){
		this.pointCollector = pointCollector;
		this.component = component; 
		this.iNRClasses = iNRClasses;
	}

	@Override
	public void check() {
		if (pointCollector.getStartPoints().isEmpty()) {
			Log.i(component, " # end: StartPoint set is empty..  ");
			return;
		}
		Log.i(component, " # StartPoints Size ", pointCollector.getStartPoints().size());
		Log.i(component, " # 1. Start HTR..  ");
		for (InitPoint point : pointCollector.getInitialPoints()) {
			HTRLoop:
			for (OneParaKeyPoint submitPoint : pointCollector.getSubmitPoints()) {
				if (!point.isAliasCaller(submitPoint)) {
					continue;
				}
				for (Type type : submitPoint.getParaLocalPossiableTypes()) {
					RefType refType = (RefType) type;
					if (HTRChecker.hasHTRMisuse(refType)) {
						ThreadErrorRecord.recordHTR(point, refType.getSootClass());
						break HTRLoop;
					}
				}
			}
		}
		Log.i(component, " # 2. Start INR..  ");
		for (InitPoint point : pointCollector.getInitialPoints()) {
			INRLoop:
			for (OneParaKeyPoint submitPoint : pointCollector.getSubmitPoints()) {
				if (!point.isAliasCaller(submitPoint)) {
					continue;
				}
				for(KeyPoint shutDownNowPoint:pointCollector.getShutDownNowPoints()) {
					if(!shutDownNowPoint.isAliasCaller(point)) {
						continue;
					}
					for (Type type : submitPoint.getParaLocalPossiableTypes()) {
						RefType refType = (RefType) type;
						if (iNRClasses.contains(refType.toString())) {
							ThreadErrorRecord.recordINR(point, refType.getSootClass());
							break INRLoop;
						}
					}
				}
			}
		}

		Log.i(component, " # 3. Start NTT..  ");
		for (OneParaKeyPoint startPoint : pointCollector.getSubmitPoints()) {
			executor.execute(new Runnable() {
				@Override
				public void run() {
					try {
						Set<KeyPoint> keyPoints = new HashSet<KeyPoint>();
						keyPoints.addAll(pointCollector.getShutDownNowPoints());
						keyPoints.addAll(pointCollector.getShutDownPoints());
						if (NTTChecker.checkNTTMisuse(startPoint, keyPoints, pointCollector)) {
							ThreadErrorRecord.recordNTT(startPoint);
						}
					} catch (Throwable e) {
						Log.i("## Exception During NTT ##", e.getClass());
					}

				}
			});

		}
		
		Log.i(component, " # 4. Start IL..  ");

		for (KeyPoint point : pointCollector.getIsTerminatedPoints()) {
			if (ILChecker.isShutDownMisuse(pointCollector.getShutDownPoints(), point)) {
				ThreadErrorRecord.recordIL(point);
			}

		}
		
		Log.i(component, " # 5. Start CallerRunsChecker..  ");
		for (InitPoint point : pointCollector.getInitialPoints()) {
			if (CallerRunsChecker.hasMisuse(point, pointCollector.getSetRejectedExecutionHandlerPoints())) {
				ThreadErrorRecord.recordCallerRunsChecker(point);
			}
		}
		
		Log.i(component, " # 6. Start ExceptionHandlerChecker..  ");
		for (InitPoint point : pointCollector.getInitialPoints()) {
			if(ExceptionHandlerChecker.hasMisuse(point, pointCollector.getSetThreadFactoryPoints(), pointCollector.getSubmitPoints(), pointCollector.getSetUncaughtExceptionHandlerPoints())) {
				ThreadErrorRecord.recordNonExceptionHandlerChecker(point);
			}
		}
		
		
		
	}
	
}
