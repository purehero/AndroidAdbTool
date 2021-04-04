package application;

import java.awt.FileDialog;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.FilenameFilter;

import application.utils.CheckBoxTableCellEx;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableView;
import purehero.adb.AdbManager;
import purehero.adb.AndroidAPP;
import purehero.adb.AndroidDeviceDataIF;
import purehero.utils.Utils;

public class MainSceneHandler {

	private final MainSceneController mainSceneController;
	private final TableView<AndroidDeviceDataIF> deviceListTableView;
	
	public MainSceneHandler(MainSceneController mainSceneController, TableView<AndroidDeviceDataIF> deviceListTableView) {
		this.deviceListTableView = deviceListTableView;
		this.mainSceneController = mainSceneController;
	}

	public void handle(ActionEvent event) {
		Object obj = event.getSource();
		
		if( obj instanceof CheckBox ) 					{ handleCheckBox(( CheckBox ) obj);
		} else if( obj instanceof CheckMenuItem ) 		{ handleCheckMenuItem(( CheckMenuItem ) obj);
		} else if( obj instanceof MenuItem ) 			{ handleMenuItem(( MenuItem ) obj);
		} else if( obj instanceof CheckBoxTableCellEx ) { handleCheckBoxTableCellEx( ( CheckBoxTableCellEx<?, ?> ) obj );
		} else if( obj instanceof Button ) 				{ handleButton(( Button ) obj);
		} else {
			System.out.println( "handle : " + obj.toString());
		}
	}
	
	private void handleButton(Button obj) {
		switch( obj.getId()) {
		case "ID_BTN_COMMAND_ACTION" 	: onHandleButtonCommnadAction(); break;
		case "ID_BTN_KEY_CODE_SEND"		: onHandleButtonKeyCodeSend(); break;
		default : System.out.println( "handleButton : " + obj.toString());
		}
	}

	private void onHandleButtonKeyCodeSend() {
		for( AndroidDeviceDataIF data : deviceListTableView.getItems() ) {
			if( data.getSelected()) {
				Utils.executorService.submit( AdbManager.createPressKeycodeJob( data, mainSceneController.getDeviceKeyCmdChoiceBox().getValue(), tableViewUpdateRunnable));
			}
		}		
	}

	private void onHandleButtonCommnadAction() {
		String key = mainSceneController.getAdbCommandComboBox().getValue();
		switch( key ) {
		case "DEVICE LIST RELOAD" : 
			deviceListTableView.setItems( FXCollections.observableArrayList( mainSceneController.getAdbManager().getAndroidDevices() ) );
			Platform.runLater( tableViewUpdateRunnable );
			break;
		case "ADB KILL-SERVER" 	: Utils.runCommand("adb kill-server"); break;
		case "ADB START-SERVER" : Utils.runCommand("adb start-server"); break;
		default : System.out.println( "onHandleButtonCommnadAction : " + key );
		}
	}

	Runnable tableViewUpdateRunnable = new Runnable() {
		@Override
		public void run() {
			deviceListTableView.refresh();
		}
	};
	
	private void handleMenuItem(MenuItem obj) {
		switch( obj.getId()) {
		case "ID_MENUITEM_APP_INSTALL" 		: onHandleMenuItemAppInstall(); break;
		case "ID_MENUITEM_APP_UNINSTALL" 	: onHandleMenuItemAppUninstall(); break;
		case "ID_MENUITEM_APP_UPDATE" 		: onHandleMenuItemAppUpdate(); break;
		case "ID_MENUITEM_APP_REINSTALL"	: onHandleMenuItemAppReinstall(); break;
		case "ID_MENUITEM_APP_RUNNING"		: onHandleMenuItemAppRunning(); break;
		case "ID_MENUITEM_APP_EXIT"				: onHandleMenuItemAppForceExit(); break;
		case "ID_MENUITEM_APP_REINSTALL_RUN" 	: onHandleMenuItemAppReinstallRun(); break;
		case "ID_MENUITEM_APP_INFO_COPY"		: onHandleMenuItemAppInfoCopy(); break;
		default : System.out.println( "handleMenuItem : " + obj.toString());
		}
	}

	private void onHandleMenuItemAppInfoCopy() {
		AndroidDeviceDataIF data = deviceListTableView.getSelectionModel().getSelectedItem();
		if( data == null ) return;
		
		StringSelection stringSelection = new StringSelection(data.getInfoString());
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(stringSelection, null);
	}
	
	private void onHandleMenuItemAppReinstallRun() {
		File appFile = filedialog("Reinstall & Run APP(APK/AAB) File chooser");
		if( appFile == null ) return ;
		
		AndroidAPP app = new AndroidAPP( appFile );
		for( AndroidDeviceDataIF data : deviceListTableView.getItems() ) {
			if( data.getSelected()) {
				Utils.executorService.submit( AdbManager.createReinstallRunAppJob(data, app, tableViewUpdateRunnable));
			}
		}
	}

