package application;

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
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		int idxCol = 0;
	
		final String CENTER = "CENTER";
		TableViewUtils.CheckBoxTableColumn	( deviceListTableView, "", 			"selected", 	CENTER,  30,  30, idxCol++, MainSceneController.this );		// check box
		TableViewUtils.IntegerTableColumn	( deviceListTableView, "No", 		"index", 		CENTER,  35,  40, idxCol++ );		// No.
		TableViewUtils.StringTableColumn	( deviceListTableView, "�ܸ��� �̸�", 	"name", 		CENTER, 230, 300, idxCol++ );		// ��ġ��
		TableViewUtils.StringTableColumn	( deviceListTableView, "�𵨸�", 		"model", 		CENTER, 130, 250, idxCol++ );		// �𵨸�
		TableViewUtils.StringTableColumn	( deviceListTableView, "����", 		"osVersion", 	CENTER,  50, 100, idxCol++ );		// OS ����
		TableViewUtils.StringTableColumn	( deviceListTableView, "���͸�", 		"batteryLevel", CENTER,  50, 100, idxCol++ );		// ���͸� ����
		TableViewUtils.StringTableColumn	( deviceListTableView, "�������", 	"state", 		CENTER,  50, 100, idxCol++ );		// ���� ����
		TableViewUtils.StringTableColumn	( deviceListTableView, "AndroidID", "androidID", 	CENTER, 150, 200, idxCol++ );		// AndroidID
		TableViewUtils.StringTableColumn	( deviceListTableView, "�޸�", 		"commant", 		CENTER, 325, 450, idxCol );			// ���
	
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
	
}
