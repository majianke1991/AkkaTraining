package akka.actor;

import akka.japi.pf.ReceiveBuilder;
import com.google.gson.Gson;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ProductActor extends AbstractLoggingActor {

	public final Map<String, ProductItem> products = IntStream.range(0, 10)
			.mapToObj(i -> new ProductItem("p" + i, "prod" + i, 10, 10d * i))
			.collect(Collectors.toMap(a -> a.getId(), a -> a));

	public static final String NO_ITEM_FOUND = "no item found!";



	@Override
	public Receive createReceive() {
		return ReceiveBuilder.create()
				.match(getProductMessage.class, message -> {
					ProductItem item = products.get(message.pid);
					if (item != null) {
					Gson gson = new Gson();
						sender().tell(gson.toJson(item), self());
					} else {
						sender().tell(NO_ITEM_FOUND, self());
					}
				})
				.match(decreaseStockMessage.class, message ->{
					ProductItem item = products.get(message.pid);
					item.setStock(item.getStock() - message.stock);
					log().info("decrease stock for product" + message.pid +", stock -" + message.stock);
				})
				.build();
	}



	public static Props props() {
		return Props.create(ProductActor.class);
	}


	public static class decreaseStockMessage{
		private  final String pid;
		private final long stock;



		public decreaseStockMessage(String pid, long stock) {
			this.pid = pid;
			this.stock = stock;
		}
	}


	public static class getProductMessage {
		private final String pid;

		public getProductMessage(String pid) {
			this.pid = pid;
		}
	}
}
