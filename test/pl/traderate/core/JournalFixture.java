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

import junit.extensions.PA;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import pl.traderate.core.exception.EntryInsertionException;
import pl.traderate.core.exception.InvalidInputException;
import pl.traderate.core.exception.ObjectConstraintsException;
import pl.traderate.core.exception.ObjectNotFoundException;
import pl.traderate.test.TestNotImplementedError;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.GregorianCalendar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class JournalFixture {

	private Journal journal;
	private ArrayList<Account> accounts;
	private ArrayList<JournalEntry> entries;
	private ArrayList<Portfolio> portfolios;
	private ArrayList<Tag> tags;

	@Before
	@SuppressWarnings("unchecked")
	public void setUp() throws NoSuchFieldException {
		journal = new Journal("Secret trade journal", "John Doe");
		accounts = (ArrayList<Account>) PA.getValue(journal, "accounts");
		portfolios = (ArrayList<Portfolio>) PA.getValue(journal, "portfolios");
		entries = (ArrayList<JournalEntry>) PA.getValue(journal, "entries");
		tags = (ArrayList<Tag>) PA.getValue(journal, "tags");
	}

	@After
	public void tearDown() {
		journal = null;
		accounts = null;
		portfolios = null;
		entries = null;
		tags = null;
	}

	@Test
	public void shouldAddAccounts() {
		assertEquals(0, accounts.size());

		journal.addAccount("Test account #1");
		assertEquals(1, accounts.size());
		assertEquals("Test account #1", accounts.get(0).getName());

		journal.addAccount("Test account #2");
		journal.addAccount("Test account #3");
		journal.addAccount("Test account #4");
		journal.addAccount("Test account #5");

		assertEquals(5, accounts.size());
		assertEquals("Test account #4", accounts.get(3).getName());
		assertEquals(3, accounts.get(3).getID());
	}

	@Test
	public void shouldAddPortfolios() throws NoSuchFieldException, ObjectNotFoundException {
		assertEquals(1, portfolios.size());

		// Check existence of a global portfolio (root node)
		Portfolio topPortfolio = portfolios.get(0);
		assertEquals(0, topPortfolio.getID());
		assertNull(PA.getValue(topPortfolio, "parent"));

		journal.addPortfolio("Test portfolio #1", 0);
		journal.addPortfolio("Test portfolio #2", 0);
		journal.addPortfolio("Test portfolio #3", 0);
		journal.addPortfolio("Test portfolio #1.1", 1);
		journal.addPortfolio("Test portfolio #2.1", 2);
		journal.addPortfolio("Test portfolio #3.1", 3);
		journal.addPortfolio("Test portfolio #1.2", 1);
		journal.addPortfolio("Test portfolio #1.2.1", 7);

		assertEquals(9, portfolios.size());

		// Verify proper tree structure
		assertEquals(portfolios.get(0), PA.getValue(portfolios.get(1), "parent"));
		assertEquals(portfolios.get(0), PA.getValue(portfolios.get(2), "parent"));
		assertEquals(portfolios.get(0), PA.getValue(portfolios.get(3), "parent"));
		assertEquals(portfolios.get(1), PA.getValue(portfolios.get(4), "parent"));
		assertEquals(portfolios.get(2), PA.getValue(portfolios.get(5), "parent"));
		assertEquals(portfolios.get(3), PA.getValue(portfolios.get(6), "parent"));
		assertEquals(portfolios.get(1), PA.getValue(portfolios.get(7), "parent"));
		assertEquals(portfolios.get(7), PA.getValue(portfolios.get(8), "parent"));
	}

	@Test
	public void shouldHandleCashOperations() throws EntryInsertionException, ObjectNotFoundException, InvalidInputException {
		journal.addAccount("Test account #1");
		assertEquals(BigDecimal.ZERO, accounts.get(0).getCashBalance());

		journal.addCashDepositEntry(0, "Example tag", new GregorianCalendar(2000, 0, 1).getTime(), "Some comment", new BigDecimal("1000.00"));
		assertEquals(new BigDecimal("1000.00"), accounts.get(0).getCashBalance());

		journal.addCashDepositEntry(0, "Example tag", new GregorianCalendar(2000, 0, 1).getTime(), "Some comment", new BigDecimal("500.00"));
		assertEquals(new BigDecimal("1500.00"), accounts.get(0).getCashBalance());

		// Check insertion with earlier date
		journal.addCashDepositEntry(0, "Example tag", new GregorianCalendar(1999, 0, 1).getTime(), "Some comment", new BigDecimal("250.00"));
		assertEquals(new BigDecimal("1750.00"), accounts.get(0).getCashBalance());

		journal.addCashWithdrawalEntry(0, "Example tag", new GregorianCalendar(2001, 0, 1).getTime(), "Some comment", new BigDecimal("1000.00"));
		assertEquals(new BigDecimal("750.00"), accounts.get(0).getCashBalance());

		// Check insertion with earlier date
		journal.addCashWithdrawalEntry(0, "Example tag", new GregorianCalendar(1999, 5, 1).getTime(), "Some comment", new BigDecimal("100.00"));
		assertEquals(new BigDecimal("650.00"), accounts.get(0).getCashBalance());
	}

	@Test(expected=EntryInsertionException.class)
	public void shouldRejectCashOperationGivenInsufficientFunds() throws EntryInsertionException, ObjectNotFoundException, InvalidInputException {
		journal.addAccount("Test account #1");
		assertEquals(BigDecimal.ZERO, accounts.get(0).getCashBalance());

		journal.addCashDepositEntry(0, "Example tag", new GregorianCalendar(2000, 0, 1).getTime(), "Some comment", new BigDecimal("1000.00"));
		assertEquals(new BigDecimal("1000.00"), accounts.get(0).getCashBalance());

		journal.addCashWithdrawalEntry(0, "Example tag", new GregorianCalendar(2001, 0, 1).getTime(), "Some comment", new BigDecimal("2000.00"));
	}

	@Test(expected=EntryInsertionException.class)
	public void shouldRejectCashOperationGivenPastInsufficientFunds() throws EntryInsertionException, ObjectNotFoundException, InvalidInputException {
		journal.addAccount("Test account #1");
		assertEquals(BigDecimal.ZERO, accounts.get(0).getCashBalance());

		journal.addCashDepositEntry(0, "Example tag", new GregorianCalendar(2000, 0, 1).getTime(), "Some comment", new BigDecimal("1000.00"));
		assertEquals(new BigDecimal("1000.00"), accounts.get(0).getCashBalance());

		journal.addCashDepositEntry(0, "Example tag", new GregorianCalendar(2002, 0, 1).getTime(), "Some comment", new BigDecimal("1000.00"));
		assertEquals(new BigDecimal("2000.00"), accounts.get(0).getCashBalance());

		journal.addCashWithdrawalEntry(0, "Example tag", new GregorianCalendar(2001, 0, 1).getTime(), "Some comment", new BigDecimal("1500.00"));
	}

	@Test
	public void shouldHandleCashReallocations() throws EntryInsertionException, ObjectNotFoundException, InvalidInputException {
		journal.addAccount("Test account #1");
		journal.addAccount("Test account #2");

		journal.addPortfolio("Test portfolio #1", 0);       // ID: 1
		journal.addPortfolio("Test portfolio #2", 0);       // ID: 2
		journal.addPortfolio("Test portfolio #3", 0);       // ID: 3
		journal.addPortfolio("Test portfolio #1.1", 1);     // ID: 4
		journal.addPortfolio("Test portfolio #2.1", 2);     // ID: 5
		journal.addPortfolio("Test portfolio #3.1", 3);     // ID: 6
		journal.addPortfolio("Test portfolio #1.2", 1);     // ID: 7
		journal.addPortfolio("Test portfolio #1.2.1", 7);   // ID: 8

		journal.addCashDepositEntry(0, "Example tag", new GregorianCalendar(2000, 0, 1).getTime(), "Some comment", new BigDecimal("10000.00"));
		journal.addCashDepositEntry(1, "Example tag", new GregorianCalendar(2000, 0, 1).getTime(), "Some comment", new BigDecimal("10000.00"));

		assertEquals(new BigDecimal("10000.00"), accounts.get(0).getCashBalance());
		assertEquals(new BigDecimal("10000.00"), accounts.get(0).getUnallocatedCash());
		assertEquals(new BigDecimal("10000.00"), accounts.get(1).getCashBalance());
		assertEquals(new BigDecimal("10000.00"), accounts.get(1).getUnallocatedCash());

		assertEquals(BigDecimal.ZERO, portfolios.get(0).getCashBalance());
		assertEquals(BigDecimal.ZERO, portfolios.get(1).getCashBalance());
		assertEquals(BigDecimal.ZERO, portfolios.get(2).getCashBalance());
		assertEquals(BigDecimal.ZERO, portfolios.get(3).getCashBalance());
		assertEquals(BigDecimal.ZERO, portfolios.get(4).getCashBalance());
		assertEquals(BigDecimal.ZERO, portfolios.get(5).getCashBalance());
		assertEquals(BigDecimal.ZERO, portfolios.get(6).getCashBalance());
		assertEquals(BigDecimal.ZERO, portfolios.get(7).getCashBalance());
		assertEquals(BigDecimal.ZERO, portfolios.get(8).getCashBalance());

		// Allocate 1000.00 from Account #1 to Portfolio #1
		journal.addCashAllocationEntry(0, 1, "Example tag", new GregorianCalendar(2002, 0, 1).getTime(), "Some comment", new BigDecimal("1000.00"));
		assertEquals(new BigDecimal("10000.00"), accounts.get(0).getCashBalance());
		assertEquals(new BigDecimal("9000.00"), accounts.get(0).getUnallocatedCash());
		assertEquals(new BigDecimal("1000.00"), portfolios.get(1).getCashBalance());
		assertEquals(BigDecimal.ZERO, portfolios.get(0).getCashBalance());
		assertEquals(new BigDecimal("1000.00"), portfolios.get(0).getAggregatedCashBalance());

		// Check earlier date
		// Allocate 5000.00 from Account #2 to Portfolio #1.2.1
		journal.addCashAllocationEntry(1, 8, "Example tag", new GregorianCalendar(2001, 0, 1).getTime(), "Some comment", new BigDecimal("5000.00"));
		assertEquals(new BigDecimal("10000.00"), accounts.get(1).getCashBalance());
		assertEquals(new BigDecimal("5000.00"), accounts.get(1).getUnallocatedCash());
		assertEquals(new BigDecimal("5000.00"), portfolios.get(8).getCashBalance());
		assertEquals(BigDecimal.ZERO, portfolios.get(7).getCashBalance()); // Portfolio #1.2
		assertEquals(new BigDecimal("1000.00"), portfolios.get(1).getCashBalance()); // Portfolio #1, 1000.00 still from previous case
		assertEquals(new BigDecimal("5000.00"), portfolios.get(7).getAggregatedCashBalance()); // Portfolio #1.2
		assertEquals(new BigDecimal("6000.00"), portfolios.get(1).getAggregatedCashBalance()); // Portfolio #1
		assertEquals(new BigDecimal("6000.00"), portfolios.get(0).getAggregatedCashBalance()); // Portfolio #0

		// Deallocate 2000.00 from Portfolio #1.2.1 to Account #2
		journal.addCashDeallocationEntry(1, 8, "Example tag", new GregorianCalendar(2003, 0, 1).getTime(), "Some comment", new BigDecimal("2000.00"));
		assertEquals(new BigDecimal("10000.00"), accounts.get(1).getCashBalance());
		assertEquals(new BigDecimal("7000.00"), accounts.get(1).getUnallocatedCash());
		assertEquals(new BigDecimal("3000.00"), portfolios.get(8).getCashBalance());
		assertEquals(BigDecimal.ZERO, portfolios.get(7).getCashBalance()); // Portfolio #1.2
		assertEquals(new BigDecimal("1000.00"), portfolios.get(1).getCashBalance()); // Portfolio #1, 1000.00 still from previous case
		assertEquals(new BigDecimal("3000.00"), portfolios.get(7).getAggregatedCashBalance()); // Portfolio #1.2
		assertEquals(new BigDecimal("4000.00"), portfolios.get(1).getAggregatedCashBalance()); // Portfolio #1
		assertEquals(new BigDecimal("4000.00"), portfolios.get(0).getAggregatedCashBalance()); // Portfolio #0

		// Deallocate 500.00 from Portfolio #1 to Account #1
		journal.addCashDeallocationEntry(0, 1, "Example tag", new GregorianCalendar(2002, 5, 1).getTime(), "Some comment", new BigDecimal("500.00"));
		assertEquals(new BigDecimal("10000.00"), accounts.get(0).getCashBalance());
		assertEquals(new BigDecimal("9500.00"), accounts.get(0).getUnallocatedCash());
		assertEquals(new BigDecimal("500.00"), portfolios.get(1).getCashBalance());
		assertEquals(BigDecimal.ZERO, portfolios.get(0).getCashBalance());
		assertEquals(new BigDecimal("3500.00"), portfolios.get(0).getAggregatedCashBalance());

		// Deallocate another 500.00 from Portfolio #1 to Account #1
		journal.addCashDeallocationEntry(0, 1, "Example tag", new GregorianCalendar(2002, 6, 1).getTime(), "Some comment", new BigDecimal("500.00"));
		assertEquals(new BigDecimal("10000.00"), accounts.get(0).getCashBalance());
		assertEquals(new BigDecimal("10000.00"), accounts.get(0).getUnallocatedCash());
		assertEquals(new BigDecimal("0.00"), portfolios.get(1).getCashBalance());
		assertEquals(BigDecimal.ZERO, portfolios.get(0).getCashBalance());
		assertEquals(new BigDecimal("3000.00"), portfolios.get(0).getAggregatedCashBalance());
	}
	
	@Test(expected=EntryInsertionException.class)
	public void shouldRejectCashReallocationGivenInsufficientAccountFunds() throws ObjectNotFoundException, EntryInsertionException, InvalidInputException {
		journal.addAccount("Test account #1");
		journal.addPortfolio("Test portfolio #1", 0);
		journal.addCashDepositEntry(0, "Example tag", new GregorianCalendar(2000, 0, 1).getTime(), "Some comment", new BigDecimal("10000.00"));
		journal.addCashAllocationEntry(0, 1, "Example tag", new GregorianCalendar(2000, 0, 2).getTime(), "Some comment", new BigDecimal("15000.00"));
	}

	@Test(expected=EntryInsertionException.class)
	public void shouldRejectCashReallocationGivenPastInsufficientAccountFunds() throws ObjectNotFoundException, EntryInsertionException, InvalidInputException {
		journal.addAccount("Test account #1");
		journal.addPortfolio("Test portfolio #1", 0);
		journal.addCashDepositEntry(0, "Example tag", new GregorianCalendar(2000, 0, 1).getTime(), "Some comment", new BigDecimal("10000.00"));
		journal.addCashDepositEntry(0, "Example tag", new GregorianCalendar(2001, 0, 1).getTime(), "Some comment", new BigDecimal("10000.00"));
		journal.addCashAllocationEntry(0, 1, "Example tag", new GregorianCalendar(2000, 0, 2).getTime(), "Some comment", new BigDecimal("15000.00"));
	}

	@Test(expected=EntryInsertionException.class)
	public void shouldRejectCashReallocationGivenInsufficientPortfolioFunds() throws ObjectNotFoundException, EntryInsertionException, InvalidInputException {
		journal.addAccount("Test account #1");
		journal.addPortfolio("Test portfolio #1", 0);
		journal.addCashDepositEntry(0, "Example tag", new GregorianCalendar(2000, 0, 1).getTime(), "Some comment", new BigDecimal("10000.00"));
		journal.addCashAllocationEntry(0, 1, "Example tag", new GregorianCalendar(2000, 0, 2).getTime(), "Some comment", new BigDecimal("5000.00"));
		journal.addCashDeallocationEntry(0, 1, "Example tag", new GregorianCalendar(2000, 0, 3).getTime(), "Some comment", new BigDecimal("7000.00"));
	}

	@Test(expected=EntryInsertionException.class)
	public void shouldRejectCashReallocationGivenPastInsufficientPortfolioFunds() throws ObjectNotFoundException, EntryInsertionException, InvalidInputException {
		journal.addAccount("Test account #1");
		journal.addPortfolio("Test portfolio #1", 0);
		journal.addCashDepositEntry(0, "Example tag", new GregorianCalendar(2000, 0, 1).getTime(), "Some comment", new BigDecimal("10000.00"));
		journal.addCashAllocationEntry(0, 1, "Example tag", new GregorianCalendar(2000, 0, 2).getTime(), "Some comment", new BigDecimal("5000.00"));
		journal.addCashAllocationEntry(0, 1, "Example tag", new GregorianCalendar(2000, 0, 5).getTime(), "Some comment", new BigDecimal("3000.00"));
		journal.addCashDeallocationEntry(0, 1, "Example tag", new GregorianCalendar(2000, 0, 3).getTime(), "Some comment", new BigDecimal("7000.00"));
	}

	@Test(expected=InvalidInputException.class)
	public void shouldRejectCashDepositGivenNegativeAmount() throws ObjectNotFoundException, EntryInsertionException, InvalidInputException {
		journal.addAccount("Test account #1");
		journal.addPortfolio("Test portfolio #1", 0);
		journal.addCashDepositEntry(0, "Example tag", new GregorianCalendar(2000, 0, 1).getTime(), "Some comment", new BigDecimal("-1000.00"));
	}

	@Test(expected=InvalidInputException.class)
	public void shouldRejectCashWithdrawalGivenNegativeAmount() throws ObjectNotFoundException, EntryInsertionException, InvalidInputException {
		journal.addAccount("Test account #1");
		journal.addPortfolio("Test portfolio #1", 0);
		journal.addCashDepositEntry(0, "Example tag", new GregorianCalendar(2000, 0, 1).getTime(), "Some comment", new BigDecimal("1000.00"));
		journal.addCashWithdrawalEntry(0, "Example tag", new GregorianCalendar(2000, 0, 1).getTime(), "Some comment", new BigDecimal("-500.00"));
	}

	@Test(expected=InvalidInputException.class)
	public void shouldRejectCashAllocationGivenNegativeAmount() throws ObjectNotFoundException, EntryInsertionException, InvalidInputException {
		journal.addAccount("Test account #1");
		journal.addPortfolio("Test portfolio #1", 0);
		journal.addCashDepositEntry(0, "Example tag", new GregorianCalendar(2000, 0, 1).getTime(), "Some comment", new BigDecimal("1000.00"));
		journal.addCashAllocationEntry(0, 1, "Example tag", new GregorianCalendar(2000, 0, 2).getTime(), "Some comment", new BigDecimal("-500.00"));
	}

	@Test(expected=InvalidInputException.class)
	public void shouldRejectCashDeallocationGivenNegativeAmount() throws ObjectNotFoundException, EntryInsertionException, InvalidInputException {
		journal.addAccount("Test account #1");
		journal.addPortfolio("Test portfolio #1", 0);
		journal.addCashDepositEntry(0, "Example tag", new GregorianCalendar(2000, 0, 1).getTime(), "Some comment", new BigDecimal("1000.00"));
		journal.addCashAllocationEntry(0, 1, "Example tag", new GregorianCalendar(2000, 0, 2).getTime(), "Some comment", new BigDecimal("500.00"));
		journal.addCashDeallocationEntry(0, 1, "Example tag", new GregorianCalendar(2000, 0, 2).getTime(), "Some comment", new BigDecimal("-300.00"));
	}

	@Test
	public void shouldHandleEquityOperations() throws ObjectNotFoundException, InvalidInputException, EntryInsertionException, ObjectConstraintsException {
		journal.addAccount("Test account #1");              // ID: 0
		journal.addAccount("Test account #2");              // ID: 1

		journal.addPortfolio("Test portfolio #1", 0);       // ID: 1
		journal.addPortfolio("Test portfolio #2", 0);       // ID: 2
		journal.addPortfolio("Test portfolio #1.1", 1);     // ID: 3
		journal.addPortfolio("Test portfolio #1.2", 2);     // ID: 4

		journal.addCashDepositEntry(0, "Example tag", new GregorianCalendar(2000, 0, 1).getTime(), "Some comment", new BigDecimal("10000.00"));
		journal.addCashDepositEntry(1, "Example tag", new GregorianCalendar(2000, 0, 1).getTime(), "Some comment", new BigDecimal("10000.00"));

		journal.addCashAllocationEntry(0, 1, "Example tag", new GregorianCalendar(2000, 0, 2).getTime(), "Some comment", new BigDecimal("1000.00"));
		journal.addCashAllocationEntry(0, 2, "Example tag", new GregorianCalendar(2000, 0, 2).getTime(), "Some comment", new BigDecimal("1000.00"));
		journal.addCashAllocationEntry(1, 3, "Example tag", new GregorianCalendar(2000, 0, 2).getTime(), "Some comment", new BigDecimal("1000.00"));
		journal.addCashAllocationEntry(1, 4, "Example tag", new GregorianCalendar(2000, 0, 2).getTime(), "Some comment", new BigDecimal("1000.00"));

		// Check buy entries with random date order
		journal.addBuyEquityTransactionEntry(0, 1, "Example tag", new GregorianCalendar(2000, 0, 3).getTime(), "Some comment", "TICKER-A", new BigDecimal("10"), new BigDecimal("5.00"), new BigDecimal("10.00"));
		journal.addBuyEquityTransactionEntry(0, 1, "Example tag", new GregorianCalendar(2000, 1, 4).getTime(), "Some comment", "TICKER-A", new BigDecimal("10"), new BigDecimal("10.00"), new BigDecimal("5.00"));
		journal.addBuyEquityTransactionEntry(0, 1, "Example tag", new GregorianCalendar(2000, 0, 2).getTime(), "Some comment", "TICKER-A", new BigDecimal("10"), new BigDecimal("15.00"), new BigDecimal("0.00"));

		verifyEquityHoldingsAfterPurchase(accounts.get(0));
		verifyEquityHoldingsAfterPurchase(portfolios.get(1));

		journal.addSellEquityTransactionEntry(0, 1, "Example tag", new GregorianCalendar(2001, 0, 5).getTime(), "Some comment", "TICKER-A", new BigDecimal("5"), new BigDecimal("20.00"), new BigDecimal("0.00"));
		journal.addSellEquityTransactionEntry(0, 1, "Example tag", new GregorianCalendar(2000, 0, 6).getTime(), "Some comment", "TICKER-A", new BigDecimal("3"), new BigDecimal("10.00"), new BigDecimal("0.00"));

		verifyEquityHoldingsAfterSell(accounts.get(0));
		verifyEquityHoldingsAfterSell(portfolios.get(1));

		// Clear all open positions, gain 1.00 x 22 = 22.00, commission paid 11.00 => Realized gain = 10.00 (previous) + 11.00 (now) = 21.00
		journal.addSellEquityTransactionEntry(0, 1, "Example tag", new GregorianCalendar(2001, 0, 6).getTime(), "Some comment", "TICKER-A", new BigDecimal("22"), new BigDecimal("9.18"), new BigDecimal("11.00"));

		verifyEquityHoldingsAfterCompleteSell(accounts.get(0));
		verifyEquityHoldingsAfterCompleteSell(portfolios.get(1));
	}

	private class HasAHoldingListAdapter {
		
		private HoldingList holdings;
		
		HoldingList getHoldings() {
			return holdings;
		}

		HasAHoldingListAdapter(Account account) {
			this.holdings = account.getHoldings();
		}

		HasAHoldingListAdapter(Portfolio portfolio) {
			this.holdings = portfolio.getHoldings();
		}
	}
	
	private void verifyEquityHoldingsAfterPurchase(Account account) {
		verifyEquityHoldingsAfterPurchase(new HasAHoldingListAdapter(account));
	}

	private void verifyEquityHoldingsAfterPurchase(Portfolio portfolio) {
		verifyEquityHoldingsAfterPurchase(new HasAHoldingListAdapter(portfolio));
	}
	
	private void verifyEquityHoldingsAfterPurchase(HasAHoldingListAdapter holder) {
		ArrayList<EquityHolding> openHoldings;
		ArrayList<EquityPosition> openPositions;
		ArrayList<EquityTrade> openTrades;

		assertTrue(holder.getHoldings().getClosedEquityHoldings().isEmpty());

		openHoldings = new ArrayList<>(holder.getHoldings().getEquityHoldings());

		assertEquals(1, openHoldings.size());
		assertEquals("TICKER-A", openHoldings.get(0).getName());
		assertTrue(new BigDecimal("30").compareTo(openHoldings.get(0).getQuantity()) == 0);
		assertTrue(new BigDecimal("10").compareTo(openHoldings.get(0).getOpenPrice()) == 0);
		assertTrue(new BigDecimal("300").compareTo(openHoldings.get(0).getOpenValue()) == 0);
		assertTrue(new BigDecimal("15").compareTo(openHoldings.get(0).getCommission()) == 0);

		openPositions = new ArrayList<>(openHoldings.get(0).getPositions());

		assertEquals(2, openPositions.size());
		assertEquals("2000-01", openPositions.get(0).getName());
		assertTrue(new BigDecimal("20").compareTo(openPositions.get(0).getQuantity()) == 0);
		assertTrue(new BigDecimal("10").compareTo(openPositions.get(0).getOpenPrice()) == 0);
		assertTrue(new BigDecimal("200").compareTo(openPositions.get(0).getOpenValue()) == 0);
		assertTrue(new BigDecimal("10").compareTo(openPositions.get(0).getCommission()) == 0);
		assertEquals("2000-02", openPositions.get(1).getName());
		assertTrue(new BigDecimal("10").compareTo(openPositions.get(1).getQuantity()) == 0);
		assertTrue(new BigDecimal("10").compareTo(openPositions.get(1).getOpenPrice()) == 0);
		assertTrue(new BigDecimal("100").compareTo(openPositions.get(1).getOpenValue()) == 0);
		assertTrue(new BigDecimal("5").compareTo(openPositions.get(1).getCommission()) == 0);

		// Check for proper ordering too!
		openTrades = new ArrayList<>(openPositions.get(0).getTrades());

		assertEquals(2, openTrades.size());
		assertTrue(new BigDecimal("10").compareTo(openTrades.get(0).getQuantity()) == 0);
		assertTrue(new BigDecimal("15").compareTo(openTrades.get(0).getOpenPrice()) == 0);
		assertTrue(new BigDecimal("150").compareTo(openTrades.get(0).getOpenValue()) == 0);
		assertTrue(new BigDecimal("0").compareTo(openTrades.get(0).getCommission()) == 0);
		assertTrue(new BigDecimal("10").compareTo(openTrades.get(1).getQuantity()) == 0);
		assertTrue(new BigDecimal("5").compareTo(openTrades.get(1).getOpenPrice()) == 0);
		assertTrue(new BigDecimal("50").compareTo(openTrades.get(1).getOpenValue()) == 0);
		assertTrue(new BigDecimal("10").compareTo(openTrades.get(1).getCommission()) == 0);

		openTrades = new ArrayList<>(openPositions.get(1).getTrades());

		assertEquals(1, openTrades.size());
		assertTrue(new BigDecimal("10").compareTo(openTrades.get(0).getQuantity()) == 0);
		assertTrue(new BigDecimal("10").compareTo(openTrades.get(0).getOpenPrice()) == 0);
		assertTrue(new BigDecimal("100").compareTo(openTrades.get(0).getOpenValue()) == 0);
		assertTrue(new BigDecimal("5").compareTo(openTrades.get(0).getCommission()) == 0);
	}

	private void verifyEquityHoldingsAfterSell(Account account) {
		verifyEquityHoldingsAfterSell(new HasAHoldingListAdapter(account));
	}

	private void verifyEquityHoldingsAfterSell(Portfolio portfolio) {
		verifyEquityHoldingsAfterSell(new HasAHoldingListAdapter(portfolio));
	}

	private void verifyEquityHoldingsAfterSell(HasAHoldingListAdapter holder) {
		ArrayList<EquityHolding> openHoldings;
		ArrayList<EquityPosition> openPositions;
		ArrayList<EquityTrade> openTrades;

		openHoldings = new ArrayList<>(holder.getHoldings().getEquityHoldings());

		assertEquals(1, openHoldings.size());
		assertEquals("TICKER-A", openHoldings.get(0).getName());
		assertTrue(new BigDecimal("22").compareTo(openHoldings.get(0).getQuantity()) == 0);
		assertTrue(new BigDecimal("8.18").compareTo(openHoldings.get(0).getOpenPrice()) == 0);
		assertTrue(new BigDecimal("180").compareTo(openHoldings.get(0).getOpenValue()) == 0);

		openPositions = new ArrayList<>(openHoldings.get(0).getPositions());

		assertEquals(2, openPositions.size());
		assertEquals("2000-01", openPositions.get(0).getName());
		assertTrue(new BigDecimal("12").compareTo(openPositions.get(0).getQuantity()) == 0);
		assertTrue(new BigDecimal("6.67").compareTo(openPositions.get(0).getOpenPrice()) == 0);
		assertTrue(new BigDecimal("80").compareTo(openPositions.get(0).getOpenValue()) == 0);
		assertEquals("2000-02", openPositions.get(1).getName());
		assertTrue(new BigDecimal("10").compareTo(openPositions.get(1).getQuantity()) == 0);
		assertTrue(new BigDecimal("10").compareTo(openPositions.get(1).getOpenPrice()) == 0);
		assertTrue(new BigDecimal("100").compareTo(openPositions.get(1).getOpenValue()) == 0);

		// Check for proper ordering too!
		openTrades = new ArrayList<>(openPositions.get(0).getTrades());

		assertEquals(2, openTrades.size());
		assertTrue(new BigDecimal("2").compareTo(openTrades.get(0).getQuantity()) == 0);
		assertTrue(new BigDecimal("15").compareTo(openTrades.get(0).getOpenPrice()) == 0);
		assertTrue(new BigDecimal("30").compareTo(openTrades.get(0).getOpenValue()) == 0);
		assertTrue(new BigDecimal("10").compareTo(openTrades.get(1).getQuantity()) == 0);
		assertTrue(new BigDecimal("5").compareTo(openTrades.get(1).getOpenPrice()) == 0);
		assertTrue(new BigDecimal("50").compareTo(openTrades.get(1).getOpenValue()) == 0);

		openTrades = new ArrayList<>(openPositions.get(1).getTrades());

		assertEquals(1, openTrades.size());
		assertTrue(new BigDecimal("10").compareTo(openTrades.get(0).getQuantity()) == 0);
		assertTrue(new BigDecimal("10").compareTo(openTrades.get(0).getOpenPrice()) == 0);
		assertTrue(new BigDecimal("100").compareTo(openTrades.get(0).getOpenValue()) == 0);

		ArrayList<EquityHolding> closedHoldings;
		ArrayList<EquityPosition> closedPositions;
		ArrayList<EquityTrade> closedTrades;

		closedHoldings = new ArrayList<>(holder.getHoldings().getClosedEquityHoldings());

		assertEquals(1, closedHoldings.size());
		assertEquals("TICKER-A", closedHoldings.get(0).getName());
		assertTrue(new BigDecimal("8").compareTo(closedHoldings.get(0).getQuantity()) == 0);
		assertTrue(new BigDecimal("15").compareTo(closedHoldings.get(0).getOpenPrice()) == 0);
		assertTrue(new BigDecimal("120").compareTo(closedHoldings.get(0).getOpenValue()) == 0);
		assertTrue(new BigDecimal("16.25").compareTo(closedHoldings.get(0).getClosePrice()) == 0);
		assertTrue(new BigDecimal("130").compareTo(closedHoldings.get(0).getCloseValue()) == 0);
		assertTrue(new BigDecimal("10").compareTo(closedHoldings.get(0).getRealizedGain()) == 0);
		assertTrue(new BigDecimal("8.33").compareTo(closedHoldings.get(0).getRealizedGainPercentage()) == 0);

		closedPositions = new ArrayList<>(closedHoldings.get(0).getPositions());

		assertEquals(1, closedPositions.size());
		assertEquals("2000-01", closedPositions.get(0).getName());
		assertTrue(new BigDecimal("8").compareTo(closedPositions.get(0).getQuantity()) == 0);
		assertTrue(new BigDecimal("15").compareTo(closedPositions.get(0).getOpenPrice()) == 0);
		assertTrue(new BigDecimal("120").compareTo(closedPositions.get(0).getOpenValue()) == 0);
		assertTrue(new BigDecimal("16.25").compareTo(closedPositions.get(0).getClosePrice()) == 0);
		assertTrue(new BigDecimal("130").compareTo(closedPositions.get(0).getCloseValue()) == 0);
		assertTrue(new BigDecimal("10").compareTo(closedPositions.get(0).getRealizedGain()) == 0);
		assertTrue(new BigDecimal("8.33").compareTo(closedPositions.get(0).getRealizedGainPercentage()) == 0);

		// Check for proper ordering too!
		closedTrades = new ArrayList<>(closedPositions.get(0).getTrades());

		assertEquals(2, closedTrades.size());
		assertTrue(new BigDecimal("3").compareTo(closedTrades.get(0).getQuantity()) == 0);
		assertTrue(new BigDecimal("15").compareTo(closedTrades.get(0).getOpenPrice()) == 0);
		assertTrue(new BigDecimal("45").compareTo(closedTrades.get(0).getOpenValue()) == 0);
		assertTrue(new BigDecimal("10").compareTo(closedTrades.get(0).getClosePrice()) == 0);
		assertTrue(new BigDecimal("30").compareTo(closedTrades.get(0).getCloseValue()) == 0);
		assertTrue(new BigDecimal("-15").compareTo(closedTrades.get(0).getRealizedGain()) == 0);
		assertTrue(new BigDecimal("-33.33").compareTo(closedTrades.get(0).getRealizedGainPercentage()) == 0);
		assertTrue(new BigDecimal("5").compareTo(closedTrades.get(1).getQuantity()) == 0);
		assertTrue(new BigDecimal("15").compareTo(closedTrades.get(1).getOpenPrice()) == 0);
		assertTrue(new BigDecimal("75").compareTo(closedTrades.get(1).getOpenValue()) == 0);
		assertTrue(new BigDecimal("20").compareTo(closedTrades.get(1).getClosePrice()) == 0);
		assertTrue(new BigDecimal("100").compareTo(closedTrades.get(1).getCloseValue()) == 0);
		assertTrue(new BigDecimal("25").compareTo(closedTrades.get(1).getRealizedGain()) == 0);
		assertTrue(new BigDecimal("33.33").compareTo(closedTrades.get(1).getRealizedGainPercentage()) == 0);
	}

	private void verifyEquityHoldingsAfterCompleteSell(Account account) {
		verifyEquityHoldingsAfterCompleteSell(new HasAHoldingListAdapter(account));
	}

	private void verifyEquityHoldingsAfterCompleteSell(Portfolio portfolio) {
		verifyEquityHoldingsAfterCompleteSell(new HasAHoldingListAdapter(portfolio));
	}

	private void verifyEquityHoldingsAfterCompleteSell(HasAHoldingListAdapter holder) {
		ArrayList<EquityHolding> openHoldings;

		openHoldings = new ArrayList<>(holder.getHoldings().getEquityHoldings());

		assertTrue(openHoldings.isEmpty());

		ArrayList<EquityHolding> closedHoldings;
		ArrayList<EquityPosition> closedPositions;
		ArrayList<EquityTrade> closedTrades;

		closedHoldings = new ArrayList<>(holder.getHoldings().getClosedEquityHoldings());

		assertEquals(1, closedHoldings.size());
		assertEquals("TICKER-A", closedHoldings.get(0).getName());
		assertTrue(new BigDecimal("30").compareTo(closedHoldings.get(0).getQuantity()) == 0);
		assertTrue(new BigDecimal("10").compareTo(closedHoldings.get(0).getOpenPrice()) == 0);
		assertTrue(new BigDecimal("300").compareTo(closedHoldings.get(0).getOpenValue()) == 0);
		assertTrue(new BigDecimal("11.07").compareTo(closedHoldings.get(0).getClosePrice()) == 0);
		assertTrue(new BigDecimal("331.96").compareTo(closedHoldings.get(0).getCloseValue()) == 0);
		assertTrue(new BigDecimal("5.96").compareTo(closedHoldings.get(0).getRealizedGain()) == 0);
		assertTrue(new BigDecimal("1.99").compareTo(closedHoldings.get(0).getRealizedGainPercentage()) == 0);

		closedPositions = new ArrayList<>(closedHoldings.get(0).getPositions());

		assertEquals(2, closedPositions.size());
		assertEquals("2000-01", closedPositions.get(0).getName());
		assertTrue(new BigDecimal("20").compareTo(closedPositions.get(0).getQuantity()) == 0);
		assertTrue(new BigDecimal("10").compareTo(closedPositions.get(0).getOpenPrice()) == 0);
		assertTrue(new BigDecimal("200").compareTo(closedPositions.get(0).getOpenValue()) == 0);
		assertTrue(new BigDecimal("12.01").compareTo(closedPositions.get(0).getClosePrice()) == 0);
		assertTrue(new BigDecimal("240.16").compareTo(closedPositions.get(0).getCloseValue()) == 0);
		assertTrue(new BigDecimal("24.16").compareTo(closedPositions.get(0).getRealizedGain()) == 0);
		assertTrue(new BigDecimal("12.08").compareTo(closedPositions.get(0).getRealizedGainPercentage()) == 0);
		assertTrue(new BigDecimal("10").compareTo(closedPositions.get(1).getQuantity()) == 0);
		assertTrue(new BigDecimal("10").compareTo(closedPositions.get(1).getOpenPrice()) == 0);
		assertTrue(new BigDecimal("100").compareTo(closedPositions.get(1).getOpenValue()) == 0);
		assertTrue(new BigDecimal("9.18").compareTo(closedPositions.get(1).getClosePrice()) == 0);
		assertTrue(new BigDecimal("91.80").compareTo(closedPositions.get(1).getCloseValue()) == 0);
		assertTrue(new BigDecimal("-18.20").compareTo(closedPositions.get(1).getRealizedGain()) == 0);
		assertTrue(new BigDecimal("-18.20").compareTo(closedPositions.get(1).getRealizedGainPercentage()) == 0);

		// Check for proper ordering too!
		closedTrades = new ArrayList<>(closedPositions.get(0).getTrades());

		assertEquals(4, closedTrades.size());
		assertTrue(new BigDecimal("3").compareTo(closedTrades.get(0).getQuantity()) == 0);
		assertTrue(new BigDecimal("15").compareTo(closedTrades.get(0).getOpenPrice()) == 0);
		assertTrue(new BigDecimal("45").compareTo(closedTrades.get(0).getOpenValue()) == 0);
		assertTrue(new BigDecimal("10").compareTo(closedTrades.get(0).getClosePrice()) == 0);
		assertTrue(new BigDecimal("30").compareTo(closedTrades.get(0).getCloseValue()) == 0);
		assertTrue(new BigDecimal("0").compareTo(closedTrades.get(0).getCommission()) == 0);
		assertTrue(new BigDecimal("-15").compareTo(closedTrades.get(0).getRealizedGain()) == 0);
		assertTrue(new BigDecimal("-33.33").compareTo(closedTrades.get(0).getRealizedGainPercentage()) == 0);
		assertTrue(new BigDecimal("5").compareTo(closedTrades.get(1).getQuantity()) == 0);
		assertTrue(new BigDecimal("15").compareTo(closedTrades.get(1).getOpenPrice()) == 0);
		assertTrue(new BigDecimal("75").compareTo(closedTrades.get(1).getOpenValue()) == 0);
		assertTrue(new BigDecimal("20").compareTo(closedTrades.get(1).getClosePrice()) == 0);
		assertTrue(new BigDecimal("100").compareTo(closedTrades.get(1).getCloseValue()) == 0);
		assertTrue(new BigDecimal("0").compareTo(closedTrades.get(1).getCommission()) == 0);
		assertTrue(new BigDecimal("25").compareTo(closedTrades.get(1).getRealizedGain()) == 0);
		assertTrue(new BigDecimal("33.33").compareTo(closedTrades.get(1).getRealizedGainPercentage()) == 0);
		assertTrue(new BigDecimal("2").compareTo(closedTrades.get(2).getQuantity()) == 0);
		assertTrue(new BigDecimal("15").compareTo(closedTrades.get(2).getOpenPrice()) == 0);
		assertTrue(new BigDecimal("30").compareTo(closedTrades.get(2).getOpenValue()) == 0);
		assertTrue(new BigDecimal("9.18").compareTo(closedTrades.get(2).getClosePrice()) == 0);
		assertTrue(new BigDecimal("18.36").compareTo(closedTrades.get(2).getCloseValue()) == 0);
		assertTrue(new BigDecimal("1").compareTo(closedTrades.get(2).getCommission()) == 0); // 0.00 open commission + 1.00 partial close
		assertTrue(new BigDecimal("-12.64").compareTo(closedTrades.get(2).getRealizedGain()) == 0);
		assertTrue(new BigDecimal("-42.13").compareTo(closedTrades.get(2).getRealizedGainPercentage()) == 0);
		assertTrue(new BigDecimal("10").compareTo(closedTrades.get(3).getQuantity()) == 0);
		assertTrue(new BigDecimal("5").compareTo(closedTrades.get(3).getOpenPrice()) == 0);
		assertTrue(new BigDecimal("50").compareTo(closedTrades.get(3).getOpenValue()) == 0);
		assertTrue(new BigDecimal("9.18").compareTo(closedTrades.get(3).getClosePrice()) == 0);
		assertTrue(new BigDecimal("91.80").compareTo(closedTrades.get(3).getCloseValue()) == 0);
		assertTrue(new BigDecimal("15").compareTo(closedTrades.get(3).getCommission()) == 0); // 10.00 open commission + 5.00 partial close
		assertTrue(new BigDecimal("26.80").compareTo(closedTrades.get(3).getRealizedGain()) == 0);
		assertTrue(new BigDecimal("53.60").compareTo(closedTrades.get(3).getRealizedGainPercentage()) == 0);

		closedTrades = new ArrayList<>(closedPositions.get(1).getTrades());
		assertEquals(1, closedTrades.size());
		assertTrue(new BigDecimal("10").compareTo(closedTrades.get(0).getQuantity()) == 0);
		assertTrue(new BigDecimal("10").compareTo(closedTrades.get(0).getOpenPrice()) == 0);
		assertTrue(new BigDecimal("100").compareTo(closedTrades.get(0).getOpenValue()) == 0);
		assertTrue(new BigDecimal("9.18").compareTo(closedTrades.get(0).getClosePrice()) == 0);
		assertTrue(new BigDecimal("91.80").compareTo(closedTrades.get(0).getCloseValue()) == 0);
		assertTrue(new BigDecimal("10").compareTo(closedTrades.get(0).getCommission()) == 0); // 5.00 open commission + 5.00 partial close
		assertTrue(new BigDecimal("-18.20").compareTo(closedTrades.get(0).getRealizedGain()) == 0);
		assertTrue(new BigDecimal("-18.20").compareTo(closedTrades.get(0).getRealizedGainPercentage()) == 0);
	}

	@Test(expected=EntryInsertionException.class)
	public void shouldRejectEquityOperationsGivenInsufficientShares() throws ObjectNotFoundException, InvalidInputException, EntryInsertionException, ObjectConstraintsException {
		journal.addAccount("Test account #1");              // ID: 0
		journal.addAccount("Test account #2");              // ID: 1

		journal.addPortfolio("Test portfolio #1", 0);       // ID: 1
		journal.addPortfolio("Test portfolio #2", 0);       // ID: 2
		journal.addPortfolio("Test portfolio #1.1", 1);     // ID: 3
		journal.addPortfolio("Test portfolio #1.2", 2);     // ID: 4

		journal.addCashDepositEntry(0, "Example tag", new GregorianCalendar(2000, 0, 1).getTime(), "Some comment", new BigDecimal("10000.00"));
		journal.addCashDepositEntry(1, "Example tag", new GregorianCalendar(2000, 0, 1).getTime(), "Some comment", new BigDecimal("10000.00"));

		journal.addCashAllocationEntry(0, 1, "Example tag", new GregorianCalendar(2000, 0, 2).getTime(), "Some comment", new BigDecimal("1000.00"));
		journal.addCashAllocationEntry(0, 2, "Example tag", new GregorianCalendar(2000, 0, 2).getTime(), "Some comment", new BigDecimal("1000.00"));
		journal.addCashAllocationEntry(1, 3, "Example tag", new GregorianCalendar(2000, 0, 2).getTime(), "Some comment", new BigDecimal("1000.00"));
		journal.addCashAllocationEntry(1, 4, "Example tag", new GregorianCalendar(2000, 0, 2).getTime(), "Some comment", new BigDecimal("1000.00"));

		// Check buy entries with random date order
		journal.addBuyEquityTransactionEntry(0, 1, "Example tag", new GregorianCalendar(2000, 0, 3).getTime(), "Some comment", "TICKER-A", new BigDecimal("10"), new BigDecimal("5.00"), new BigDecimal("10.00"));
		journal.addBuyEquityTransactionEntry(0, 1, "Example tag", new GregorianCalendar(2000, 1, 4).getTime(), "Some comment", "TICKER-A", new BigDecimal("10"), new BigDecimal("10.00"), new BigDecimal("5.00"));
		journal.addBuyEquityTransactionEntry(0, 1, "Example tag", new GregorianCalendar(2000, 0, 2).getTime(), "Some comment", "TICKER-A", new BigDecimal("10"), new BigDecimal("15.00"), new BigDecimal("0.00"));
		journal.addSellEquityTransactionEntry(0, 1, "Example tag", new GregorianCalendar(2001, 0, 5).getTime(), "Some comment", "TICKER-A", new BigDecimal("5"), new BigDecimal("20.00"), new BigDecimal("0.00"));
		journal.addSellEquityTransactionEntry(0, 1, "Example tag", new GregorianCalendar(2000, 0, 6).getTime(), "Some comment", "TICKER-A", new BigDecimal("30"), new BigDecimal("10.00"), new BigDecimal("0.00"));
	}

	@Test(expected=InvalidInputException.class)
	public void shouldRejectEquityOperationsGivenNegativePrice() throws ObjectNotFoundException, InvalidInputException, EntryInsertionException, ObjectConstraintsException {
		journal.addAccount("Test account #1");
		journal.addPortfolio("Test portfolio #1", 0);
		journal.addBuyEquityTransactionEntry(0, 1, "Example tag", new GregorianCalendar(2000, 0, 1).getTime(), "Some comment", "TICKER", new BigDecimal("30"), new BigDecimal("-1.50"), new BigDecimal("5.00"));
	}

	@Test(expected=InvalidInputException.class)
	public void shouldRejectEquityOperationsGivenNegativeQuantity() throws ObjectNotFoundException, InvalidInputException, EntryInsertionException, ObjectConstraintsException {
		journal.addAccount("Test account #1");
		journal.addPortfolio("Test portfolio #1", 0);
		journal.addBuyEquityTransactionEntry(0, 1, "Example tag", new GregorianCalendar(2000, 0, 1).getTime(), "Some comment", "TICKER", new BigDecimal("-30"), new BigDecimal("1.50"), new BigDecimal("5.00"));
	}

	@Test(expected=InvalidInputException.class)
	public void shouldRejectEquityOperationsGivenFractionalQuantity() throws ObjectNotFoundException, InvalidInputException, EntryInsertionException, ObjectConstraintsException {
		journal.addAccount("Test account #1");
		journal.addPortfolio("Test portfolio #1", 0);
		journal.addBuyEquityTransactionEntry(0, 1, "Example tag", new GregorianCalendar(2000, 0, 1).getTime(), "Some comment", "TICKER", new BigDecimal("30.5"), new BigDecimal("1.50"), new BigDecimal("5.00"));
	}

	@Test(expected=InvalidInputException.class)
	public void shouldRejectEquityOperationsGivenNegativeCommission() throws ObjectNotFoundException, InvalidInputException, EntryInsertionException, ObjectConstraintsException {
		journal.addAccount("Test account #1");
		journal.addPortfolio("Test portfolio #1", 0);
		journal.addBuyEquityTransactionEntry(0, 1, "Example tag", new GregorianCalendar(2000, 0, 1).getTime(), "Some comment", "TICKER", new BigDecimal("30"), new BigDecimal("1.50"), new BigDecimal("-2.00"));
	}

	/**
	 * Warning: This test is designed to take a long time to execute (around 20 seconds for a passing test).
	 *
	 * @throws ObjectNotFoundException
	 * @throws InvalidInputException
	 * @throws EntryInsertionException
	 * @throws ObjectConstraintsException
	 */
	@Test
	public void shouldHandleLargeNumberOfEntries() throws ObjectNotFoundException, InvalidInputException, EntryInsertionException, ObjectConstraintsException, NoSuchFieldException {
		journal.addAccount("Test account #1");
		journal.addAccount("Test account #2");

		journal.addPortfolio("Test portfolio #1", 0);       // ID: 1
		journal.addPortfolio("Test portfolio #2", 0);       // ID: 2
		journal.addPortfolio("Test portfolio #3", 0);       // ID: 3
		journal.addPortfolio("Test portfolio #1.1", 1);     // ID: 4
		journal.addPortfolio("Test portfolio #2.1", 2);     // ID: 5
		journal.addPortfolio("Test portfolio #3.1", 3);     // ID: 6
		journal.addPortfolio("Test portfolio #1.2", 1);     // ID: 7
		journal.addPortfolio("Test portfolio #1.2.1", 7);   // ID: 8

		int operationsPerformed = 0;
		long startTime = System.nanoTime();

		PA.setValue(TradeRateConfig.class, "deferredComputationMode", true);

		// Test a high volume situation (1 year worth of trading history)
		for (int m = 0; m < 12; ++m) {
			for (int d = 1; d < 28; ++d) {
				// Simulated transactions per day: 100 * 9
				// Around 300 000 entries in total over 1 year, approx. one entry every 30 seconds during trading hours.
				// Seems to be a reasonable bound for non-HFT applications.
				for (int t = 0; t < 100; ++t) {
					journal.addCashDepositEntry(0, "Example tag", new GregorianCalendar(2000, m, d).getTime(), "Some comment", new BigDecimal("10.00"));
					journal.addCashDepositEntry(1, "Example tag", new GregorianCalendar(2000, m, d).getTime(), "Some comment", new BigDecimal("10.00"));
					journal.addCashAllocationEntry(0, 8, "Example tag", new GregorianCalendar(2000, m, d).getTime(), "Some comment", new BigDecimal("10.00"));
					journal.addCashAllocationEntry(1, 6, "Example tag", new GregorianCalendar(2000, m, d).getTime(), "Some comment", new BigDecimal("10.00"));
					journal.addBuyEquityTransactionEntry(0, 8, "Example tag", new GregorianCalendar(2000, m, d).getTime(), "Some comment", "TICKER-A", new BigDecimal("5"), new BigDecimal("1.00"), new BigDecimal("0.00"));
					journal.addBuyEquityTransactionEntry(0, 8, "Example tag", new GregorianCalendar(2000, m, d).getTime(), "Some comment", "TICKER-A", new BigDecimal("5"), new BigDecimal("1.00"), new BigDecimal("0.00"));
					journal.addBuyEquityTransactionEntry(1, 6, "Example tag", new GregorianCalendar(2000, m, d).getTime(), "Some comment", "TICKER-A", new BigDecimal("10"), new BigDecimal("1.00"), new BigDecimal("0.00"));
					journal.addSellEquityTransactionEntry(0, 8, "Example tag", new GregorianCalendar(2000, m, d).getTime(), "Some comment", "TICKER-A", new BigDecimal("5"), new BigDecimal("2.00"), new BigDecimal("0.00"));
					journal.addSellEquityTransactionEntry(1, 6, "Example tag", new GregorianCalendar(2000, m, d).getTime(), "Some comment", "TICKER-A", new BigDecimal("5"), new BigDecimal("2.00"), new BigDecimal("0.00"));
					operationsPerformed = operationsPerformed + 9;
				}
			}
		}

		PA.setValue(TradeRateConfig.class, "deferredComputationMode", false);

		long endTime = System.nanoTime();
		long duration = endTime - startTime;
		BigDecimal durationSeconds = new BigDecimal ((double) duration / 1000000000.0);

		// Loading such a big journal shouldn't take more than 10 seconds
		assertTrue(durationSeconds.intValue() < 10);

		//:--------------------------------------------------------------------

		// Check how long it takes to insert a new entry at a random date (total recalc needed)
		// Each type of operation shouldn't take more than 3 seconds to complete (per individual case)
		startTime = System.nanoTime();
		journal.addCashDepositEntry(0, "Example tag", new GregorianCalendar(2000, 0, 5).getTime(), "Some comment", new BigDecimal("10.00"));
		endTime = System.nanoTime();
		duration = endTime - startTime;
		durationSeconds = new BigDecimal ((double) duration / 1000000000.0);
		assertTrue(durationSeconds.intValue() < 3);

		startTime = System.nanoTime();
		journal.addCashAllocationEntry(0, 8, "Example tag", new GregorianCalendar(2000, 0, 5).getTime(), "Some comment", new BigDecimal("5.00"));
		endTime = System.nanoTime();
		duration = endTime - startTime;
		durationSeconds = new BigDecimal ((double) duration / 1000000000.0);
		assertTrue(durationSeconds.intValue() < 3);

		startTime = System.nanoTime();
		journal.addBuyEquityTransactionEntry(0, 8, "Example tag", new GregorianCalendar(2000, 0, 5).getTime(), "Some comment", "TICKER-A", new BigDecimal("1"), new BigDecimal("2.00"), new BigDecimal("1.00"));
		endTime = System.nanoTime();
		duration = endTime - startTime;
		durationSeconds = new BigDecimal ((double) duration / 1000000000.0);
		assertTrue(durationSeconds.intValue() < 3);

		startTime = System.nanoTime();
		journal.addSellEquityTransactionEntry(0, 8, "Example tag", new GregorianCalendar(2000, 0, 5).getTime(), "Some comment", "TICKER-A", new BigDecimal("1"), new BigDecimal("2.00"), new BigDecimal("1.00"));
		endTime = System.nanoTime();
		duration = endTime - startTime;
		durationSeconds = new BigDecimal ((double) duration / 1000000000.0);
		assertTrue(durationSeconds.intValue() < 3);
	}
}
