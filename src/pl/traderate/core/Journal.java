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
import pl.traderate.core.exception.InvalidInputException;
import pl.traderate.core.exception.ObjectConstraintsException;
import pl.traderate.core.exception.ObjectNotFoundException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 *
 *
 * <b>Implementation details:</b>
 * Barricade function. All input should be sanitized inside implementations of
 * public methods of this class. Further internal calls to other classes in this
 * package assume that no additional checking is needed and passed values adhere to
 * expected standards (e.g. cash amount are not negative etc.).
 */
class Journal {

	/** */
	private final ArrayList<JournalEntry> entries;

	/** */
	private final ArrayList<Account> accounts;

	/** */
	private final ArrayList<Portfolio> portfolios;

	/** */
	private final ArrayList<Tag> tags;

	/** */
	private String name;

	/** */
	private String owner;

	/** */
	private final Date creationDate;

	/** */
	private Date lastUpdateDate;

	/**
	 *
	 * @param name
	 * @param owner
	 */
	Journal(String name, String owner) {
		entries = new ArrayList<JournalEntry>(1000);
		accounts = new ArrayList<Account>(10);
		portfolios = new ArrayList<Portfolio>(25);
		tags = new ArrayList<Tag>(25);

		JournalEntry.resetIDIncrement();
		Account.resetIDIncrement();
		Portfolio.resetIDIncrement();

		setName(name);
		setOwner(owner);
		creationDate = new Date();
		setLastUpdateDate(new Date());

		portfolios.add(new Portfolio(this, "Portfel globalny"));
	}

	/**
	 *
	 * @param name
	 */
	void addAccount(String name) {
		accounts.add(new Account(name));
	}

	/**
	 *
	 * @param name
	 * @param parentID
	 * @throws ObjectNotFoundException
	 */
	void addPortfolio(String name, int parentID) throws ObjectNotFoundException {
		portfolios.add(new Portfolio(this, name, findObjectByID(parentID, portfolios)));
	}

	void addBuyEquityTransactionEntry(int accountID, int portfolioID, String tags, Date date, String comment, String ticker, BigDecimal quantity, BigDecimal price, BigDecimal commission) throws ObjectNotFoundException, EntryInsertionException, ObjectConstraintsException, InvalidInputException {
		assertNumberIsPositive(quantity);
		assertNumberIsInteger(quantity);
		assertNumberIsNotNegative(price);
		assertNumberIsNotNegative(commission);

		quantity = sanitizeQuantity(quantity);
		price = sanitizePrice(price);
		commission = sanitizeCommission(commission);

		Account account = findObjectByID(accountID, accounts);
		Portfolio portfolio = findObjectByID(portfolioID, portfolios);

		// TODO: Proper BuyEquityTransaction tag handling
		// TODO: Proper BuyEquityTransaction position handling
		BuyEquityTransactionEntry entry = new BuyEquityTransactionEntry(account, portfolio, null, date, comment, ticker, quantity, price, commission, new StringBuilder(new SimpleDateFormat("yyyy-MM").format(date)).toString());

		addEntry(entry);
	}

	void addSellEquityTransactionEntry(int accountID, int portfolioID, String tags, Date date, String comment, String ticker, BigDecimal quantity, BigDecimal price, BigDecimal commission) throws ObjectNotFoundException, EntryInsertionException, ObjectConstraintsException, InvalidInputException {
		assertNumberIsPositive(quantity);
		assertNumberIsInteger(quantity);
		assertNumberIsNotNegative(price);
		assertNumberIsNotNegative(commission);

		quantity = sanitizeQuantity(quantity);
		price = sanitizePrice(price);
		commission = sanitizeCommission(commission);

		Account account = findObjectByID(accountID, accounts);
		Portfolio portfolio = findObjectByID(portfolioID, portfolios);

		// TODO: Proper SellEquityTransaction tag handling
		// TODO: Proper SellEquityTransaction position handling
		SellEquityTransactionEntry entry = new SellEquityTransactionEntry(account, portfolio, null, date, comment, ticker, quantity, price, commission, new StringBuilder(new SimpleDateFormat("yyyy-MM").format(date)).toString());

		addEntry(entry);
	}

	/**
	 *
	 * @param accountID
	 * @param portfolioID
	 * @param tags
	 * @param date
	 * @param comment
	 * @param amount
	 * @throws ObjectNotFoundException
	 * @throws EntryInsertionException
	 */
	void addCashAllocationEntry(int accountID, int portfolioID, String tags, Date date, String comment, BigDecimal amount) throws ObjectNotFoundException, EntryInsertionException, InvalidInputException {
		assertNumberIsPositive(amount);

		amount = sanitizeCashAmount(amount);

		Account account = findObjectByID(accountID, accounts);
		Portfolio portfolio = findObjectByID(portfolioID, portfolios);

		// TODO: Proper tag handling
		CashAllocationEntry entry = new CashAllocationEntry(account, portfolio, null, date, comment, amount);

		addEntry(entry);
	}

