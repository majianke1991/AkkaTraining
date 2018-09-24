package akka;

import akka.actor.*;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import akka.http.javadsl.unmarshalling.StringUnmarshallers;
import akka.http.scaladsl.unmarshalling.Unmarshal;
import akka.pattern.Patterns;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;
import akka.stream.javadsl.Flow;
import akka.util.Timeout;
import scala.compat.java8.FutureConverters;
import scala.concurrent.duration.FiniteDuration;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

public class ProductApp extends AllDirectives {


	private final ActorSystem system;
	private final ActorRef productAactor;


	public ProductApp(int port) {
		system = ActorSystem.create("productSystem");
	 productAactor =  system.actorOf(ProductActor.props());
		Http http = Http.get(system);
		Materializer materializer = ActorMaterializer.create(system);
		final Flow<HttpRequest, HttpResponse, NotUsed> routeFlow = createRoute().flow(system, materializer);
		final CompletionStage<ServerBinding> binding = http.bindAndHandle(routeFlow,
				ConnectHttp.toHost("localhost", port), materializer);
	}



	public Route createRoute() {
		return route(
				path("getProduct", () ->
						parameter("pid", pid ->{
//							CompletableFuture<String> result =  (CompletableFuture<String>)FutureConverters.toJava(Patterns.ask(productAactor,new ProductActor.getProductMessage(pid),
//									Timeout.durationToTimeout(FiniteDuration.apply(1000, TimeUnit.SECONDS)))).thenApply(String::valueOf);
							CompletableFuture<HttpResponse> result =  (CompletableFuture<HttpResponse>)FutureConverters.toJava(Patterns.ask(productAactor,new ProductActor.getProductMessage(pid),
									Timeout.durationToTimeout(FiniteDuration.apply(1000, TimeUnit.SECONDS)))).thenApply(String::valueOf).thenApply(
											s->{
												if(ProductActor.NO_ITEM_FOUND.equals(s)){
													return HttpResponse.create().withStatus(StatusCodes.NOT_FOUND).withEntity(s);
												}else{
													return HttpResponse.create().withStatus(StatusCodes.OK).withEntity(s);
												}
											});


							return completeWithFuture(result);
						})),
				path("submitOrder", ()-> post(()->entity(Jackson.unmarshaller(OrderItem.class), orderItem ->{
					for(ProductItem item : orderItem.getItems()){
						productAactor.tell(new ProductActor.decreaseStockMessage(item.getId(),item.getStock()),ActorRef.noSender());
					}
					return complete("order submitted");
				})))

		);
	}
}
