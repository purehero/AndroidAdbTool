package application.utils;

import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableCell;

public class CheckBoxTableCellEx<S, T> extends TableCell<S, T> implements EventHandler<ActionEvent> {
	private final CheckBox checkBox;
    private ObservableValue<T> ov;

    public CheckBoxTableCellEx() {
        this.checkBox = new CheckBox();
        this.checkBox.setAlignment(Pos.CENTER);

        setAlignment(Pos.CENTER);
        setGraphic(checkBox);
        
        checkBox.setOnAction(this);
        chClickEvent = new ActionEvent( this, null );
    } 

    ActionEvent chClickEvent = null;
    EventHandler<ActionEvent> actionEvent = null;
    public void setOnAction( EventHandler<ActionEvent> arg0) {
    	actionEvent = arg0;	  
    }
    
    @Override 
    public void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);
        
        if (empty) {
            setText(null);
            setGraphic(null);
            
        } else {
            setGraphic(checkBox);
            if (ov instanceof BooleanProperty) {
                checkBox.selectedProperty().unbindBidirectional((BooleanProperty) ov);
            }
            
            ov = getTableColumn().getCellObservableValue(getIndex());
            if (ov instanceof BooleanProperty) {
                checkBox.selectedProperty().bindBidirectional((BooleanProperty) ov);
            }
            
            checkBox.setSelected((Boolean)item );
        }        
    }

	@Override
	public void handle(ActionEvent arg0) {
		if( actionEvent != null ) {
			actionEvent.handle(chClickEvent);
		}
	}
}