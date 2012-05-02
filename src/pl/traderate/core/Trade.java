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

package pl.traderate.core;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Date;

abstract class Trade extends PerformanceData implements Comparable<Trade>, Identifiable {

	/** */
	protected static int numberOfTradesCreated;

	/** */
	protected final int ID;
	
	protected Position parent;
	
	protected Account account;

	protected Portfolio portfolio;

	protected Date date;

	protected String comment;

	protected String ticker;

	protected boolean closed;

	Trade(Account account, Portfolio portfolio, Date date, String comment, String ticker, BigDecimal quantity, BigDecimal openPrice, BigDecimal commission) {
		this.ID = numberOfTradesCreated++;
		this.account = account;
		this.portfolio = portfolio;
		this.date = new Date(date.getTime());
		this.comment = comment;
		this.ticker = ticker;
		this.quantity = quantity;
		this.openPrice = openPrice;
		this.commission = commission;

		this.openValue = openPrice.multiply(quantity);
	}

	@Override
	public int compareTo(Trade o) {
		if (this.date.compareTo(o.date) == 0) {
			if (this.ID > o.ID) {
				return 1;
			} else if (this.ID < o.ID) {
				return -1;
			}
			return 0;
		}
		return this.date.compareTo(o.date);
	}

	@Override
	public int getID() {
		return ID;
	}

	static void resetIDIncrement() {
		numberOfTradesCreated = 0;
	}

	Position getParent() {
		return parent;
	}

	void setParent(Position position) {
		parent = position;
	}

	Account getAccount() {
		return account;
	}

	String getTicker() {
		return ticker;
	}

	void close(BigDecimal price) {
		closePrice = price;
		closeValue = closePrice.multiply(quantity);
		realizedGain = closeValue.subtract(openValue).subtract(commission);
		realizedGainPercentage = realizedGain.divide(openValue, 4, RoundingMode.HALF_EVEN).multiply(new BigDecimal(100)).setScale(2, RoundingMode.HALF_EVEN);
		closed = true;
	}

	boolean isClosed() {
		return closed;
	}

	abstract void updateQuotes();
}
