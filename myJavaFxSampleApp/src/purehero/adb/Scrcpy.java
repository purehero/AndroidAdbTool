package purehero.adb;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Scrcpy {
	public File getFile() {
		File tmpFolder 		= new File( System.getProperty("java.io.tmpdir"));
		File scrcpyFolder 	= new File( tmpFolder, "SCRCPY-FOLDER" );
		File scrcpyFile  	= new File( scrcpyFolder, "scrcpy.exe" );
		
		if( scrcpyFile.exists()) return scrcpyFile;
		
		final int bufferSize = 1024*1024;
		byte buffer [] = new byte[ bufferSize ];
		
		try ( InputStream is = Scrcpy.this.getClass().getClassLoader().getResourceAsStream( "scrcpy-win64-v1.17.zip" )) {
			
			ZipEntry zipEntry = null;
			ZipInputStream zis = new ZipInputStream(is);
			
			while(( zipEntry = zis.getNextEntry()) != null ) {
				String filename = zipEntry.getName();
				File file = new File( scrcpyFolder, filename );
				if( zipEntry.isDirectory()) {
					file.mkdir();
				} else {
					File parent = file.getParentFile();
					if( !parent.exists()) {
						parent.mkdirs();
					}
					
					try( FileOutputStream fos = new FileOutputStream( file )) {
						int nRead = 0;
						while(( nRead = zis.read( buffer, 0, bufferSize )) > 0 ) {
							fos.write( buffer, 0, nRead );
						}
					}					
				}
			}
			
		} catch (IOException e) {			
			e.printStackTrace();
		}
		
		if( scrcpyFile.exists()) return scrcpyFile;
		return null;
	}
}
