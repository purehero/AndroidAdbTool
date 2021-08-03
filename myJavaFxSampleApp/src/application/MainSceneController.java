package application;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import application.utils.TableViewUtils;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import purehero.adb.AdbManager;
import purehero.adb.AndroidDevice;
import purehero.adb.AndroidDeviceDataIF;

public class MainSceneController implements Initializable, EventHandler<ActionEvent> {

	private MainSceneHandler handler = null;
	private AdbManager adbManager = null;
	
	@FXML
	private TableView<AndroidDeviceDataIF> deviceListTableView;
	
	@FXML
	private ChoiceBox<String> deviceKeyCmdChoiceBox;
	
	@FXML
	private ComboBox<String> adbCommandComboBox;
	
	@FXML
	private TextField selectiveFileTextField;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		int idxCol = 0;
	
		final String CENTER = "CENTER";
		TableViewUtils.CheckBoxTableColumn	( deviceListTableView, "", 			"selected", 	CENTER,  30,  30, idxCol++, MainSceneController.this );		// check box
		TableViewUtils.IntegerTableColumn	( deviceListTableView, "No", 		"index", 		CENTER,  35,  40, idxCol++ );		// No.
		TableViewUtils.StringTableColumn	( deviceListTableView, "단말기 이름", 	"name", 		CENTER, 230, 300, idxCol++ );		// 장치명
		TableViewUtils.StringTableColumn	( deviceListTableView, "모델명", 		"model", 		CENTER, 130, 250, idxCol++ );		// 모델명
		TableViewUtils.StringTableColumn	( deviceListTableView, "버전", 		"osVersion", 	CENTER,  50, 100, idxCol++ );		// OS 버전
		TableViewUtils.StringTableColumn	( deviceListTableView, "베터리", 		"batteryLevel", CENTER,  50, 100, idxCol++ );		// 베터리 레벨
		TableViewUtils.StringTableColumn	( deviceListTableView, "연결상태", 	"state", 		CENTER,  50, 100, idxCol++ );		// 연결 상태
		TableViewUtils.StringTableColumn	( deviceListTableView, "AndroidID", "androidID", 	CENTER, 150, 200, idxCol++ );		// AndroidID
		TableViewUtils.StringTableColumn	( deviceListTableView, "메모", 		"commant", 		CENTER, 325, 450, idxCol );			// 비고
	
		adbManager = new AdbManager();
		List<AndroidDevice> devices = adbManager.getAndroidDevices();
		
		ObservableList<AndroidDeviceDataIF> deviceInfoData = FXCollections.observableArrayList( devices );		
		deviceListTableView.setItems( deviceInfoData );
		
		deviceKeyCmdChoiceBox.setItems( FXCollections.observableArrayList( 
				"POWER", "HOME", "MENU", "VOLUME_UP", "VOLUME_DOWN", "BACK" ));
		deviceKeyCmdChoiceBox.setValue("POWER");
		
		adbCommandComboBox.setItems( FXCollections.observableArrayList( 
				"DEVICE LIST RELOAD", "ADB KILL-SERVER", "ADB START-SERVER" ));
		adbCommandComboBox.setValue("DEVICE LIST RELOAD");
		
		handler = new MainSceneHandler( this, deviceListTableView );
	}
	
	public AdbManager getAdbManager() { return adbManager; }
	public ChoiceBox<String> getDeviceKeyCmdChoiceBox() { return deviceKeyCmdChoiceBox; }
	public ComboBox<String> getAdbCommandComboBox() { return adbCommandComboBox; }
	
	@Override
	public void handle(ActionEvent event) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				handler.handle( event );
			}} );
	}
	
	public void setSelectiveFileTextField( File selectiveFile ) {
		if( selectiveFile == null ) {
			selectiveFileTextField.setText("no selected file");
		} else {
			selectiveFileTextField.setText( selectiveFile.getAbsolutePath());
		}
	}
}
