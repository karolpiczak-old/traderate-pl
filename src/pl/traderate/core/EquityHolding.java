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

import pl.traderate.core.exception.ObjectNotFoundException;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.TreeSet;

/**
 * A holding in equities.
 */
class EquityHolding extends Holding {

	/**
	 * Position of this holding.
	 */
	protected TreeSet<EquityPosition> positions;

	/**
	 * All trades of this holding.
	 */
	protected TreeSet<EquityTrade> trades;

	/**
	 * Creates a new equities holding.
	 *
	 * @param ticker Name of the held instrument
	 * @param closed True if holding has been closed
	 */
	EquityHolding(String ticker, boolean closed) {
		super(ticker, closed);
		positions = new TreeSet<>();
		trades = new TreeSet<>();
	}

	/**
	 * Creates a copy of a holding object.
	 *
	 * @param holding Holding object to copy
	 */
	EquityHolding(EquityHolding holding) {
		this(holding.ticker, holding.closed);
		
		for (EquityPosition position : holding.positions) {
			this.positions.add(new EquityPosition(position));
		}

		for (EquityTrade trade : holding.trades) {
			this.trades.add(new EquityTrade(trade));
		}
	}

	/**
	 * Merges the given holding object with this holding.
	 *
	 * @param otherHolding A holding object to be merged with
	 */
	void merge(EquityHolding otherHolding) {
		for (EquityPosition otherPosition : otherHolding.positions) {
			EquityPosition thisPosition;
			try {
				thisPosition = ObjectFinder.findByName(otherPosition.name, this.positions);
				thisPosition.merge(otherPosition);
			} catch (ObjectNotFoundException e) {
				this.positions.add(new EquityPosition(otherPosition));
			}
		}
		
		for (EquityTrade otherTrade: otherHolding.trades) {
			this.trades.add(new EquityTrade(otherTrade));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	void update() {
		for (EquityPosition position : positions) {
			position.update();
		}

		quantity = BigDecimal.ZERO;
		openPrice = BigDecimal.ZERO;
		openValue = BigDecimal.ZERO;
		commission = BigDecimal.ZERO;

		if (isClosed()) {
			closePrice = BigDecimal.ZERO;
			closeValue = BigDecimal.ZERO;
			realizedGain = BigDecimal.ZERO;
			realizedGainPercentage = BigDecimal.ZERO;
		}

		for (EquityPosition position : positions) {
			quantity = quantity.add(position.quantity);
			openValue = openValue.add(position.openValue);
			commission = commission.add(position.commission);
			if (isClosed()) {
				closeValue = closeValue.add(position.closeValue);
			}
		}

		if (quantity.signum() == 0) {
			openPrice = BigDecimal.ZERO;
			closePrice = BigDecimal.ZERO;
			commission = BigDecimal.ZERO;
			realizedGain = BigDecimal.ZERO;
			realizedGainPercentage = BigDecimal.ZERO;
		} else {
			openPrice = openValue.divide(quantity, 2, RoundingMode.HALF_EVEN);
			if (isClosed()) {
				closePrice = closeValue.divide(quantity, 2, RoundingMode.HALF_EVEN);
				realizedGain = closeValue.subtract(openValue).subtract(commission);;
				realizedGainPercentage = realizedGain.divide(openValue, 4, RoundingMode.HALF_EVEN).multiply(new BigDecimal(100)).setScale(2, RoundingMode.HALF_EVEN);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	void updateQuotes() {
		for (EquityPosition position : positions) {
			position.updateQuotes();
		}

		marketValue = BigDecimal.ZERO;

		for (EquityPosition position : positions) {
			lastMarketPrice = position.lastMarketPrice;
			if (lastMarketPrice != null) {
				marketValue = marketValue.add(position.marketValue);
			}
		}

		if (marketValue.equals(BigDecimal.ZERO)) {
			lastMarketPrice = null;
			marketValue = null;
			paperGain = null;
			paperGainPercentage = null;
		} else {
			paperGain = marketValue.subtract(openValue).subtract(commission);
			paperGainPercentage = paperGain.divide(openValue, 4, RoundingMode.HALF_EVEN).multiply(new BigDecimal(100)).setScale(2, RoundingMode.HALF_EVEN);
		}
	}

	/**
	 * Adds an equity position to the position list.
	 *
	 * @param position Equity position to be added
	 */
	void attach(EquityPosition position) {
		position.setParent(this);
		positions.add(position);
	}

	/**
	 * Removes an equity position from the position list.
	 *
	 * @param position Equity position to be removed.
	 */
	void detach(EquityPosition position) {
		position.setParent(null);
		positions.remove(position);
	}

	/**
	 * Adds an equity trade to the list of trades.
	 *
	 * @param trade Trade to be added
	 */
	void attach(EquityTrade trade) {
		trades.add(trade);
	}

	/**
	 * Removes an equity trade from the list of trades.
	 *
	 * @param trade Trade to be removed
	 */
	void detach(EquityTrade trade) {
		trades.remove(trade);
	}

	/**
	 * Returns all positions of this holding.
	 *
	 * @return Equity position of this holding
	 */
	TreeSet<EquityPosition> getPositions() {
		return positions;
	}

	/**
	 * Returns all trades of this holding.
	 *
	 * @return Equity trades of this holding
	 */
	TreeSet<EquityTrade> getTrades() {
		return trades;
	}

	/**
	 * Checks if holding is empty.
	 *
	 * @return True if there are no positions
	 */
	boolean isEmpty() {
		return positions.isEmpty();
	}
}
