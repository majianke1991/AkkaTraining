package akka.actor;

import akka.japi.pf.ReceiveBuilder;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AccountActor extends AbstractLoggingActor {

	private final Map<String, String> accounts = Stream.of("ac1", "ac2", "mark")
			.collect(Collectors.toMap((s) -> s, (s) -> "pw_" + s));



	@Override
	public Receive createReceive() {
		return ReceiveBuilder.create()
				.match(LoginMessage.class, this::processLogin)
				.build();
	}



	private void processLogin(LoginMessage pMessage) {
		if (accounts.get(pMessage.name) != null && pMessage.password.equals(accounts.get(pMessage.name))) {
			getSender().tell(true, sender());
		} else {
			getSender().tell(false, sender());

		}
	}



	public static Props props() {
		return Props.create(AccountActor.class);
	}



	public static class LoginMessage {
		final private String name;
		final private String password;



		public LoginMessage(String name, String password) {
			this.name = name;
			this.password = password;
		}
	}
}
