package purehero.adb;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class BundleTool {
	private static final String BUNDLETOOL_RES_NAME = "bundletool-all-1.5.0.jar";
	private static final String KS_RES_NAME 		= "test.jks";
	
	public File getBundletoolFile() { return getResourceFile( BUNDLETOOL_RES_NAME, "bundletool.jar" ); }
	
	private File getResourceFile( String resFilename, String outFilename ) {
		File tmpFolder = new File( System.getProperty("java.io.tmpdir"));
		File bundletoolFile = new File( tmpFolder, outFilename );
		if( bundletoolFile.exists()) return bundletoolFile;
		
		final int bufferSize = 1024*1024;
		
		InputStream is = BundleTool.this.getClass().getClassLoader().getResourceAsStream( resFilename );
		try {
			FileOutputStream fos = new FileOutputStream( bundletoolFile );
			
			int nSize = 0;
			byte buffer[] = new byte[bufferSize];
			while(( nSize = is.read( buffer )) > 0 ) {
				fos.write( buffer, 0, nSize );
			}
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			
		} catch (IOException e) {
			e.printStackTrace();
			
		} finally {
			try { is.close(); } catch (IOException e) {}
		}
		
		return bundletoolFile;
	}

	/**
	 * 키 저장소 파일
	 * @return
	 */
	public File getKeyStoreFile() { return getResourceFile( KS_RES_NAME, "debug.jks" ); }

	/**
	 * 키 저장소의 비밀번호
	 * @return
	 */
	public String getKeyStorePwd() { return "1q2w3e"; }

	/**
	 * 서명하려는 서명 키의 별칭
	 * @return
	 */
	public String getSignAlias() { return "test"; }

	/**
	 * 서명 키의 비밀번호
	 * @return
	 */
	public String getSignPwd() { return "1q2w3e"; }
	
}
