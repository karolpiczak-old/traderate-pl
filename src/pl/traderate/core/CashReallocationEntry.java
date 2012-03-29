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
import java.util.Date;

public abstract class CashReallocationEntry extends PortfolioEntry {

	protected BigDecimal amount;

	protected CashReallocationEntry(Account account, Portfolio portfolio, ArrayList<Tag> tags, Date date, String comment, BigDecimal amount) {
		super(account, portfolio, tags, date, comment);

		this.amount = amount;
	}

	public BigDecimal getAmount() {
		return amount;
	}
}
