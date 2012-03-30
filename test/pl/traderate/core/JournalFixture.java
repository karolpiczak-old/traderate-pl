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
import pl.traderate.core.exception.ObjectNotFoundException;
import pl.traderate.test.TestNotImplementedError;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.GregorianCalendar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

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
		assertEquals(new BigDecimal("0"), accounts.get(0).getCashBalance());

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
		assertEquals(new BigDecimal("0"), accounts.get(0).getCashBalance());

		journal.addCashDepositEntry(0, "Example tag", new GregorianCalendar(2000, 0, 1).getTime(), "Some comment", new BigDecimal("1000.00"));
		assertEquals(new BigDecimal("1000.00"), accounts.get(0).getCashBalance());

		journal.addCashWithdrawalEntry(0, "Example tag", new GregorianCalendar(2001, 0, 1).getTime(), "Some comment", new BigDecimal("2000.00"));
	}

	@Test(expected=EntryInsertionException.class)
	public void shouldRejectCashOperationGivenPastInsufficientFunds() throws EntryInsertionException, ObjectNotFoundException, InvalidInputException {
		journal.addAccount("Test account #1");
		assertEquals(new BigDecimal("0"), accounts.get(0).getCashBalance());

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

		assertEquals(new BigDecimal("0"), portfolios.get(0).getCashBalance());
		assertEquals(new BigDecimal("0"), portfolios.get(1).getCashBalance());
		assertEquals(new BigDecimal("0"), portfolios.get(2).getCashBalance());
		assertEquals(new BigDecimal("0"), portfolios.get(3).getCashBalance());
		assertEquals(new BigDecimal("0"), portfolios.get(4).getCashBalance());
		assertEquals(new BigDecimal("0"), portfolios.get(5).getCashBalance());
		assertEquals(new BigDecimal("0"), portfolios.get(6).getCashBalance());
		assertEquals(new BigDecimal("0"), portfolios.get(7).getCashBalance());
		assertEquals(new BigDecimal("0"), portfolios.get(8).getCashBalance());

		// Allocate 1000.00 from Account #1 to Portfolio #1
		journal.addCashAllocationEntry(0, 1, "Example tag", new GregorianCalendar(2002, 0, 1).getTime(), "Some comment", new BigDecimal("1000.00"));
		assertEquals(new BigDecimal("10000.00"), accounts.get(0).getCashBalance());
		assertEquals(new BigDecimal("9000.00"), accounts.get(0).getUnallocatedCash());
		assertEquals(new BigDecimal("1000.00"), portfolios.get(1).getCashBalance());
		assertEquals(new BigDecimal("0"), portfolios.get(0).getCashBalance());
		assertEquals(new BigDecimal("1000.00"), portfolios.get(0).getChildrenCashBalance());

		// Check earlier date
		// Allocate 5000.00 from Account #2 to Portfolio #1.2.1
		journal.addCashAllocationEntry(1, 8, "Example tag", new GregorianCalendar(2001, 0, 1).getTime(), "Some comment", new BigDecimal("5000.00"));
		assertEquals(new BigDecimal("10000.00"), accounts.get(1).getCashBalance());
		assertEquals(new BigDecimal("5000.00"), accounts.get(1).getUnallocatedCash());
		assertEquals(new BigDecimal("5000.00"), portfolios.get(8).getCashBalance());
		assertEquals(new BigDecimal("0"), portfolios.get(7).getCashBalance()); // Portfolio #1.2
		assertEquals(new BigDecimal("1000.00"), portfolios.get(1).getCashBalance()); // Portfolio #1, 1000.00 still from previous case
		assertEquals(new BigDecimal("5000.00"), portfolios.get(7).getChildrenCashBalance()); // Portfolio #1.2
		assertEquals(new BigDecimal("5000.00"), portfolios.get(1).getChildrenCashBalance()); // Portfolio #1
		assertEquals(new BigDecimal("6000.00"), portfolios.get(0).getChildrenCashBalance()); // Portfolio #0

		// Deallocate 2000.00 from Portfolio #1.2.1 to Account #2
		journal.addCashDeallocationEntry(1, 8, "Example tag", new GregorianCalendar(2003, 0, 1).getTime(), "Some comment", new BigDecimal("2000.00"));
		assertEquals(new BigDecimal("10000.00"), accounts.get(1).getCashBalance());
		assertEquals(new BigDecimal("7000.00"), accounts.get(1).getUnallocatedCash());
		assertEquals(new BigDecimal("3000.00"), portfolios.get(8).getCashBalance());
		assertEquals(new BigDecimal("0"), portfolios.get(7).getCashBalance()); // Portfolio #1.2
		assertEquals(new BigDecimal("1000.00"), portfolios.get(1).getCashBalance()); // Portfolio #1, 1000.00 still from previous case
		assertEquals(new BigDecimal("3000.00"), portfolios.get(7).getChildrenCashBalance()); // Portfolio #1.2
		assertEquals(new BigDecimal("3000.00"), portfolios.get(1).getChildrenCashBalance()); // Portfolio #1
		assertEquals(new BigDecimal("4000.00"), portfolios.get(0).getChildrenCashBalance()); // Portfolio #0

		// Deallocate 500.00 from Portfolio #1 to Account #1
		journal.addCashDeallocationEntry(0, 1, "Example tag", new GregorianCalendar(2002, 5, 1).getTime(), "Some comment", new BigDecimal("500.00"));
		assertEquals(new BigDecimal("10000.00"), accounts.get(0).getCashBalance());
		assertEquals(new BigDecimal("9500.00"), accounts.get(0).getUnallocatedCash());
		assertEquals(new BigDecimal("500.00"), portfolios.get(1).getCashBalance());
		assertEquals(new BigDecimal("0"), portfolios.get(0).getCashBalance());
		assertEquals(new BigDecimal("3500.00"), portfolios.get(0).getChildrenCashBalance());

		// Deallocate another 500.00 from Portfolio #1 to Account #1
		journal.addCashDeallocationEntry(0, 1, "Example tag", new GregorianCalendar(2002, 6, 1).getTime(), "Some comment", new BigDecimal("500.00"));
		assertEquals(new BigDecimal("10000.00"), accounts.get(0).getCashBalance());
		assertEquals(new BigDecimal("10000.00"), accounts.get(0).getUnallocatedCash());
		assertEquals(new BigDecimal("0.00"), portfolios.get(1).getCashBalance());
		assertEquals(new BigDecimal("0"), portfolios.get(0).getCashBalance());
		assertEquals(new BigDecimal("3000.00"), portfolios.get(0).getChildrenCashBalance());
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
	public void shouldHandleEquityOperations() {
		throw new TestNotImplementedError();
	}

	@Test
	public void shouldRejectEquityOperationsGivenInsufficientShares() {
		throw new TestNotImplementedError();
	}

	@Test
	public void shouldRejectEquityOperationsGivenInvalidPrice() {
		throw new TestNotImplementedError();
	}

	@Test
	public void shouldRejectEquityOperationsGivenInvalidQuantity() {
		throw new TestNotImplementedError();
	}
}
