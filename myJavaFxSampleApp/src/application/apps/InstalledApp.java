package application.apps;

public class InstalledApp {
	final int index;
	final String path;
	final String packageName;
	boolean selected = false;
	
	public InstalledApp(int index, String line) {
		this.index = index;
		
		int idx = line.indexOf("/");
		line = line.substring( idx + 1 );
		
		idx = line.lastIndexOf("=");
		path = line.substring( 0, idx );
		packageName = line.substring( idx + 1 );
	}

	public boolean 	getSelected() { return selected; }
	public void 	setSelected( boolean s ) { selected = s; }
	
	public int getIndex() { return index; }
	public String getAppName() { return "app name"; }
	public String getPackageName() { return packageName; }
	public String getPath() { return path; }
}
