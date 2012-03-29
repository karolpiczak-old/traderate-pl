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
import pl.traderate.core.exception.PortfolioRecalcException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

/**
 *
 */
class Portfolio implements Identifiable {

	/** */
	private final int id;

	/** */
	private static int numberOfPortfoliosCreated;

	/** */
	private String name;

	/** */
	private final ArrayList<PortfolioEntry> entries;

	/**
	 * Reference to the parent portfolio.
	 *
	 * <tt>null</tt> if portfolio has no ancestors. Should be the case only for the
	 * single unique global portfolio.
	 */
	private Portfolio parent;

	/** */
	private final ArrayList<Portfolio> children;

	/** */
	private final HoldingList holdings;

	/** */
	private BigDecimal cashBalance;

	/** */
	private Date latestEntryDate;

	/**
	 *
	 * @param name
	 */
	Portfolio(String name) {
		id = numberOfPortfoliosCreated++;
		setName(name);
		entries = new ArrayList<PortfolioEntry>();
		children = new ArrayList<Portfolio>();
		holdings = new HoldingList();

		cashBalance = new BigDecimal("0");
		latestEntryDate = new Date(0L);
	}

	/**
	 *
	 * @param name
	 * @param parent
	 */
	Portfolio(String name, Portfolio parent) {
		this(name);

		this.parent = parent;
		parent.children.add(this);
	}

	/**
	 *
	 */
	private void wipeCalculations() {
		holdings.clear();
		cashBalance = new BigDecimal("0");
		latestEntryDate = new Date(0L);
	}

	/**
	 *
	 * @throws PortfolioRecalcException
	 */
	private void recalc() throws PortfolioRecalcException {
		wipeCalculations();

		ArrayList<PortfolioEntry> sortedEntries = new ArrayList<PortfolioEntry>(entries);
		Collections.sort(sortedEntries, new JournalEntry.DateComparator());

		for (PortfolioEntry entry : sortedEntries) {
			try {
				entry.apply(this);
			} catch (EntryInsertionException e) {
				throw new PortfolioRecalcException();
			}
		}

		if (!sortedEntries.isEmpty()) {
			JournalEntry latestEntry = sortedEntries.get(sortedEntries.size() - 1);
			latestEntryDate = latestEntry.getDate();
		}
	}

	/**
	 *
	 * @param entry
	 * @throws EntryInsertionException
	 */
	public void addEntry(PortfolioEntry entry) throws EntryInsertionException {
		// Checks if entry date is newer or equal to latestEntryDate
		if (entry.getDate().compareTo(latestEntryDate) >= 0) {
			entry.apply(this);
			entries.add(entry);
			latestEntryDate = entry.getDate();
		} else {
			try {
				entries.add(entry);
				recalc();
			} catch (PortfolioRecalcException e) {
				entries.remove(entry);
				try {
					recalc();
				} catch (PortfolioRecalcException e2) {
					throw new InternalError();
				}
				throw new EntryInsertionException();
			}
		}
	}

	/**
	 *
	 * @param entry
	 * @throws EntryInsertionException
	 */
	public void removeEntry(PortfolioEntry entry) throws EntryInsertionException {
		try {
			entries.remove(entry);
			recalc();
		} catch (PortfolioRecalcException e) {
			entries.add(entry);
			try {
				recalc();
			} catch (PortfolioRecalcException e2) {
				throw new InternalError();
			}
			throw new EntryInsertionException();
		}
	}

	public void applyEntry(BuyEquityTransactionEntry entry) throws EntryInsertionException {
		BigDecimal purchaseValue = entry.getCashValue();

		BigDecimal newBalance = cashBalance.subtract(purchaseValue);

		if ((newBalance.compareTo(new BigDecimal("0")) < 0)) {
			throw new EntryInsertionException();
		}

		holdings.open(entry);

		cashBalance = newBalance;
	}

	public void applyEntry(SellEquityTransactionEntry entry) throws EntryInsertionException {
		BigDecimal sellValue = entry.getCashValue();

		BigDecimal newBalance = cashBalance.add(sellValue);

		// New cash balance can be negative when commission paid is greater than proceeds from sale
		if ((newBalance.compareTo(new BigDecimal("0")) < 0)) {
			throw new EntryInsertionException();
		}

		holdings.close(entry);

		cashBalance = newBalance;
	}

	/**
	 *
	 * @param entry
	 * @throws EntryInsertionException
	 */
	public void applyEntry(CashAllocationEntry entry) {
		cashBalance = cashBalance.add(entry.getAmount());
	}

	/**
	 *
	 * @param entry
	 * @throws EntryInsertionException
	 */
	public void applyEntry(CashDeallocationEntry entry) throws EntryInsertionException {
		BigDecimal newBalance = cashBalance.subtract(entry.getAmount());

		if ((newBalance.compareTo(new BigDecimal("0")) < 0)) {
			throw new EntryInsertionException();
		}

		cashBalance = newBalance;
	}

	/**
	 *
	 * @return
	 */
	public int getID() {
		return id;
	}

	/**
	 *
	 * @return
	 */
	String getName() {
		return name;
	}

	/**
	 *
	 * @param name
	 */
	void setName(String name) {
		this.name = name;
	}
}
