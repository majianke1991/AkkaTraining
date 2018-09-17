package common;

import java.util.logging.Logger;

/**
 * Created by markma on 18-9-17.
 */
public class Main {

	public static void main(String[] args) {
		final CartService cartService = new CartService(50);
		for (int i = 0; i < 100; i++) {
			Thread thread = new Thread(new Runnable() {
				public void run() {
					cartService.addToCart();
				}
			});
			thread.start();
		}
	}



	static class CartService {
		private long stockLevel;
		private final Logger mLogger = Logger.getLogger("CartService");



		public CartService(long pStockLevel) {
			stockLevel = pStockLevel;
		}


		public synchronized void addToCart() {
			if (stockLevel <= 0l) {
				mLogger.warning("no item in stock");
			} else {
				mLogger.info(Thread.currentThread().getName() + "Bought one item, current instock: " + stockLevel);
				stockLevel--;
			}
		}

	}
}
