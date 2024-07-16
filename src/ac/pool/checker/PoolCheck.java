package ac.pool.checker;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ac.component.PointCollectorExecutor;
import ac.pool.ThreadErrorRecord;
import ac.pool.point.InitPoint;
import ac.pool.point.KeyPoint;
import ac.pool.point.OneParaKeyPoint;
import ac.pool.point.OneParaValueKeyPoint;
import ac.pool.point.PointCollector;
import ac.util.Log;
import soot.RefType;
import soot.Scene;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;

public class PoolCheck implements ICheck {

	public static final ExecutorService executor = Executors
			.newFixedThreadPool(Runtime.getRuntime().availableProcessors() / 2);

	protected PointCollector pointCollector = null;

	protected String component = null;

	protected Set<String> iNRClasses = null;

	public PoolCheck(PointCollector pointCollector, String component, Set<String> iNRClasses) {
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
		Log.i(component, " # InitialPoints Size ", pointCollector.getInitialPoints().size());
		Log.i(component, " # StartPoints Size ", pointCollector.getStartPoints().size());
		Log.i(component, " # 1. Start HTR..  ");
		for (InitPoint point : pointCollector.getInitialPoints()) {
			HTRLoop: for (OneParaKeyPoint submitPoint : pointCollector.getSubmitPoints()) {
				if (!point.isAliasCaller(submitPoint)) {
					continue;
				}
				for (RefType refType : submitPoint.getParaLocalPossiableTypes()) {
					if (HTRChecker.hasHTRMisuse(refType)) {
						ThreadErrorRecord.recordHTR(component, point, refType.getSootClass());
						break HTRLoop;
					}
				}
			}
		}
		Log.i(component, " # 2. Start INR..  ");
		for (InitPoint point : pointCollector.getInitialPoints()) {
			INRLoop: for (OneParaKeyPoint submitPoint : pointCollector.getSubmitPoints()) {
				if (!point.isAliasCaller(submitPoint)) {
					continue;
				}
				for (KeyPoint shutDownNowPoint : pointCollector.getShutDownNowPoints()) {
					if (!shutDownNowPoint.isAliasCaller(point)) {
						continue;
					}
					for (RefType refType : submitPoint.getParaLocalPossiableTypes()) {
						if (iNRClasses.contains(refType.toString())) {
							ThreadErrorRecord.recordINR(component, point, refType.getSootClass());
							break INRLoop;
						}
					}
				}
			}
		}

		Log.i(component, " # 3. Start NTT..  ");
		for (InitPoint point : pointCollector.getInitialPoints()) {
			for (OneParaKeyPoint startPoint : pointCollector.getSubmitPoints()) {
				if (!startPoint.isAliasCaller(point)) {
					continue;
				}
				executor.execute(new Runnable() {
					@Override
					public void run() {
						try {
							Set<KeyPoint> keyPoints = new HashSet<KeyPoint>();
							keyPoints.addAll(pointCollector.getShutDownNowPoints());
							keyPoints.addAll(pointCollector.getShutDownPoints());
							if (NTTChecker.checkNTTMisuse(startPoint, keyPoints, pointCollector)) {
								ThreadErrorRecord.recordNTT(component, point);
							}
						} catch (Throwable e) {
							Log.i("## Exception During NTT ##", e.getClass());
						}

					}
				});
			}
		}

		Log.i(component, " # 3.1. Start NT..  ");

		for (InitPoint point : pointCollector.getInitialPoints()) {
			Set<KeyPoint> keyPoints = new HashSet<KeyPoint>();
			keyPoints.addAll(pointCollector.getShutDownNowPoints());
			keyPoints.addAll(pointCollector.getShutDownPoints());
			boolean terminate = false;
			for (KeyPoint terminatePoint : keyPoints) {
				if (point.isAliasCaller(terminatePoint)) {
					terminate = true;
					break;
				}
			}
			if (!terminate) {
				ThreadErrorRecord.recordNT(component, point);
			}
		}

		Log.i(component, " # 4. Start IL..  ");

		for (KeyPoint point : pointCollector.getIsTerminatedPoints()) {
			if (ILChecker.isShutDownMisuse(pointCollector.getShutDownPoints(), point)) {
				ThreadErrorRecord.recordIL(component, point);
			}

		}

		Log.i(component, " # 5. Start CallerRunsChecker..  ");
		for (InitPoint point : pointCollector.getInitialPoints()) {
			if (CallerRunsChecker.hasMisuse(point, pointCollector.getSetRejectedExecutionHandlerPoints())) {
				ThreadErrorRecord.recordCallerRunsChecker(component, point);
			}
		}

		Log.i(component, " # 6. Start ExceptionHandlerChecker..  ");
		for (InitPoint point : pointCollector.getInitialPoints()) {
			if (ExceptionHandlerChecker.hasMisuse(point, pointCollector.getSetThreadFactoryPoints(),
					pointCollector.getSubmitPoints(), pointCollector.getSetUncaughtExceptionHandlerPoints())) {
				ThreadErrorRecord.recordNonExceptionHandlerChecker(component, point);
			}
		}

		Log.i(component, " # 7. Start RepeatedlyCreateThreadPool..  ");
		for (InitPoint point : pointCollector.getInitialPoints()) {
			CallGraph callGraph = Scene.v().getCallGraph();
			Iterator<Edge> edgesInto = callGraph.edgesInto(point.getMethod());
			int count = 0;
			while (edgesInto.hasNext()) {
				edgesInto.next();
				count++;
				if (count > 1) {
					ThreadErrorRecord.recordRCTP(component, point);
					break;
				}
			}
		}

		Log.i(component, " # 8. Start UnrefactoredThreadLocal (UTL)..  ");
		if (pointCollector instanceof PointCollectorExecutor) {
			for (InitPoint point : pointCollector.getInitialPoints()) {
				for (OneParaKeyPoint submitPoint : pointCollector.getSubmitPoints()) {
					if (!point.isAliasCaller(submitPoint)) {
						continue;
					}
					if (ThreadLocalChecker.hasThreadLocalMisuse(point, submitPoint)) {
						ThreadErrorRecord.recordUTL(component, point);
						break;
					}
				}
			}
		}

		Log.i(component, " # 9. Start UnboundedNumberOfThread (UBNT Core)..  ");
		if (pointCollector instanceof PointCollectorExecutor) {
			for (InitPoint point : pointCollector.getInitialPoints()) {
				for (OneParaValueKeyPoint setSizePoint : pointCollector.getSetCoreThreadSizePoints()) {
					if (!setSizePoint.isAliasCaller(point)) {
						continue;
					}
					if (IntMaxChecker.hasMaxIntegerSizeMisuse(setSizePoint)) {
						ThreadErrorRecord.recordUBNT(component + "-coreSize-", point);
						break;
					}
				}
			}
		}

		Log.i(component, " # 10. Start UnboundedNumberOfThread (UBNT Max)..  ", pointCollector.getSetMaxThreadSizePoints());
		if (pointCollector instanceof PointCollectorExecutor) {
			for (InitPoint point : pointCollector.getInitialPoints()) {
				for (OneParaValueKeyPoint setSizePoint : pointCollector.getSetMaxThreadSizePoints()) {
					if (!setSizePoint.isAliasCaller(point)) {
						continue;
					}
					if (IntMaxChecker.hasMaxIntegerSizeMisuse(setSizePoint)) {
						ThreadErrorRecord.recordUBNT(component + "-MaxSize-", point);
						break;
					}
				}
			}
		}

		Log.i(component, " # 11. Start UnnamedThread (UNT) ..  ");
		for (InitPoint point : pointCollector.getInitialPoints()) {
			if (SetThreadNameChecker.hasMisuse(point, pointCollector.getSetThreadFactoryPoints(),
					pointCollector.getSetNamePoints())) {
				ThreadErrorRecord.recordUNT(component, point);
				break;
			}
		}

	}

}
