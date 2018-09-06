package com.mark.akka.actor;

import akka.actor.AbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.pf.ReceiveBuilder;
import com.mark.akka.bean.SetRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by markma on 18-9-5.
 */
public class AkkaSimpleDB extends AbstractActor {

	private final LoggingAdapter      mLog = Logging.getLogger(context().system(), this);
	private final Map<String, Object> mMap = new HashMap<String, Object>();



	@Override
	public Receive createReceive() {
		return ReceiveBuilder.create().match(SetRequest.class, message -> {
			mLog.info("receive message:{}", message);
			getMap().put(message.getKey(), message.getValue());
			mLog.info("all messages :{}", getMap());
		}).matchAny(message -> {
			getLog().error("receive any message");
		}).build();
	}



	public LoggingAdapter getLog() {
		return mLog;
	}



	public Map<String, Object> getMap() {
		return mMap;
	}
}
