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
import java.util.ArrayList;

public class PortfolioDetailsDTO {

	public final int ID;

	public final String name;

	public final BigDecimal cashBalance;

	public final BigDecimal aggregatedCashBalance;

	public final BigDecimal currentValue;

	public final BigDecimal openValue;

	public final BigDecimal paperGain;

	public final BigDecimal realizedGain;

	public final BigDecimal realizedIncome;

	public final BigDecimal realizedCost;

	public final BigDecimal value;

	public final HoldingsDTO holdings;

	public final HoldingsDTO aggregatedHoldings;

	public final ArrayList<EntryDTO> entries;

	PortfolioDetailsDTO(Portfolio portfolio) {
		this.ID = portfolio.getID();
		this.name = portfolio.getName();
		this.cashBalance = portfolio.getCashBalance().setScale(2);
		this.aggregatedCashBalance = portfolio.getAggregatedCashBalance().setScale(2);
		this.holdings = new HoldingsDTO(portfolio.getHoldings());
		this.aggregatedHoldings = new HoldingsDTO(portfolio.getAggregatedHoldings());
		if (portfolio.getAggregatedHoldings() != null) {
			this.currentValue = portfolio.getAggregatedHoldings().getCurrentValue();
			this.openValue = portfolio.getAggregatedHoldings().getOpenValue();
			this.paperGain = portfolio.getAggregatedHoldings().getPaperGain();
			this.realizedGain = portfolio.getAggregatedHoldings().getRealizedGain();
			this.realizedIncome = portfolio.getAggregatedHoldings().getRealizedIncome();
			this.realizedCost = portfolio.getAggregatedHoldings().getRealizedCost();
			if (this.currentValue != null) {
				this.value = this.currentValue.add(this.aggregatedCashBalance);
			} else {
				this.value = null;
			}
		} else {
			this.currentValue = null;
			this.openValue = null;
			this.paperGain = null;
			this.realizedGain = null;
			this.realizedIncome = null;
			this.realizedCost = null;
			this.value = null;
		}
		this.entries = new ArrayList<>();
	}

	@Override
	public String toString() {
		return name;
	}
	
}
