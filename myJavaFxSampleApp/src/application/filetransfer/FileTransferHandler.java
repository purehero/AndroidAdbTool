package application.filetransfer;

import java.io.File;
import java.util.Optional;

import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.MenuItem;
import purehero.adb.AdbManager;
import purehero.adb.AndroidDeviceDataIF;
import purehero.utils.Utils;

public class FileTransferHandler {
	final FileTransferController controller;
	
	public FileTransferHandler( FileTransferController controller ) {
		this.controller = controller;
	}

	public void handle(ActionEvent event, AndroidDeviceDataIF device) {
		final Object obj = event.getSource();
		if( obj instanceof Button ) { 
			handleButton((Button) obj, device );
			
		} else if( obj instanceof MenuItem ) {
			handleMenuItem((MenuItem) obj, device );
			
		} else {
			System.out.println( "FileTransferHandler::handle : " + obj.toString());
		}
	}

	private void handleMenuItem(MenuItem obj, AndroidDeviceDataIF device) {
		Utils.executorService.submit( AdbManager.createPressKeycodeJob( device, obj.getText(), null ));
		switch( obj.getId()) {
		default : System.out.println( "FileTransferHandler::handleMenuItem : " + obj.toString()); 
		}
	}

	private void handleButton(Button obj, AndroidDeviceDataIF device ) {
		switch( obj.getId()) {
		case "ID_BTN_PUSH" : onClickPushButton(); break;
		case "ID_BTN_PULL" : onClickPullButton(); break;
		default : System.out.println( "FileTransferHandler::handleButton : " + obj.toString()); 
		}
	}

	private void onClickPullButton() {
		controller.pullFile();		
	}

	private void onClickPushButton() {
		controller.pushFile();
	}
}
