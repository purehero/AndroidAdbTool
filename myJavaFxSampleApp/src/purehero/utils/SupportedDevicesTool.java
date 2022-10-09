package purehero.utils;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;

import org.apache.commons.io.FileUtils;

public class SupportedDevicesTool {

	public static void main(String[] args) { 
		try {
			File csv = new File( "D:\\workTemp\\supported_devices.csv" );
			new SupportedDevicesTool().run( csv );
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
	
	private void run( File csv ) throws Exception 
	{
		FileInputStream fis = new FileInputStream(csv);
		BufferedReader br = new BufferedReader( new InputStreamReader( fis, "UTF-8" ));
		
		File out = new File( csv.getParentFile(), csv.getName() + ".out" );
		FileOutputStream fos = new FileOutputStream(out);
		BufferedOutputStream bos = new BufferedOutputStream( fos );
		
		String lastKey = null;
		String value = null;
		
		br.readLine();
		
		String line;
		while(( line = br.readLine()) != null ) {
			String tokens[] = line.split(",");
			if( tokens.length != 4 ) continue;
			
			String key = tokens[0] + " " + tokens[1];
			if( key.trim().length() < 2 ) continue;
			
			if( lastKey == null ) {
				lastKey = key;
				value = tokens[3];
				
			} else {
				if( key.compareTo(lastKey) == 0 ) {
					value = value == null ? tokens[3] : value + "," + tokens[3]; 
				} else {
					bos.write(lastKey.getBytes("UTF-8"));
					bos.write("=".getBytes("UTF-8"));
					bos.write(value.getBytes("UTF-8"));
					bos.write("\r\n".getBytes("UTF-8"));
					
					lastKey = key;
					value = tokens[3];
				}
			}
		}
		
		bos.close();
		br.close();
	}

	public static final String DOWNLOAD_URL = "https://storage.googleapis.com/play_public/supported_devices.csv";
	public static void updateDataFromURL() {
		File downloadFile = new File( Utils.getWorkTempFolder(), "supported_devices.csv" );
		
		try {
			Utils.downloadFile( new URL(DOWNLOAD_URL), downloadFile );
			FileUtils.writeLines( downloadFile, "UTF-8", FileUtils.readLines( downloadFile, Charset.forName("UTF-16LE")));
			
			new SupportedDevicesTool().run( downloadFile );
		} catch (MalformedURLException e) {
			e.printStackTrace();
			
		} catch (IOException e) {
			e.printStackTrace();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
}
