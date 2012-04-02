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

import pl.traderate.core.PortfolioDetailsDTO;
import pl.traderate.core.PortfolioNodeDTO;
import pl.traderate.desktop.presenter.SummaryPresenter;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.math.BigDecimal;

public class SummaryViewModel extends GenericViewModel {

	protected SummaryView view;
	
	protected String portfolioName;

	protected BigDecimal currentValueOfOpenPositions;

	protected BigDecimal openValueOfOpenPositions;

	protected BigDecimal paperGain;

	protected BigDecimal realizedIncome;

	protected BigDecimal realizedCost;

	protected BigDecimal realizedGain;

	protected BigDecimal realizedTax;

	protected BigDecimal taxPaid;

	protected BigDecimal taxDue;

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

	public void setPortfolio(PortfolioDetailsDTO portfolio) {
		this.portfolioName = portfolio.name;
		//this.currentValueOfOpenPositions = portfolio.holdings;

		
		notifyChange();
	}
}
