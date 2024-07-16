package ac.pool.checker;

import java.util.Set;

import ac.pool.point.KeyPoint;
import soot.Unit;

public class ILChecker {

	public static boolean isShutDownMisuse(Set<KeyPoint> shutDowns, KeyPoint isShutDownPoint) {
		boolean isLoopCondition = isLoopCondition(isShutDownPoint);
		if (!isLoopCondition) {
			return false;
		}
		for (KeyPoint shutDown : shutDowns) {
			if (isShutDownPoint.isAliasCaller(shutDown)) {
				return false;
			}
		}
		return true;
	}

	private static boolean isLoopCondition(KeyPoint isShutDown) {
		return new LoopConditionAnalysis(isShutDown).isShutDownLoopCondition;
	}

	static class LoopConditionAnalysis extends MethodLoopAnalyzer {

		Unit isShutDownUnit = null;

		boolean isShutDownLoopCondition = false;

		public LoopConditionAnalysis(KeyPoint isShutDown) {
			super(isShutDown.getMethod());
			isShutDownUnit = isShutDown.getStmt();
			generation();
		}

		@Override
		protected void afterLoopAnalysis() {
			isShutDownLoopCondition = mLoopHeaderList.contains(isShutDownUnit);
		}

	}
}
