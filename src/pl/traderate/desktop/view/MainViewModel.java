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
import pl.traderate.core.PortfolioNodeDTO;
import pl.traderate.core.TradeRateConfig;
import pl.traderate.desktop.presenter.MainPresenter;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.util.ArrayList;

public class MainViewModel extends GenericViewModel {

	protected MainView view;

	private PortfolioNodeDTO rootPortfolioNode;
	
	private ArrayList<AccountDTO> accountNodes;

	private DefaultTreeModel navigationTree;

	private String version;

	public MainViewModel(MainPresenter presenter, GenericView summaryView, GenericView journalView) {
		super(presenter);

		view = new MainView(this, presenter, summaryView, journalView);
		addObserver(view);

		// Make sure that both views reference the same object
		super.view = view;

		version = TradeRateConfig.getVersion();
		accountNodes = new ArrayList<>();
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
		notifyChange(SyncType.NODES);
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

//:-- ViewModel sync types ---------------------------------------------------------

	public enum SyncType {
		NODES
	}
}
