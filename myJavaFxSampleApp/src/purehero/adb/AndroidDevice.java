package purehero.adb;

import java.awt.Dimension;
import java.awt.Point;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import purehero.utils.Utils;

public class AndroidDevice implements AndroidDeviceIF, AndroidDeviceDataIF {

	/**
	 *  
	 * @param info : adb devices 명령어로 받은 문자열(-l 옵션을 상관없다) 
	 * @return null : return null when device info parsing error, otherwise return device instance
	 */
	public static AndroidDevice CreateDevice(String info) {
		if( info == null ) return null;
		if( info.startsWith("List of")) return null;
		if( info.startsWith("* daemon")) return null;
		
		info = info.trim();
		if( info.length() < 10 ) return null;	// 대략 한 라인에 10자 이상으로 판단한다. 
		
		String tokens[] = info.split(" ");	// adb devices -l 
		if( tokens.length == 1 ) {			// adb devices
			tokens = info.split("\t");
		}
		return new AndroidDevice( tokens[0].trim() );
	}
	
	private final String serial;
	private Map<String, String> properties = new HashMap<String, String>();
	private String lastMessage = "";
	
	public AndroidDevice(String serial) {
		this.serial = serial;
		try {
			List<String> propLines = runCommand("shell getprop");
			for( String propLine : propLines ) {
				// System.out.println( propLine );
				
				propLine = propLine.replace("[", "").replace("]", "").trim();
				String tokens[] = propLine.split(": ");
				if( tokens.length == 2 ) {
					properties.put( tokens[0], tokens[1] );
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void print() {}
	public boolean isValid() {
		return properties.size() > 0;
	}

	@Override
	public List<String> runCommand(String command, String grap ) throws Exception {
		return Utils.runCommandJob( String.format( "adb -s %s %s", serial, command ), grap ).get();
	}
	public List<String> runCommand(String command ) throws Exception { return runCommand( command, null ); } 

	@Override
	public String getProperties(String key) {
		return properties.get(key);
	}
	
	@Override
	public boolean installApp(AndroidAPP app, File obb) throws Exception {
		if( obb != null && obb.exists()) {
			lastMessage = runCommand("shell mkdir /mnt/sdcard/Android/obb").toString();
			if(isFailed()) return false;
			
			lastMessage = runCommand("shell mkdir /mnt/sdcard/Android/obb/" + app.getPackageName()).toString();
			if(isFailed()) return false;
			
			lastMessage = runCommand("push " + obb.getAbsolutePath() + " /mnt/sdcard/Android/obb/" + app.getPackageName() + "/").toString();
			if(isFailed()) return false;
		}
		
		if( app.isApk()) {
			lastMessage = runCommand("install " + app.getAbsolutePath()).toString();
		} else {
			lastMessage = bundletoolInstallApp( app );
		}
		System.out.println( lastMessage );
		
		return !isFailed();
	}
	
	private String bundletoolInstallApp(AndroidAPP app) {
		BundleTool bt = new BundleTool();
		File bundletool = bt.getBundletoolFile();
		//File ks			= bt.getKeyStoreFile();
		//String ks_pwd	= bt.getKeyStorePwd(); 
		//String ks_alias	= bt.getSignAlias();
		//String key_pwd	= bt.getSignPwd();

		try {
			makeApksFromAabFile( app, bundletool );
			return Utils.runCommandJob( String.format( "java -jar %s install-apks --device-id=%s --apks=%s", bundletool.getAbsolutePath(), serial, app.getAbsolutePath()+".apks")).get().toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	synchronized private void makeApksFromAabFile(AndroidAPP app, File bundletool ) throws InterruptedException, ExecutionException {
		File targetFile = new File( app.getAbsolutePath()+".apks" );
		if( !targetFile.exists()) {
			Utils.runCommandJob( String.format( "java -jar %s build-apks --bundle=%s --local-testing --overwrite --output=%s", bundletool.getAbsolutePath(), app.getAbsolutePath(), targetFile.getAbsolutePath() )).get();
		}
	}

	public boolean isFailed() {
		return lastMessage.contains("Failure");
	}

	@Override
	public boolean uninstallApp(AndroidAPP app) throws Exception {
		lastMessage = runCommand("uninstall " + app.getPackageName()).toString();
		return !isFailed();
	}

	@Override
	public boolean updateApp(AndroidAPP app) throws Exception {
		if( app.isApk()) {
			lastMessage = runCommand("install -r " + app.getAbsolutePath()).toString();
		} else {
			lastMessage = bundletoolUpdateApp( app );
		}
		return !isFailed();				
	}

	private String bundletoolUpdateApp(AndroidAPP app) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean reinstallApp(AndroidAPP app, File obb) throws Exception {
		uninstallApp( app );
		return installApp( app, obb );
	}
	
	@Override
	public boolean pushFile(File srcFile, String destPath) throws Exception {
		lastMessage = runCommand("push " + srcFile.getAbsolutePath() + " " + destPath).toString();
		return !isFailed();
	}

	@Override
	public boolean pullFile(String srcPath, File destFile) throws Exception {
		String cmd = "pull " + srcPath + " " + destFile.getAbsolutePath();
		if( destFile.isDirectory()) {
			cmd = cmd + "/";
		}
		lastMessage = runCommand(cmd).toString();
		return !isFailed();
	}

	@Override
	public String getLastMessage() { return lastMessage; }
	
	@Override
	public String getFailMessage() {
		int idx = lastMessage.indexOf("Failure");
		if( idx > 0 ) {
			return lastMessage.substring( idx + "Failure".length()).replace("[", "").replace("]", "").trim();
		}
		return lastMessage;
	}
	
	// AndroidDeviceDataIF 
	String deviceName = null;
	public String getName() { 
		if( deviceName != null ) { return deviceName; }

		String model = getModel();
		File tmpNameFile = new File( "supported_devices.tmp" );		// 이전 검색 저장 파일에서 장치 이름을 검색한다. 
		if( tmpNameFile.exists()) {
			FileInputStream fis = null;
			try {
				fis = new FileInputStream(tmpNameFile);
				deviceName = searchDeviceName(new BufferedReader( new InputStreamReader( fis, "UTF-8" )), model );
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			if( fis != null ) {
				try { fis.close(); } catch (IOException e) {}
			}
		}
		if( deviceName == null ) {									// 이전 검색 저장 파일에서 장치 이름을 못 찾은 경우
			InputStreamReader isr = null;
			try {
				isr = new InputStreamReader( AndroidDevice.this.getClass().getClassLoader().getResourceAsStream("supported_devices.csv.out"), "UTF-8");
				deviceName = searchDeviceName(new BufferedReader( isr ), model );	// 전체 장치 이름 파일에서 장치 이름을 검색한다. 
				
				if( deviceName != null ) {											// 전체 장치 파일에서 검색된 경우 이전 검색 저장 파일에 해당 내용을 기록한다. 
					BufferedWriter output = new BufferedWriter(new FileWriter(tmpNameFile, true));
					output.write(String.format("%s=%s\r\n", deviceName, model ));
					output.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			if( isr != null ) {
				try { isr.close(); } catch (IOException e) {}
			} 
		}
		
		return deviceName; 
	}

	private String searchDeviceName(BufferedReader br, String model ) throws IOException {
		String findStr1 = "=" + model;		// 모델이 하나인 경우
		String findStr2 = "="+model+",";	// 모델이 여러고 이고 처음에 위치한 경우
		String findStr3 = ","+model+",";	// 모델이 여러개 이고 중간에 위치한 경우
		String findStr4 = ","+model;		// 모델이 여러개 이고 마지막에 위치한 경우
		
		String line;
		while(( line = br.readLine()) != null ) {
			if( line.endsWith( findStr1 ) || line.contains(findStr2) || line.contains(findStr3) || line.endsWith(findStr4) ) {
				int idx = line.indexOf("=");
				return line.substring(0, idx);
			}
		}
		
		return null;
	}

	@Override
	public String getModel() { return properties.get("ro.product.model"); }

	@Override
	public String getOsVersion() { return properties.get("ro.build.version.release"); }

	String battery = "NONE";
	long nBatteryCheckedTime = 0;
	public String getBatteryLevel() {				  
		if( System.currentTimeMillis() - nBatteryCheckedTime < 10000 ) {	// 자주 호출되는 것을 방지하기 이해 10 초 이내의 정보는 이전것을 사용하도록 한다.  
			return battery; 
		}
		
		try {
			for( String line : runCommand("shell dumpsys battery", "level")) {
				battery = line.replace( "level:", "").trim();
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();			
		}
		nBatteryCheckedTime = System.currentTimeMillis();
		return battery;
	}

	private boolean selected = false;
	public Boolean 	getSelected() { return selected; }
	public void 	setSelected(boolean bSelected) { selected = bSelected; }

	private int index;
	public Integer 	getIndex() { return index; }
	public void 	setIndex( int idx ) { index = idx; }

	@Override
	public String getState() { return "ONLINE"; }

	@Override
	public String getAndroidID() { 
		try {
			if( androidID == null ) {
				androidID = runCommand("shell settings get secure android_id").get(0);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return androidID;
	}
	private String androidID = null;
	
	private String commant;
	public String getCommant() { return commant; }
	public void setCommant(String msg) { commant = msg; }

	@Override
	public AndroidDevice getInstance() { return this; }

	@Override
	public String getInfoString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getIndex());sb.append(" "); 
		sb.append(getName());sb.append(" ");
		sb.append(getModel());sb.append(" ");
		sb.append(getOsVersion());sb.append(" ");
		sb.append(getBatteryLevel());sb.append(" ");
		sb.append(getState());sb.append(" ");
		sb.append(getAndroidID());sb.append(" ");
		sb.append(getCommant());sb.append(" ");
		return sb.toString();
	}

	int screencapModiType = -1;
	class ScreencapThread extends Thread implements Runnable {
		Process process;
		ImageView imageView;
		File tmpScreenCapFile = null;
		
		public ScreencapThread( AndroidDevice device, ImageView screenImageView ) {
			try {
				imageView = screenImageView;
				
				if( screencapModiType == -1 ) {
					process = Runtime.getRuntime().exec( String.format( "adb -s %s shell screencap -p /sdcard/screen.png", serial ) );
					process.waitFor();
					
					tmpScreenCapFile = File.createTempFile( "tmp", ".png" );
					process = Runtime.getRuntime().exec( String.format( "adb -s %s pull /sdcard/screen.png %s", serial, tmpScreenCapFile.getAbsolutePath() ) );
					process.waitFor();
				}
				
				process = Runtime.getRuntime().exec( String.format( "adb -s %s shell screencap -p", serial ) );
				start();
				process.waitFor();
				
			} catch (IOException e) {			
				e.printStackTrace();
				
			} catch (InterruptedException e) {
				e.printStackTrace();
				
			} finally {
				if( process != null ) {
					process.destroy();
				}
			}
		}
		
		@Override
		public void run() {
			try {
				InputStream is = process.getInputStream();
				
				ByteArrayOutputStream bos = new ByteArrayOutputStream(); 
				
				final int BUFFER_LEN = 1024 * 100;	// 100K
				byte buffer[] = new byte[ BUFFER_LEN ];
				int nRead = 0;
				
				while(( nRead = is.read( buffer, 0, BUFFER_LEN )) > 0 ) {
					bos.write( buffer, 0, nRead );
				}

				byte result [] = bos.toByteArray();
				bos.close();
								
				if( screencapModiType == -1 && ( tmpScreenCapFile != null && tmpScreenCapFile.exists())) {
					 FileInputStream fis = new FileInputStream( tmpScreenCapFile );
					 nRead = fis.read( buffer, 0, BUFFER_LEN );
					 fis.close();
					 
					 for( int i = 0; i < nRead; i++ ) {
						 if( result[i] != '\r' && result[i] != '\n' ) continue;
						 
						 if( buffer[i] != result[i] ) {
							if(( buffer[i] == '\n' && buffer[i+1] != '\r' ) && ( result[i] == '\r' && result[i+1] == '\n' )) {
								// \n -> \r\n 이 되는 경우
								screencapModiType = 1;
								break;
							} else if(( buffer[i] == '\n' && buffer[i+1] != '\r' ) && ( result[i] == '\r' && result[i+1] == '\r' && result[i+2] == '\n')) {
								// \n -> \r\r\n 이 되는 경우
								screencapModiType = 2;
								break;
							}
							
							//System.out.println ( String.format( "imgBuf %d => %x %x %x", i, buffer[i], buffer[i+1], buffer[i+2] ));
							//System.out.println ( String.format( "cmdBuf %d => %x %x %x", i, result[i], result[i+1], result[i+2] ));
						 }
					 }
					 fis = new FileInputStream( tmpScreenCapFile );
					 Image image = new Image( fis );
					 fis.close();
					 
					 imageView.setPreserveRatio(true);
					 imageView.setImage( image );
					 
					 tmpScreenCapFile.delete();
				}
				
				// result array 에서 0x0D, 0x0D, 0x0A 값을 0x0A 로 치환해 준다. 
				bos = new ByteArrayOutputStream();
				for( int i = 0; i < result.length - 3; i++ ) {
					if( screencapModiType == 1 ) { // \n -> \r\n 이 되는 경우
						if( result[i] == '\r' && result[i+1] == '\n' ) {
							//System.out.println ( String.format( "%d => %x %x %x", i, result[i], result[i+1], result[i+2] ));
							bos.write( '\n' ); i+=1;
						} else {
							bos.write( result[i] );
						}
					} else if( screencapModiType == 2 ) { // \n -> \r\r\n 이 되는 경우
						if( result[i] == '\r' && result[i+1] == '\r' && result[i+2] == '\n' ) {
							//System.out.println ( String.format( "%d => %x %x %x", i, result[i], result[i+1], result[i+2] ));
							bos.write( '\n' ); i+=2;
						} else {
							bos.write( result[i] );
						}
					} else {
						bos.write( result[i] );
					}
				}
				
				byte finalResult [] = bos.toByteArray();

				Image image = new Image( new ByteArrayInputStream( finalResult ));
				imageView.setImage( image );
				
				
			} catch (IOException e) {
				e.printStackTrace();
			}				
		}
	};
		
	public void screencap(ImageView screenImageView) {
		new ScreencapThread( this, screenImageView );
	}

	public void swipe( Point p1, Point p2, long duration ) { swipe( p1.x, p1.y, p2.x, p2.y, duration ); }
	public void swipe( int x1, int y1, int x2, int y2, long duration ) {
		try {
			runCommand( String.format( "shell input swipe %d %d %d %d %d", x1, y1, x2, y2, duration ));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void touch(Point point ) { touch( point.x, point.y ); }
	public void touch( int x, int y) {
		try {
			runCommand( String.format( "shell input tap %d %d", x, y ));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public String getSerialNumber() {
		return serial;
	}

	@Override
	public Dimension getScreenSize() {
		try {
			for( String line : runCommand("shell wm size")) {
				if( line.startsWith("Physical size:")) {
					String token[] = line.split(" ");
					if( token.length > 2 ) {
						int idx = token[2].indexOf("x");
						if( idx > 0 ) {
							return new Dimension( Integer.valueOf( token[2].substring(0,idx)), Integer.valueOf( token[2].substring(idx+1)));
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();			
		}
		return null;
	}
}
