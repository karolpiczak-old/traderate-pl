package pl.traderate.data;

import java.math.BigDecimal;

public class QuoteEngine extends CachedQuoteEngine {

	/** */
	private final static QuoteEngine instance = new QuoteEngine();

	/**
	 *
	 */
	private QuoteEngine() {

	}

	/**
	 *
	 * @return
	 */
	public static QuoteEngine getInstance() {
		return instance;
	}

	public BigDecimal getLast(String ticker) {
		if (ticker.equals("KGHM")) {
			return new BigDecimal("134.00");
		}
		return null;
	}
}
