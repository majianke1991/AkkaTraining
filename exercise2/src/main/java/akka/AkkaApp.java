package akka;

import akka.actor.*;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.*;
import akka.http.javadsl.model.headers.Cookie;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import akka.http.javadsl.server.directives.CookieDirectives;
import akka.http.javadsl.unmarshalling.StringUnmarshallers;
import akka.http.scaladsl.unmarshalling.Unmarshal;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;
import akka.util.Timeout;
import com.google.gson.Gson;
import scala.Product;
import scala.compat.java8.FutureConverters;

import java.io.IOException;
import java.text.Format;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Formatter;

import akka.pattern.Patterns;
import scala.concurrent.duration.FiniteDuration;

import static scala.compat.java8.FutureConverters.*;

/**
 * Created by markma on 18-9-17.
 */
public class AkkaApp extends AllDirectives {

	public static void main(String[] args) throws IOException {
		AkkaApp app = new AkkaApp();
		System.out.println("Service started");

	}



	ActorSystem system;
	final   Http              http;
	final   ActorMaterializer materializer;
	final   ActorRef          accountActor;
	private int               ProductClientPort = 8081;
	int                    orderSeed = 0;
	Map<String, OrderItem> orders;
	Gson gson = new Gson();



	public AkkaApp() {
		orders = new HashMap<String, OrderItem>();
		ProductApp productSystem = new ProductApp(ProductClientPort);
		system = ActorSystem.create("httpServer");
		accountActor = system.actorOf(AccountActor.props());
		http = Http.get(system);
		materializer = ActorMaterializer.create(system);
		final Flow<HttpRequest, HttpResponse, NotUsed> routeFlow = createRoute().flow(system, materializer);
		final CompletionStage<ServerBinding> binding = http.bindAndHandle(routeFlow,
				ConnectHttp.toHost("localhost", 8080), materializer);

	}



	private Route createRoute() {
		AkkaApp app = this;
		return route(
				path("hello", () ->
						get(() ->
								{
									return complete("<-test --> \nhello world!");
								}
						)),
				path("terminate", () ->
						get(() -> {
									system.terminate();
									return complete("service terminated!");
								}
						))
				, path("login", () -> parameter("name", name ->
						parameter("password", pass -> {
							CompletableFuture<HttpResponse> result = sendLogin(
									new AccountActor.LoginMessage(name, pass))
									.thenApply(success -> HttpResponse.create()
											.withEntity(success ? "Hi, Mr. " + name : "login failed!"));
							return completeWithFuture(result);
						}))),
				path("addtocart",() -> parameter("pid", pid -> optionalCookie("orderId", orderId ->parameter(StringUnmarshallers.LONG,"quantity", quantity->{
					CompletionStage<HttpResponse> productQuery = http.singleRequest(
							HttpRequest.create("http://localhost:" + ProductClientPort + "/getProduct?pid=" + pid)).thenApply(res -> {
						String respStr = ((HttpEntity.Strict)res.entity()).getData().decodeString("utf-8");
						HttpResponse resp = null;
						if (res.status().isSuccess()) {
							ProductItem productItem = gson.fromJson(respStr, ProductItem.class);
							if(productItem.getStock() - quantity>=0 ){
								productItem.setStock(quantity);
								OrderItem orderItem = addItem2Order(productItem,orderId.isPresent()?orderId.get().value():"");
								resp = HttpResponse.create().withEntity("orderId:"+orderItem.getOrderId() +", item added to your cart: \n" + orderItem.toString())
										.addHeader(HttpHeader.parse("set-cookie","orderId="+orderItem.getOrderId()))
										.addHeader(Cookie.create("order",orderItem.getOrderId()));
							}else{
								resp= HttpResponse.create().withEntity("no stock enough, stock level -" +productItem.getStock());
							}

						} else {
							resp = HttpResponse.create().withEntity(ProductActor.NO_ITEM_FOUND);
						}
						return resp;
					});
					return completeWithFuture(productQuery);
				})))),
				path("submitOrder",()-> optionalCookie("orderId",orderId ->{
			if(!orderId.isPresent() || !orders.containsKey(orderId.get().value())){
			return complete("order not existed");
			}else{
				http.singleRequest(HttpRequest.create("http://localhost:" + ProductClientPort + "/submitOrder").withMethod(HttpMethods.POST).withEntity(HttpEntities.create(
						ContentTypes.APPLICATION_JSON, gson.toJson(orders.get(orderId.get().value())))));
				orders.remove(orderId.get().value());
				return complete(HttpResponse.create().withEntity("order submitted").addHeader(HttpHeader.parse("set-cookie","orderId=")));
			}
		}))
		);
	}




	public CompletableFuture<Boolean> sendLogin(AccountActor.LoginMessage loginMessage) {
		CompletionStage<Boolean> cs = toJava(Patterns.ask(accountActor, loginMessage,
				Timeout.durationToTimeout(FiniteDuration.apply(2, TimeUnit.SECONDS)))).thenApply(String::valueOf)
				.thenApply(Boolean::valueOf);
		return (CompletableFuture<Boolean>) cs;
	}


	public OrderItem createOrder(ProductItem productItem){
		OrderItem order = new OrderItem(String.valueOf(this.orderSeed), new ArrayList<ProductItem>(Arrays.asList(productItem)));
		orderSeed ++;
		orders.put(order.getOrderId(),order);
		return order;
	}
	public OrderItem addItem2Order(ProductItem productItem,String orderId){
		OrderItem item = orders.get(orderId);
		if(item==null){
			item = createOrder(productItem);
		}else{
			item.getItems().add(productItem);
		}
		return item;
	}
}
