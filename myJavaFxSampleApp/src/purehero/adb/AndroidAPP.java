package purehero.adb;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import purehero.utils.Utils;

public class AndroidAPP {
	final File app;
	String packageName = null;
	String launcherActivityName = null;
	String mainActivityName = null;
	String appName = null;
	
	static File 	previous_app = null;
	static String 	previous_packageName = null;
	static String 	previous_launcherActivityName = null;
	static String 	previous_mainActivityName = null;
	static String 	previous_appName = null;
	
	public AndroidAPP( File app ) {
		if( previous_app != null ) {	// 동일한 파일을 반복적으로 선택하였을 경우 다시 앱 정보를 파싱하지 않아도 되도록 이전 값들을 저장해 놓는다. 
			if( previous_app.getAbsolutePath().compareTo( app.getAbsolutePath()) == 0 ) {
				this.packageName = previous_packageName;
				this.launcherActivityName = previous_launcherActivityName;
				this.mainActivityName = previous_mainActivityName;
				this.appName = previous_appName;
				this.app = app;
				return;
			}
		}
		
		this.app = app;
		getAppInfos();
		
		previous_app = app;
		previous_packageName = packageName;
		previous_launcherActivityName = launcherActivityName;
		previous_appName = appName;
		previous_mainActivityName = mainActivityName;
	}
	
	public void getAppInfos() {
		if( isApk()) {
			parseApkInfos( app.getAbsolutePath() );
		} else if( isAab()){
			parseAabInfos();
		} else if( isApks()) {
			parserApksInfos();
		}
	}
	
	private void parserApksInfos() {
		
	}
	
