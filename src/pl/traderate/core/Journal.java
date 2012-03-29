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
import pl.traderate.core.exception.ObjectNotFoundException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;

/**
 *
 *
 */
class Journal {

	private final ArrayList<JournalEntry> entries;

	private final ArrayList<Account> accounts;

	private final ArrayList<Portfolio> portfolios;
	
	private final ArrayList<Tag> tags;

	private String name;

	private String owner;

	private final Date creationDate;

	private Date lastUpdateDate;

	Journal(String name, String owner) {
		entries = new ArrayList<JournalEntry>(1000);
		accounts = new ArrayList<Account>(10);
		portfolios = new ArrayList<Portfolio>(25);
		tags = new ArrayList<Tag>(25);

		setName(name);
		setOwner(owner);
		creationDate = new Date();
		setLastUpdateDate(new Date());

		portfolios.add(new Portfolio("Portfel globalny"));
	}

	void addAccount(String name) {
		accounts.add(new Account(name));
	}

	void addPortfolio(String name, int parentID) throws ObjectNotFoundException {
		portfolios.add(new Portfolio(name, findPortfolioByID(parentID)));
	}

	void addEntry(JournalEntry entry) {
		entries.add(entry);
	}
	
	void addBuyEquityTransactionEntry() {
//		try {
//			account.addEntry(entry);
//		} catch (EntryInsertionException e) {
//			throw e;
//		}
//
//		try {
//			portfolio.addEntry(entry);
//		} catch (EntryInsertionException e) {
//			accounts.removeEntry(entry);
//			throw e;
//		}
//
//		entries.add(entry);
	}

	void addCashDepositEntry(int accountID, String tags, Date date, String comment, BigDecimal amount) throws ObjectNotFoundException, EntryInsertionException {
		Account account = findAccountByID(accountID);

		// TODO: Proper tag handling
		CashDepositEntry entry = new CashDepositEntry(account, null, date, comment, amount);

		try {
			account.addEntry(entry);
		} catch (EntryInsertionException e) {
			throw e;
		}

		entries.add(entry);
	}

	void addCashWithdrawalEntry(int accountID, String tags, Date date, String comment, BigDecimal amount) throws ObjectNotFoundException, EntryInsertionException {
		Account account = findAccountByID(accountID);

		// TODO: Proper tag handling
		CashWithdrawalEntry entry = new CashWithdrawalEntry(account, null, date, comment, amount);

		try {
			account.addEntry(entry);
		} catch (EntryInsertionException e) {
			throw e;
		}

		entries.add(entry);
	}

	void removeEntry(int entryID) {
		// TODO: Implement 2nd
	}

// TODO: Deep copy needed here?
//	ArrayList<Account> getAccounts() {
//		return new ArrayList<Account>(accounts);
//	}

// TODO: Deep copy needed here?
//	ArrayList<Portfolio> getPortfolios() {
//		return new ArrayList<Portfolio>(portfolios);
//	}

	String getName() {
		return name;
	}

	void setName(String name) {
		this.name = name;
	}

	String getOwner() {
		return owner;
	}

	void setOwner(String owner) {
		this.owner = owner;
	}

	Date getCreationDate() {
		return new Date(creationDate.getTime());
	}

	Date getLastUpdateDate() {
		return new Date(lastUpdateDate.getTime());
	}

	void setLastUpdateDate(Date lastUpdateDate) {
		this.lastUpdateDate = new Date(lastUpdateDate.getTime());
	}

	private Account findAccountByID(int accountID) throws ObjectNotFoundException {
		Account account = null;

		for (Account checkedAccount : accounts) {
			if (checkedAccount.getID() == accountID) {
				account = checkedAccount;
				break;
			}
		}

		if (account == null) {
			throw new ObjectNotFoundException();
		}

		return account;
	}

	private Portfolio findPortfolioByID(int portfolioID) throws ObjectNotFoundException {
		Portfolio portfolio = null;

		for (Portfolio checkedPortfolio : portfolios) {
			if (checkedPortfolio.getID() == portfolioID) {
				portfolio = checkedPortfolio;
				break;
			}
		}

		if (portfolio == null) {
			throw new ObjectNotFoundException();
		}

		return portfolio;
	}
}
