package info.mraduljain.inventorysearch;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;

import info.mraduljain.inventorysearch.model.ProductWithPrice;

public class InventoryManager {

	public String readJson(String filename){
		String lines="",line;
			try {				
					BufferedReader bufferedReader = new BufferedReader(new FileReader(filename));				
					while((line=bufferedReader.readLine())!=null){
						lines+=line;					
					}
				
				bufferedReader.close();
			} catch (FileNotFoundException e) {
				System.out.println("ERROR: File not foud - "+filename);
				e.printStackTrace();
			}catch (IOException e) {
				System.out.println("ERROR: Error reading file - "+filename);
				e.printStackTrace();
			}	
			return lines;
	}
	
	
	public static void main(String[] args) {
		InventoryManager im = new InventoryManager();
		String json=im.readJson("C:\\Project\\STS\\InventorySearch\\src\\main\\resources\\inventory.json");
		ReadContext rCtx=JsonPath.parse(json);
		List<String> productTypes = rCtx.read("$..type");
		
		//System.out.println(productTypes);
		
		for(String aProductType:productTypes){
			List<String>  productList=rCtx.read("$..[?(@.type == '"+aProductType+"')]");
			List<Map<String, Object>> keyValues = JsonPath.parse(productList.toString()).read("");
			List<ProductWithPrice> ppList= new ArrayList<ProductWithPrice>();
			for(Map<String, Object> aKeyVal : keyValues){
				Double price =Double.parseDouble(""+aKeyVal.get("price"));
				String name =(String) aKeyVal.get("title");
				ppList.add(new ProductWithPrice(name, price));
			}
			ppList.sort((p1, p2) -> { 
				return p2.compareTo(p1);
			});
			System.out.println(ppList);
		}
		/*
		System.out.println(productList);
		Map <String,Double> productPrice = new HashMap<String, Double>();
		for(String aProd:productList){
			Double price=new JsonPath(price,"$..price");
		}
		/*
		Map<String, String> productsByTypes = new HashMap<String, String>();
		for(String aProductType:productTypes){
			List<Double>  productPrices=rCtx.read("$..[?(@.type == '"+aProductType+"')].price");
			Collections.sort(productPrices);
			System.out.println(productPrices);
		}
		*/
	}
	
}
