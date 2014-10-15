package d.core.url.type;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;

public class Type {
	protected String name;
	public int id;
	
	public static ObservableList<TreeItem<Type>> 
		categoryList = FXCollections.observableArrayList();
	public static ObservableList<TreeItem<Type>> 
		queueList = FXCollections.observableArrayList();
	
	
	public Type() {
		id = 0;
	}
	
	public Type(String str) {
		super();
		name = str;
	}
	
	public static void addTreeCategory(Type cat){
		categoryList.add(new TreeItem<Type>(cat));
	}
	
	public static void addTreeQueue(Type queue){
		queueList.add(new TreeItem<Type>(queue));
	}


	@Override
	public String toString() {
		return name;
	}
	
	public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
	
}