	/**
	 *
	 * @param accountID
	 * @param portfolioID
	 * @param tags
	 * @param date
	 * @param comment
	 * @param amount
	 * @throws ObjectNotFoundException
	 * @throws EntryInsertionException
	 */
	void addCashDeallocationEntry(int accountID, int portfolioID, String tags, Date date, String comment, BigDecimal amount) throws ObjectNotFoundException, EntryInsertionException, InvalidInputException {
		assertNumberIsPositive(amount);

		amount = sanitizeCashAmount(amount);

		Account account = findObjectByID(accountID, accounts);
		Portfolio portfolio = findObjectByID(portfolioID, portfolios);

		// TODO: Proper tag handling
		CashDeallocationEntry entry = new CashDeallocationEntry(account, portfolio, null, date, comment, amount);

		addEntry(entry);
	}

	/**
	 *
	 * @param accountID
	 * @param tags
	 * @param date
	 * @param comment
	 * @param amount
	 * @throws ObjectNotFoundException
	 * @throws EntryInsertionException
	 * @throws InvalidInputException
	 */
	void addCashDepositEntry(int accountID, String tags, Date date, String comment, BigDecimal amount) throws ObjectNotFoundException, EntryInsertionException, InvalidInputException {
		assertNumberIsPositive(amount);

		amount = sanitizeCashAmount(amount);

		Account account = findObjectByID(accountID, accounts);

		// TODO: Proper tag handling
		CashDepositEntry entry = new CashDepositEntry(account, null, date, comment, amount);

		addEntry(entry);
	}

	/**
	 *
	 * @param accountID
	 * @param tags
	 * @param date
	 * @param comment
	 * @param amount
	 * @throws ObjectNotFoundException
	 * @throws EntryInsertionException
	 */
	void addCashWithdrawalEntry(int accountID, String tags, Date date, String comment, BigDecimal amount) throws ObjectNotFoundException, EntryInsertionException, InvalidInputException {
		assertNumberIsPositive(amount);

		amount = sanitizeCashAmount(amount);

		Account account = findObjectByID(accountID, accounts);

		// TODO: Proper tag handling
		CashWithdrawalEntry entry = new CashWithdrawalEntry(account, null, date, comment, amount);

		addEntry(entry);
	}

	/**
	 *
	 * @param entryID
	 * @throws ObjectNotFoundException
	 * @throws EntryInsertionException
	 */
	void removeEntry(int entryID) throws ObjectNotFoundException, EntryInsertionException {
		JournalEntry entry = findObjectByID(entryID, entries);
		removeEntry(entry);
	}

	/**
	 *
	 * @param entry
	 * @throws EntryInsertionException
	 */
	private void addEntry(JournalEntry entry) throws EntryInsertionException {
		entry.attach();
		entries.add(entry);
	}

	/**
	 *
	 * @param entry
	 * @throws EntryInsertionException
	 */
	private void removeEntry(JournalEntry entry) throws EntryInsertionException {
		entry.detach();
		entries.remove(entry);
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
	String getOwner() {
		return owner;
	}

	/**
	 *
	 * @param owner
	 */
	void setOwner(String owner) {
		this.owner = owner;
	}

	/**
	 *
	 * @return
	 */
	Date getCreationDate() {
		return new Date(creationDate.getTime());
	}

	/**
	 *
	 * @return
	 */
	Date getLastUpdateDate() {
		return new Date(lastUpdateDate.getTime());
	}

	/**
	 *
	 * @param lastUpdateDate
	 */
	void setLastUpdateDate(Date lastUpdateDate) {
		this.lastUpdateDate = new Date(lastUpdateDate.getTime());
	}

	ArrayList<Account> getAccounts() {
		return accounts;
	}
	
	Portfolio getGlobalPortfolio() {
		return portfolios.get(0);
	}

	void update() {
		for (Account account : accounts) {
			account.update();
		}

		for (Portfolio portfolio : portfolios) {
			portfolio.update();
		}
	}
	
	/**
	 *
	 * @param objectID
	 * @param arrayList
	 * @param <T>
	 * @return
	 * @throws ObjectNotFoundException
	 */
	private <T extends Identifiable> T findObjectByID(int objectID, ArrayList<T> arrayList) throws ObjectNotFoundException {
		T object = null;

		for (T checkedObject : arrayList) {
			if (checkedObject.getID() == objectID) {
				object = checkedObject;
				break;
			}
		}

		if (object == null) {
			throw new ObjectNotFoundException();
		}

		return object;
	}

	private void assertNumberIsPositive(BigDecimal number) throws InvalidInputException {
		if ((number.compareTo(BigDecimal.ZERO) <= 0)) {
			throw new InvalidInputException();
		}
	}

	private void assertNumberIsNotNegative(BigDecimal number) throws InvalidInputException {
		if ((number.compareTo(BigDecimal.ZERO) < 0)) {
			throw new InvalidInputException();
		}
	}

	private void assertNumberIsInteger(BigDecimal number) throws InvalidInputException {
		try {
			number.intValueExact();
		} catch (ArithmeticException e) {
			throw new InvalidInputException();
		}
	}

	private BigDecimal sanitizeCommission(BigDecimal number) {
		number = number.setScale(2, RoundingMode.HALF_EVEN);
		return number;
	}

	private BigDecimal sanitizePrice(BigDecimal number) {
		number = number.setScale(2, RoundingMode.HALF_EVEN);
		return number;
	}

	private BigDecimal sanitizeQuantity(BigDecimal number) {
		number = number.setScale(0);
		return number;
	}

	private BigDecimal sanitizeCashAmount(BigDecimal number) {
		number = number.setScale(2, RoundingMode.HALF_EVEN);
		return number;
	}

}