	private void onHandleMenuItemAppForceExit() {
		File appFile = filedialog("ForceExit APP(APK/AAB) File chooser");
		if( appFile == null ) return ;
		
		AndroidAPP app = new AndroidAPP( appFile );
		for( AndroidDeviceDataIF data : deviceListTableView.getItems() ) {
			if( data.getSelected()) {
				Utils.executorService.submit( AdbManager.createForceExitAppJob(data, app, tableViewUpdateRunnable));
			}
		}
	}

	private void onHandleMenuItemAppRunning() {
		File appFile = filedialog("Running APP(APK/AAB) File chooser");
		if( appFile == null ) return ;
		
		AndroidAPP app = new AndroidAPP( appFile );
		for( AndroidDeviceDataIF data : deviceListTableView.getItems() ) {
			if( data.getSelected()) {
				Utils.executorService.submit( AdbManager.createRunningAppJob(data, app, tableViewUpdateRunnable));
			}
		}		
	}

	private void onHandleMenuItemAppReinstall() {
		File appFile = filedialog("Reinstall APP(APK/AAB) File chooser");
		if( appFile == null ) return ;
		
		AndroidAPP app = new AndroidAPP( appFile );
		for( AndroidDeviceDataIF data : deviceListTableView.getItems() ) {
			if( data.getSelected()) {
				Utils.executorService.submit( AdbManager.createReinstallAppJob(data, app, tableViewUpdateRunnable));
			}
		}
	}

	private void onHandleMenuItemAppUpdate() {		
		File appFile = filedialog("Update APP(APK/AAB) File chooser");
		if( appFile == null ) return ;
		
		AndroidAPP app = new AndroidAPP( appFile );
		for( AndroidDeviceDataIF data : deviceListTableView.getItems() ) {
			if( data.getSelected()) {
				Utils.executorService.submit( AdbManager.createUpdateAppJob(data, app, tableViewUpdateRunnable));
			}
		}
	}

	private void onHandleMenuItemAppUninstall() {
		File appFile = filedialog("Uninstall APP(APK/AAB) File chooser");
		if( appFile == null ) return ;
		
		AndroidAPP app = new AndroidAPP( appFile );
		for( AndroidDeviceDataIF data : deviceListTableView.getItems() ) {
			if( data.getSelected()) {
				Utils.executorService.submit( AdbManager.createUninstallAppJob(data, app, tableViewUpdateRunnable));
			}
		}
	}

	private void onHandleMenuItemAppInstall() {
		File appFile = filedialog("Install APP(APK/AAB) File chooser");
		if( appFile == null ) return ;
				
		AndroidAPP app = new AndroidAPP( appFile );
		for( AndroidDeviceDataIF data : deviceListTableView.getItems() ) {
			if( data.getSelected()) {
				Utils.executorService.submit( AdbManager.createInstallAppJob(data, app, tableViewUpdateRunnable));
			}
		}
	}

	private void handleCheckBoxTableCellEx(CheckBoxTableCellEx<?, ?> obj) {
		AndroidDeviceDataIF device = deviceListTableView.getItems().get( obj.getIndex());
		device.setSelected( !device.getSelected());		
	}

	private void handleCheckMenuItem(CheckMenuItem obj) {
		switch( obj.getId()) {
		default : System.out.println( "handleCheckMenuItem : " + obj.toString());
		}
	}

	private void handleCheckBox(CheckBox obj) {
		switch( obj.getId()) {
		case "ID_CHECKBOX_DEVICE_SELECT_ALL" : onHandlerDeviceSelectAll(obj); break;
		default : System.out.println( "handleCheckBox : " + obj.toString());
		}
	}

	/**
	 * 디바이스의 전체 선택/해제 버튼
	 * @param obj checkbox object
	 */
	private void onHandlerDeviceSelectAll(CheckBox obj) {
		boolean bSelected = obj.isSelected();
	
		ObservableList<AndroidDeviceDataIF> devices = deviceListTableView.getItems();
		for( AndroidDeviceDataIF device : devices ) {
			device.setSelected( bSelected );
		}
		
		deviceListTableView.refresh();
	}
	
	/**
	 * 파일 선택 다이얼로그를 표시하여 파일을 선택 받은 후 결과를 반환한다. 
	 * 
	 * @param title 파일 다이얼로그의 타이틀 문구
	 * @return		선택한 파일의 객체, 선택한 파일이 없으면 null 을 반환한다. 
	 */
	private File filedialog(String title) {
		java.awt.FileDialog dialog = new java.awt.FileDialog((java.awt.Frame) null, title, FileDialog.LOAD );
		dialog.setFile("*.apk; *.aab");
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
}
	