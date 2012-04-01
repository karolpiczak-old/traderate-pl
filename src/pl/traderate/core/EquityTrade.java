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

class EquityTrade extends Trade {

	EquityTrade(Account account, Portfolio portfolio, Date date, String comment, String ticker, BigDecimal quantity, BigDecimal price, BigDecimal commission) {
		super(account, portfolio, date, comment, ticker, quantity, price, commission);
	}

	/**
	 * Create a clone
	 *
	 * @param equityTrade
	 */
	private EquityTrade(EquityTrade equityTrade) {
		super(equityTrade.account, equityTrade.portfolio, equityTrade.date, equityTrade.comment, equityTrade.ticker, equityTrade.quantity, equityTrade.openPrice, equityTrade.commission);
	}

	void close(SellEquityTransactionEntry entry, BigDecimal allocatedCommission) {
		commission = commission.add(allocatedCommission);
		super.close(entry.price);
	}

	EquityTrade divide(BigDecimal sharesToReturn) {
		EquityTrade tradeToBeClosed;
		EquityTrade tradeStillOpen;
		
		EquityHolding parentHolding = (EquityHolding) parent.getParent();
		EquityPosition parentPosition = (EquityPosition) parent;
		
		tradeToBeClosed = new EquityTrade(this);
		tradeStillOpen = new EquityTrade(this);
		
		tradeToBeClosed.setQuantity(sharesToReturn);
		tradeStillOpen.setQuantity(quantity.subtract(sharesToReturn));

		BigDecimal partialCommission = sharesToReturn.divide(quantity, new MathContext(2, RoundingMode.HALF_EVEN)).multiply(commission, new MathContext(2, RoundingMode.HALF_EVEN));
		tradeToBeClosed.setCommission(partialCommission);
		tradeStillOpen.setCommission(commission.subtract(partialCommission));
		
		tradeToBeClosed.update();
		tradeStillOpen.update();
		
		parentHolding.attach(tradeToBeClosed);
		parentPosition.attach(tradeToBeClosed);
		parentHolding.attach(tradeStillOpen);
		parentPosition.attach(tradeStillOpen);

		parentHolding.detach(this);
		parentPosition.detach(this);
		
		return tradeToBeClosed;
	}

	private void setQuantity(BigDecimal quantity) {
		this.quantity = quantity;
	}

	private void setCommission(BigDecimal commission) {
		this.commission = commission;
	}
	
	private void update() {
		openValue = openPrice.multiply(quantity);
		if (isClosed()) {
			closeValue = closePrice.multiply(quantity);
		}
	}
}
