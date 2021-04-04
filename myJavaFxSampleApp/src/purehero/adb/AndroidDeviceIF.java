package purehero.adb;

import java.io.File;
import java.util.List;

public interface AndroidDeviceIF {
	List<String> runCommand(String command, String grap ) throws Exception;	
	String getProperties( String key );
	
	String getLastMessage();
	String getFailMessage();
	
	boolean installApp( AndroidAPP app, File obb ) throws Exception;
	boolean uninstallApp( AndroidAPP app ) throws Exception;
	boolean updateApp( AndroidAPP app ) throws Exception;
	boolean reinstallApp(AndroidAPP app, File obb ) throws Exception;
	
	boolean pushFile( File srcFile, String destPath ) throws Exception;
	boolean pullFile( String srcPath, File destFile ) throws Exception;
} 
