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
 * An analytical portfolio of financial instruments.
 *
 * <p>This object represents a pure virtual portfolio. Portfolios are nested in
 * a tree structure with a unique predefined global portfolio as the root node.</p>
 */
class Portfolio implements Identifiable {

	/**
	 * An auto increment counter.
	 */
	private static int numberOfPortfoliosCreated;

	/**
	 * Handle to the journal object.
	 */
	private final Journal journal;

	/**
	 * Portfolio ID.
	 */
	private final int ID;

	/**
	 * Portfolio name as displayed in navigation.
	 */
	private String name;

	/**
	 * List of journal entries related to this portfolio.
	 */
	private final ArrayList<PortfolioEntry> entries;

	/**
	 * Reference to the parent portfolio.
	 *
	 * <tt>null</tt> if portfolio has no ancestors. Should be the case only for the
	 * single unique global portfolio.
	 */
	private Portfolio parent;

	/**
	 * Children portfolios.
	 */
	private final ArrayList<Portfolio> children;

	/**
	 * List of portfolio holdings.
	 */
	private HoldingList holdings;

	/**
	 * List of holdings aggregated among this portfolio and all subportfolios.
	 */
	private HoldingList aggregatedHoldings;

	/**
	 * Amount of cash available.
	 */
	private BigDecimal cashBalance;

	/**
	 * Amount of cash available (including subportfolios).
	 */
	private BigDecimal aggregatedCashBalance;

	/**
	 * Date of the most recent journal entry.
	 */
	private Date latestEntryDate;

	/**
	 * A streamlined DTO version of this portfolio.
	 */
	private PortfolioNodeDTO nodeDTO;

	/**
	 * A full DTO version of this portfolio.
	 */
	private PortfolioDetailsDTO detailsDTO;

	/**
	 * Creates a new global portfolio.
	 *
	 * @param journal Main journal
	 * @param name Portfolio name
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
	 * Creates a new portfolio.
	 *
	 * @param journal Main journal
	 * @param name Portfolio name
	 * @param parent Parent portfolio
	 */
	Portfolio(Journal journal, String name, Portfolio parent) {
		this(journal, name);

		this.parent = parent;
		parent.children.add(this);
	}

	/**
	 * Creates a new portfolio with a predefined ID.
	 *
	 * Internal use only.
	 *
	 * @param journal Main journal
	 * @param name Portfolio name
	 * @param ID Desired new portfolio ID
	 * @param parent Parent portfolio
	 */
	Portfolio(Journal journal, String name, int ID, Portfolio parent) {
		this.journal = journal;
		this.ID = ID;
		numberOfPortfoliosCreated++;
		setName(name);
		entries = new ArrayList<PortfolioEntry>();
		children = new ArrayList<Portfolio>();

		initVolatile();

		this.parent = parent;
		parent.children.add(this);
	}

	/**
	 * Removes a portfolio from children list.
	 *
	 * @param portfolio Portfolio to be removed
	 */
	void removeChild(Portfolio portfolio) {
		children.remove(portfolio);
	}

	/**
	 * Initializes all portfolio aggregates.
	 */
	private void initVolatile() {
		holdings = new HoldingList();
		aggregatedHoldings = new HoldingList();
		cashBalance = BigDecimal.ZERO;
		aggregatedCashBalance = BigDecimal.ZERO;
		latestEntryDate = new Date(0L);
	}

	/**
	 * Resets all calculated values.
	 */
	private void wipeCalculations() {
		initVolatile();
	}

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
		updateCashBalance();
		updateCashAggregates();
	}

	void updateQuotes() {
		holdings.updateQuotes();
		aggregatedHoldings.updateQuotes();
	}

	public void applyEntry(BuyEquityTransactionEntry entry) throws EntryInsertionException {
		holdings.open(entry);
		updateCashBalance();
	}

	public void applyEntry(SellEquityTransactionEntry entry) throws EntryInsertionException {
		holdings.close(entry);
		updateCashBalance();
	}

	public void applyEntry(CashAllocationEntry entry) {
		updateCashBalance();
		updateCashAggregates();
	}

	public void applyEntry(CashDeallocationEntry entry) {
		updateCashBalance();
		updateCashAggregates();
	}

	private void updateCashBalance() {
		cashBalance = BigDecimal.ZERO;
		
		for (Account account : journal.getAccounts()) {
			cashBalance = cashBalance.add(account.getCashAllocation(this.ID));
		}
	}

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

	public int getID() {
		return ID;
	}

	static void resetIDIncrement() {
		numberOfPortfoliosCreated = 0;
	}

	public static int getNumberOfPortfoliosCreated() {
		return numberOfPortfoliosCreated;
	}

	public static void setNumberOfPortfoliosCreated(Integer numberOfPortfoliosCreated) {
		Portfolio.numberOfPortfoliosCreated = numberOfPortfoliosCreated;
	}

	String getName() {
		return name;
	}

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

	public PortfolioDetailsDTO getDetailsDTO(ArrayList<Account> accounts) {
		return detailsDTO == null ? new PortfolioDetailsDTO(this, new PortfolioCashAllocationsDTO(this, accounts)) : detailsDTO;
	}

	public ArrayList<PortfolioEntry> getEntries() {
		return entries;
	}

	public Portfolio getParent() {
		return parent;
	}
}
