package purehero.adb;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import javafx.application.Platform;
import purehero.utils.Utils;

public class AdbManager {
	List<AndroidDevice> devices = new ArrayList<AndroidDevice>();
	
	public List<AndroidDevice> getAndroidDevices() {
		Future<List<String>> resultLines = Utils.runCommandJob("adb devices");
		try {
			List<AndroidDevice> newDevices = new ArrayList<AndroidDevice>();
			
			int idx = 0;
			for( String line : resultLines.get() ) {
				AndroidDevice device = AndroidDevice.CreateDevice( line );
				if( device != null && device.isValid()) {
					device.setIndex(++idx);
					
					if( newDevices.add( device )) {
						System.out.println( "AdbManager added device : " + device.getModel());
					}
				}
			}
			
			devices.clear();
			devices.addAll( newDevices );
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return devices;
	}
	
	public void printAndroidDevices() {
		for( AndroidDevice device : devices ) {
			device.print();
		}
	}
	
	interface AppJobIF {
		public boolean doWork( AndroidDevice device, AndroidAPP app );
	}
	
	static class AppJobWorker implements Runnable {
		private final String jobName;
		private final AppJobIF workIF;
		private final AndroidDeviceDataIF dData;
		private final AndroidAPP app;
		private final Runnable viewUpdateRunnable;
		
		public AppJobWorker( AndroidDeviceDataIF dData, AndroidAPP app, Runnable viewUpdateRunnable, String jobName, AppJobIF workIF ) {
			this.jobName = jobName;
			this.workIF = workIF;
			this.dData = dData;
			this.app = app;
			this.viewUpdateRunnable = viewUpdateRunnable;
		}
		
		@Override
		public void run() {
			AndroidDevice device = dData.getInstance();
			boolean bResult = false;
			
			try {
				dData.setCommant(String.format("'%s' %s...중", app.getAppName(), jobName ));
				Platform.runLater(viewUpdateRunnable);
				bResult = workIF.doWork(device, app);
				
			} catch (Exception e) {
				bResult = false;
			}
			
			if( bResult ) {
				dData.setCommant(String.format("'%s' %s...완료", app.getAppName(), jobName));
			} else {
				dData.setCommant(String.format("'%s' %s failed...[%s]", app.getAppName(), jobName, device.getFailMessage()));					
			}				
			Platform.runLater(viewUpdateRunnable);
		}
	};
	
	private static File getObbFile( AndroidAPP app ) {
		File obb = null;
		
		String obbFilename = app.getPackageName() + ".obb";
		File folder = new File( app.getAbsolutePath()).getParentFile();
		File files[] = folder.listFiles();
		if( files != null && files.length > 0 ) {
			for( File file : files ) {
				if( file.getName().endsWith( obbFilename )) {
					obb = file;
					break;
				}
			}
		}
		
		return obb;
	}
	
	public static Runnable createInstallAppJob( AndroidDeviceDataIF dData, AndroidAPP app, Runnable viewUpdateRunnable ) {
		return new AppJobWorker( dData, app, viewUpdateRunnable, "Install", new AppJobIF(){
			@Override
			public boolean doWork(AndroidDevice device, AndroidAPP app) {
				try {
					return device.installApp(app, getObbFile(app));
				} catch (Exception e) {
					e.printStackTrace();
				}
				return false;
			}}); 
	}
	
	public static Runnable createUninstallAppJob(AndroidDeviceDataIF dData, AndroidAPP app, Runnable viewUpdateRunnable) {
		return new AppJobWorker( dData, app, viewUpdateRunnable, "Uninstall", new AppJobIF(){
			@Override
			public boolean doWork(AndroidDevice device, AndroidAPP app) {
				try {
					return device.uninstallApp(app);
				} catch (Exception e) {
					e.printStackTrace();
				}
				return false;
			}}); 
	}

	public static Runnable createUpdateAppJob(AndroidDeviceDataIF dData, AndroidAPP app, Runnable viewUpdateRunnable) {
		return new AppJobWorker( dData, app, viewUpdateRunnable, "Update", new AppJobIF(){
			@Override
			public boolean doWork(AndroidDevice device, AndroidAPP app) {
				try {
					return device.updateApp(app);
				} catch (Exception e) {
					e.printStackTrace();
				}
				return false;
			}}); 
	}

	public static Runnable createReinstallAppJob(AndroidDeviceDataIF dData, AndroidAPP app, Runnable viewUpdateRunnable) { 
		return new AppJobWorker( dData, app, viewUpdateRunnable, "Reinstall", new AppJobIF(){
			@Override
			public boolean doWork(AndroidDevice device, AndroidAPP app) {
				try {
					return device.reinstallApp(app, getObbFile(app));
				} catch (Exception e) {
					e.printStackTrace();
				}
				return false;
			}});
	}

	public static Runnable createRunningAppJob(AndroidDeviceDataIF dData, AndroidAPP app, Runnable viewUpdateRunnable) {
		return new AppJobWorker( dData, app, viewUpdateRunnable, "Running", new AppJobIF(){
			@Override
			public boolean doWork(AndroidDevice device, AndroidAPP app) {
				try {
					device.runCommand( String.format( "shell am start -a android.intent.action.MAIN -n %s/%s", app.getPackageName(), app.getLauncherActivityName()));
					return !device.isFailed();
				} catch (Exception e) {
					e.printStackTrace();
				}
				return false;
			}});
	}

	public static Runnable createForceExitAppJob(AndroidDeviceDataIF dData, AndroidAPP app, Runnable viewUpdateRunnable) {
		return new AppJobWorker( dData, app, viewUpdateRunnable, "Exit", new AppJobIF(){
			@Override
			public boolean doWork(AndroidDevice device, AndroidAPP app) {
				try {
					device.runCommand( String.format( "shell am force-stop %s", app.getPackageName()));
					device.runCommand( String.format( "shell am kill %s", app.getPackageName()));
					return !device.isFailed();
				} catch (Exception e) {
					e.printStackTrace();
				}
				return false;
			}});
	}

	public static Runnable createReinstallRunAppJob(AndroidDeviceDataIF dData, AndroidAPP app, Runnable viewUpdateRunnable) { 
		return new AppJobWorker( dData, app, viewUpdateRunnable, "Reinstall&Run", new AppJobIF(){
			@Override
			public boolean doWork(AndroidDevice device, AndroidAPP app) {
				try {
					if( !device.reinstallApp(app, getObbFile(app))) return false;
					device.runCommand( String.format( "shell am start -a android.intent.action.MAIN -n %s/%s", app.getPackageName(), app.getLauncherActivityName()));
					return !device.isFailed();
				} catch (Exception e) {
					e.printStackTrace();
				}
				return false;
			}});
	}

	public static Runnable createPressKeycodeJob(AndroidDeviceDataIF dData, String keycode, Runnable viewUpdateRunnable) { 
		return new Runnable() {

			@Override
			public void run() {
				try {
					dData.getInstance().runCommand( String.format( "shell input keyevent KEYCODE_%s", keycode.toUpperCase() ));
					dData.setCommant( String.format( "'%s' 버튼 클릭", keycode ));
				} catch (Exception e) {
					e.printStackTrace();
					dData.setCommant( String.format( "'%s' 버튼 클릭 오류", keycode ));
				}
				
				Platform.runLater(viewUpdateRunnable);
			}};
	}
}
