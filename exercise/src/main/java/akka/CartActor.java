package akka;

import akka.actor.AbstractActor;
import akka.actor.AbstractLoggingActor;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import scala.PartialFunction;
import scala.runtime.BoxedUnit;

import java.util.function.BiFunction;

/**
 * Created by markma on 18-9-17.
 */
public class CartActor extends AbstractLoggingActor {

	private long mStockLevel;



	public CartActor(long pStockLevel) {
		mStockLevel = pStockLevel;
	}



	public static Props props(long pStockLevel) {
		return Props.create(CartActor.class, pStockLevel);
	}



	public Receive createReceive() {
		return enable();
	}



	public void addToCart(AddToCartMessage message) {
		if (mStockLevel > 0) {
			log().info("Add to cart success, current stock" + mStockLevel);
			mStockLevel--;
		} else {
			log().error("out of stock, close received cart message");
			this.getContext().become(disable());
		}
	}



	public Receive enable() {
		return ReceiveBuilder.create()
				.match(AddToCartMessage.class, this::addToCart)
				.build();
	}



	public Receive disable() {
		return ReceiveBuilder.create().build();
	}



	static class AddToCartMessage {

	}
}
