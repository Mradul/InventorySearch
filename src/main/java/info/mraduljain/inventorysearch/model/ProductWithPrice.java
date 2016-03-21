package info.mraduljain.inventorysearch.model;

public class ProductWithPrice implements Comparable<ProductWithPrice>{
	private String name;
	private Double price;
	private Object product;
	public ProductWithPrice(String name, Double price) {
		super();
		this.name = name;
		this.price = price;
	}
	public ProductWithPrice(String name, Double price, Object product) {
		super();
		this.name = name;
		this.price = price;
		this.product=product;
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
		return getName()+"="+getPrice();
	}
	public Object getProduct() {
		return product;
	}
	public void setProduct(Object product) {
		this.product = product;
	}
}
