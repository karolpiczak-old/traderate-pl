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
import pl.traderate.core.exception.EntryInsertionException;
import pl.traderate.core.exception.JournalNotLoadedException;
import pl.traderate.core.exception.ObjectNotFoundException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;

/**
 * Main application class.
 *
 * Singleton encapsulating all business logic.
 */
public final class TradeRate extends GenericModelEventSource {

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

	public void addAccount(String name) throws JournalNotLoadedException {
		assertJournalIsLoaded();
		journal.addAccount(name);

		fireEvent(new DataUpdateModelEvent(this));
	}
	
	public void addPortfolio(String name, int parentID) throws JournalNotLoadedException, ObjectNotFoundException {
		assertJournalIsLoaded();
		journal.addPortfolio(name, parentID);

		fireEvent(new DataUpdateModelEvent(this));
	}

	public void addBuyTransactionEntry() {

	}

	public void addSellTransactionEntry() {

	}

	public void addCashAllocationEntry() {

	}

	public void addCashDepositEntry(int accountID, String tags, Date date, String comment, BigDecimal amount) throws JournalNotLoadedException, ObjectNotFoundException, EntryInsertionException {
		assertJournalIsLoaded();
		journal.addCashDepositEntry(accountID, tags, date, comment, amount);

		fireEvent(new DataUpdateModelEvent(this));
	}

	public void addCashWithdrawalEntry(int accountID, String tags, Date date, String comment, BigDecimal amount) throws JournalNotLoadedException, ObjectNotFoundException, EntryInsertionException {
		assertJournalIsLoaded();
		journal.addCashWithdrawalEntry(accountID, tags, date, comment, amount);

		fireEvent(new DataUpdateModelEvent(this));
	}

	public void removeEntry(int entryID) throws JournalNotLoadedException {
		assertJournalIsLoaded();
		journal.removeEntry(entryID);

		fireEvent(new DataUpdateModelEvent(this));
	}

	public void updateQuotes() {

	}

	private void assertJournalIsLoaded() throws JournalNotLoadedException {
		if (journal == null) throw new JournalNotLoadedException();
	}
}
