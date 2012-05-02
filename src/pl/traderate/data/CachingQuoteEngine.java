/*
 * Copyright (C) 2012 Karol Piczak <karol@dvl.pl>
 *
 * This file is part of the TradeRate package.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package pl.traderate.data;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;

class CachingQuoteEngine implements QuoteEngineInterface {

	private long cachingNanoTime = 1000000000L * 30;

	private HashMap<String, Quote> quotes;

	CachingQuoteEngine() {
		quotes = new HashMap<>();
	}

	@Override
	public BigDecimal getLast(String ticker) {
		Quote quote = quotes.get(ticker);
		
		if (quote == null || System.nanoTime() - quote.expirationTime > 0) {
			quote = new Quote(fetchQuote(ticker), System.nanoTime() + cachingNanoTime);
			quotes.put(ticker, quote);
		}
		
		return quote.lastPrice;
	}

	private BigDecimal fetchQuote(String ticker) {
		try {
			URL feed;
			feed = new URL("http://api.traderate.pl/get/equity/" + ticker + "/close");

			URLConnection connection;
			connection = feed.openConnection();

			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String line = in.readLine();
			in.close();

			if (line == null) {
				return null;
			}

			return new BigDecimal(line);
		} catch (Throwable e) {
			return null;
		}
	}

	private class Quote {

		long expirationTime;
		BigDecimal lastPrice;
		
		Quote(BigDecimal lastPrice, long expirationTime) {
			this.lastPrice = lastPrice;
			this.expirationTime = expirationTime;
		}
	}
}
