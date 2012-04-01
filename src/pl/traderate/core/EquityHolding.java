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
import java.util.TreeSet;

class EquityHolding extends Holding {

	protected TreeSet<EquityPosition> positions;

	protected TreeSet<EquityTrade> trades;

	EquityHolding(String ticker, boolean closed) {
		super(ticker, closed);
		positions = new TreeSet<>();
		trades = new TreeSet<>();
	}

	@Override
	void update() {
		if (!TradeRateConfig.isDeferredComputationMode()) {
			quantity = BigDecimal.ZERO;
			openPrice = BigDecimal.ZERO;
			openValue = BigDecimal.ZERO;

			if (isClosed()) {
				closePrice = BigDecimal.ZERO;
				closeValue = BigDecimal.ZERO;
				realizedGain = BigDecimal.ZERO;
				realizedGainPercentage = BigDecimal.ZERO;
			}

			for (EquityPosition position : positions) {
				quantity = quantity.add(position.quantity);
				openValue = openValue.add(position.openValue);
				if (isClosed()) {
					closeValue = closeValue.add(position.closeValue);
				}
			}

			if (quantity.signum() == 0) {
				openPrice = BigDecimal.ZERO;
				closePrice = BigDecimal.ZERO;
				realizedGain = BigDecimal.ZERO;
				realizedGainPercentage = BigDecimal.ZERO;
			} else {
				openPrice = openValue.divide(quantity, new MathContext(2, RoundingMode.HALF_EVEN));
				if (isClosed()) {
					closePrice = closeValue.divide(quantity, new MathContext(2, RoundingMode.HALF_EVEN));
					realizedGain = closeValue.subtract(openValue);
					realizedGainPercentage = realizedGain.divide(openValue, new MathContext(2, RoundingMode.HALF_EVEN));
				}
			}
		}
	}

	void attach(EquityPosition position) {
		position.setParent(this);
		positions.add(position);
	}

	void detach(EquityPosition position) {
		position.setParent(null);
		positions.remove(position);
	}
	
	void attach(EquityTrade trade) {
		trades.add(trade);
	}
	
	void detach(EquityTrade trade) {
		trades.remove(trade);
	}

	TreeSet<EquityPosition> getPositions() {
		return positions;
	}

	TreeSet<EquityTrade> getTrades() {
		return trades;
	}

	boolean isEmpty() {
		return positions.isEmpty();
	}
}
