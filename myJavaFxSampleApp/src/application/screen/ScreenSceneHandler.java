package application.screen;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;
import purehero.adb.AdbManager;
import purehero.adb.AndroidDeviceDataIF;
import purehero.utils.Utils;

public class ScreenSceneHandler {
	final ScreenSceneController screenSceneController;
	final ImageView screenImageView;
	
	public ScreenSceneHandler(ScreenSceneController screenSceneController, ImageView screenImageView) {
		this.screenSceneController = screenSceneController;
		this.screenImageView = screenImageView;
	}

	public void handle(ActionEvent event, AndroidDeviceDataIF device) {
		final Object obj = event.getSource();
		if( obj instanceof Button ) { 
			handleButton((Button) obj, device );
			
		} else if( obj instanceof MenuItem ) {
			handleMenuItem((MenuItem) obj, device );
			
		} else {
			System.out.println( "ScreenSceneHandler::handle : " + obj.toString());
		}
	}

	private void handleMenuItem(MenuItem obj, AndroidDeviceDataIF device) {
		Utils.executorService.submit( AdbManager.createPressKeycodeJob( device, obj.getText(), null ));
		switch( obj.getId()) {
		default : System.out.println( "ScreenSceneHandler::handleMenuItem : " + obj.toString()); 
		}
	}

	private void handleButton(Button obj, AndroidDeviceDataIF device ) {
		switch( obj.getId()) {
		case "ID_BTN_REFRESH_SCREEN" 		: device.getInstance().screencap( screenImageView ); break;
		case "ID_BTN_AUTO_REFRESH_SCREEN" 	:
			
		default : System.out.println( "ScreenSceneHandler::handleButton : " + obj.toString()); 
		}
	}
}
