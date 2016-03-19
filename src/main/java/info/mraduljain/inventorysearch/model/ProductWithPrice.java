package info.mraduljain.inventorysearch.model;

import com.sun.xml.internal.bind.v2.runtime.RuntimeUtil.ToStringAdapter;

public class ProductWithPrice implements Comparable<ProductWithPrice>{
	private String name;
	private Double price;
	
	public ProductWithPrice(String name, Double price) {
		super();
		this.name = name;
		this.price = price;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Double getPrice() {
		return price;
	}
	public void setPrice(Double price) {
		this.price = price;
	}
	public int compareTo(ProductWithPrice o) {
		
		return Double.compare(getPrice(), o.getPrice());
	}
	
	public String toString(){
		return getName()+"-"+getPrice();
	}
}
