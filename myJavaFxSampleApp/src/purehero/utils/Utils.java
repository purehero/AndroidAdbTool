package purehero.utils;

import java.awt.FileDialog;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class Utils {
	
	public static ExecutorService executorService;
	
	/**
	 * @param command
	 * @return
	 */
	public static List<String> runCommand( String command ) { return runCommand( command, null ); }
	public static List<String> runCommand( String command, String grap ) {
		List<String> ret = null;
		
		System.out.println( command );
		try {
			Process process = Runtime.getRuntime().exec( command );
			BufferedReader outReader = new BufferedReader( new InputStreamReader( process.getInputStream(), "UTF-8"));
			BufferedReader errReader = new BufferedReader( new InputStreamReader( process.getErrorStream(), "UTF-8"));
			
			ret = new ArrayList<String>();
			
			String line = null;
			while(( line = outReader.readLine()) != null ) {
				if( line.length() < 1 ) continue;
				if( grap == null ) { ret.add( line );
				} else { 
					if( line.contains( grap )) {
						ret.add( line );
					}
				}
			}
			while(( line = errReader.readLine()) != null ) {
				if( line.length() < 1 ) continue;
				if( grap == null ) { ret.add( line );
				} else { 
					if( line.contains( grap )) {
						ret.add( line );
					}
				}
			}
			
			try {
				process.waitFor();
				
			} catch (InterruptedException e) {
				e.printStackTrace();
				
			} finally {
				process.destroy();
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return ret;
	}
	
	public static Future<List<String>> runCommandJob( String command ) { return runCommandJob( command, null ); } 
	public static Future<List<String>> runCommandJob( String command, String grap ) {
		return executorService.submit( new Callable<List<String>>() {
			@Override
			public List<String> call() throws Exception {
				return Utils.runCommand(command, grap);
			}} );
	}
	
	public static boolean isWindows() {
		return System.getProperty( "os.name" ).contains( "Window" );
	}
	
	public static String getComputerName() {
		Map<String, String> env = System.getenv();
	    if (env.containsKey("COMPUTERNAME")) {
	        return env.get("COMPUTERNAME");
	    } else if (env.containsKey("HOSTNAME")) {
	        return env.get("HOSTNAME");
	    } 
	    
	    return "Unknown Computer";	    
	}
	
	
	public static File folderDialog( String title ) {
		System.setProperty( "apple.awt.fileDialogForDirectories", "true" );
		
		java.awt.FileDialog dialog = new java.awt.FileDialog((java.awt.Frame) null, title, FileDialog.LOAD );
		dialog.setFilenameFilter( new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				return new File( dir, name ).isDirectory();
			}});
		dialog.setFile( "SELECT FOLDER" );
		dialog.setVisible(true);
		
		System.setProperty( "apple.awt.fileDialogForDirectories", "false" );
		
		final String pathname = dialog.getDirectory();
		if( pathname == null ) return null;
		
		return new File( pathname );
	}
	
	public static File fileDialog( String title, String fileFilter, boolean readMode ) {
		java.awt.FileDialog dialog = new java.awt.FileDialog((java.awt.Frame) null, title, readMode ? FileDialog.LOAD : FileDialog.SAVE );
		dialog.setModal(true);
		dialog.setFile( fileFilter );
		dialog.setFilenameFilter(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				name = name.toLowerCase();
				return name.endsWith(".apk") || name.endsWith(".aab");
			}});
		dialog.setVisible(true);
			
			
		final String pathname = dialog.getDirectory();
		final String filename = dialog.getFile();
		dialog.dispose();
		if( filename == null ) return null;
			
		return new File( pathname, filename );
	}
	
	
	public static File getWorkTempFolder() {
		File tmpFolder 		= new File( System.getProperty("java.io.tmpdir"));
		File workTempFolder	= new File( tmpFolder, "AdbConnection" );
		
		if( !workTempFolder.exists()) {
			workTempFolder.mkdirs();
		}
		
		return workTempFolder;
	}
	
	public static void downloadFile(URL url, File outputFile) throws IOException {
        try (InputStream in = url.openStream(); 
            ReadableByteChannel rbc = Channels.newChannel(in);
            FileOutputStream fos = new FileOutputStream(outputFile)) {
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        }
    }
}
