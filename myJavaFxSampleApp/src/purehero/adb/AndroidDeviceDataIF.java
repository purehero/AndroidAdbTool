package purehero.adb;

public interface AndroidDeviceDataIF {
	public Boolean 	getSelected();
	public void 	setSelected( boolean bSelected );
	
	public Integer getIndex();
	public String getName();
	public String getModel();
	public String getOsVersion();
	public String getBatteryLevel();
	public String getState();
	public String getAndroidID();
	
	public String 	getCommant();
	public void		setCommant( String msg );
	
	public AndroidDevice getInstance();
	public String getInfoString();
}
