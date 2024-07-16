package ac;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ac.pool.PoolMain;
import ac.util.AsyncInherit;
import ac.util.Log;
import soot.Body;
import soot.BodyTransformer;
import soot.G;
import soot.Local;
import soot.Main;
import soot.Pack;
import soot.PackManager;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.Transform;
import soot.Unit;
import soot.Value;
import soot.jimple.DefinitionStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.NewExpr;
import soot.jimple.infoflow.android.SetupApplication;
import soot.jimple.infoflow.android.manifest.IActivity;
import soot.jimple.infoflow.android.manifest.IAndroidComponent;
import soot.jimple.infoflow.android.manifest.IBroadcastReceiver;
import soot.jimple.infoflow.android.manifest.IContentProvider;
import soot.jimple.infoflow.android.manifest.IManifestHandler;
import soot.jimple.infoflow.android.manifest.IService;
import soot.options.Options;

public class Filter {

	static String inputPath = "/Users/cbq/Desktop/apks/app.fedilab.lite.apk";

	static class MySetupApplication extends SetupApplication {

		public MySetupApplication(String androidJar, String apkFileLocation) {
			super(androidJar, apkFileLocation);
		}

		IManifestHandler<?, ?, ?, ?> getManifest() {
			try {
				parseAppResources();
				return manifest;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
	}

	public static void main(String[] args) {
//		inputPath = "/home/hongwj/scripts/fdroid/2021/";sftp://124.16.137.61/home/hongwj/scripts/google
		inputPath = "/home/hongwj/scripts/google/";
		String apkName = args[0];
		inputPath += apkName;
//		String apkName = "com.placz.cricketpakistan.apk";
//		LeopardPoolMain.Android_Platforms = "/Users/cbq/Desktop/code/android-platforms-master";
//		inputPath = "/Users/cbq/Desktop/apks/com.placz.cricketpakistan.apk";
		MySetupApplication application = new MySetupApplication(PoolMain.Android_Platforms, inputPath);

		IManifestHandler<?, ?, ?, ?> manifest = application.getManifest();

		final Set<String> activityNames = new HashSet<>();
		final Set<String> serviceNames = new HashSet<>();
		final Set<String> receiverNames = new HashSet<>();
		final Set<String> providerNames = new HashSet<>();

		if (manifest != null) {
			List<? extends IActivity> activities = manifest.getActivities().asList();
			if (activities != null && !activities.isEmpty()) {
				for (IAndroidComponent component : activities) {
					activityNames.add(component.getNameString());
				}
			}

			List<? extends IContentProvider> providers = manifest.getContentProviders().asList();
			if (providers != null && !providers.isEmpty()) {
				for (IAndroidComponent component : providers) {
					providerNames.add(component.getNameString());
				}
			}

			List<? extends IService> services = manifest.getServices().asList();
			if (services != null && !services.isEmpty()) {
				for (IAndroidComponent component : services) {
					serviceNames.add(component.getNameString());
				}
			}

			List<? extends IBroadcastReceiver> receivers = manifest.getBroadcastReceivers().asList();
			if (receivers != null && !receivers.isEmpty()) {
				for (IAndroidComponent component : receivers) {
					receiverNames.add(component.getNameString());
				}
			}
		}else {
			Log.e(apkName, "manifest is null !!");
		}

		// Clean up any old Soot instance we may have
		G.reset();
		Options.v().set_no_bodies_for_excluded(true);
		Options.v().set_allow_phantom_refs(true);
		Options.v().set_output_format(Options.output_format_none);
		Options.v().set_whole_program(false);
		Options.v().set_process_dir(Collections.singletonList(inputPath));
		Options.v().set_android_jars(PoolMain.Android_Platforms);
		Options.v().set_src_prec(Options.src_prec_apk);
		Options.v().set_keep_offset(false);
		Options.v().set_process_multiple_dex(true);
		Options.v().set_ignore_resolution_errors(true);

		Pack p1 = PackManager.v().getPack("jtp");
		String phaseName = "jtp.bt";

		final List<String> activitiesWithPoolAsLocal = new ArrayList<>();
		final List<String> servicesWithPoolAsLocal = new ArrayList<>();
		final List<String> receiversWithPoolAsLocal = new ArrayList<>();
		final List<String> providersWithPoolAsLocal = new ArrayList<>();
		final List<String> othersWithPoolAsLocal = new ArrayList<>();

		Transform t1 = new Transform(phaseName, new BodyTransformer() {
			@Override
			protected void internalTransform(Body b, String phase, Map<String, String> options) {
				Set<Local> poolLocals = new HashSet<>();
				for (Local local : b.getLocals()) {
					if (AsyncInherit.isInheritedFromExecutorService(local.getType())) {
						poolLocals.add(local);
					}
				}
				if (poolLocals.isEmpty()) {
					return;
				}
				for (Unit unit : b.getUnits()) {
					if (unit instanceof DefinitionStmt) {
						DefinitionStmt definitionStmt = (DefinitionStmt) unit;

						if (poolLocals.contains(definitionStmt.getLeftOp())) {
							Value rightOp = definitionStmt.getRightOp();
							if (rightOp instanceof NewExpr || rightOp instanceof InvokeExpr) {
								String className = b.getMethod().getDeclaringClass().getName();
								if (activityNames.contains(className)) {
									synchronized (activitiesWithPoolAsLocal) {
										activitiesWithPoolAsLocal.add(unit.toString());
									}
								} else if (serviceNames.contains(className)) {
									synchronized (servicesWithPoolAsLocal) {
										servicesWithPoolAsLocal.add(unit.toString());
									}
								} else if (receiverNames.contains(className)) {
									synchronized (receiversWithPoolAsLocal) {
										receiversWithPoolAsLocal.add(unit.toString());
									}
								} else if (providerNames.contains(className)) {
									synchronized (providersWithPoolAsLocal) {
										providersWithPoolAsLocal.add(unit.toString());
									}
								} else {
									synchronized (othersWithPoolAsLocal) {
										othersWithPoolAsLocal.add(unit.toString());
									}
								}
							}
						}

					}
				}
			}
		});
		p1.add(t1);
		Main.v().autoSetOptions();
		try {
			Scene.v().loadNecessaryClasses();
			PackManager.v().runPacks();
		} catch (Exception e) {
			e.printStackTrace();
		}
		Set<SootClass> classes = new HashSet<>(Scene.v().getApplicationClasses());
		Set<String> activitiesWithPoolAsField = new HashSet<>();
		Set<String> servicesWithPoolAsField = new HashSet<>();
		Set<String> receiversWithPoolAsField = new HashSet<>();
		Set<String> providersWithPoolAsField = new HashSet<>();
		Set<String> othersWithPoolAsField = new HashSet<>();
		for (SootClass sootClass : classes) {
			for (SootField f : sootClass.getFields()) {
				if (AsyncInherit.isInheritedFromExecutorService(f.getType())) {
					if (activityNames.contains(sootClass.getName())) {
						activitiesWithPoolAsField.add(f.getSignature());
					} else if (serviceNames.contains(sootClass.getName())) {
						servicesWithPoolAsField.add(f.getSignature());
					} else if (receiverNames.contains(sootClass.getName())) {
						receiversWithPoolAsField.add(f.getSignature());
					} else if (providerNames.contains(sootClass.getName())) {
						providersWithPoolAsField.add(f.getSignature());
					} else {
						othersWithPoolAsField.add(f.getSignature());
					}
				}
			}
		}

		print(apkName, "Field", "Activity", activitiesWithPoolAsField);
		print(apkName, "Field", "Service", servicesWithPoolAsField);
		print(apkName, "Field", "Receiver", receiversWithPoolAsField);
		print(apkName, "Field", "Provider", providersWithPoolAsField);
		print(apkName, "Field", "Other", othersWithPoolAsField);

		print(apkName, "Local", "Activity", activitiesWithPoolAsLocal);
		print(apkName, "Local", "Service", servicesWithPoolAsLocal);
		print(apkName, "Local", "Receiver", receiversWithPoolAsLocal);
		print(apkName, "Local", "Provider", providersWithPoolAsLocal);
		print(apkName, "Local", "Other", othersWithPoolAsLocal);
	}

	static void print(String apkName, String pre, String componentType, Collection<String> c) {
		if (c.isEmpty()) {
			return;
		}
		Log.i("aaa##", apkName, "##", pre, "##", componentType, "##", c.size());
		for (String string : c) {
			Log.e("aaa##", apkName, "##", pre, "##", componentType, "##", string);
		}
	}
}
