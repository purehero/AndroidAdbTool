package application.filetransfer;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import javafx.scene.control.TreeView;
import javafx.scene.control.Alert.AlertType;
import purehero.adb.AndroidDeviceDataIF;

public class FileTransferController implements Initializable, EventHandler<ActionEvent> {
	final static String ID_DISK_TREECELL 	= "id_disk_treecell";
	final static String ID_FOLDER_TREECELL 	= "id_folder_treecell";
	
	@FXML
	private TreeView<String> localDirectoryTreeView;
		
	@FXML
	private ListView<String> localFileListView;
	
	@FXML
	private TreeView<String> deviceDirectoryTreeView;
		
	@FXML
	private ListView<String> deviceFileListView;
	
	@FXML
	private Button ID_BTN_PUSH;
	
	@FXML
	private Button ID_BTN_PULL;
	
	
	private FileTransferHandler handler = null;
	private AndroidDeviceDataIF device = null;
	
	private DesktopFileSystem desktopFileSystem = new DesktopFileSystem(); 
	private DeviceFileSystem  deviceFileSystem	= new DeviceFileSystem();	
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		handler = new FileTransferHandler( this );
		
		desktopFileSystem.init( localDirectoryTreeView, localFileListView );
		deviceFileSystem.init( deviceDirectoryTreeView, deviceFileListView );
		
		ID_BTN_PUSH.setDisable( false );
		ID_BTN_PULL.setDisable( false );
	}

	@Override
	public void handle(ActionEvent event) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				handler.handle( event, device );
			}} );		
	}

	public void setDevice(AndroidDeviceDataIF device) {
		 this.device = device;
	
		 deviceFileSystem.setDevice( device );		 
	}
	
	public void pushFile() {
		File selectedDesktopFile 	= desktopFileSystem.getSelectedFile();
		if( selectedDesktopFile == null ) {
			Alert a = new Alert( AlertType.ERROR );
			a.setContentText( "DeskTop 파일 선택이 되어 있지 않습니다. 확인 후 다시 시도해 주세요" );
			a.show();
			return;
		}
		
		String selectedDeviceFolder 	= deviceFileSystem.getSelectedFolder();
		if( selectedDeviceFolder == null ) {
			Alert a = new Alert( AlertType.ERROR );
			a.setContentText( "Device 의 폴더 선택이 되어 있지 않습니다. 확인 후 다시 시도해 주세요" );
			a.show();
			return;
		}
		
		Alert a = new Alert( AlertType.CONFIRMATION );
		a.setContentText( String.format( "adb push %s %s", selectedDesktopFile.getAbsolutePath(), selectedDeviceFolder ));
		
		Optional<ButtonType> result = a.showAndWait();
		if( result.get() == ButtonType.OK ) {
			try {
				device.getInstance().pushFile( selectedDesktopFile, selectedDeviceFolder );
			} catch (Exception e) {
				e.printStackTrace();
			}
			deviceFileSystem.refreshFileList();
		} else {
			System.out.println( "Cancel clicked" );
		}
	}

	public void pullFile() {
		String selectedDeviceFile 	= deviceFileSystem.getSelectedFile();
		if( selectedDeviceFile == null ) {
			Alert a = new Alert( AlertType.ERROR );
			a.setContentText( "Device 의 파일 선택이 되어 있지 않습니다. 확인 후 다시 시도해 주세요" );
			a.show();
			return;
		}
		
		File selectedDesktopFolder 	= desktopFileSystem.getSelectedFolder();
		if( selectedDesktopFolder == null ) {
			Alert a = new Alert( AlertType.ERROR );
			a.setContentText( "DeskTop 폴더 선택이 되어 있지 않습니다. 확인 후 다시 시도해 주세요" );
			a.show();
			return;
		}
		
		Alert a = new Alert( AlertType.CONFIRMATION );
		a.setContentText( String.format( "adb pull %s %s", selectedDeviceFile, selectedDesktopFolder.getAbsolutePath() ));
		
		Optional<ButtonType> result = a.showAndWait();
		if( result.get() == ButtonType.OK ) {
			try {
				device.getInstance().pullFile( selectedDeviceFile, selectedDesktopFolder );
			} catch (Exception e) {
				e.printStackTrace();
			}
			desktopFileSystem.refreshFileList();
		} else {
			System.out.println( "Cancel clicked" );
		}
	};
}
