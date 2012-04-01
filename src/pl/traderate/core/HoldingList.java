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
import pl.traderate.core.exception.InternalLogicError;
import pl.traderate.core.exception.ObjectNotFoundException;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeSet;

/**
 *
 */
final class HoldingList {

	private TreeSet<EquityHolding> equityHoldings;
	private TreeSet<EquityHolding> closedEquityHoldings;

	HoldingList() {
		equityHoldings = new TreeSet<>();
		closedEquityHoldings = new TreeSet<>();
	}

	void open(BuyEquityTransactionEntry entry) throws EntryInsertionException {
		EquityTrade trade = new EquityTrade(entry.account, entry.portfolio, entry.date, entry.comment, entry.ticker, entry.quantity, entry.price, entry.commission);

		EquityHolding holding;

		try {
			holding = findObjectByName(entry.ticker, equityHoldings);
		} catch (ObjectNotFoundException e) {
			holding = new EquityHolding(entry.ticker, false);
			equityHoldings.add(holding);
		}

		EquityPosition position;

		try {
			position = findObjectByName(entry.position, holding.getPositions());
		} catch (ObjectNotFoundException e) {
			position = new EquityPosition(entry.position, false);
			holding.attach(position);
		}

		position.attach(trade);
		holding.attach(trade);
	}

	void close(SellEquityTransactionEntry entry) throws EntryInsertionException {
		EquityHolding holding;

		try {
			holding = findObjectByName(entry.ticker, equityHoldings);
		} catch (ObjectNotFoundException e) {
			throw new EntryInsertionException();
		}

		TreeSet<EquityTrade> trades = holding.getTrades();
		Iterator<EquityTrade> tradeIterator = trades.iterator();
		ArrayList<EquityTrade> tradesToClose = new ArrayList<>();

		BigDecimal sharesFound = BigDecimal.ZERO;

		while (sharesFound.compareTo(entry.quantity) < 0) {
			if (tradeIterator.hasNext()) {
				EquityTrade trade = tradeIterator.next();
				if (trade.getAccount() == entry.account) {
					sharesFound = sharesFound.add(trade.getQuantity());
					tradesToClose.add(trade);
				}
			} else {
				throw new EntryInsertionException();
			}
		}

		BigDecimal sharesLeftToClose = entry.quantity;
		BigDecimal unallocatedCommission = entry.commission;

		for (EquityTrade trade : tradesToClose) {
			BigDecimal partialCommission = trade.getQuantity().divide(entry.quantity,  10, RoundingMode.HALF_EVEN).multiply(entry.commission).setScale(2, RoundingMode.HALF_EVEN);

			if (unallocatedCommission.compareTo(partialCommission) > 0) {
				unallocatedCommission = unallocatedCommission.subtract(partialCommission);
			} else {
				partialCommission = unallocatedCommission;
				unallocatedCommission = BigDecimal.ZERO;
			}

			if (sharesLeftToClose.compareTo(trade.getQuantity()) >= 0) {
				trade.close(entry, partialCommission);
				moveToClosed(trade);
				sharesLeftToClose = sharesLeftToClose.subtract(trade.getQuantity());
			} else {
				EquityTrade partialTrade = trade.divide(sharesLeftToClose);
				partialTrade.close(entry, partialCommission);
				moveToClosed(partialTrade);
			}
		}
	}
	
	void update() {
		for (EquityHolding holding : equityHoldings) {
			holding.update();
		}

		for (EquityHolding holding : closedEquityHoldings) {
			holding.update();
		}
	}

	private void moveToClosed(EquityTrade trade) {
		EquityHolding closedHolding;

		try {
			closedHolding = findObjectByName(trade.getTicker(), closedEquityHoldings);
		} catch (ObjectNotFoundException e) {
			closedHolding = new EquityHolding(trade.getTicker(), true);
			closedEquityHoldings.add(closedHolding);
		}

		EquityPosition closedPosition;

		try {
			closedPosition = findObjectByName(trade.getParent().getName(), closedHolding.getPositions());
		} catch (ObjectNotFoundException e) {
			closedPosition = new EquityPosition(trade.getParent().getName(), true);
			closedHolding.attach(closedPosition);
		}

		EquityHolding openHolding = (EquityHolding) trade.getParent().getParent();
		EquityPosition openPosition = (EquityPosition) trade.getParent();
		
		openHolding.detach(trade);
		openPosition.detach(trade);
		
		if (openHolding.isEmpty()) {
			equityHoldings.remove(openHolding);
		}

		closedPosition.attach(trade);
		closedHolding.attach(trade);
	}

	private <T extends IdentifiableByName> T findObjectByName(String objectName, TreeSet<T> sortedSet) throws ObjectNotFoundException {
		T object = null;

		for (T checkedObject : sortedSet) {
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

	TreeSet<EquityHolding> getEquityHoldings() {
		return equityHoldings;
	}

	TreeSet<EquityHolding> getClosedEquityHoldings() {
		return closedEquityHoldings;
	}
}