	private void parseAabInfos() {
		BundleTool bt = new BundleTool();
		File bundletool = bt.getBundletoolFile();
		
		try {
			String result = Utils.runCommandJob( String.format( "java -jar %s dump manifest --bundle=%s", bundletool.getAbsolutePath(), app.getAbsolutePath())).get().toString();
			result = result.substring(1, result.length() - 1 );
						
			DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = docBuilder.parse( new ByteArrayInputStream( result.getBytes("UTF-8")) );
			
			packageName = parseAabPackageName(doc);
			launcherActivityName = parseAabLauncherActivityName(doc);
			appName = parseAabApplicationLabel( bundletool );			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	

	private String parseAabApplicationLabel(File bundletool) throws InterruptedException, ExecutionException {
		String ret = null;
		
		List<String> values = Utils.runCommandJob( String.format( "java -jar %s dump resources --bundle=%s --resource string/app_name --values", bundletool.getAbsolutePath(), app.getAbsolutePath())).get();
		
		boolean bCheckNextLine = false;
		for( String line : values ) {
			if( bCheckNextLine ) {
				int idx = line.indexOf("[STR]");
				if( idx > 0 ) {
					ret = line.substring( idx + "[STR] ".length());
					ret = ret.replace("\"", "").trim();
					break;
				}
				bCheckNextLine = false;
			} else {
				bCheckNextLine = line.endsWith("string/app_name");
			}
		}
		return ret;
	}

	private void parseApkInfos( String apkPath ) {
		try {
			List<String> result = Utils.runCommandJob("aapt2 dump badging " + apkPath).get();
			for( String line : result ) {
				
				int idx = 0;
				
				if( packageName == null ) {
					idx = line.indexOf("package:");
					if( idx != -1 ) {
						packageName = parseApkPackageName( idx, line );
						continue;
					}
				}
								
				idx = line.indexOf("application-label-ko:");
				if( idx != -1 ) {
					appName = line.substring( idx + "application-label-ko:".length()).replace("'", "").replace("\"", "").trim();
				}
				
				if( appName == null ) {					
					idx = line.indexOf("application:");
					if( idx != -1 ) {
						appName = parseApkApplicationLabel( idx, line );
						continue;
					}
				}
								
				if( launcherActivityName == null ) {
					idx = line.indexOf("launchable-activity:");
					if( idx != -1 ) {
						launcherActivityName = parseApkLauncherActivityName( idx, line );
						continue;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private String parseApkApplicationLabel(int idx, String line) {
		idx += "application: label=".length();
		int idx2 = line.indexOf(" ", idx);
		if( idx2 < 0 ) return null;
		
		String ret = line.substring( idx, idx2 );
		ret = ret.replace("'", "").trim();
		
		return ret;
	}

	private String parseApkLauncherActivityName(int idx, String line) {
		// launchable-activity: name='co.fount.litchi.MainActivity'  label='�뙆�슫�듃' icon=''
		idx += "launchable-activity: name=".length();
		
		int idx2 = line.indexOf(" ", idx);
		if( idx2 < 0 ) return null;
		
		String ret = line.substring( idx, idx2 );
		ret = ret.replace("'", "").trim();
		
		return ret;
	}

	private static String parseApkPackageName(int idx, String line) {
		idx += "package: name=".length();

		int idx2 = line.indexOf(" ", idx);
		if( idx2 < 0 ) return null;
		
		String ret = line.substring( idx, idx2 );
		ret = ret.replace("'", "").trim();
		
		return ret;
	}
	
	/**
	 * @param manifest
	 * @return
	 */
	private String parseAabPackageName(Document manifest) {
		NodeList manifestNodes = manifest.getElementsByTagName("manifest");
		Node manifestNode = manifestNodes.item(0);
		Element manifestElmnt = (Element) manifestNode;
		
		return manifestElmnt.getAttribute("package");
	}

	/**
	 * @param manifest
	 * @return
	 */
	private String parseAabLauncherActivityName(Document manifest) {
		NodeList activities = manifest.getElementsByTagName("activity");
		for( int idxActivity = 0; idxActivity < activities.getLength() && launcherActivityName == null; idxActivity++ ) {
			Element activityElmnt = (Element) activities.item(idxActivity);
			
			NodeList intfilters = activityElmnt.getElementsByTagName("intent-filter");
			for( int idxFilter = 0; idxFilter < intfilters.getLength(); idxFilter++ ) {
				Element filterElmnt = (Element) intfilters.item(idxFilter);
			
				boolean hasMainAction = false;
				boolean hasLauncherCategory = false;
				
				try {
					NodeList actions = filterElmnt.getElementsByTagName("action");
					for( int idxAction = 0; idxAction < actions.getLength() && !hasMainAction; idxAction++ ) {
						Element actionElmnt = (Element) actions.item(idxAction);
						hasMainAction = actionElmnt.getAttribute("android:name").toUpperCase().compareTo("ANDROID.INTENT.ACTION.MAIN") == 0;
					}						
				} catch( Exception e) {}
				
				try {
					NodeList categories = filterElmnt.getElementsByTagName("category");
					for( int idxcategory = 0; idxcategory < categories.getLength() && !hasLauncherCategory; idxcategory++ ) {
						Element categoryElmnt = (Element) categories.item(idxcategory);
						hasLauncherCategory = categoryElmnt.getAttribute("android:name").toUpperCase().compareTo("ANDROID.INTENT.CATEGORY.LAUNCHER") == 0;
					}						
				} catch( Exception e) {}

				if( hasMainAction && hasLauncherCategory ) {
					return activityElmnt.getAttribute("android:name");
				}
			}
		}
		
		return null;
	}
	
	public String getPackageName() 	{ return packageName; }
	public String getLauncherActivityName() 	{ return launcherActivityName; }
	
	public String getAbsolutePath() { return app.getAbsolutePath(); }
	public String getFilename() 	{ return app.getName(); }
	public String getAppName() 		{ return appName == null ? app.getName() : appName; }
	
	public boolean isApks(){ return app.getName().toUpperCase().endsWith(".APKS"); } 
	public boolean isAab() { return app.getName().toUpperCase().endsWith(".AAB"); }
	public boolean isApk() { return app.getName().toUpperCase().endsWith(".APK"); }
}
