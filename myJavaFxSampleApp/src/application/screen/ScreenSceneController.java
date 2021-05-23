package application.screen;

import java.awt.Dimension;
import java.awt.Point;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import purehero.adb.AndroidDeviceDataIF;

public class ScreenSceneController implements Initializable, EventHandler<ActionEvent> {

	@FXML
	private ImageView screenImageView;
	
	@FXML
	private Label txtMousePosition;
	
	private ScreenSceneHandler handler = null;
	private AndroidDeviceDataIF device = null;
	private Dimension screenSize = null;
	private ContextMenu contextMenu = new ContextMenu();
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		handler = new ScreenSceneHandler( this, screenImageView );
		
		screenImageView.addEventHandler( MouseEvent.MOUSE_MOVED, mouseMovedEvent );
		screenImageView.addEventHandler( MouseEvent.MOUSE_CLICKED, mouseClickedEvent );
		screenImageView.addEventHandler( MouseEvent.MOUSE_RELEASED, mouseReleasedEvent );
		screenImageView.addEventHandler( MouseEvent.MOUSE_PRESSED, mousePressedEvent );
		screenImageView.addEventHandler( MouseEvent.MOUSE_DRAGGED, mouseDraggedEvent );
		
		String contextMenuStrings[] = { "BACK", "HOME", "MENU", "VOLUME_UP", "VOLUME_DOWN", "POWER"  }; 
		for( String menuString : contextMenuStrings ) {
			MenuItem menu = new MenuItem( menuString );
			menu.setId( "MENUITEM_ID_" + menuString );
			menu.setOnAction( this );
			
			contextMenu.getItems().add( menu );
		}
		contextMenu.setHideOnEscape(true);
		contextMenu.setAutoHide(true);
	    screenImageView.setOnContextMenuRequested(e -> contextMenu.show( screenImageView, e.getScreenX(), e.getScreenY()));
	}

	public void setDevice(AndroidDeviceDataIF device) {
		this.device = device;
		
		if( !bRunningFlag ) {
			bRunningFlag = true;
			new Thread( screenCaptionRunnable ).start();
		}
		
		screenSize = device.getScreenSize();
		System.out.println( screenSize.toString());
	}
	
	@Override
	public void handle(ActionEvent event) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				handler.handle( event, device );
			}} );
	}

	public void terminate() {
		bRunningFlag = false;
	}
	
	boolean bRunningFlag = false;
	Runnable screenCaptionRunnable = new Runnable() {
		@Override
		public void run() {
			while( bRunningFlag ) {
				device.getInstance().screencap( screenImageView );
				try {
					Thread.sleep( 250 );
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			System.out.println("STOPED screenCaptionRunnable");
		}};
		
	long mPressedTime = -1;
	Point mPressedPoint;
	EventHandler<MouseEvent> mousePressedEvent = new EventHandler<MouseEvent>() {
		@Override
		public void handle(MouseEvent event) {
			// System.out.println("Mouse pressed");
			if( event.getButton() == MouseButton.PRIMARY ) {
				contextMenu.hide();
				
				mPressedTime = System.currentTimeMillis();
				mPressedPoint = getMousePosToScreenPoint( event, screenImageView );
			}
		}
	};
	
	long mSwipedTime = -1;
	EventHandler<MouseEvent> mouseReleasedEvent  = new EventHandler<MouseEvent>() {
		@Override
		public void handle(MouseEvent event) {
			// System.out.println("Mouse released");
			if( event.getButton() == MouseButton.PRIMARY ) {
				if( mPressedTime != -1 && mDraggedTime != -1 && mPressedTime < mDraggedTime ) {
					mSwipedTime = System.currentTimeMillis();
						
					Point releasedPoint = getMousePosToScreenPoint( event, screenImageView );
					device.getInstance().swipe( mPressedPoint, releasedPoint, mSwipedTime - mPressedTime );
				}				
			}
			
			mPressedTime = -1;
			mDraggedTime = -1;						
		}
	};
	
	
	long mDraggedTime = -1;
	EventHandler<MouseEvent> mouseDraggedEvent  = new EventHandler<MouseEvent>() {
		@Override
		public void handle(MouseEvent event) {
			// System.out.println("Mouse dragged");
			if( event.getButton() == MouseButton.PRIMARY ) {
				mDraggedTime = System.currentTimeMillis();
			}
		}
	};
	
	
	EventHandler<MouseEvent> mouseClickedEvent = new EventHandler<MouseEvent>() {
		@Override
		public void handle(MouseEvent event) {
			if( event.getButton() == MouseButton.PRIMARY ) {
				// System.out.println("Left mouse clicked");
				
				long mClickedTime = System.currentTimeMillis();
				if( mSwipedTime != -1 && mClickedTime - mSwipedTime > 2000 ) {	// swipe 가 끝나고 500ms 이내에 clicked 이벤트는 무시하도록 한다. 
					device.getInstance().touch( getMousePosToScreenPoint( event, screenImageView ) );
				}
				
			} else if( event.getButton() == MouseButton.SECONDARY ) {
				// System.out.println("Right mouse clicked");
			}
		}
	}; 
		
	EventHandler<MouseEvent> mouseMovedEvent = new EventHandler<MouseEvent>() {
		@Override
		public void handle(MouseEvent event) {
			Point point = getMousePosToScreenPoint( event, screenImageView );
			txtMousePosition.setText( String.format( "%d, %d", point.x, point.y ));
		}
	};
		
	private Point getMousePosToScreenPoint(MouseEvent event, ImageView imageView) {
		Image img = screenImageView.getImage();
		
		double aspectRatio = img.getWidth() / img.getHeight();
		double realWidth = Math.min(imageView.getFitWidth(), imageView.getFitHeight() * aspectRatio);
		double realHeight = Math.min(imageView.getFitHeight(), imageView.getFitWidth() / aspectRatio);
		
		double x = img.getWidth() / realWidth;
		double y = img.getHeight() / realHeight; 	
		
		return new Point((int)( event.getX() * x), (int)( event.getY() * y ));
	}
}
