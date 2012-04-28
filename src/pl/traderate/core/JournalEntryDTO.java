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
import java.util.Date;

public class JournalEntryDTO implements Comparable<JournalEntryDTO> {

	public final int ID;
	
	public final Date date;
	
	public final String comment;
	
	public final AccountDTO account;

	public PortfolioNodeDTO portfolio;

	public String type;

	public BigDecimal amount;
	
	public String ticker;

	public BigDecimal quantity;

	public BigDecimal price;

	public BigDecimal commission;
	
	JournalEntryDTO(JournalEntry entry) {
		this.ID = entry.ID;
		this.date = new Date(entry.date.getTime());
		this.comment = entry.comment;
		this.account = entry.account.getDTO();

		entry.apply(this);
	}

	public void setType(BuyEquityTransactionEntry entry) {
		type = "K";
		portfolio = entry.portfolio.getNodeDTO();
		ticker = entry.ticker;
		quantity = entry.quantity;
		price = entry.price;
		commission = entry.commission;
	}

	public void setType(SellEquityTransactionEntry entry) {
		type = "S";
		portfolio = entry.portfolio.getNodeDTO();
		ticker = entry.ticker;
		quantity = entry.quantity;
		price = entry.price;
		commission = entry.commission;
	}

	public void setType(CashAllocationEntry entry) {
		type = "A+";
		amount = entry.amount;
		portfolio = entry.portfolio.getNodeDTO();
	}

	public void setType(CashDeallocationEntry entry) {
		type = "A-";
		amount = entry.amount;
		portfolio = entry.portfolio.getNodeDTO();
	}

	public void setType(CashDepositEntry entry) {
		type = "W+";
		amount = entry.amount;
	}

	public void setType(CashWithdrawalEntry entry) {
		type = "W-";
		amount = entry.amount;
	}

	@Override
	public int compareTo(JournalEntryDTO o) {
		int comparison = date.compareTo(o.date) * -1;
		return (comparison == 0) ? ((Integer) ID).compareTo(o.ID) * -1 : comparison;
	}
}
