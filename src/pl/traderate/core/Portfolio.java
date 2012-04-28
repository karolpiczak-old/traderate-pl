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
	private static int numberOfPortfoliosCreated;

	/** */
	private final Journal journal;

	/** */
	private final int ID;

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
	private HoldingList holdings;

	/** */
	private HoldingList aggregatedHoldings;

	/** */
	private BigDecimal cashBalance;

	/** */
	private BigDecimal aggregatedCashBalance;

	/** */
	private Date latestEntryDate;

	/** */
	private PortfolioNodeDTO nodeDTO;

	/** */
	private PortfolioDetailsDTO detailsDTO;

	/**
	 *
	 * @param name
	 */
	Portfolio(Journal journal, String name) {
		this.journal = journal;
		ID = numberOfPortfoliosCreated++;
		setName(name);
		entries = new ArrayList<PortfolioEntry>();
		children = new ArrayList<Portfolio>();

		initVolatile();
	}

	/**
	 *
	 * @param name
	 * @param parent
	 */
	Portfolio(Journal journal, String name, Portfolio parent) {
		this(journal, name);

		this.parent = parent;
		parent.children.add(this);
	}

	private void initVolatile() {
		holdings = new HoldingList();
		aggregatedHoldings = new HoldingList();
		cashBalance = BigDecimal.ZERO;
		aggregatedCashBalance = BigDecimal.ZERO;
		latestEntryDate = new Date(0L);
	}

	/**
	 *
	 */
	private void wipeCalculations() {
		initVolatile();
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

		if (!TradeRateConfig.isDeferredComputationMode()) {
			update();
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
			if (!TradeRateConfig.isDeferredComputationMode()) {
				update();
			}
		} else {
			try {
				entries.add(entry);
				recalc();
			} catch (PortfolioRecalcException e) {
				entries.remove(entry);
				try {
					recalc();
				} catch (PortfolioRecalcException e2) {
					throw new InternalLogicError();
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
				throw new InternalLogicError();
			}
			throw new EntryInsertionException();
		}
	}

	void update() {
		holdings.update();
		updateHoldingsAggregates();
	}

	public void applyEntry(BuyEquityTransactionEntry entry) throws EntryInsertionException {
		holdings.open(entry);
		updateCashBalance();
	}

	public void applyEntry(SellEquityTransactionEntry entry) throws EntryInsertionException {
		holdings.close(entry);
		updateCashBalance();
	}

	/**
	 *
	 * @param entry
	 */
	public void applyEntry(CashAllocationEntry entry) {
		updateCashBalance();
		updateCashAggregates();
	}

	/**
	 *
	 * @param entry
	 */
	public void applyEntry(CashDeallocationEntry entry) {
		updateCashBalance();
		updateCashAggregates();
	}

	/**
	 *
	 */
	private void updateCashBalance() {
		cashBalance = BigDecimal.ZERO;
		
		for (Account account : journal.getAccounts()) {
			cashBalance = cashBalance.add(account.getCashAllocation(this.ID));
		}
	}
	
	/**
	 *
	 *
	 */
	private void updateCashAggregates() {
		aggregatedCashBalance = cashBalance;

		for (Portfolio child : children) {
			aggregatedCashBalance = aggregatedCashBalance.add(child.getAggregatedCashBalance());
		}

		if (parent != null) {
			parent.updateCashAggregates();
		}
	}

	private void updateHoldingsAggregates() {
		aggregatedHoldings = new HoldingList(holdings);

		for (Portfolio child : children) {
			aggregatedHoldings.merge(child.getHoldings());
		}
		
		aggregatedHoldings.update();

		if (parent != null) {
			parent.update();
		}
	}

	/**
	 *
	 * @return
	 */
	public int getID() {
		return ID;
	}

	static void resetIDIncrement() {
		numberOfPortfoliosCreated = 0;
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

	public BigDecimal getCashBalance() {
		return cashBalance;
	}

	public BigDecimal getAggregatedCashBalance() {
		return aggregatedCashBalance;
	}

	HoldingList getHoldings() {
		return holdings;
	}

	HoldingList getAggregatedHoldings() {
		return aggregatedHoldings;
	}

	ArrayList<Portfolio> getChildren() {
		return children;
	}

	public PortfolioNodeDTO getNodeDTO() {
		return nodeDTO == null ? new PortfolioNodeDTO(this) : nodeDTO;
	}

	public PortfolioDetailsDTO getDetailsDTO() {
		return detailsDTO == null ? new PortfolioDetailsDTO(this) : detailsDTO;
	}
}
