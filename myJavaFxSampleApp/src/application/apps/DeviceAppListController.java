package application.apps;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import application.utils.TableViewUtils;
import javafx.animation.RotateTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableView;
import purehero.adb.AndroidDeviceDataIF;

public class DeviceAppListController implements Initializable, EventHandler<ActionEvent> {
	
	@FXML
	private TableView<InstalledApp> appListTableView;
	
	private DeviceAppListHandler handler = null;
	private AndroidDeviceDataIF device = null;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		handler = new DeviceAppListHandler( this );

		int idxCol = 0;
		final String CENTER = "CENTER";
		TableViewUtils.CheckBoxTableColumn	( appListTableView, "", 			"selected", 	CENTER,  30,  30, idxCol++, DeviceAppListController.this );		// check box
		//TableViewUtils.IntegerTableColumn	( appListTableView, "No", 			"index", 		CENTER,  35,  40, idxCol++ );		// No.
		//TableViewUtils.StringTableColumn	( appListTableView, "AppName",		"appName", 		CENTER, 230, 300, idxCol++ );		// 앱 이름
		TableViewUtils.StringTableColumn	( appListTableView, "PackageName", 	"packageName", 	CENTER, 250, 400, idxCol++ );		// 패키지명
		TableViewUtils.StringTableColumn	( appListTableView, "path", 		"path", 		CENTER, 300, 600, idxCol++ );		// 설치 경로
		
		appListTableView.setSelectionModel(null);
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
		 reloadTableViewDatas();
	}

	public void reloadTableViewDatas() {
		List<InstalledApp> installedApps = getInstallApps();
		 ObservableList<InstalledApp> deviceInfoData = FXCollections.observableArrayList( installedApps );		
		 appListTableView.setItems( deviceInfoData );
	}
	
	private List<InstalledApp> getInstallApps() {
		List<InstalledApp> ret = new ArrayList<InstalledApp>();
		try {
			int index = 0;
			List<String> result = device.getInstance().runCommand( "shell pm list package -f -3" );
			for( String line : result ) {
				ret.add( new InstalledApp( index++, line ));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return ret;
	}

	public TableView<InstalledApp> getAppListTableView() { return appListTableView; }
}
