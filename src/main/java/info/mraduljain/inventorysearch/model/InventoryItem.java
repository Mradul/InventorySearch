package info.mraduljain.inventorysearch.model;

import org.json.simple.JSONObject;

public class InventoryItem {
 private JSONObject item;

 public Object getValue(String key){
	 return item.get(key);
 }
public InventoryItem(JSONObject item) {
	super();
	this.item = item;
}

public JSONObject getItem() {
	return item;
}

public void setItem(JSONObject item) {
	this.item = item;
}
 
}
