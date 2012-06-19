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
 * A real brokerage account with an individual trade history and holdings.
 *
 * <p>This object represents a real-world brokerage account (1:1
 * representation). Each account is treated individually. All accounts use a
 * single level flat structure - no subaccounts are possible.</p>
 *
 * <p>Every journal operation must have a corresponding account. Each account
 * has a reference to the journal operations performed on the account. A history
 * of an account should reflect actual trading as presented on broker's
 * statements. A <em>FIFO</em> methodology is used for handling equity
 * operations pertaining to a specific account.</p>
 *
 * <p>The cash balance of an account represents real world cash that was
 * physically deposited on the account. Available physical cash can be virtually
 * assigned to individual portfolios for performance measurement
 * applications.</p>
 */
class Account implements Identifiable {

	/**
	 * An auto increment value for account ID creation.
	 */
	private static int numberOfAccountsCreated;

	/**
	 * Unique ID of this account. Does not have to correspond to the ID in the
	 * Journal account list.
	 */
	private final int ID;

	/**
	 * The displayed name of this account.
	 */
	private String name;

	/**
	 * List of referenced entries for this account.
	 */
	private final ArrayList<JournalEntry> entries;

	/**
	 * List of all holdings of this account.
	 */
	private HoldingList holdings;

	/**
	 * Date of last operation performed on this account.
	 */
	private Date latestEntryDate;

	/**
	 * Physical cash deposited on this account.
	 *
	 * This amount represents actual real world cash available on this account.
	 * Virtual cash allocations do not influence this value. Only operations
	 * involving real cash changes (e.g. share purchase) are reflected here.
	 */
	private BigDecimal cashBalance;

	/**
	 * The virtual amount of cash that has not been allocated.
	 *
	 * This amount represents the level of virtual (performance based) cash
	 * available on this account. Only cash transfers to/from a portfolio for
	 * performance allocation should be reflected here.
	 */
	private BigDecimal unallocatedCash;

	/**
	 * A map of all cash allocations.
	 *
	 * Presents cash amounts allocated to given portfolio IDs.
	 */
	private HashMap<Integer, BigDecimal> cashAllocations;

	/**
	 * A Data Transfer Object version of this account.
	 */
	private AccountDTO DTO;

	/**
	 * Creates a new account given its name.
	 *
	 * @param name Displayed name of the account
	 */
	Account(String name) {
		ID = numberOfAccountsCreated++;
		setName(name);
		entries = new ArrayList<>();
		initVolatile();
	}

	/**
	 * Creates a new account with a predefined ID.
	 *
	 * <b>Only for internal use.</b>
	 *
	 * @param name Displayed name of the account
	 * @param ID   ID of the account
	 */
	Account(String name, int ID) {
		this.ID = ID;
		numberOfAccountsCreated++;
		setName(name);
		entries = new ArrayList<>();
		initVolatile();
	}

	/**
	 * Initializes all volatile fields.
	 */
	private void initVolatile() {
		holdings = new HoldingList();
		cashBalance = BigDecimal.ZERO;
		unallocatedCash = BigDecimal.ZERO;
		latestEntryDate = new Date(0L);
		cashAllocations = new HashMap<>();
	}

	/**
	 * Purges account state by reinitializing all volatile fields.
	 */
	private void wipeCalculations() {
		initVolatile();
	}

	/**
	 * Recalculates account history from scratch.
	 *
	 * If deferred computation is not enabled, a recalculation will trigger an
	 * {@link update()}.
	 *
	 * @throws AccountRecalcException Thrown when current account history
	 *                                represents an invalid state.
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

		// Do not update when batch loading
		if (!TradeRateConfig.isDeferredComputationMode()) {
			update();
		}
	}

	/**
	 * Adds a new entry to this account's history.
	 *
	 * Specific handling of the entry is performed by designated {@link
	 * applyEntry()} methods (visitor pattern).
	 *
	 * @param entry An entry to be added
	 * @throws EntryInsertionException Thrown when entry insertion fails.
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
	 * Removes an entry from this account's history.
	 *
	 * @param entry Entry to be removed
	 * @throws EntryInsertionException Thrown when entry removal is not possible.
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

	/**
	 * Updates holdings of this account.
	 */
	void update() {
		holdings.update();
	}

	/**
	 * Updates prices of all holdings.
	 */
	void updateQuotes() {
		holdings.updateQuotes();
	}

