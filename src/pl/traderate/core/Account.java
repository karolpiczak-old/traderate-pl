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

import pl.traderate.core.exception.AccountRecalcException;
import pl.traderate.core.exception.EntryInsertionException;
import pl.traderate.core.exception.InternalLogicError;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

/**
 *
 */
class Account implements Identifiable {

	/** */
	private final int ID;

	/** */
	private static int numberOfAccountsCreated;

	/** */
	private String name;

	/** */
	private final ArrayList<JournalEntry> entries;

	/** */
	private HoldingList holdings;

	/** */
	private Date latestEntryDate;

	/** */
	private BigDecimal cashBalance;

	/** */
	private BigDecimal unallocatedCash;

	/** */
	private HashMap<Integer, BigDecimal> cashAllocations;

	/** */
	private AccountDTO DTO;

	/**
	 *
	 * @param name
	 */
	Account(String name) {
		ID = numberOfAccountsCreated++;
		setName(name);
		entries = new ArrayList<>();
		initVolatile();
	}

	/**
	 *
	 */
	private void initVolatile() {
		holdings = new HoldingList();
		cashBalance = BigDecimal.ZERO;
		unallocatedCash = BigDecimal.ZERO;
		latestEntryDate = new Date(0L);
		cashAllocations = new HashMap<>();
	}

	/**
	 *
	 */
	private void wipeCalculations() {
		initVolatile();
	}

	/**
	 *
	 * @throws AccountRecalcException
	 */
	private void recalc() throws AccountRecalcException {
		wipeCalculations();

		ArrayList<JournalEntry> sortedEntries = new ArrayList<>(entries);
		Collections.sort(sortedEntries, new JournalEntry.DateComparator());

		for (JournalEntry entry : sortedEntries) {
			try {
				entry.apply(this);
			} catch (EntryInsertionException e) {
				throw new AccountRecalcException();
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
	public void addEntry(JournalEntry entry) throws EntryInsertionException {
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
			} catch (AccountRecalcException e) {
				entries.remove(entry);
				try {
					recalc();
				} catch (AccountRecalcException e2) {
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
	public void removeEntry(JournalEntry entry) throws EntryInsertionException {
		try {
			entries.remove(entry);
			recalc();
		} catch (AccountRecalcException e) {
			entries.add(entry);
			try {
				recalc();
			} catch (AccountRecalcException e2) {
				throw new InternalLogicError();
			}
			throw new EntryInsertionException();
		}
	}
	
	void update() {
		holdings.update();
	}

	/**
	 *
	 * @param entry
	 * @throws EntryInsertionException
	 */
	public void applyEntry(BuyEquityTransactionEntry entry) throws EntryInsertionException {
		BigDecimal purchaseValue = entry.getCashValue();
		BigDecimal newBalance = cashBalance.subtract(purchaseValue);
		BigDecimal newPortfolioCash = getCashAllocation(entry.getPortfolioID()).subtract(purchaseValue);

		if ((newBalance.compareTo(BigDecimal.ZERO) < 0) || (newPortfolioCash.compareTo(BigDecimal.ZERO) < 0)) {
			throw new EntryInsertionException();
		}

		holdings.open(entry);

		cashBalance = newBalance;
		setCashAllocation(entry.getPortfolioID(), newPortfolioCash);
	}

	public void applyEntry(SellEquityTransactionEntry entry) throws EntryInsertionException {
		BigDecimal sellValue = entry.getCashValue();
		BigDecimal newBalance = cashBalance.add(sellValue);
		BigDecimal newPortfolioCash = getCashAllocation(entry.getPortfolioID()).add(sellValue);

		// New cash balance can be negative when commission paid is greater than proceeds from sale
		if ((newBalance.compareTo(BigDecimal.ZERO) < 0) || (newPortfolioCash.compareTo(BigDecimal.ZERO) < 0)) {
			throw new EntryInsertionException();
		}

		holdings.close(entry);

		cashBalance = newBalance;
		setCashAllocation(entry.getPortfolioID(), newPortfolioCash);
	}

	/**
	 *
	 * @param entry
	 * @throws EntryInsertionException
	 */
	public void applyEntry(CashAllocationEntry entry) throws EntryInsertionException {
		BigDecimal newUnallocatedCash = unallocatedCash.subtract(entry.getAmount());
		BigDecimal newPortfolioCash = getCashAllocation(entry.getPortfolioID()).add(entry.getAmount());

		if (newUnallocatedCash.compareTo(BigDecimal.ZERO) < 0) {
			throw new EntryInsertionException();
		}

		unallocatedCash = newUnallocatedCash;
		setCashAllocation(entry.getPortfolioID(), newPortfolioCash);
	}

	/**
	 *
	 * @param entry
	 * @throws EntryInsertionException
	 */
	public void applyEntry(CashDeallocationEntry entry) throws EntryInsertionException {
		BigDecimal newPortfolioCash = getCashAllocation(entry.getPortfolioID()).subtract(entry.getAmount());
		
		if (newPortfolioCash.compareTo(BigDecimal.ZERO) < 0) {
			throw new EntryInsertionException();
		}

		unallocatedCash = unallocatedCash.add(entry.getAmount());
		setCashAllocation(entry.getPortfolioID(), newPortfolioCash);
	}

	/**
	 *
	 * @param entry
	 */
	public void applyEntry(CashDepositEntry entry) {
		cashBalance = cashBalance.add(entry.getAmount());
		unallocatedCash = unallocatedCash.add(entry.getAmount());
	}

	/**
	 *
	 * @param entry
	 * @throws EntryInsertionException
	 */
	public void applyEntry(CashWithdrawalEntry entry) throws EntryInsertionException {
		BigDecimal newBalance = cashBalance.subtract(entry.getAmount());
		BigDecimal newUnallocatedCash = unallocatedCash.subtract(entry.getAmount());

		if ((newBalance.compareTo(BigDecimal.ZERO) < 0) || (newUnallocatedCash.compareTo(BigDecimal.ZERO) < 0)) {
			throw new EntryInsertionException();
		}

		cashBalance = newBalance;
		unallocatedCash = newUnallocatedCash;
	}

	/**
	 *
	 * @return
	 */
	public int getID() {
		return ID;
	}

	static void resetIDIncrement() {
		numberOfAccountsCreated = 0;
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

	/**
	 * 
	 * @return
	 */
	BigDecimal getCashBalance() {
		return cashBalance;
	}

	/**
	 *
	 * @return
	 */
	public BigDecimal getUnallocatedCash() {
		return unallocatedCash;
	}

	/**
	 *
	 * @param portfolioID
	 * @return
	 */
	BigDecimal getCashAllocation(int portfolioID) {
		BigDecimal amount = cashAllocations.get(portfolioID);

		if (amount == null) {
			amount = BigDecimal.ZERO;
		}

		return amount;
	}

	/**
	 *
	 * @param portfolioID
	 * @param amount
	 */
	private void setCashAllocation(int portfolioID, BigDecimal amount) {
		cashAllocations.put(portfolioID, amount);
	}

	HoldingList getHoldings() {
		return holdings;
	}

	public AccountDTO getDTO() {
		return DTO == null ? new AccountDTO(this) : DTO;
	}

	public ArrayList<JournalEntry> getEntries() {
		return entries;
	}
}
