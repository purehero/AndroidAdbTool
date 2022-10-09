package purehero.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Shell {
	Process process = null;
	BufferedReader outReader = null;
	BufferedReader errReader = null;
	BufferedWriter outWriter = null;
	
	public Shell( String command ) {
		try {
			process = Runtime.getRuntime().exec( command );
			outReader = new BufferedReader( new InputStreamReader( process.getInputStream(), "UTF-8"));
			errReader = new BufferedReader( new InputStreamReader( process.getErrorStream(), "UTF-8"));
			outWriter = new BufferedWriter( new OutputStreamWriter( process.getOutputStream(), "UTF=8" ));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public List<String> runCommand( String command, String grap ) { 
		List<String> ret = null;
		
		System.out.println( command );
		try {
			outWriter.write( command );
			
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
				process.waitFor( 500, TimeUnit.MILLISECONDS );
				
			} catch (InterruptedException e) {
				e.printStackTrace();
				
			} finally {	
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return ret;
	}
	
	public void release() {
		try {
			if( errReader != null ) errReader.close();
			errReader = null;
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			if( outReader != null ) outReader.close();
			outReader = null;
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			if( outWriter != null ) outWriter.close();
			outWriter = null;
			
		} catch (IOException e) {
			e.printStackTrace();
		}
				
		process.destroy();
	}
}
