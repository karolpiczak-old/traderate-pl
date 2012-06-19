package pl.traderate.data;

/**
 * Current implementation of a caching quote engine.
 *
 * At the moment it's just a barebone adapter class.
 */
public class QuoteEngine extends CachingQuoteEngine {

	private final static QuoteEngine instance = new QuoteEngine();

	private QuoteEngine() {
		super();
	}

	public static QuoteEngine getInstance() {
		return instance;
	}
}