	/**
	 * Handles a new equity purchase entry.
	 *
	 * Adds a new equity trade to the opened holdings and deducts transaction cash
	 * value from account's cash balance/allocations. Fails when presented with an
	 * insufficient cash balance.
	 *
	 * @param entry A new {@link BuyEquityTransactionEntry}
	 * @throws EntryInsertionException Thrown when entry insertion is not
	 *                                 possible.
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

	/**
	 * Handles a new equity sell entry.
	 *
	 * Sells given equity instrument and increments available cash
	 * balance/allocations. Fails when sell is not possible (insufficient shares,
	 * abnormal commission).
	 *
	 * @param entry A new {@link SellEquityTransactionEntry}
	 * @throws EntryInsertionException Thrown when entry insertion is not
	 *                                 possible.
	 */
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
	 * Handles a new cash allocation entry.
	 *
	 * Transfers a given virtual cash amount from the unallocated cash balance to a
	 * given portfolio.
	 *
	 * @param entry A new {@link CashAllocationEntry}
	 * @throws EntryInsertionException Thrown when entry insertion is not possible
	 *                                 (insufficient funds).
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
	 * Handles a new cash deallocation entry.
	 *
	 * Transfers a given virtual cash amount from a portfolio to the unallocated
	 * cash balance of this account.
	 *
	 * @param entry A new {@link CashDeallocationEntry}
	 * @throws EntryInsertionException Thrown when entry insertion is not possible
	 *                                 (insufficient funds).
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
	 * Handles a new cash deposit entry.
	 *
	 * Records a new physical cash deposit.
	 *
	 * @param entry A new {@link CashDepositEntry}
	 */
	public void applyEntry(CashDepositEntry entry) {
		cashBalance = cashBalance.add(entry.getAmount());
		unallocatedCash = unallocatedCash.add(entry.getAmount());
	}

	/**
	 * Handles a new cash withdrawal entry.
	 *
	 * Records a new physical cash withdrawal. Fails on insufficient funds.
	 *
	 * @param entry A new {@link CashDepositEntry}
	 * @throws EntryInsertionException Thrown when entry insertion is not possible
	 *                                 (insufficient funds).
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
	 * {@inheritDoc}
	 */
	@Override
	public int getID() {
		return ID;
	}

	/**
	 * Resets the static ID auto increment value.
	 */
	static void resetIDIncrement() {
		numberOfAccountsCreated = 0;
	}

	/**
	 * Returns the static ID auto increment value.
	 *
	 * The returned value does not have to correspond to the number of currently
	 * existing accounts.
	 *
	 * @return Number of accounts created historically
	 */
	public static int getNumberOfAccountsCreated() {
		return numberOfAccountsCreated;
	}

	/**
	 * Sets the static ID auto increment value to a given Integer.
	 *
	 * <b>Internal use only. No sanity checks.</b>
	 *
	 * @param numberOfAccountsCreated A new auto increment value
	 */
	static void setNumberOfAccountsCreated(Integer numberOfAccountsCreated) {
		Account.numberOfAccountsCreated = numberOfAccountsCreated;
	}

	/**
	 * Returns a display name of this account.
	 *
	 * @return Display name
	 */
	String getName() {
		return name;
	}

	/**
	 * Sets account name.
	 *
	 * <b>Internal use.</b>
	 *
	 * @param name New account name
	 */
	void setName(String name) {
		this.name = name;
	}

	/**
	 * Returns physical cash available.
	 *
	 * @return Amount of available (physical) cash
	 */
	BigDecimal getCashBalance() {
		return cashBalance;
	}

	/**
	 * Returns virtual cash available.
	 *
	 * @return Amount of unallocated cash
	 */
	public BigDecimal getUnallocatedCash() {
		return unallocatedCash;
	}

	/**
	 * Gets the virtual cash allocation for a given portfolio ID.
	 *
	 * @param portfolioID An ID of a portfolio
	 * @return Portfolio cash allocation
	 */
	BigDecimal getCashAllocation(int portfolioID) {
		BigDecimal amount = cashAllocations.get(portfolioID);

		if (amount == null) {
			amount = BigDecimal.ZERO;
		}

		return amount;
	}

	/**
	 * Sets the virtual cash allocation for a given portfolio ID.
	 *
	 * <b>Internal use. No sanity checks.</b>
	 *
	 * @param portfolioID An ID of a portfolio
	 * @param amount      Cash amount
	 */
	private void setCashAllocation(int portfolioID, BigDecimal amount) {
		cashAllocations.put(portfolioID, amount);
	}

	/**
	 * Returns all holdings of this account.
	 *
	 * @return A list of all account holdings
	 */
	HoldingList getHoldings() {
		return holdings;
	}

	/**
	 * Returns a cached Data Transfer Object version of this account.
	 *
	 * @param portfolios A list of all portfolios
	 * @return An {@link AccountDTO} object
	 */
	public AccountDTO getDTO(ArrayList<Portfolio> portfolios) {
		return DTO == null ? new AccountDTO(this, new AccountCashAllocationsDTO(this, portfolios)) : DTO;
	}

	/**
	 * Returns all journal entries for this account.
	 *
	 * @return List of account's journal entries
	 */
	public ArrayList<JournalEntry> getEntries() {
		return entries;
	}
}
