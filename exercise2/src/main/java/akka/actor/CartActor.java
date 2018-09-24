package akka.actor;

import akka.http.javadsl.Http;
import akka.http.javadsl.model.HttpEntity;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.headers.Cookie;
import akka.japi.pf.ReceiveBuilder;
import com.google.gson.Gson;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletionStage;

public class CartActor extends AbstractLoggingActor {

	private final ActorRef productActor;
	final   Http              http;
	int orderSeed = 0;
	Map<String, OrderItem> orders;

	public CartActor(ActorRef productActor) {
		orders = new HashMap<String, OrderItem>();
		http = Http.get(getContext().getSystem());
		this.productActor = productActor;
	}



	public static Props props() {
		return Props.create(CartActor.class);
	}



	@Override
	public Receive createReceive() {
		return ReceiveBuilder.create()
				.match(AddToCartMessage.class, message -> {
					CompletionStage<HttpResponse> productQuery = http.singleRequest(
							HttpRequest.create("http://localhost:8081/getProduct?pid=" + message.pid)).thenApply(res -> {
						String respStr = ((HttpEntity.Strict)res.entity()).getData().decodeString("utf-8");
						HttpResponse resp = HttpResponse.create();
						if (res.status().isSuccess()) {
							Gson gson = new Gson();
							ProductItem productItem = gson.fromJson(respStr, ProductItem.class);
							if(productItem.getStock() - message.quantity>=0 ){
								resp.withEntity("item added to your cart");
								resp.addHeader(Cookie.create("order","sd"));
							}else{
								resp.withEntity("");
							}

						} else {
							return resp.withEntity(ProductActor.NO_ITEM_FOUND);
						}
						return resp;
					});

				})
				.build();
	}

	public OrderItem createOrder(ProductItem productItem){
		 OrderItem order = new OrderItem(String.valueOf(this.orderSeed),Arrays.asList(productItem));
		orderSeed ++;
		orders.put(order.getOrderId(),order);
		return order;
	}


	public static class AddToCartMessage {
		private final String pid;
		private final long   quantity;



		public AddToCartMessage(String pid, long quantity) {
			this.pid = pid;
			this.quantity = quantity;
		}
	}

}
