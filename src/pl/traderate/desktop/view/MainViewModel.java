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
import pl.traderate.core.PortfolioDetailsDTO;
import pl.traderate.core.PortfolioNodeDTO;
import pl.traderate.core.TradeRateConfig;
import pl.traderate.desktop.presenter.MainPresenter;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;

public class MainViewModel extends GenericViewModel {

	protected MainView view;

	private PortfolioNodeDTO rootPortfolioNode;

	private Object selectedNode;
	
	private SelectedType selectedType;

	private String selectedName;

	private BigDecimal selectedCash;

	private BigDecimal selectedAggregatedCash;

	private ArrayList<AccountDTO> accountNodes;

	private DefaultTreeModel navigationTree;

	private String version;

	private String journalName;

	private String journalOwner;

	private boolean interfaceLocked;

	private File journalFile;

	private boolean journalUnsaved;

	public MainViewModel(MainPresenter presenter, GenericView summaryView, GenericView journalView) {
		super(presenter);

		view = new MainView(this, presenter, summaryView, journalView);
		addObserver(view);

		// Make sure that both views reference the same object
		super.view = view;

		version = TradeRateConfig.getVersion();
		accountNodes = new ArrayList<>();

		interfaceLocked = true;

		notifyChange(SyncType.META);
		notifyChange(SyncType.LOCK);
	}

	public MainView getView() {
		return view;
	}

	public void setRootPortfolioNode(PortfolioNodeDTO rootPortfolioNode) {
		this.rootPortfolioNode = rootPortfolioNode;
		updateNavigationTree();
		notifyChange(SyncType.NODES);
	}

	public void setAccountNodes(ArrayList<AccountDTO> accounts) {
		this.accountNodes = accounts;
		updateNavigationTree();
		notifyChange(SyncType.NODES);
	}

	private void updateNavigationTree() {
		if (rootPortfolioNode == null) {
			navigationTree = new DefaultTreeModel(new DefaultMutableTreeNode("---"));
		} else {
			DefaultMutableTreeNode root = new DefaultMutableTreeNode("Portfele i konta");
			DefaultMutableTreeNode accounts = new DefaultMutableTreeNode("Konta");

			DefaultMutableTreeNode portfolio = new DefaultMutableTreeNode(rootPortfolioNode);
			populatePortfolioChildren(portfolio);

			for (AccountDTO account : accountNodes) {
				accounts.add(new DefaultMutableTreeNode(account));
			}

			root.add(portfolio);
			root.add(accounts);

			navigationTree = new DefaultTreeModel(root);
		}
		notifyChange(SyncType.NODES);
	}

	public void setSelectedNode(Object selectedNode) {
		this.selectedNode = selectedNode;
		
		if (this.selectedNode instanceof PortfolioDetailsDTO) {
			PortfolioDetailsDTO portfolio = (PortfolioDetailsDTO) this.selectedNode;
			this.selectedType = SelectedType.PORTFOLIO;
			this.selectedName = portfolio.name;
			this.selectedCash = portfolio.cashBalance;
			this.selectedAggregatedCash = portfolio.aggregatedCashBalance;
			notifyChange(SyncType.INFO);
		}
		
		if (this.selectedNode instanceof AccountDTO) {
			AccountDTO account = (AccountDTO) this.selectedNode;
			this.selectedType = SelectedType.ACCOUNT;
			this.selectedName = account.name;
			this.selectedCash = account.unallocatedCash;
			this.selectedAggregatedCash = account.cashBalance;
			notifyChange(SyncType.INFO);
		}
	}

	public void purgeSelectedNode() {
		this.selectedNode = null;
		this.selectedType = null;
		this.selectedName = null;
		this.selectedCash = null;
		this.selectedAggregatedCash = null;
	}

	private void populatePortfolioChildren(DefaultMutableTreeNode portfolioNode) {
		PortfolioNodeDTO portfolioTree = (PortfolioNodeDTO) portfolioNode.getUserObject();
		
		for (PortfolioNodeDTO child : portfolioTree.children) {
			DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(child);
			populatePortfolioChildren(childNode);
			portfolioNode.add(childNode);
		}
	}

	public DefaultTreeModel getNavigationTree() {
		return navigationTree;
	}

	public String getVersion() {
		return version;
	}

	public void setActiveTab(int i) {
		view.setActiveTab(i);
	}

	public void setJournalName(String journalName) {
		this.journalName = journalName;
		notifyChange(SyncType.META);
	}

	public void setJournalOwner(String journalOwner) {
		this.journalOwner = journalOwner;
		notifyChange(SyncType.META);
	}

	public String getJournalName() {
		return journalName;
	}

	public String getJournalOwner() {
		return journalOwner;
	}

	public void setInterfaceLock(boolean lock) {
		interfaceLocked = lock;
		notifyChange(SyncType.LOCK);
	}

	public boolean isInterfaceLocked() {
		return interfaceLocked;
	}

	public void purgeRootPortfolioNode() {
		rootPortfolioNode = null;
		updateNavigationTree();
		notifyChange(SyncType.NODES);
	}

	public void purgeAccountNodes() {
		accountNodes = new ArrayList<>();
		updateNavigationTree();
		notifyChange(SyncType.NODES);
	}

	public void purgeJournalName() {
		journalName = null;
		notifyChange(SyncType.META);
	}

	public void purgeJournalOwner() {
		journalOwner = null;
		notifyChange(SyncType.META);
	}

	public File getJournalFile() {
		return journalFile;
	}

	public void setJournalFile(File journalFile) {
		this.journalFile = journalFile;
	}

	public void setJournalUnsaved(boolean journalUnsaved) {
		this.journalUnsaved = journalUnsaved;
		notifyChange(SyncType.LOCK);
	}

	public boolean isJournalUnsaved() {
		return journalUnsaved;
	}

	public SelectedType getSelectedType() {
		return selectedType;
	}

	public String getSelectedName() {
		return selectedName;
	}

	public BigDecimal getSelectedCash() {
		return selectedCash;
	}

	public BigDecimal getSelectedAggregatedCash() {
		return selectedAggregatedCash;
	}

	//:-- ViewModel sync types ---------------------------------------------------------

	public enum SyncType {
		NODES,
		META,
		LOCK,
		INFO
	}
	
	public enum SelectedType {
		ACCOUNT,
		PORTFOLIO
	}
}
