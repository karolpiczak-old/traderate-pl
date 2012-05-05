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

import pl.traderate.core.event.*;
import pl.traderate.core.exception.*;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
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

	private String journalName;

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
		fireEvent(new JournalCreatedModelEvent(this));
	}

	public void openJournal(File file) throws JournalLoadException {
		Journal openedJournal = new Journal("", "");
		openedJournal.loadFromFile(file);
		journal = openedJournal;
		fireEvent(new JournalOpenedModelEvent(this));
	}

	public void saveJournal(File file) throws JournalNotLoadedException, JournalSaveException {
		assertJournalIsLoaded();
		journal.saveToFile(file);
		fireEvent(new JournalSavedModelEvent(this));
	}

	public void closeJournal() throws JournalNotLoadedException {
		assertJournalIsLoaded();
		journal = null;
		fireEvent(new JournalClosedModelEvent(this));
	}

	/**
	 *
	 * @param name
	 * @throws JournalNotLoadedException
	 */
	public void addAccount(String name) throws JournalNotLoadedException {
		assertJournalIsLoaded();
		journal.addAccount(name);

		fireEvent(new NodesUpdatedModelEvent(this));
	}

	/**
	 *
	 * @param accountID
	 */
	public void removeAccount(int accountID) throws JournalNotLoadedException, ObjectNotFoundException, NodeNotEmptyException {
		assertJournalIsLoaded();
		journal.removeAccount(accountID);

		fireEvent(new NodesUpdatedModelEvent(this));
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

		fireEvent(new NodesUpdatedModelEvent(this));
	}

	/**
	 *
	 * @param portfolioID
	 * @throws JournalNotLoadedException
	 */
	public void removePortfolio(int portfolioID) throws JournalNotLoadedException, ObjectNotFoundException, NodeNotEmptyException, GlobalPortfolioRemovalException {
		assertJournalIsLoaded();
		journal.removePortfolio(portfolioID);

		fireEvent(new NodesUpdatedModelEvent(this));
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
		updateQuotes();

		fireEvent(new JournalUpdatedModelEvent(this));
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
		updateQuotes();

		fireEvent(new JournalUpdatedModelEvent(this));
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
		updateQuotes();

		fireEvent(new JournalUpdatedModelEvent(this));
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
		updateQuotes();

		fireEvent(new JournalUpdatedModelEvent(this));
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
		updateQuotes();

		fireEvent(new JournalUpdatedModelEvent(this));
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
		updateQuotes();

		fireEvent(new JournalUpdatedModelEvent(this));
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
		updateQuotes();

		fireEvent(new JournalUpdatedModelEvent(this));
	}

	public void updateQuotes() throws JournalNotLoadedException {
		assertJournalIsLoaded();
		journal.updateQuotes();

		fireEvent(new QuoteUpdatedModelEvent(this));
	}
	
	public ArrayList<AccountDTO> getAccounts() throws JournalNotLoadedException {
		assertJournalIsLoaded();
		ArrayList<AccountDTO> accountDTOs = new ArrayList<>();
		for (Account account : journal.getAccounts()) {
			accountDTOs.add(account.getDTO(journal.getOrderedPortfolios()));
		}

		return accountDTOs;
	}

	public AccountDTO getAccount(int accountID) throws ObjectNotFoundException, JournalNotLoadedException {
		assertJournalIsLoaded();
		return journal.getAccount(accountID).getDTO(journal.getOrderedPortfolios());
	}

	public ArrayList<JournalEntryDTO> getEntries() throws JournalNotLoadedException {
		assertJournalIsLoaded();
		ArrayList<JournalEntryDTO> entryDTOs = new ArrayList<>();
		for (JournalEntry entry : journal.getEntries()) {
			entryDTOs.add(new JournalEntryDTO(entry));
		}

		Collections.sort(entryDTOs);

		return entryDTOs;
	}

	public PortfolioNodeDTO getPortfolioNodes() throws JournalNotLoadedException {
		assertJournalIsLoaded();
		return journal.getGlobalPortfolio().getNodeDTO();
	}

	public ArrayList<PortfolioNodeDTO> getAllPortfolioNodes() throws JournalNotLoadedException {
		assertJournalIsLoaded();
		ArrayList<PortfolioNodeDTO> portfolios = new ArrayList<>();
		PortfolioNodeDTO root = journal.getGlobalPortfolio().getNodeDTO();
		portfolios.add(root);

		populateChildNodes(root, portfolios);

		return portfolios;
	}

	private void populateChildNodes(PortfolioNodeDTO parent, ArrayList<PortfolioNodeDTO> portfolios) {
		for (PortfolioNodeDTO child : parent.children) {
			portfolios.add(child);
			populateChildNodes(child, portfolios);
		}
	}

	public PortfolioDetailsDTO getPortfolio(int portfolioID) throws ObjectNotFoundException, JournalNotLoadedException {
		assertJournalIsLoaded();
		return journal.getPortfolio(portfolioID).getDetailsDTO(journal.getAccounts());
	}

	/**
	 *
	 * @throws JournalNotLoadedException
	 */
	private void assertJournalIsLoaded() throws JournalNotLoadedException {
		if (journal == null) throw new JournalNotLoadedException();
	}

	public String getJournalName() throws JournalNotLoadedException {
		assertJournalIsLoaded();
		return journal.getName();
	}
	
	public String getJournalOwner() throws JournalNotLoadedException {
		assertJournalIsLoaded();
		return journal.getOwner();
	}
}
