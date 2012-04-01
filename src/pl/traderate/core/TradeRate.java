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

import pl.traderate.core.event.GenericModelEventSource;
import pl.traderate.core.event.DataUpdateModelEvent;
import pl.traderate.core.exception.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;

/**
 * Main application class.
 *
 * Singleton encapsulating all business logic.
 */
public final class TradeRate extends GenericModelEventSource {

	/** */
	private final static TradeRate instance = new TradeRate();

	/**
	 * Currently open journal.
	 *
	 * Per design only one journal can be opened at a time.
	 */
	private Journal journal;

	/**
	 * Restricted constructor.
	 *
	 * Prevents direct instantiation.
	 */
	private TradeRate() {

	}

	/**
	 *
	 * @return
	 */
	public static TradeRate getInstance() {
		return instance;
	}

	public void createJournal(String name, String owner) {
		journal = new Journal(name, owner);

		fireEvent(new DataUpdateModelEvent(this));
	}

	public void openJournal() {

	}


	public void saveJournal() {

	}

	public void closeJournal() {
		saveJournal();
		journal = null;

		fireEvent(new DataUpdateModelEvent(this));
	}

	/**
	 *
	 * @param name
	 * @throws JournalNotLoadedException
	 */
	public void addAccount(String name) throws JournalNotLoadedException {
		assertJournalIsLoaded();
		journal.addAccount(name);

		fireEvent(new DataUpdateModelEvent(this));
	}

	/**
	 *
	 * @param name
	 * @param parentID
	 * @throws JournalNotLoadedException
	 * @throws ObjectNotFoundException
	 */
	public void addPortfolio(String name, int parentID) throws JournalNotLoadedException, ObjectNotFoundException {
		assertJournalIsLoaded();
		journal.addPortfolio(name, parentID);

		fireEvent(new DataUpdateModelEvent(this));
	}

	/**
	 *
	 * @param accountID
	 * @param portfolioID
	 * @param tags
	 * @param date
	 * @param comment
	 * @param ticker
	 * @param quantity
	 * @param price
	 * @param commission
	 */
	public void addBuyEquityTransactionEntry(int accountID, int portfolioID, String tags, Date date, String comment, String ticker, BigDecimal quantity, BigDecimal price, BigDecimal commission) throws JournalNotLoadedException, EntryInsertionException, ObjectNotFoundException, ObjectConstraintsException, InvalidInputException {
		assertJournalIsLoaded();
		journal.addBuyEquityTransactionEntry(accountID, portfolioID, tags, date, comment, ticker, quantity, price, commission);
		journal.update();

		fireEvent(new DataUpdateModelEvent(this));
	}

	/**
	 *
	 * @param accountID
	 * @param portfolioID
	 * @param tags
	 * @param date
	 * @param comment
	 * @param ticker
	 * @param quantity
	 * @param price
	 * @param commission
	 */
	public void addSellEquityTransactionEntry(int accountID, int portfolioID, String tags, Date date, String comment, String ticker, BigDecimal quantity, BigDecimal price, BigDecimal commission) throws JournalNotLoadedException, EntryInsertionException, ObjectNotFoundException, ObjectConstraintsException, InvalidInputException {
		assertJournalIsLoaded();
		journal.addSellEquityTransactionEntry(accountID, portfolioID, tags, date, comment, ticker, quantity, price, commission);
		journal.update();

		fireEvent(new DataUpdateModelEvent(this));
	}

	/**
	 *
	 * @param accountID
	 * @param portfolioID
	 * @param tags
	 * @param date
	 * @param comment
	 * @param amount
	 * @throws pl.traderate.core.exception.JournalNotLoadedException
	 */
	public void addCashAllocationEntry(int accountID, int portfolioID, String tags, Date date, String comment, BigDecimal amount) throws JournalNotLoadedException, ObjectNotFoundException, EntryInsertionException, InvalidInputException {
		assertJournalIsLoaded();
		journal.addCashAllocationEntry(accountID, portfolioID, tags, date, comment, amount);
		journal.update();

		fireEvent(new DataUpdateModelEvent(this));
	}

	/**
	 *
	 * @param accountID
	 * @param portfolioID
	 * @param tags
	 * @param date
	 * @param comment
	 * @param amount
	 * @throws pl.traderate.core.exception.JournalNotLoadedException
	 */
	public void addCashDeallocationEntry(int accountID, int portfolioID, String tags, Date date, String comment, BigDecimal amount) throws JournalNotLoadedException, ObjectNotFoundException, EntryInsertionException, InvalidInputException {
		assertJournalIsLoaded();
		journal.addCashDeallocationEntry(accountID, portfolioID, tags, date, comment, amount);
		journal.update();

		fireEvent(new DataUpdateModelEvent(this));
	}

	/**
	 *
	 * @param accountID
	 * @param tags
	 * @param date
	 * @param comment
	 * @param amount
	 * @throws JournalNotLoadedException
	 * @throws ObjectNotFoundException
	 * @throws EntryInsertionException
	 */
	public void addCashDepositEntry(int accountID, String tags, Date date, String comment, BigDecimal amount) throws JournalNotLoadedException, ObjectNotFoundException, EntryInsertionException, InvalidInputException {
		assertJournalIsLoaded();
		journal.addCashDepositEntry(accountID, tags, date, comment, amount);
		journal.update();

		fireEvent(new DataUpdateModelEvent(this));
	}

	/**
	 *
	 * @param accountID
	 * @param tags
	 * @param date
	 * @param comment
	 * @param amount
	 * @throws JournalNotLoadedException
	 * @throws ObjectNotFoundException
	 * @throws EntryInsertionException
	 */
	public void addCashWithdrawalEntry(int accountID, String tags, Date date, String comment, BigDecimal amount) throws JournalNotLoadedException, ObjectNotFoundException, EntryInsertionException, InvalidInputException {
		assertJournalIsLoaded();
		journal.addCashWithdrawalEntry(accountID, tags, date, comment, amount);
		journal.update();

		fireEvent(new DataUpdateModelEvent(this));
	}

	/**
	 *
	 * @param entryID
	 * @throws JournalNotLoadedException
	 * @throws ObjectNotFoundException
	 * @throws EntryInsertionException
	 */
	public void removeEntry(int entryID) throws JournalNotLoadedException, ObjectNotFoundException, EntryInsertionException {
		assertJournalIsLoaded();
		journal.removeEntry(entryID);
		journal.update();

		fireEvent(new DataUpdateModelEvent(this));
	}

	public void updateQuotes() {

	}
	
	public ArrayList<AccountDTO> getAccounts() {
		ArrayList<AccountDTO> accountDTOs = new ArrayList<>();
		for (Account account : journal.getAccounts()) {
			accountDTOs.add(new AccountDTO(account));
		}

		return accountDTOs;
	}

	public PortfolioNodeDTO getPortfolioNodes() {
		return new PortfolioNodeDTO(journal.getGlobalPortfolio(), null);
	}

	/**
	 *
	 * @throws JournalNotLoadedException
	 */
	private void assertJournalIsLoaded() throws JournalNotLoadedException {
		if (journal == null) throw new JournalNotLoadedException();
	}
}
