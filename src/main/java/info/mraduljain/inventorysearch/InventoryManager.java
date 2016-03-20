package info.mraduljain.inventorysearch;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;

import info.mraduljain.inventorysearch.model.ProductWithPrice;

public class InventoryManager {

	public JSONArray  readJson(String filename){
		JSONObject jsonObject=null;
		JSONArray jsonArray=null;
		JSONParser parser = new JSONParser();
			try {				
				jsonArray = (JSONArray) parser.parse(new FileReader(filename));				
					
			} catch (FileNotFoundException e) {
				System.out.println("ERROR: File not foud - "+filename);
				e.printStackTrace();
			}catch (IOException e) {
				System.out.println("ERROR: Error reading file - "+filename);
				e.printStackTrace();
			}catch (ParseException e) {
				System.out.println("ERROR: Error parsing file - "+filename+" to json");
				e.printStackTrace();
			}	
			
			return jsonArray;
	}
	
	

	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		InventoryManager im = new InventoryManager();
		String filename="C:\\Project\\STS\\InventorySearch\\src\\main\\resources\\inventory.json";
		//read input file into a string
		JSONArray productInventory=im.readJson(filename);
		
		Map<String,List<ProductWithPrice>> productTypes = im.getProductCategoryMap(productInventory);
		//System.out.println(productTypes);
		//Which cds have a total running time longer than 60 minutes?
		List<ProductWithPrice> cdList=productTypes.get("cd");
		for(ProductWithPrice aCD:cdList){
			JSONObject prod= (JSONObject) aCD.getProduct();
			System.out.println(prod.get("tracks"));
			JSONArray tracks=(JSONArray) prod.get("tracks");
			int totalSeconds=0;
			for(Object trk:tracks){
				JSONObject aTrack= (JSONObject) trk;
				totalSeconds+=Integer.parseInt(""+aTrack.get("seconds"));
			}
			System.out.println(totalSeconds);
		}
		
		Iterator it = productTypes.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry keyVal = (Map.Entry)it.next();
	        String productType = (String) keyVal.getKey();
	        List<ProductWithPrice> productList = (List<ProductWithPrice>) keyVal.getValue();
	        
	        //Java 8 Lambda Expression
	        productList.sort((p1, p2) -> { 
	        	//sort in desc
				return p2.compareTo(p1);
			});
	        
	        System.out.println(productType+"->"+productList);
	     // avoid ConcurrentModificationException
	        it.remove();
	    }

	}
	
	public List<String> getMaxPricedProductsByCategory(List<Map<String, Object>> keyValuesForProd, int limit){
		List<ProductWithPrice> ppList= new ArrayList<ProductWithPrice>();
		for(Map<String, Object> aKeyVal : keyValuesForProd){
			Double price =Double.parseDouble(""+aKeyVal.get("price")); 
			String name =(String) aKeyVal.get("title");
			ppList.add(new ProductWithPrice(name, price));
		}
		ppList.sort((p1, p2) -> { 
			return p2.compareTo(p1);
		});
		List<String> listOfProductsByPriceDesc = new ArrayList<String>();
		
		int i=0;
		while( i<ppList.size() && i<limit){
			listOfProductsByPriceDesc.add(ppList.get(i).getName());
			i++;
		}
		
		return listOfProductsByPriceDesc;
	}
	
	public Map<String,List<ProductWithPrice>> getProductCategoryMap(JSONArray productInventory){
		Map<String,List<ProductWithPrice>> productTypes = new HashMap<String,List<ProductWithPrice>>();
		System.out.println(productInventory.toJSONString());
		
		JSONObject newJson= new JSONObject();
		for(Object obj:productInventory ){
			JSONObject prod= (JSONObject) obj;
			//JSONObject child= new JSONObject();
			String type = (String)prod.get("type");
			String name = (String)prod.get("title");
			Double price = Double.parseDouble(""+prod.get("price"));
			ProductWithPrice pp = new ProductWithPrice(name, price, prod);
			List<ProductWithPrice> productList;
			if(( productList=productTypes.remove(type)) != null){
				productList.add(pp);
				productTypes.put(type, productList);
			}else{
				productList=new ArrayList<ProductWithPrice>() ;
				productList.add(pp);
				productTypes.put(type,productList);
			}
			
		}
		return productTypes;
	}
}
