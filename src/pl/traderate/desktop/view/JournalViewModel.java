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

package pl.traderate.desktop.view;

import pl.traderate.core.AccountDTO;
import pl.traderate.core.JournalEntryDTO;
import pl.traderate.core.PortfolioNodeDTO;
import pl.traderate.desktop.presenter.JournalPresenter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;

public class JournalViewModel extends GenericViewModel {

	protected JournalView view;

	protected ArrayList<JournalEntryDTO> entries;
	
	protected JournalTable journalTable;

	protected ArrayList<AccountDTO> accounts;

	protected ArrayList<PortfolioNodeDTO> portfolios;
	
	// Allocation entry

	protected Date allocationEntryDate = new Date();

	protected AccountDTO allocationEntryAccount;

	protected PortfolioNodeDTO allocationEntryPortfolio;

	protected BigDecimal allocationEntryAmount = new BigDecimal("1000.00");

	protected AllocationEntryType allocationEntryType = AllocationEntryType.ALLOCATION;

	protected String allocationEntryComment;
	
	// Cash entry

	protected Date cashEntryDate = new Date();

	protected AccountDTO cashEntryAccount;

	protected BigDecimal cashEntryAmount = new BigDecimal("1000.00");

	protected CashEntryType cashEntryType = CashEntryType.DEPOSIT;

	protected String cashEntryComment;
	
	// Equity entry

	protected Date equityEntryDate = new Date();

	protected AccountDTO equityEntryAccount;

	protected PortfolioNodeDTO equityEntryPortfolio;

	protected BigDecimal equityEntryQuantity = BigDecimal.ONE;

	protected BigDecimal equityEntryPrice = new BigDecimal("10.00");

	protected BigDecimal equityEntryCommission = BigDecimal.ZERO;

	protected EquityEntryType equityEntryType = EquityEntryType.BUY;

	protected String equityEntryComment;

	protected String equityEntryTicker;

	// Management tab

	protected String addPortfolioName = "Nowy portfel";

	protected String addAccountName = "Nowe konto";

	protected AccountDTO removeAccount;

	protected PortfolioNodeDTO removePortfolio;

	protected PortfolioNodeDTO addPortfolioParent;

	public JournalViewModel(JournalPresenter presenter) {
		super(presenter);

		view = new JournalView(this, presenter);
		addObserver(view);

		// Make sure that both views reference the same object
		super.view = view;
	}

	public JournalView getView() {
		return view;
	}

	public void setEntries(ArrayList<JournalEntryDTO> entries) {
		this.entries = entries;
		journalTable = new JournalTable(this.entries);

		notifyChange(SyncType.ENTRIES);
	}

	public JournalTable getJournalTable() {
		return journalTable;
	}

	public void setActiveTab(int i) {
		view.setActiveTab(i);
	}

	public ArrayList<AccountDTO> getAccounts() {
		return accounts;
	}

	public ArrayList<PortfolioNodeDTO> getPortfolios() {
		return portfolios;
	}

	public void setAccounts(ArrayList<AccountDTO> accounts) {
		this.accounts = accounts;
		this.allocationEntryAccount = this.accounts.get(0);

		notifyChange(SyncType.NODES);
	}

	public void setPortfolios(ArrayList<PortfolioNodeDTO> portfolios) {
		this.portfolios = portfolios;
		this.allocationEntryPortfolio = this.portfolios.get(0);

		notifyChange(SyncType.NODES);
	}

	// Allocation entry handling

	public void setAllocationEntryType(AllocationEntryType allocationEntryType) {
		this.allocationEntryType = allocationEntryType;
	}

	public void setAllocationEntryAmount(double allocationEntryAmount) {
		this.allocationEntryAmount = new BigDecimal(allocationEntryAmount).setScale(2, RoundingMode.HALF_EVEN);
	}
	
	public void setAllocationEntryPortfolio(PortfolioNodeDTO allocationEntryPortfolio) {
		this.allocationEntryPortfolio = allocationEntryPortfolio;
	}

	public void setAllocationEntryAccount(AccountDTO allocationEntryAccount) {
		this.allocationEntryAccount = allocationEntryAccount;
	}

	public void setAllocationEntryDate(Date allocationEntryDate) {
		this.allocationEntryDate = allocationEntryDate;
	}

	public void setAllocationEntryComment(String allocationEntryComment) {
		this.allocationEntryComment = allocationEntryComment;
	}

	public AccountDTO getAllocationEntryAccount() {
		return allocationEntryAccount;
	}

	public PortfolioNodeDTO getAllocationEntryPortfolio() {
		return allocationEntryPortfolio;
	}

	public Date getAllocationEntryDate() {
		return allocationEntryDate;
	}

	public BigDecimal getAllocationEntryAmount() {
		return allocationEntryAmount;
	}

	public AllocationEntryType getAllocationEntryType() {
		return allocationEntryType;
	}

