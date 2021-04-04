package purehero.adb;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import purehero.utils.Utils;

public class AndroidDeviceTest {

	public static void main(String[] args) {
		Utils.executorService = Executors.newFixedThreadPool( Runtime.getRuntime().availableProcessors() );
		
		AdbManager mng = new AdbManager();
		List<AndroidDevice> devices = mng.getAndroidDevices();
		AndroidAPP app = new AndroidAPP( new File("D:\\workTemp\\site.cowcow.apk"));
		
		for( AndroidDevice device : devices ) {
			try {
				if( !device.installApp( app, null )) {
					System.err.println( "\t" + device.getLastMessage());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		for( AndroidDevice device : devices ) {
			try {
				if( !device.uninstallApp( app )) {
					System.err.println( "\t" + device.getLastMessage());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		Utils.executorService.shutdown();
	}
}
