package application.filetransfer;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import javax.swing.filechooser.FileSystemView;

import application.filetransfer.DesktopFileSystem.AttachmentListCell;
import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Callback;
import purehero.adb.AndroidDeviceDataIF;

public class DeviceFileSystem {
	final static int TYPE_DESKTOP 	= 0;
	final static int TYPE_FOLDER	= 1;
	
	private TreeView<String> treeView 	= null;
	private ListView<String> listView	= null;
	
	private Image treeIconImages[] = new Image[3];
	private HashMap<String, Image> mapOfFileExtToSmallIcon = new HashMap<String, Image>();
	private AndroidDeviceDataIF device = null;
	
	public void setDevice(AndroidDeviceDataIF device) {
		this.device = device;
		
		TreeItem<String> root = treeView.getRoot();
		if( root.getChildren() != null && root.getChildren().size() > 0 ) return;
		
		for( String dirName : getRootDirectories()) {
			TreeItem<String> treeItem = new TreeItem<>( dirName, new ImageView( treeIconImages[TYPE_FOLDER] ));
			root.getChildren().add( treeItem );
		}
			
		root.expandedProperty().addListener( treeItemChangeListener );		
	}
	
	public void init(TreeView<String> treeView, ListView<String> listView) {
		this.treeView	= treeView;
		this.listView	= listView;
		
		ClassLoader classLoader = DeviceFileSystem.this.getClass().getClassLoader();
		treeIconImages[TYPE_DESKTOP] = new Image( classLoader.getResourceAsStream("img/icons8-desktop-16.png"));
		treeIconImages[TYPE_FOLDER]  = new Image( classLoader.getResourceAsStream("img/icons8-folder-16.png"));
		
		treeView.getSelectionModel().selectedItemProperty().addListener( directoryChangeListener );
		listView.setCellFactory( listViewCallback );
		
		// root './' 등록
		TreeItem<String> root = new TreeItem<>( "./", new ImageView( treeIconImages[TYPE_DESKTOP] ) );
		treeView.setRoot( root );
	}

	private List<String> getRootDirectories() {
		List<String> ret = new ArrayList<String>();
		
		try {
			List<String> result = device.getInstance().runCommand("shell ls ./ -l");
			for( String line : result ) {
				if( line.endsWith("Permission denied")) continue;
				
				DeviceFile dFile = new DeviceFile( "", line );
				
				if( !dFile.isDirectory()) continue;
				if( dFile.isHidden()) continue;
				
				ret.add( dFile.getName());				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return ret;
	}

	ChangeListener<Boolean> treeItemChangeListener = new ChangeListener<Boolean>() {
		@Override
		public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
			if( !newValue ) return;
					
			BooleanProperty bb = (BooleanProperty) observable;
	        TreeItem<String> treeItem = (TreeItem<String>) bb.getBean();
	        
	        addSubDirectory( treeItem );
		}
	};
	
	private void addSubDirectory(TreeItem<String> treeItem) {
		String currentDirectory = getTreeItemDirectory( treeItem );
		
		for( TreeItem<String> child : treeItem.getChildren()) {
			String directory = currentDirectory + child.getValue();
			
			ObservableList<TreeItem<String>> childTreeItemList = child.getChildren();
			if( childTreeItemList != null && childTreeItemList.size() > 0 ) continue;
				
			try {
				List<String> lines = device.getInstance().runCommand("shell ls " + directory + " -l");
				for( String line : lines ) {
					DeviceFile dFile = new DeviceFile( directory, line );
					
					if( !dFile.isDirectory()) continue;
					if( dFile.isHidden()) continue;

					childTreeItemList.add( new TreeItem<>( dFile.getName(), new ImageView( treeIconImages[TYPE_FOLDER] )));												
				}				
			} catch (Exception e) {
				e.printStackTrace();
			}						
				
			child.expandedProperty().addListener( treeItemChangeListener );			
		}
	}
	
	class DeviceFile {
		private String path;
		private String name;
		private boolean isDirectory = false;
		
		public DeviceFile( String path, String line ) {
			this.path 	= path;
			isDirectory = line.charAt(0) == 'd' || line.charAt(0) == 'l';
			
			int idx = line.lastIndexOf( " ->");
			if( idx != -1 ) {
				line = line.substring( 0, idx ).trim();
			}
			name = line.substring( line.lastIndexOf( " ")).trim();
		}
		
		public boolean isDirectory() 	{ return isDirectory; }
		public boolean isFile() 		{ return !isDirectory(); }
		public boolean isHidden() 		{ return false; }
		
		public String getName() 		{ return name; }
		public String getAbsolutePath() { return path + "/" + name; }

		@Override
		public String toString() {
			return String.format( "%c %s/%s", isDirectory?'d':'-', path, name );
		}
	};
	
	String getTreeItemDirectory( TreeItem<String> treeItem ) {
		Stack<String> stack = new Stack<String>();
		stack.push( treeItem.getValue());
		
		TreeItem<String> parent = treeItem.getParent();
		while( parent != null ) {
			stack.push( parent.getValue());
			parent = parent.getParent();
		}
		
		StringBuilder sb = new StringBuilder();
		while( !stack.isEmpty()) {
			sb.append( stack.pop());
			sb.append( "/" );
		}
		
		return sb.toString().replace(".//", "./");
	}
	
	ChangeListener<TreeItem<String>> directoryChangeListener = new ChangeListener<TreeItem<String>>() {
		@Override
		public void changed(ObservableValue<? extends TreeItem<String>> observable, TreeItem<String> oldValue, TreeItem<String> newValue) {
			TreeItem<String> selectedItem = newValue;
            String folder = getTreeItemDirectory( selectedItem );
            
            System.out.println("Selected folder : " + folder );
            
            refreshFileList( folder );
		}		
	};
	
	private void refreshFileList( String folder ) {
		List<String> fileItems = new ArrayList<>();
        try {
			List<String> lines = device.getInstance().runCommand("shell ls " + folder + " -l");
			for( String line : lines ) {
				if( line.endsWith("Permission denied")) continue;
				DeviceFile dFile = new DeviceFile( folder, line );
				
				if( dFile.isDirectory()) continue;
				if( dFile.isHidden()) continue;

				fileItems.add( dFile.getName());												
			}				
		} catch (Exception e) {
			e.printStackTrace();
		}
                    
        listView.setItems( FXCollections.observableArrayList( fileItems ));
	}
	
	Callback<ListView<String>,ListCell<String>> listViewCallback = new Callback<ListView<String>,ListCell<String>>(){
		@Override
		public ListCell<String> call(ListView<String> param) {
			return new AttachmentListCell();
		}
	};
	
	class AttachmentListCell extends ListCell<String> {
        @Override
        public void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            
            if (empty) {
                setGraphic(null);
                setText(null);
            } else {
                Image fxImage = getFileIcon(item);
                ImageView imageView = new ImageView(fxImage);
                setGraphic(imageView);
                setText(item);
            }
        }
    }
	
