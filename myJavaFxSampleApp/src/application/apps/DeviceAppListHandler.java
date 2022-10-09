package application.apps;

import java.io.File;

import application.utils.CheckBoxTableCellEx;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableView;
import purehero.adb.AdbManager;
import purehero.adb.AndroidDeviceDataIF;
import purehero.utils.Utils;

public class DeviceAppListHandler {
	final DeviceAppListController controller;
	
	public DeviceAppListHandler( DeviceAppListController controller ) {
		this.controller = controller;
	}

	public void handle(ActionEvent event, AndroidDeviceDataIF device) {
		final Object obj = event.getSource();
		if( obj instanceof Button ) 		 	 { handleButton((Button) obj, device );
		} else if( obj instanceof MenuItem ) 	 { handleMenuItem((MenuItem) obj, device );
		} else if( obj instanceof CheckBox ) 	 { handleCheckBox(( CheckBox ) obj);
		} else if( obj instanceof CheckMenuItem ){ handleCheckMenuItem(( CheckMenuItem ) obj);
		} else if( obj instanceof CheckBoxTableCellEx ) { handleCheckBoxTableCellEx( ( CheckBoxTableCellEx<?, ?> ) obj );
		} else {
			System.out.println( this.getClass() + "::handle : " + obj.toString());
		}
	}

	private void handleCheckBoxTableCellEx(CheckBoxTableCellEx<?, ?> obj) {
		InstalledApp app = controller.getAppListTableView().getItems().get( obj.getIndex());
		app.setSelected( !app.getSelected());		
	}

	private void handleCheckMenuItem(CheckMenuItem obj) {
		switch( obj.getId()) {
		default : System.out.println( this.getClass() + "::handleCheckMenuItem : " + obj.toString()); 
		}
	}

	private void handleCheckBox(CheckBox obj) {
		switch( obj.getId()) {
		case "ID_CHECKBOX_SELECT_ALL" : onCheckBoxClicked( obj ); break;
		default : System.out.println( this.getClass() + "::handleCheckBox : " + obj.toString()); 
		}
	}

	private void onCheckBoxClicked(CheckBox obj) {		
		boolean bSelected = obj.isSelected();
		
		TableView<InstalledApp> tableView = controller.getAppListTableView();
		ObservableList<InstalledApp> apps = tableView.getItems();
		for( InstalledApp app : apps ) {
			app.setSelected( bSelected );
		}
		
		tableView.refresh();
	}

	private void handleButton(Button obj, AndroidDeviceDataIF device ) {
		switch( obj.getId()) {
		default : System.out.println( this.getClass() + "::handleButton : " + obj.toString()); 
		}
	}
	
	private void handleMenuItem(MenuItem obj, AndroidDeviceDataIF device) {
		switch( obj.getId()) {
		case "ID_MENUITEM_APP_UNINSTALL" 	: onClickedAppUninstall( device );  break;
		case "ID_MENUITEM_APP_EXPORT" 		: onClickedAppExport( device );  break;
		default : System.out.println( this.getClass() + "::handleMenuItem : " + obj.toString()); 
		}
	}

	private void onClickedAppExport(AndroidDeviceDataIF device) {
		File exportFolder = Utils.folderDialog( "APK Export folder chooser");
		if( exportFolder == null ) return ;
		
		TableView<InstalledApp> tableView = controller.getAppListTableView();
		for( InstalledApp app : tableView.getItems()) {
			if( app.getSelected()) {
				//System.out.println( app.getPath() + " -> " + new File( exportFolder, app.getPackageName() + ".apk").getAbsolutePath());
				try {
					device.getInstance().runCommand( String.format( "pull %s %s", app.getPath(), new File( exportFolder, app.getPackageName() + ".apk").getAbsolutePath() ));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void onClickedAppUninstall(AndroidDeviceDataIF device) {
		TableView<InstalledApp> tableView = controller.getAppListTableView();
		for( InstalledApp app : tableView.getItems()) {
			if( app.getSelected()) {
				try {
					device.getInstance().runCommand( "uninstall " + app.getPackageName());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		controller.reloadTableViewDatas();
	}
}
