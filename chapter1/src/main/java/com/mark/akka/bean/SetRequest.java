package com.mark.akka.bean;

/**
 * Created by markma on 18-9-5.
 */
public class SetRequest {
	private final String mKey;
	private final String mValue;



	public SetRequest(String key, String value) {
		mKey = key;
		mValue = value;
	}



	public String getKey() {
		return mKey;
	}



	public String getValue() {
		return mValue;
	}



	@Override
	public String toString() {
		return "SetRequest{" +
				"mKey='" + mKey + '\'' +
				", mValue='" + mValue + '\'' +
				'}';
	}
}
