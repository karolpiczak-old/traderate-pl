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

import pl.traderate.core.exception.EntryInsertionException;
import pl.traderate.core.exception.ObjectNotFoundException;

import java.util.ArrayList;

/**
 *
 */
final class HoldingList {

	private ArrayList<EquityHolding> equityHoldings;

	HoldingList() {
		equityHoldings = new ArrayList<>();
	}

	void open(BuyEquityTransactionEntry entry) throws EntryInsertionException {
		EquityTrade trade = new EquityTrade(entry.account, entry.portfolio, entry.date, entry.comment, entry.ticker, entry.quantity, entry.price, entry.commission);

		EquityHolding holding;

		try {
			holding = findObjectByName(entry.ticker, equityHoldings);
		} catch (ObjectNotFoundException e) {
			holding = new EquityHolding(entry.ticker);
			equityHoldings.add(holding);
		}
		
		EquityPosition position;

		try {
			position = findObjectByName(entry.position, holding.getPositions());
		} catch (ObjectNotFoundException e) {
			position = new EquityPosition(entry.position);
			holding.attach(position);
		}
		
		position.attach(trade);

		position.update();
		holding.update();
	}

	void close(SellEquityTransactionEntry entry) throws EntryInsertionException {
		
	}

	private <T extends IdentifiableByName> T findObjectByName(String objectName, ArrayList<T> arrayList) throws ObjectNotFoundException {
		T object = null;

		for (T checkedObject : arrayList) {
			if (checkedObject.getName().equals(objectName)) {
				object = checkedObject;
				break;
			}
		}

		if (object == null) {
			throw new ObjectNotFoundException();
		}

		return object;
	}
}
