package akka.actor;

import java.io.Serializable;
import java.util.Objects;

public class ProductItem implements Serializable {

	private String id;
	private String name;
	private long   stock;
	private double price;



	public ProductItem() {
	}



	public ProductItem(String id, String name, long stock, double price) {
		this.id = id;
		this.name = name;
		this.stock = stock;
		this.price = price;
	}



	@Override
	public String toString() {
		return "ProductItem{" +
				"id='" + id + '\'' +
				", name='" + name + '\'' +
				", stock=" + stock +
				", price=" + price +
				'}';
	}



	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		ProductItem that = (ProductItem) o;
		return stock == that.stock &&
				Objects.equals(id, that.id);
	}



	@Override
	public int hashCode() {
		return 0;
	}



	public String getId() {
		return id;
	}



	public void setId(String id) {
		this.id = id;
	}



	public String getName() {
		return name;
	}



	public void setName(String name) {
		this.name = name;
	}



	public long getStock() {
		return stock;
	}



	public void setStock(long stock) {
		this.stock = stock;
	}



	public double getPrice() {
		return price;
	}



	public void setPrice(double price) {
		this.price = price;
	}




}
