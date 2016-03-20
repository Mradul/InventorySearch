package info.mraduljain.inventorysearch;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.management.relation.RelationServiceNotRegisteredException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import info.mraduljain.inventorysearch.model.ProductWithPrice;

public class InventoryManager {

	public JSONArray  readJson(String filename){
		
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
		
		int maxProds=2;
		System.out.println(maxProds+" most expensive items from each category \n"+im.getMaxPricedProductsByCategory(productTypes,maxProds));
		int thresholdMinutes=60;
		int SEC_PER_MIN=60;
		
		System.out.println("CDs that have total running time longer than "+thresholdMinutes+" minutes \n"
							+im.getCDsWithGreaterRunTime(productTypes,thresholdMinutes*SEC_PER_MIN));
		
		Set<String> setOfCDAuthors = new HashSet<String>();
		Set<String> setOfNonCDAuthors = new HashSet<String>();
		for(Object obj:productInventory ){
			JSONObject prod= (JSONObject) obj;
			String anAuthor=(String) prod.get("author");
			if(prod.get("type").equals("cd")&& anAuthor!=null){
				setOfCDAuthors.add(anAuthor);
			}else if(anAuthor!=null){
				setOfNonCDAuthors.add(anAuthor);
			}
			
		}
		setOfCDAuthors.retainAll(setOfNonCDAuthors);
		
		System.out.println("Authors that released CDs also - "+setOfCDAuthors);
		
		//Q4  Which items have a title, track, or chapter that contains a year.
		//Any set of digits starting with 1-9 considered as a year for simplicity. Although 1000000000 BC/AD might not be considered as a year, in reality, for example.
		String pattern = "[1-9]\\d*";
		JSONArray itemsContainingYear = new JSONArray();
		for(Object obj:productInventory ){
			JSONObject prod= (JSONObject) obj;
			String title=(String) prod.get("title");
			
			List<String> list;
			if(im.regexTester(title, pattern)){
				System.out.println("Title-"+title);
				itemsContainingYear.add(prod);
			}else if((list= (List<String>) prod.get("chapters"))!=null){
				//System.out.println(list);
				for(String str:list){
					if(im.regexTester( str, pattern)){
						System.out.println("chapter-"+str);
						itemsContainingYear.add(prod);
						continue;
					}
				}
			}else if((list= (List<String>) prod.get("tracks"))!=null){
				//System.out.println(list);	
				for(Object ob:list){
					JSONObject p= (JSONObject) ob;
					if(im.regexTester( (String) p.get("name"), pattern)){
						System.out.println("track-"+p.get("name"));
						itemsContainingYear.add(prod);
						continue;
					}
				}
			}
		}	
		System.out.println(itemsContainingYear);
		
	}
	
	private boolean regexTester(String str, String pattern){
		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(str);
		return m.find();
	}
	
	public Map<String,List<JSONObject>> getMaxPricedProductsByCategory(Map<String,List<ProductWithPrice>> productTypes, int limit){
		Iterator<Entry<String, List<ProductWithPrice>>> it = productTypes.entrySet().iterator();
		Map<String,List<JSONObject>> priceyProdsByCat = new HashMap<String,List<JSONObject>>();
	    while (it.hasNext()) {
	        @SuppressWarnings("rawtypes")
			Map.Entry keyVal = (Map.Entry)it.next();
	        String productType = (String) keyVal.getKey();
	        @SuppressWarnings("unchecked")
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
