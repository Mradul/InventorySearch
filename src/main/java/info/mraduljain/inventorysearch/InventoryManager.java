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
		System.out.println("5 most expensive items from each category \n"+im.getMaxPricedProductsByCategory(productTypes,2));
		int thresholdMinutes=60;
		int SEC_PER_MIN=60;
		
		System.out.println("CDs that have total running time longer than "+thresholdMinutes+" minutes \n"
							+im.getCDsWithGreaterRunTime(productTypes,thresholdMinutes*SEC_PER_MIN));
		
		
	}
	
	public Map<String,List<JSONObject>> getMaxPricedProductsByCategory(Map<String,List<ProductWithPrice>> productTypes, int limit){
		Iterator it = productTypes.entrySet().iterator();
		Map<String,List<JSONObject>> priceyProdsByCat = new HashMap<String,List<JSONObject>>();
	    while (it.hasNext()) {
	        Map.Entry keyVal = (Map.Entry)it.next();
	        String productType = (String) keyVal.getKey();
	        List<ProductWithPrice> productList = (List<ProductWithPrice>) keyVal.getValue();
	        //get top <limit> priced products for current category 
	        priceyProdsByCat.put(productType, this.getMaxPricedProducts(productList, limit));
	    	       
	    }
	    return priceyProdsByCat;
	}
	
	private List<JSONObject> getMaxPricedProducts(List<ProductWithPrice> productList, int limit) {
		 //Java 8 Lambda Expression
        productList.sort((p1, p2) -> { 
        	//sort in desc
			return p2.compareTo(p1);
		});
        List<JSONObject> listOfProductsByPriceDesc = new ArrayList<JSONObject>();
		
		int i=0;
		while( i<productList.size() && i<limit){
			listOfProductsByPriceDesc.add((JSONObject) productList.get(i).getProduct());
			i++;
		}
		
		return listOfProductsByPriceDesc;
	}



	public Map<String,List<ProductWithPrice>> getProductCategoryMap(JSONArray productInventory){
		Map<String,List<ProductWithPrice>> productTypes = new HashMap<String,List<ProductWithPrice>>();
		
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
	
	public List<JSONObject> getCDsWithGreaterRunTime(Map<String,List<ProductWithPrice>> productTypes,int thresholdTimeInSecs){
		List<ProductWithPrice> cdList=productTypes.get("cd");
		List<JSONObject> cdListWithThresholdRuntime = new ArrayList<JSONObject>();
		for(ProductWithPrice aCD:cdList){
			JSONObject prod= (JSONObject) aCD.getProduct();
			
			JSONArray tracks=(JSONArray) prod.get("tracks");
			int totalSeconds=0;
			for(Object trk:tracks){
				JSONObject aTrack= (JSONObject) trk;
				totalSeconds+=Integer.parseInt(""+aTrack.get("seconds"));
			}
			
			if(totalSeconds>thresholdTimeInSecs){
				cdListWithThresholdRuntime.add(prod);
			}
		}
		return cdListWithThresholdRuntime;
	}
}
