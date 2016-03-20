package info.mraduljain.inventorysearch;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
		Set<String> productTypes = new HashSet<String>(rCtx.read("$..type"));
		
		//System.out.println(productTypes);
		
		for(String aProductType:productTypes){
			List<Map<String, Object>> keyValues = rCtx.read("$..[?(@.type == '"+aProductType+"')]");			
			System.out.println(aProductType+"-"+im.getMaxPricedProductsByCategory(keyValues,4));
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
	
}
