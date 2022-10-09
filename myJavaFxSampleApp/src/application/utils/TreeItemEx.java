package application.utils;

import java.util.HashMap;
import java.util.Map;

import javafx.scene.Node;
import javafx.scene.control.TreeItem;

public class TreeItemEx<T> extends TreeItem<T> {
	public TreeItemEx() {
		super();
	}

	public TreeItemEx(T value, Node graphic) {
		super(value, graphic);
	}

	public TreeItemEx(T value) {
		super(value);
	}

	public Object getCustomValue( String key ) {
		return customValue.get(key);
	}
	
	public Object setCustomValue( String key, Object value ) {
		return customValue.put( key, value );
	}
	
	private Map<String,Object> customValue = new HashMap<String,Object>();
}
