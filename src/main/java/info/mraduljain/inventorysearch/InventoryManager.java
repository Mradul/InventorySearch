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
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import info.mraduljain.inventorysearch.model.ProductWithPrice;

public class InventoryManager {

	JSONArray productInventory;
	
	public static void main(String[] args) {
		Scanner kb = new Scanner(System.in);
		System.out.println("Enter the filename to read inventory from(absolute path):");
		String filename=kb.nextLine();
		//String filename="C:\\Project\\STS\\InventorySearch\\src\\main\\resources\\inventory.json";
		InventoryManager im = new InventoryManager();
		//read input file into a json array
		im.readJsonFromFile(filename);	
		//print ans to questions
		im.printAnsToQuestions();
	}
	
	/**
	 * Read file and store json objects into productInventory
	 * @param filename - Name of the containing inventory
	 */
	public void  readJsonFromFile(String filename){
		
		JSONParser parser = new JSONParser();
			try {				
				this.setProductInventory((JSONArray) parser.parse(new FileReader(filename)));				
					
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
	}
	
	/**
	 * Print ans to the given questions
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void printAnsToQuestions(){
		Map<String,List<ProductWithPrice>> productTypes = this.getProductCategoryMap();
		//1. What are the 5 most expensive items from each category?

		int maxProds=5;
		System.out.println("***What are the "+maxProds+" most expensive items from each category?***");
		System.out.println(maxProds+" most expensive items from each category (map): \n"+this.getMaxPricedProductsByCategory(productTypes,maxProds));
		
		//iterate over the map to print items for each category
		Iterator<Entry<String, List<JSONObject>>> it = this.getMaxPricedProductsByCategory(productTypes,maxProds).entrySet().iterator();
	    while (it.hasNext()) {	        
			Map.Entry keyVal = (Map.Entry)it.next();
	        String cat = (String) keyVal.getKey();
			List<JSONObject> itemList = (List<JSONObject>) keyVal.getValue();  
			System.out.println("Most expensive items in category - "+cat+" are: ");
			System.out.println(itemList);
	    }
	    System.out.println("************************************************************************\n");
	    
	    //2. Which cds have a total running time longer than 60 minutes?
		int thresholdMinutes=60;
		int SEC_PER_MIN=60;
		System.out.println("***Which cds have a total running time longer than "+thresholdMinutes+" minutes?***");
		System.out.println("CDs that have total running time longer than "+thresholdMinutes+" minutes (array of items):\n"
							+this.getCDsWithGreaterRunTime(productTypes,thresholdMinutes*SEC_PER_MIN));
		System.out.println("************************************************************************\n");	
		
		//3. Which authors have also released cds?
		System.out.println("***Which authors have also released cds?***");
		System.out.println("Authors that authored CDs also: "+this.getAuthorsWithCD());
		System.out.println("************************************************************************\n");
		
		//4.  Which items have a title, track, or chapter that contains a year?		
		System.out.println("***Which items have a title, track, or chapter that contains a year?***");
		//Any set of digits starting with 1-9 considered as a year for simplicity. Although 1000000000 BC/AD might not be considered as a year, in reality, for example.
		String pattern = "\\b[1-9]\\d*\\b";
		JSONArray itemsContainingYear = this.getItemsContainingPattern(pattern);
		System.out.println("items having title, track, or chapter that contains a year (array of items):\n"+itemsContainingYear);
	}
	
	/*
	 * Test pattern for the given string and return true if matches, false otherwise
	 */
	private boolean regexTester(String str, String pattern){
		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(str);
		return m.find();
	}
	
	/**
	 * Get given number of most priced products by category 
	 * @param productTypes - Map of category and list of ProductWithPrice
	 * @param limit - number of items to send back
	 * @return Map of category and list of items
	 */
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
	
	/*
	 * Get given number of most priced products
	 * @param productList
	 * @param limit
	 * @return
	 */
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


	/**
	 * Get map of category and list of ProductWithPrice
	 * @return Map of category and list of ProductWithPrice
	 */
	public Map<String,List<ProductWithPrice>> getProductCategoryMap(){
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
	
	/**
	 * Get cd items that have a total track time of more than given seconds
	 * @param productTypes - Map of category and list of ProductWithPrice
	 * @param thresholdTimeInSecs - Threshold in seconds
	 * @return List of items
	 */
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
	
	/**
	 * Get all products that contain a pattern in either title, chapters, or tracks
	 * @param pattern - pattern to be matched against title, chapters, or tracks of products
	 * @return JSONArray of products containing the pattern
	 */
	@SuppressWarnings("unchecked")
	public JSONArray getItemsContainingPattern(String pattern){
		
				JSONArray itemsContainingYear = new JSONArray();
				for(Object obj:productInventory ){
					JSONObject prod= (JSONObject) obj;
					String title=(String) prod.get("title");
					
					List<String> list;
					if(this.regexTester(title, pattern)){
						
						itemsContainingYear.add(prod);
					}else if((list= (List<String>) prod.get("chapters"))!=null){
						//System.out.println(list);
						for(String str:list){
							if(this.regexTester( str, pattern)){
								
								itemsContainingYear.add(prod);
								continue;
							}
						}
					}else if((list= (List<String>) prod.get("tracks"))!=null){
						//System.out.println(list);	
						for(Object ob:list){
							JSONObject p= (JSONObject) ob;
							if(this.regexTester( (String) p.get("name"), pattern)){
								
								itemsContainingYear.add(prod);
								continue;
							}
						}
					}
				}
				return itemsContainingYear;
	}
	
	/**
	 * Get authors that have also authored CDs
	 * @return Authors that have also authored CDs
	 */
	public Set<String> getAuthorsWithCD(){
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
		return setOfCDAuthors;
	}


	/**
	 * 
	 * @return productInventory
	 */
	public JSONArray getProductInventory() {
		return productInventory;
	}
	/**
	 * 
	 * @param productInventory to set
	 */
	public void setProductInventory(JSONArray productInventory) {
		this.productInventory = productInventory;
	}
	
	
}
