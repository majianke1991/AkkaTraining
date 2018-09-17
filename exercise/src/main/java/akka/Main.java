package akka;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;

/**
 * Created by markma on 18-9-17.
 */
public class Main {
	public static void main(String[] args){
		ActorSystem actorSystem = ActorSystem.create("addToCartSys");
		 ActorRef addToCart = actorSystem.actorOf(CartActor.props(50),"cartActor");

		 for(int i=0; i<100; i++){
			 addToCart.tell(new CartActor.AddToCartMessage(), ActorRef.noSender());
		 }

	}
}
