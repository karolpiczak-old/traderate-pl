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
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;

public class JournalViewModel extends GenericViewModel {

	protected JournalView view;

	protected ArrayList<JournalEntryDTO> entries;
	
	protected JournalTable journalTable;

	protected ArrayList<AccountDTO> accounts;

	protected ArrayList<PortfolioNodeDTO> portfolios;

	protected Date allocationEntryDate = new Date();

	protected AccountDTO allocationEntryAccount;

	protected PortfolioNodeDTO allocationEntryPortfolio;

	protected BigDecimal allocationEntryAmount = BigDecimal.ZERO;

	protected AllocationEntryType allocationEntryType = AllocationEntryType.ALLOCATION;

	protected String allocationEntryComment;

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

	public void setAllocationEntryType(AllocationEntryType allocationEntryType) {
		this.allocationEntryType = allocationEntryType;
	}

	public void setAllocationEntryAmount(double allocationEntryAmount) {
		this.allocationEntryAmount = new BigDecimal(allocationEntryAmount).setScale(2, RoundingMode.HALF_EVEN);
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

//:-- ViewModel sync types ---------------------------------------------------------

	public enum SyncType {
		ENTRIES,
		NODES
	}
}
