package application.filetransfer;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import javax.swing.filechooser.FileSystemView;

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
import purehero.utils.Utils;

public class DesktopFileSystem {
	final static int TYPE_DESKTOP 	= 0;
	final static int TYPE_DISK 		= 1;
	final static int TYPE_FOLDER	= 2;
	
	private TreeView<String> treeView 	= null;
	private ListView<String> listView	= null;
	
	private Image treeIconImages[] = new Image[3];
	private HashMap<String, Image> mapOfFileExtToSmallIcon = new HashMap<String, Image>();
	private FileSystem fileSystem = FileSystems.getDefault();
	
	public DesktopFileSystem() {
	}

	public void init(TreeView<String> treeView, ListView<String> listView) {
		this.treeView	= treeView;
		this.listView	= listView;
		
		ClassLoader classLoader = DesktopFileSystem.this.getClass().getClassLoader();
		treeIconImages[TYPE_DESKTOP] = new Image( classLoader.getResourceAsStream("img/icons8-desktop-16.png"));
		treeIconImages[TYPE_DISK] 	 = new Image( classLoader.getResourceAsStream("img/icons8-hdd-16.png"));
		treeIconImages[TYPE_FOLDER]  = new Image( classLoader.getResourceAsStream("img/icons8-folder-16.png"));
		
		treeView.getSelectionModel().selectedItemProperty().addListener( directoryChangeListener );
		listView.setCellFactory( listViewCallback );
		
		// root 'My Computer' 등록
		TreeItem<String> root = new TreeItem<>( Utils.getComputerName(), new ImageView( treeIconImages[TYPE_DESKTOP] ) );
		
		// system disk driver 등록
		for( Path path : fileSystem.getRootDirectories()) {	
			TreeItem<String> treeItem = new TreeItem<>( path.toString(), new ImageView( treeIconImages[TYPE_DISK] ));
			root.getChildren().add( treeItem );
		}
		
		treeView.setRoot( root );
		root.expandedProperty().addListener( treeItemChangeListener );
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
	
	ChangeListener<TreeItem<String>> directoryChangeListener = new ChangeListener<TreeItem<String>>() {
		@Override
		public void changed(ObservableValue<? extends TreeItem<String>> observable, TreeItem<String> oldValue, TreeItem<String> newValue) {
			TreeItem<String> selectedItem = newValue;
            File folder = getTreeItemDirectory( selectedItem );
            
            System.out.println("Selected folder : " + folder.getAbsolutePath());
            
            refreshFileList( folder );            
		}		
	};
	
	private void refreshFileList( File folder ) {
		List<String> fileItems = new ArrayList<>();
        File list[] = folder.listFiles();
        if( list != null && list.length > 0 ) {
        	for( File file : list ) {
        		if( file.isDirectory()) continue;
        		if( file.isHidden()) continue;
        		
        		fileItems.add( file.getName());
        	}
        }
        listView.setItems( FXCollections.observableArrayList( fileItems ));
	}
	
	File getTreeItemDirectory( TreeItem<String> treeItem ) {
		Stack<String> stack = new Stack<String>();
		stack.push( treeItem.getValue());
		
		TreeItem<String> parent = treeItem.getParent();
		while( parent != null ) {
			stack.push( parent.getValue());
			parent = parent.getParent();
		}
		stack.pop();	// window 의 경우 최상위 'My Computer' 항목은 포함시키지 않기 위함
		
		StringBuilder sb = new StringBuilder();
		while( !stack.isEmpty()) {
			sb.append( stack.pop());
			sb.append( fileSystem.getSeparator());
		}
		
		return new File( sb.toString());
	}
	
	private void addSubDirectory(TreeItem<String> treeItem) {
		File currentDirectory = getTreeItemDirectory( treeItem );
		
		for( TreeItem<String> child : treeItem.getChildren()) {
			File directory = null;
			
			if( currentDirectory.exists() ) { 
				directory = new File( currentDirectory, (String) child.getValue());
			} else {
				directory = new File( (String) child.getValue());
			}
			
			if( directory.exists() && directory.isDirectory() ) {
				ObservableList<TreeItem<String>> childTreeItemList = child.getChildren();
				if( childTreeItemList != null && childTreeItemList.size() > 0 ) continue;
								
				File subItems[] = directory.listFiles();
				if( subItems != null && subItems.length > 0 ) {
					for( File item : subItems ) {
						if( !item.isDirectory()) continue;
						if( item.isHidden()) continue;

						childTreeItemList.add( new TreeItem<>( item.getName(), new ImageView( treeIconImages[TYPE_FOLDER] )));												
					}
				}
				
				child.expandedProperty().addListener( treeItemChangeListener );
			}
		}
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

    public File getSelectedFolder() {
		TreeItem<String> selectedItem = treeView.getSelectionModel().getSelectedItem();
		if( selectedItem == null ) return null;
		
		return getTreeItemDirectory( selectedItem );
	}
    
	public File getSelectedFile() {
		File folder = getSelectedFolder();
		if( folder == null ) return null;
		
		String filename = listView.getSelectionModel().getSelectedItem();
		if( filename == null ) return null;
		
		return new File( folder, filename );
	}

	public void refreshFileList() {
		TreeItem<String> selectedItem = treeView.getSelectionModel().getSelectedItem();
        File folder = getTreeItemDirectory( selectedItem );
        
        refreshFileList( folder );
	}
}
