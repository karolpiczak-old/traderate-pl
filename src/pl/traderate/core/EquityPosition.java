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

class EquityPosition extends Position {

	protected TreeSet<EquityTrade> trades;

	EquityPosition(String name, boolean closed) {
		super(name, closed);
		trades = new TreeSet<>();
	}

	@Override
	void update() {
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

		for (EquityTrade trade : trades) {
			quantity = quantity.add(trade.quantity);
			openValue = openValue.add(trade.openValue);
			commission = commission.add(trade.commission);
			if (isClosed()) {
				closeValue = closeValue.add(trade.closeValue);
			}
		}

		if (quantity.signum() == 0) {
			// TODO: This should probably never happen?
			openPrice = BigDecimal.ZERO;
			closePrice = BigDecimal.ZERO;
			commission = BigDecimal.ZERO;
			realizedGain = BigDecimal.ZERO;
			realizedGainPercentage = BigDecimal.ZERO;
		} else {
			openPrice = openValue.divide(quantity, 2, RoundingMode.HALF_EVEN);
			if (isClosed()) {
				closePrice = closeValue.divide(quantity, 2, RoundingMode.HALF_EVEN);
				realizedGain = closeValue.subtract(openValue);
				realizedGainPercentage = realizedGain.divide(openValue, 4, RoundingMode.HALF_EVEN).multiply(new BigDecimal(100)).setScale(2, RoundingMode.HALF_EVEN);
			}
		}
	}
	
	void attach(EquityTrade trade) {
		trade.setParent(this);
		trades.add(trade);
	}
	
	void detach(EquityTrade trade) {
		trade.setParent(null);
		trades.remove(trade);
		if (trades.isEmpty()) {
			((EquityHolding) parent).detach(this);
		}
	}

	TreeSet<EquityTrade> getTrades() {
		return trades;
	}
}