	public String getAllocationEntryComment() {
		return allocationEntryComment;
	}

	public enum AllocationEntryType {
		ALLOCATION,
		DEALLOCATION
	}
	
	// Cash entry handling

	public void setCashEntryType(CashEntryType cashEntryType) {
		this.cashEntryType = cashEntryType;
	}

	public void setCashEntryAmount(double cashEntryAmount) {
		this.cashEntryAmount = new BigDecimal(cashEntryAmount).setScale(2, RoundingMode.HALF_EVEN);
	}

	public void setCashEntryAccount(AccountDTO cashEntryAccount) {
		this.cashEntryAccount = cashEntryAccount;
	}

	public void setCashEntryDate(Date cashEntryDate) {
		this.cashEntryDate = cashEntryDate;
	}

	public void setCashEntryComment(String cashEntryComment) {
		this.cashEntryComment = cashEntryComment;
	}

	public AccountDTO getCashEntryAccount() {
		return cashEntryAccount;
	}

	public Date getCashEntryDate() {
		return cashEntryDate;
	}

	public BigDecimal getCashEntryAmount() {
		return cashEntryAmount;
	}

	public CashEntryType getCashEntryType() {
		return cashEntryType;
	}

	public String getCashEntryComment() {
		return cashEntryComment;
	}

	public enum CashEntryType {
		DEPOSIT,
		WITHDRAWAL
	}

	// Equity entry handling

	public void setEquityEntryType(EquityEntryType equityEntryType) {
		this.equityEntryType = equityEntryType;
	}

	public void setEquityEntryQuantity(int equityEntryQuantity) {
		this.equityEntryQuantity = new BigDecimal(equityEntryQuantity);
	}

	public void setEquityEntryPrice(double equityEntryPrice) {
		this.equityEntryPrice = new BigDecimal(equityEntryPrice).setScale(2, RoundingMode.HALF_EVEN);
	}

	public void setEquityEntryCommission(double equityEntryCommission) {
		this.equityEntryCommission = new BigDecimal(equityEntryCommission).setScale(2, RoundingMode.HALF_EVEN);
	}
	
	public void setEquityEntryPortfolio(PortfolioNodeDTO equityEntryPortfolio) {
		this.equityEntryPortfolio = equityEntryPortfolio;
	}

	public void setEquityEntryAccount(AccountDTO equityEntryAccount) {
		this.equityEntryAccount = equityEntryAccount;
	}

	public void setEquityEntryDate(Date equityEntryDate) {
		this.equityEntryDate = equityEntryDate;
	}

	public void setEquityEntryComment(String equityEntryComment) {
		this.equityEntryComment = equityEntryComment;
	}

	public void setEquityEntryTicker(String equityEntryTicker) {
		this.equityEntryTicker = equityEntryTicker;
	}

	public AccountDTO getEquityEntryAccount() {
		return equityEntryAccount;
	}

	public PortfolioNodeDTO getEquityEntryPortfolio() {
		return equityEntryPortfolio;
	}

	public Date getEquityEntryDate() {
		return equityEntryDate;
	}

	public BigDecimal getEquityEntryQuantity() {
		return equityEntryQuantity;
	}

	public BigDecimal getEquityEntryPrice() {
		return equityEntryPrice;
	}

	public BigDecimal getEquityEntryCommission() {
		return equityEntryCommission;
	}

	public EquityEntryType getEquityEntryType() {
		return equityEntryType;
	}

	public String getEquityEntryComment() {
		return equityEntryComment;
	}

	public String getEquityEntryTicker() {
		return equityEntryTicker;
	}

	public enum EquityEntryType {
		BUY,
		SELL
	}

	// Management handling

	public void setAddPortfolioName(String addPortfolioName) {
		this.addPortfolioName = addPortfolioName;
	}

	public void setAddAccountName(String addAccountName) {
		this.addAccountName = addAccountName;
	}

	public void setRemoveAccount(AccountDTO removeAccount) {
		this.removeAccount = removeAccount;
	}

	public void setRemovePortfolio(PortfolioNodeDTO removePortfolio) {
		this.removePortfolio = removePortfolio;
	}

	public void setAddPortfolioParent(PortfolioNodeDTO addPortfolioParent) {
		this.addPortfolioParent = addPortfolioParent;
	}

	public String getAddAccountName() {
		return addAccountName;
	}

	public String getAddPortfolioName() {
		return addPortfolioName;
	}

	public PortfolioNodeDTO getAddPortfolioParent() {
		return addPortfolioParent;
	}

	public AccountDTO getRemoveAccount() {
		return removeAccount;
	}

	public PortfolioNodeDTO getRemovePortfolio() {
		return removePortfolio;
	}

//:-- ViewModel sync types ---------------------------------------------------------

	public enum SyncType {
		ENTRIES,
		NODES
	}
}