	private Image getFileIcon(String fname) {
        final String ext = getFileExt(fname);

        Image fileIcon = mapOfFileExtToSmallIcon.get(ext);
        if (fileIcon == null) {

            javax.swing.Icon jswingIcon = null; 

            File file = new File(fname);
            if (file.exists()) {
                jswingIcon = getJSwingIconFromFileSystem(file);
            }
            else {
                File tempFile = null;
                try {
                    tempFile = File.createTempFile("icon", ext);
                    jswingIcon = getJSwingIconFromFileSystem(tempFile);
                }
                catch (IOException ignored) {
                    // Cannot create temporary file. 
                }
                finally {
                    if (tempFile != null) tempFile.delete();
                }
            }

            if (jswingIcon != null) {
                fileIcon = jswingIconToImage(jswingIcon);
                mapOfFileExtToSmallIcon.put(ext, fileIcon);
            }
        }

        return fileIcon;
    }
	
	private static String getFileExt(String fname) {
        String ext = ".";
        int p = fname.lastIndexOf('.');
        if (p >= 0) {
            ext = fname.substring(p);
        }
        return ext.toLowerCase();
    }

    private static javax.swing.Icon getJSwingIconFromFileSystem(File file) {

        // Windows {
        FileSystemView view = FileSystemView.getFileSystemView();
        javax.swing.Icon icon = view.getSystemIcon(file);
        // }

        // OS X {
        //final javax.swing.JFileChooser fc = new javax.swing.JFileChooser();
        //javax.swing.Icon icon = fc.getUI().getFileView(fc).getIcon(file);
        // }

        return icon;
    }
    
    private static Image jswingIconToImage(javax.swing.Icon jswingIcon) {
        BufferedImage bufferedImage = new BufferedImage(jswingIcon.getIconWidth(), jswingIcon.getIconHeight(),
                BufferedImage.TYPE_INT_ARGB);
        jswingIcon.paintIcon(null, bufferedImage.getGraphics(), 0, 0);
        return SwingFXUtils.toFXImage(bufferedImage, null);
    }

	public String getSelectedFolder() {
		TreeItem<String> selectedItem = treeView.getSelectionModel().getSelectedItem();
		if( selectedItem == null ) return null;
		
		return getTreeItemDirectory( selectedItem );
	}
	
	public String getSelectedFile() {
		String folder = getSelectedFolder();
		if( folder == null ) return null;
		
		String filename = listView.getSelectionModel().getSelectedItem();
		if( filename == null ) return null;
		
		return folder + filename;
	}

	public void refreshFileList() {
		TreeItem<String> selectedItem = treeView.getSelectionModel().getSelectedItem();
        String folder = getTreeItemDirectory( selectedItem );
        
        refreshFileList( folder );
	}
	
	
	
}
