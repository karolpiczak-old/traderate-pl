package pl.traderate.data;

public class QuoteEngine extends CachingQuoteEngine {

	/** */
	private final static QuoteEngine instance = new QuoteEngine();

	/**
	 *
	 */
	private QuoteEngine() {
		super();
	}

	/**
	 *
	 * @return
	 */
	public static QuoteEngine getInstance() {
		return instance;
	}
}
