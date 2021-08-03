package application;
	
import java.util.concurrent.Executors;

import javax.swing.UIManager;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import purehero.utils.Utils;

public class Main extends Application {
	
	@Override
	public void start(Stage primaryStage) {
		
		try {
			Parent root = FXMLLoader.load(getClass().getResource("MainScene.fxml"));
			Scene scene = new Scene(root);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.setTitle("Purehero Android Device Tool(v1.0)");
			primaryStage.setMinWidth(800);
			primaryStage.setMaxHeight(600);
			primaryStage.show();
			
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		// System.out.println( System.getProperty("java.io.tmpdir"));
		try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) { e.printStackTrace(); }
		//java.awt.FileDialog dialog = new java.awt.FileDialog((java.awt.Frame) null);
		//dialog.setVisible(true);
	}
	
	@Override
	public void init() throws Exception {
		super.init();
		Utils.executorService = Executors.newFixedThreadPool( Runtime.getRuntime().availableProcessors() );	// 고정 개수 생성, worker 가 더 많으면 thread 생성
		//Utils.executorService = Executors.newCachedThreadPool();	// worker가 있으면 thread 생성, 이후 휴면 thread 는 삭제
	}
	
	@Override
	public void stop() throws Exception {
		if( Utils.executorService != null ) {
			Utils.executorService.shutdownNow();
		}
		Utils.executorService = null;		
		super.stop();
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
