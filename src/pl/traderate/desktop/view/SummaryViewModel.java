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

import org.netbeans.swing.outline.OutlineModel;
import pl.traderate.core.AccountDTO;
import pl.traderate.core.HoldingsDTO;
import pl.traderate.core.PortfolioDetailsDTO;
import pl.traderate.desktop.presenter.SummaryPresenter;

import java.math.BigDecimal;
import java.util.ArrayList;

public class SummaryViewModel extends GenericViewModel {

	protected SummaryView view;
	
	protected Integer nodeID;
	
	protected String nodeName;
	
	protected NodeType nodeType;

	public enum NodeType {
		ACCOUNT,
		PORTFOLIO
	}

	protected BigDecimal currentValue;

	protected BigDecimal openValue;

	protected BigDecimal paperGain;

	protected BigDecimal cashAvailable;

	protected BigDecimal aggregatedCash;

	protected BigDecimal realizedIncome;

	protected BigDecimal realizedCost;

	protected BigDecimal realizedGain;

	protected BigDecimal realizedTax;

	protected BigDecimal taxPaid;

	protected BigDecimal taxDue;
	
	protected HoldingsDTO holdings;

	private OutlineModel openHoldingsTreeTable;

	private OutlineModel closedHoldingsTreeTable;

	public SummaryViewModel(SummaryPresenter presenter) {
		super(presenter);

		view = new SummaryView(this, presenter);
		addObserver(view);

		// Make sure that both views reference the same object
		super.view = view;
	}

	public SummaryView getView() {
		return view;
	}

	public void setNode(PortfolioDetailsDTO portfolio) {
		nodeID = portfolio.ID;
		nodeName = portfolio.name;
		nodeType = NodeType.PORTFOLIO;
		cashAvailable = portfolio.cashBalance;
		aggregatedCash = portfolio.aggregatedCashBalance;
		currentValue = portfolio.currentValue;
		openValue = portfolio.openValue;
		paperGain = portfolio.paperGain;
		realizedGain = portfolio.realizedGain;
		realizedIncome = portfolio.realizedIncome;
		realizedCost = portfolio.realizedCost;
		holdings = portfolio.aggregatedHoldings;
		
		updateHoldings();
		notifyChange(SyncType.NODE);
	}

	public void setNode(AccountDTO account) {
		nodeID = account.ID;
		nodeName = account.name;
		nodeType = NodeType.ACCOUNT;
		cashAvailable = account.unallocatedCash;
		aggregatedCash = account.cashBalance;
		currentValue = account.currentValue;
		openValue = account.openValue;
		paperGain = account.paperGain;
		realizedGain = account.realizedGain;
		realizedIncome = account.realizedIncome;
		realizedCost = account.realizedCost;
		holdings = account.holdings;
		
		updateHoldings();
		notifyChange(SyncType.NODE);
	}

	public void purgeNode() {
		nodeID = null;
		nodeName = null;
		nodeType = null;
		cashAvailable = null;
		aggregatedCash = null;
		currentValue = null;
		openValue = null;
		paperGain = null;
		realizedGain = null;
		realizedIncome = null;
		realizedCost = null;
		holdings = null;

		purgeHoldings();
		notifyChange(SyncType.NODE);
	}

	private void updateHoldings() {
		HoldingTable.ParentType parentType = nodeType == NodeType.PORTFOLIO ? HoldingTable.ParentType.PORTFOLIO : HoldingTable.ParentType.ACCOUNT;
		openHoldingsTreeTable = new HoldingTable("Otwarte pozycje", holdings.equityHoldings, false, parentType, nodeID).getOutlineModel();
		closedHoldingsTreeTable = new HoldingTable("Zamknięte pozycje", holdings.closedEquityHoldings, true, parentType, nodeID).getOutlineModel();
	}

	private void purgeHoldings() {
		openHoldingsTreeTable = new HoldingTable("Otwarte pozycje", new ArrayList<HoldingsDTO.EquityHoldingDTO>(), false, HoldingTable.ParentType.PORTFOLIO, 0).getOutlineModel();
		closedHoldingsTreeTable = new HoldingTable("Zamknięte pozycje", new ArrayList<HoldingsDTO.EquityHoldingDTO>(), true, HoldingTable.ParentType.PORTFOLIO, 0).getOutlineModel();
	}

	public String getNodeName() {
		return nodeName;
	}

	public NodeType getNodeType() {
		return nodeType;
	}

	public BigDecimal getCurrentValue() {
		return currentValue;
	}

	public BigDecimal getOpenValue() {
		return openValue;
	}

	public BigDecimal getPaperGain() {
		return paperGain;
	}

	public BigDecimal getCashAvailable() {
		return cashAvailable;
	}

	public BigDecimal getAggregatedCash() {
		return aggregatedCash;
	}

	public BigDecimal getRealizedIncome() {
		return realizedIncome;
	}

	public BigDecimal getRealizedCost() {
		return realizedCost;
	}

	public BigDecimal getRealizedGain() {
		return realizedGain;
	}

	public BigDecimal getRealizedTax() {
		return realizedTax;
	}

	public BigDecimal getTaxPaid() {
		return taxPaid;
	}

	public BigDecimal getTaxDue() {
		return taxDue;
	}

	public OutlineModel getOpenHoldingsTreeTable() {
		return openHoldingsTreeTable;
	}

	public OutlineModel getClosedHoldingsTreeTable() {
		return closedHoldingsTreeTable;
	}

	public int getNodeID() {
		return nodeID;
	}

//:-- ViewModel sync types ---------------------------------------------------------

	public enum SyncType {
		NODE
	}
}
