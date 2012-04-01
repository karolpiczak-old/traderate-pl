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
		journal.addBuyEquityTransactionEntry(0, 1, "Example tag", new GregorianCalendar(2000, 0, 4).getTime(), "Some comment", "TICKER-A", new BigDecimal("10"), new BigDecimal("10.00"), new BigDecimal("5.00"));
		journal.addBuyEquityTransactionEntry(0, 1, "Example tag", new GregorianCalendar(2000, 0, 2).getTime(), "Some comment", "TICKER-A", new BigDecimal("10"), new BigDecimal("15.00"), new BigDecimal("0.00"));

		journal.addSellEquityTransactionEntry(0, 1, "Example tag", new GregorianCalendar(2000, 0, 5).getTime(), "Some comment", "TICKER-A", new BigDecimal("5"), new BigDecimal("20.00"), new BigDecimal("0.00"));
		journal.addSellEquityTransactionEntry(0, 1, "Example tag", new GregorianCalendar(2000, 0, 6).getTime(), "Some comment", "TICKER-A", new BigDecimal("3"), new BigDecimal("10.00"), new BigDecimal("0.00"));
		
		assertTrue(true);
		//assertEquals(portfolios.get(1).get);
		
//		journal.addBuyEquityTransactionEntry(0, 2, "Example tag", new GregorianCalendar(2000, 0, 3).getTime(), "Some comment", "TICKER-A", new BigDecimal("10"), new BigDecimal("10.00"), new BigDecimal("2.00"));
//		journal.addBuyEquityTransactionEntry(1, 3, "Example tag", new GregorianCalendar(2000, 0, 4).getTime(), "Some comment", "TICKER-A", new BigDecimal("10"), new BigDecimal("10.00"), new BigDecimal("3.00"));
//		journal.addBuyEquityTransactionEntry(1, 4, "Example tag", new GregorianCalendar(2000, 0, 5).getTime(), "Some comment", "TICKER-A", new BigDecimal("10"), new BigDecimal("4.00"), new BigDecimal("2.50"));
//
//		journal.addSellEquityTransactionEntry(0, 8, "Example tag", new GregorianCalendar(2000, m, d).getTime(), "Some comment", "TICKER-A", new BigDecimal("5"), new BigDecimal("2.00"), new BigDecimal("0.00"));
//		journal.addSellEquityTransactionEntry(1, 6, "Example tag", new GregorianCalendar(2000, m, d).getTime(), "Some comment", "TICKER-A", new BigDecimal("5"), new BigDecimal("2.00"), new BigDecimal("0.00"));
//		operationsPerformed = operationsPerformed + 8;
		
		// TODO: Implement complete test case
	}

	@Test
	public void shouldRejectEquityOperationsGivenInsufficientShares() {
		throw new TestNotImplementedError();
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
	 * Warning: This test is designed to take a long time to execute (around 10 seconds for a passing test).
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

//		System.out.println("Testing journal performance via shouldHandleLargeNumberOfEntries():");

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
//				System.out.print('.');
			}
		}

//		System.out.println("");

		PA.setValue(TradeRateConfig.class, "deferredComputationMode", false);

		long endTime = System.nanoTime();
		long duration = endTime - startTime;
		BigDecimal durationSeconds = new BigDecimal ((double) duration / 1000000000.0);

		// Loading such a big journal shouldn't take more than 10 seconds
		assertTrue(durationSeconds.intValue() < 10);

		//:--------------------------------------------------------------------

		// Check how long it takes to insert a new entry at a random date (total recalc needed)
		// Each type of operation shouldn't take more than a second to complete (per individual case)
		startTime = System.nanoTime();
		journal.addCashDepositEntry(0, "Example tag", new GregorianCalendar(2000, 0, 5).getTime(), "Some comment", new BigDecimal("10.00"));
		endTime = System.nanoTime();
		duration = endTime - startTime;
		durationSeconds = new BigDecimal ((double) duration / 1000000000.0);
		assertTrue(durationSeconds.intValue() < 1);

		startTime = System.nanoTime();
		journal.addCashAllocationEntry(0, 8, "Example tag", new GregorianCalendar(2000, 0, 5).getTime(), "Some comment", new BigDecimal("5.00"));
		endTime = System.nanoTime();
		duration = endTime - startTime;
		durationSeconds = new BigDecimal ((double) duration / 1000000000.0);
		assertTrue(durationSeconds.intValue() < 1);

		startTime = System.nanoTime();
		journal.addBuyEquityTransactionEntry(0, 8, "Example tag", new GregorianCalendar(2000, 0, 5).getTime(), "Some comment", "TICKER-A", new BigDecimal("1"), new BigDecimal("2.00"), new BigDecimal("1.00"));
		endTime = System.nanoTime();
		duration = endTime - startTime;
		durationSeconds = new BigDecimal ((double) duration / 1000000000.0);
		assertTrue(durationSeconds.intValue() < 1);

		startTime = System.nanoTime();
		journal.addSellEquityTransactionEntry(0, 8, "Example tag", new GregorianCalendar(2000, 0, 5).getTime(), "Some comment", "TICKER-A", new BigDecimal("1"), new BigDecimal("2.00"), new BigDecimal("1.00"));
		endTime = System.nanoTime();
		duration = endTime - startTime;
		durationSeconds = new BigDecimal ((double) duration / 1000000000.0);
		assertTrue(durationSeconds.intValue() < 1);
	}
}
