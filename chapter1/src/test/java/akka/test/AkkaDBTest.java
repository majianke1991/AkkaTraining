package akka.test;

import akka.actor.Actor;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.TestActorRef;
import com.mark.akka.actor.AkkaSimpleDB;
import com.mark.akka.bean.SetRequest;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by markma on 18-9-5.
 */
public class AkkaDBTest {

	ActorSystem mActorSystem = ActorSystem.create();

	@Test
	public void testOnAkkaDB(){
		TestActorRef<AkkaSimpleDB> actorRef = TestActorRef.create(mActorSystem, Props.create(AkkaSimpleDB.class));
		actorRef.tell(new SetRequest("test", "value"), Actor.noSender());
		AkkaSimpleDB simpleDB = actorRef.underlyingActor();
		Assert.assertEquals(simpleDB.getMap().get("test"),"value");

	}

	@Test
	public void testOnAkkaDB2(){
		TestActorRef<AkkaSimpleDB> actorRef = TestActorRef.create(mActorSystem, Props.create(AkkaSimpleDB.class));
		actorRef.tell(new SetRequest("test1", "value1"), Actor.noSender());
		actorRef.tell(new SetRequest("test2", "value2"), Actor.noSender());
		AkkaSimpleDB simpleDB = actorRef.underlyingActor();
		Assert.assertEquals(simpleDB.getMap().get("test1"),"value1");
		Assert.assertEquals(simpleDB.getMap().get("test2"),"value2");

	}
}
