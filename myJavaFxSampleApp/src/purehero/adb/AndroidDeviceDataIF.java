package purehero.adb;

import java.awt.Dimension;

public interface AndroidDeviceDataIF {
	public Boolean 	getSelected();
	public void 	setSelected( boolean bSelected );
	
	public Integer getIndex();
	public String getName();
	public String getModel();
	public String getOsVersion();
	public String getBatteryLevel();
	public String getBatteryTemperature();
	public String getState();
	public String getAndroidID();
	public String getSerialNumber();
	public Dimension getScreenSize();
	
	public String 	getCommant();
	public void		setCommant( String msg );
	
	public AndroidDevice getInstance();
	public String getInfoString();
}
