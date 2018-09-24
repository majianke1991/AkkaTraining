package akka.actor;

import java.util.List;

public class OrderItem {
	private String               orderId;
	private List<ProductItem> items;



	public OrderItem() {
	}



	public OrderItem(String orderId, List<ProductItem> items) {
		this.orderId = orderId;
		this.items = items;
	}



	public String getOrderId() {
		return orderId;
	}



	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}



	public List<ProductItem> getItems() {
		return items;
	}



	public void setItems(List<ProductItem> items) {
		this.items = items;
	}



	@Override
	public String toString() {
		return "OrderItem{" +
				"orderId='" + orderId + '\'' +
				", items=" + items +
				'}';
	}
}
