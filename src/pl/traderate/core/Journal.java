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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import pl.traderate.core.exception.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

/**
 * Main journal class.
 *
 * <b>Implementation details:</b>
 * Barricade function. All input should be sanitized inside implementations of
 * public methods of this class. Further internal calls to other classes in this
 * package assume that no additional checking is needed and passed values adhere to
 * expected standards (e.g. cash amounts are not negative etc.).
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

		JournalEntry.resetIDIncrement();
		Account.resetIDIncrement();
		Portfolio.resetIDIncrement();

		setName(name);
		setOwner(owner);
		creationDate = new Date();
		setLastUpdateDate(new Date());

		portfolios.add(new Portfolio(this, "Portfel globalny"));
	}

	void addAccount(String name) {
		accounts.add(new Account(name));
	}
	
	private void addAccount(String name, int accountID) {
		accounts.add(new Account(name, accountID));
	}

	void removeAccount(int accountID) throws ObjectNotFoundException, NodeNotEmptyException {
		Account account = findObjectByID(accountID, accounts);
		
		if (account.getEntries().size() == 0) {
			accounts.remove(account);
		} else {
			throw new NodeNotEmptyException();
		}
	}

	void addPortfolio(String name, int parentID) throws ObjectNotFoundException {
		portfolios.add(new Portfolio(this, name, findObjectByID(parentID, portfolios)));
	}
	
	private void addPortfolio(String name, int portfolioID, int parentID) throws ObjectNotFoundException {
		portfolios.add(new Portfolio(this, name, portfolioID, findObjectByID(parentID, portfolios)));
	}

	void removePortfolio(int portfolioID) throws ObjectNotFoundException, NodeNotEmptyException, GlobalPortfolioRemovalException {
		Portfolio portfolio = findObjectByID(portfolioID, portfolios);

		if (portfolio.getEntries().size() == 0 && portfolio.getChildren().size() == 0) {
			if (portfolio.getParent() == null) {
				throw new GlobalPortfolioRemovalException();
			}
			
			portfolio.getParent().removeChild(portfolio);
			portfolios.remove(portfolio);
		} else {
			throw new NodeNotEmptyException();
		}
	}

	void addBuyEquityTransactionEntry(int accountID, int portfolioID, String tags, Date date, String comment, String ticker, BigDecimal quantity, BigDecimal price, BigDecimal commission) throws ObjectNotFoundException, EntryInsertionException, ObjectConstraintsException, InvalidInputException {
		assertNumberIsPositive(quantity);
		assertNumberIsInteger(quantity);
		assertNumberIsNotNegative(price);
		assertNumberIsNotNegative(commission);

		quantity = sanitizeQuantity(quantity);
		price = sanitizePrice(price);
		commission = sanitizeCommission(commission);
		date = sanitizeDate(date);

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
		date = sanitizeDate(date);

		Account account = findObjectByID(accountID, accounts);
		Portfolio portfolio = findObjectByID(portfolioID, portfolios);

		// TODO: Proper SellEquityTransaction tag handling
		// TODO: Proper SellEquityTransaction position handling
		SellEquityTransactionEntry entry = new SellEquityTransactionEntry(account, portfolio, null, date, comment, ticker, quantity, price, commission, new StringBuilder(new SimpleDateFormat("yyyy-MM").format(date)).toString());

		addEntry(entry);
	}

	void addCashAllocationEntry(int accountID, int portfolioID, String tags, Date date, String comment, BigDecimal amount) throws ObjectNotFoundException, EntryInsertionException, InvalidInputException {
		assertNumberIsPositive(amount);

		amount = sanitizeCashAmount(amount);
		date = sanitizeDate(date);

		Account account = findObjectByID(accountID, accounts);
		Portfolio portfolio = findObjectByID(portfolioID, portfolios);

		// TODO: Proper tag handling
		CashAllocationEntry entry = new CashAllocationEntry(account, portfolio, null, date, comment, amount);

		addEntry(entry);
	}

	void addCashDeallocationEntry(int accountID, int portfolioID, String tags, Date date, String comment, BigDecimal amount) throws ObjectNotFoundException, EntryInsertionException, InvalidInputException {
		assertNumberIsPositive(amount);

		amount = sanitizeCashAmount(amount);
		date = sanitizeDate(date);

		Account account = findObjectByID(accountID, accounts);
		Portfolio portfolio = findObjectByID(portfolioID, portfolios);

		// TODO: Proper tag handling
		CashDeallocationEntry entry = new CashDeallocationEntry(account, portfolio, null, date, comment, amount);

		addEntry(entry);
	}

	void addCashDepositEntry(int accountID, String tags, Date date, String comment, BigDecimal amount) throws ObjectNotFoundException, EntryInsertionException, InvalidInputException {
		assertNumberIsPositive(amount);

		amount = sanitizeCashAmount(amount);
		date = sanitizeDate(date);

		Account account = findObjectByID(accountID, accounts);

		// TODO: Proper tag handling
		CashDepositEntry entry = new CashDepositEntry(account, null, date, comment, amount);

		addEntry(entry);
	}

	void addCashWithdrawalEntry(int accountID, String tags, Date date, String comment, BigDecimal amount) throws ObjectNotFoundException, EntryInsertionException, InvalidInputException {
		assertNumberIsPositive(amount);

		amount = sanitizeCashAmount(amount);
		date = sanitizeDate(date);

		Account account = findObjectByID(accountID, accounts);

		// TODO: Proper tag handling
		CashWithdrawalEntry entry = new CashWithdrawalEntry(account, null, date, comment, amount);

		addEntry(entry);
	}

	void removeEntry(int entryID) throws ObjectNotFoundException, EntryInsertionException {
		JournalEntry entry = findObjectByID(entryID, entries);
		removeEntry(entry);
	}

	private void addEntry(JournalEntry entry) throws EntryInsertionException {
		entry.attach();
		entries.add(entry);
	}

	private void removeEntry(JournalEntry entry) throws EntryInsertionException {
		entry.detach();
		entries.remove(entry);
	}

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

	ArrayList<Account> getAccounts() {
		return accounts;
	}

	Account getAccount(int accountID) throws ObjectNotFoundException {
		return findObjectByID(accountID, accounts);
	}

	ArrayList<JournalEntry> getEntries() {
		return entries;
	}
	
	Portfolio getGlobalPortfolio() {
		return portfolios.get(0);
	}

	Portfolio getPortfolio(int portfolioID) throws ObjectNotFoundException {
		return findObjectByID(portfolioID, portfolios);
	}

	public ArrayList<Portfolio> getOrderedPortfolios() {
		ArrayList<Portfolio> portfolios = new ArrayList<>();
		populatePortfolioList(portfolios, this.portfolios.get(0));
		return portfolios;
	}
	
	private void populatePortfolioList(ArrayList<Portfolio> portfolios, Portfolio portfolio) {
		portfolios.add(portfolio);
		for (Portfolio child : portfolio.getChildren()) {
			populatePortfolioList(portfolios, child);
		}
	}

	void update() {
		for (Account account : accounts) {
			account.update();
		}

		for (Portfolio portfolio : portfolios) {
			portfolio.update();
		}
	}

	void updateQuotes() {
		for (Account account : accounts) {
			account.updateQuotes();
		}

		for (Portfolio portfolio : portfolios) {
			portfolio.updateQuotes();
		}
	}

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

	private Date sanitizeDate(Date date) {
		SimpleDateFormat dateResolution = new SimpleDateFormat("dd.MM.yyyy");
		String dateString = dateResolution.format(date);
		Date roundedDate = null;

		try {
			roundedDate = dateResolution.parse(dateString);
		} catch (ParseException e) {
			throw new InternalLogicError();
		}

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(roundedDate);
		calendar.add(Calendar.HOUR, 12);
		return calendar.getTime();
	}

	public void saveToFile(File file) throws JournalSaveException {
		try {
			DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();

			Document document = documentBuilder.newDocument();

			Element journal = document.createElement("Journal");
			journal.setAttribute("name", name);
			journal.setAttribute("owner", owner);
			document.appendChild(journal);

			//:--- Save accounts

			Element accounts = document.createElement("Accounts");
			accounts.setAttribute("accountsCreated", ((Integer) Account.getNumberOfAccountsCreated()).toString());
			journal.appendChild(accounts);
			
			for (Account account : this.accounts) {
				Element accountNode = document.createElement("Account");
				accountNode.setAttribute("ID", ((Integer) account.getID()).toString());
				accountNode.setAttribute("name", account.getName());
				accounts.appendChild(accountNode);
			}

			//:--- Save portfolios

			Element portfolios = document.createElement("Portfolios");
			portfolios.setAttribute("portfoliosCreated", ((Integer) Portfolio.getNumberOfPortfoliosCreated()).toString());
			journal.appendChild(portfolios);

			for (Portfolio portfolio : this.portfolios) {
				Element portfolioNode = document.createElement("Portfolio");
				portfolioNode.setAttribute("ID", ((Integer) portfolio.getID()).toString());
				portfolioNode.setAttribute("name", portfolio.getName());
				if (portfolio.getParent() != null) {
					portfolioNode.setAttribute("parentID", ((Integer) portfolio.getParent().getID()).toString());
				}
				portfolios.appendChild(portfolioNode);
			}

			//:--- Save entries

			Element entries = document.createElement("Entries");
			entries.setAttribute("entriesCreated", ((Integer) JournalEntry.getNumberOfJournalEntriesCreated()).toString());
			journal.appendChild(entries);

			for (JournalEntry entry : this.entries) {
				Element entryNode = document.createElement("Entry");

				entryNode.setAttribute("ID", ((Integer) entry.getID()).toString());
				entryNode.setAttribute("date", new SimpleDateFormat("yyyy-MM-dd").format(entry.getDate()));
				entryNode.setAttribute("comment", entry.getComment());
				entryNode.setAttribute("accountID", ((Integer) entry.getAccount().getID()).toString());
				
				if (entry instanceof CashDepositEntry) {
					entryNode.setAttribute("type", "CashDeposit");
					entryNode.setAttribute("amount", ((CashDepositEntry) entry).getAmount().toPlainString());
				}

				if (entry instanceof CashWithdrawalEntry) {
					entryNode.setAttribute("type", "CashWithdrawal");
					entryNode.setAttribute("amount", ((CashWithdrawalEntry) entry).getAmount().toPlainString());
				}

				if (entry instanceof CashAllocationEntry) {
					entryNode.setAttribute("type", "CashAllocation");
					entryNode.setAttribute("amount", ((CashAllocationEntry) entry).getAmount().toPlainString());
					entryNode.setAttribute("portfolioID", ((Integer) ((CashAllocationEntry) entry).getPortfolioID()).toString());
				}

				if (entry instanceof CashDeallocationEntry) {
					entryNode.setAttribute("type", "CashDeallocation");
					entryNode.setAttribute("amount", ((CashDeallocationEntry) entry).getAmount().toPlainString());
					entryNode.setAttribute("portfolioID", ((Integer) ((CashDeallocationEntry) entry).getPortfolioID()).toString());
				}

				if (entry instanceof BuyEquityTransactionEntry) {
					entryNode.setAttribute("type", "BuyEquity");
					entryNode.setAttribute("portfolioID", ((Integer) ((BuyEquityTransactionEntry) entry).getPortfolioID()).toString());
					entryNode.setAttribute("ticker", ((BuyEquityTransactionEntry) entry).getTicker());
					entryNode.setAttribute("quantity", ((BuyEquityTransactionEntry) entry).getQuantity().toPlainString());
					entryNode.setAttribute("price", ((BuyEquityTransactionEntry) entry).getPrice().toPlainString());
					entryNode.setAttribute("commission", ((BuyEquityTransactionEntry) entry).getCommission().toPlainString());
				}

				if (entry instanceof SellEquityTransactionEntry) {
					entryNode.setAttribute("type", "SellEquity");
					entryNode.setAttribute("portfolioID", ((Integer) ((SellEquityTransactionEntry) entry).getPortfolioID()).toString());
					entryNode.setAttribute("ticker", ((SellEquityTransactionEntry) entry).getTicker());
					entryNode.setAttribute("quantity", ((SellEquityTransactionEntry) entry).getQuantity().toPlainString());
					entryNode.setAttribute("price", ((SellEquityTransactionEntry) entry).getPrice().toPlainString());
					entryNode.setAttribute("commission", ((SellEquityTransactionEntry) entry).getCommission().toPlainString());
				}
				
				entries.appendChild(entryNode);
			}

			//:--- Write file
			
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
			DOMSource source = new DOMSource(document);
			StreamResult result = new StreamResult(file);

			transformer.transform(source, result);
		} catch (ParserConfigurationException | TransformerException e) {
			throw new JournalSaveException();
		}
	}

	public void loadFromFile(File file) throws JournalLoadException {
		try {
			DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
			
			Document document = documentBuilder.parse(file);
			document.getDocumentElement().normalize();

			Element journal = document.getDocumentElement();
			if (!journal.getNodeName().equals("Journal")) throw new JournalLoadException();

			if (!journal.hasAttribute("name")) throw new JournalLoadException();
			if (!journal.hasAttribute("owner")) throw new JournalLoadException();
			String name = journal.getAttribute("name");
			String owner = journal.getAttribute("owner");
			this.name = name;
			this.owner = owner;
			
			//:--- Load accounts

			Element accounts = (Element) journal.getElementsByTagName("Accounts").item(0);			
			if (accounts == null) throw new JournalLoadException();

			if (!accounts.hasAttribute("accountsCreated")) throw new JournalLoadException();
			Integer accountsCreated = Integer.parseInt(accounts.getAttribute("accountsCreated"));

			NodeList accountNodes = accounts.getElementsByTagName("Account");
			for (int i = 0; i < accountNodes.getLength(); ++i) {
				Element account = (Element) accountNodes.item(i);
				if (!account.hasAttribute("name")) throw new JournalLoadException();
				if (!account.hasAttribute("ID")) throw new JournalLoadException();
				String accountName = account.getAttribute("name");
				Integer accountID = Integer.parseInt(account.getAttribute("ID"));
				
				if (accountID < 0) throw new JournalLoadException();
				
				addAccount(accountName, accountID);
			}
			
			if (accountsCreated < Account.getNumberOfAccountsCreated()) throw new JournalLoadException();
			Account.setNumberOfAccountsCreated(accountsCreated);

			//:--- Load portfolios

			Element portfolios = (Element) journal.getElementsByTagName("Portfolios").item(0);
			if (portfolios == null) throw new JournalLoadException();

			if (!portfolios.hasAttribute("portfoliosCreated")) throw new JournalLoadException();
			Integer portfoliosCreated = Integer.parseInt(portfolios.getAttribute("portfoliosCreated"));

			NodeList portfolioNodes = portfolios.getElementsByTagName("Portfolio");
			for (int i = 0; i < portfolioNodes.getLength(); ++i) {
				Element portfolio = (Element) portfolioNodes.item(i);
				if (!portfolio.hasAttribute("name")) throw new JournalLoadException();
				if (!portfolio.hasAttribute("ID")) throw new JournalLoadException();
				String portfolioName = portfolio.getAttribute("name");
				Integer portfolioID = Integer.parseInt(portfolio.getAttribute("ID"));

				if (portfolioID < 0) throw new JournalLoadException();

				if (portfolioID > 0) {
					if (!portfolio.hasAttribute("parentID")) throw new JournalLoadException();
					Integer portfolioParentID = Integer.parseInt(portfolio.getAttribute("parentID"));

					addPortfolio(portfolioName, portfolioID, portfolioParentID);
				}
			}

			if (portfoliosCreated < Portfolio.getNumberOfPortfoliosCreated()) throw new JournalLoadException();
			Portfolio.setNumberOfPortfoliosCreated(portfoliosCreated);

			//:--- Load entries

			Element entries = (Element) journal.getElementsByTagName("Entries").item(0);
			if (entries == null) throw new JournalLoadException();

			if (!entries.hasAttribute("entriesCreated")) throw new JournalLoadException();
			Integer entriesCreated = Integer.parseInt(entries.getAttribute("entriesCreated"));

			NodeList entryNodes = entries.getElementsByTagName("Entry");
			for (int i = 0; i < entryNodes.getLength(); ++i) {
				Element entry = (Element) entryNodes.item(i);

				if (!entry.hasAttribute("ID")) throw new JournalLoadException();
				if (!entry.hasAttribute("date")) throw new JournalLoadException();
				if (!entry.hasAttribute("comment")) throw new JournalLoadException();
				if (!entry.hasAttribute("accountID")) throw new JournalLoadException();
				
				Integer entryID = Integer.parseInt(entry.getAttribute("ID"));
				Date entryDate = new SimpleDateFormat("yyyy-MM-dd").parse(entry.getAttribute("date"));
				String entryComment = entry.getAttribute("comment");
				Integer entryAccountID = Integer.parseInt(entry.getAttribute("accountID"));

				if (entryID < 0) throw new JournalLoadException();
				
				if (!entry.hasAttribute("type")) throw new JournalLoadException();

				String entryType = entry.getAttribute("type");

				switch (entryType) {
					case "CashDeposit": {
						BigDecimal amount = new BigDecimal(entry.getAttribute("amount"));
						addCashDepositEntry(entryAccountID, "", entryDate, entryComment, amount);
						break;
					}
					case "CashWithdrawal": {
						BigDecimal amount = new BigDecimal(entry.getAttribute("amount"));
						addCashWithdrawalEntry(entryAccountID, "", entryDate, entryComment, amount);
						break;
					}
					case "CashAllocation": {
						BigDecimal amount = new BigDecimal(entry.getAttribute("amount"));
						Integer entryPortfolioID = Integer.parseInt(entry.getAttribute("portfolioID"));
						addCashAllocationEntry(entryAccountID, entryPortfolioID, "", entryDate, entryComment, amount);
						break;
					}
					case "CashDeallocation": {
						BigDecimal amount = new BigDecimal(entry.getAttribute("amount"));
						Integer entryPortfolioID = Integer.parseInt(entry.getAttribute("portfolioID"));
						addCashDeallocationEntry(entryAccountID, entryPortfolioID, "", entryDate, entryComment, amount);
						break;
					}
					case "BuyEquity": {
						Integer entryPortfolioID = Integer.parseInt(entry.getAttribute("portfolioID"));
						String ticker = entry.getAttribute("ticker");
						BigDecimal quantity = new BigDecimal(entry.getAttribute("quantity"));
						BigDecimal price = new BigDecimal(entry.getAttribute("price"));
						BigDecimal commission = new BigDecimal(entry.getAttribute("commission"));
						addBuyEquityTransactionEntry(entryAccountID, entryPortfolioID, "", entryDate, entryComment, ticker, quantity, price, commission);
						break;
					}
					case "SellEquity": {
						Integer entryPortfolioID = Integer.parseInt(entry.getAttribute("portfolioID"));
						String ticker = entry.getAttribute("ticker");
						BigDecimal quantity = new BigDecimal(entry.getAttribute("quantity"));
						BigDecimal price = new BigDecimal(entry.getAttribute("price"));
						BigDecimal commission = new BigDecimal(entry.getAttribute("commission"));
						addSellEquityTransactionEntry(entryAccountID, entryPortfolioID, "", entryDate, entryComment, ticker, quantity, price, commission);
						break;
					}
				}

				// Force sync ID
				this.entries.get(this.entries.size() - 1).setID(entryID);
			}

			// Check ID consistency
			HashMap<Integer, Boolean> entryIDs = new HashMap<>();
			
			for (JournalEntry entry : this.entries) {
				if (entryIDs.get(entry.getID()) == null) {
					entryIDs.put(entry.getID(), true);
				} else {
					// Duplicate ID found
					throw new JournalLoadException();
				}
			}

			if (entriesCreated < JournalEntry.getNumberOfJournalEntriesCreated()) throw new JournalLoadException();
			JournalEntry.setNumberOfJournalEntriesCreated(entriesCreated);
		} catch (Exception e) {
			throw new JournalLoadException();
		}
	}
}
