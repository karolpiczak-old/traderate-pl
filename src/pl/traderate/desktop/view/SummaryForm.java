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

import org.netbeans.swing.outline.Outline;

import javax.swing.*;

public class SummaryForm extends GenericForm {

	JPanel root;

	JLabel paperGain;

	JLabel currentValue;

	JLabel openValue;

	JLabel cashAvailable;

	JLabel portfolioName;

	JLabel aggregatedCash;

	JLabel realizedGain;

	JLabel realizedIncome;

	JLabel realizedCost;

	JLabel realizedTax;

	JLabel taxPaid;

	Outline openHoldingsTreeTable;

	Outline closedHoldingsTreeTable;

	JLabel cashAvailableLabel;

	JLabel aggregatedCashLabel;

	JLabel taxDue;

	JTable allocationTable;

	public SummaryForm(GenericView view) {
		super(view);
		this.view = (SummaryView) super.view;

		openHoldingsTreeTable.setRootVisible(false);
		closedHoldingsTreeTable.setRootVisible(false);
	}

	void show() {

	}

	private void createUIComponents() {
		allocationTable = new JTable();
	}
}
