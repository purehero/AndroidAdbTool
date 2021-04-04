package purehero.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
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
}
