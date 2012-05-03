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

import pl.traderate.desktop.presenter.SummaryPresenter;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class SummaryView extends GenericView {

	private SummaryViewModel viewModel;

	/**
	 * Typecasted reference to the summary form.
	 *
	 * Hides <tt>form</tt> from superclass for convenience only.
	 */
	private SummaryForm form;

	SummaryView(SummaryViewModel viewModel, SummaryPresenter presenter) {
		super(viewModel, presenter);
		this.viewModel = (SummaryViewModel) super.viewModel;

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				form = new SummaryForm(SummaryView.this);

				// Make sure that both forms reference the same object
				SummaryView.super.form = form;
			}
		});
	}

	/**
	 *
	 */
	protected void syncViewModel(final Object arg) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if (arg instanceof SummaryViewModel.SyncType) {
					SummaryViewModel.SyncType syncType = (SummaryViewModel.SyncType) arg;
					switch (syncType) {
						case NODE:
							form.portfolioName.setText(viewModel.getNodeName() == null ? "" : viewModel.getNodeName());
							form.cashAvailable.setText(viewModel.getCashAvailable() == null ? "---" : viewModel.getCashAvailable().toPlainString());
							form.aggregatedCash.setText(viewModel.getAggregatedCash() == null ? "---" : viewModel.getAggregatedCash().toPlainString());
							form.openHoldingsTreeTable.setModel(viewModel.getOpenHoldingsTreeTable());
							form.closedHoldingsTreeTable.setModel(viewModel.getClosedHoldingsTreeTable());
							break;
					}
				}
			}
		});
	}

	@Override
	public SummaryForm getForm() {
		return form;
	}

	//:-- Listeners for GUI events -------------------------------------------------


}
