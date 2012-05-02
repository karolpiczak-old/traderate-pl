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

import com.michaelbaranov.microba.calendar.DatePicker;
import pl.traderate.core.AccountDTO;
import pl.traderate.core.PortfolioNodeDTO;
import pl.traderate.desktop.presenter.JournalPresenter;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static pl.traderate.desktop.presenter.JournalPresenter.Events;

public class JournalView extends GenericView {

	private JournalViewModel viewModel;

	/**
	 * Typecasted reference to the summary form.
	 *
	 * Hides <tt>form</tt> from superclass for convenience only.
	 */
	private JournalForm form;

	JournalView(JournalViewModel viewModel, JournalPresenter presenter) {
		super(viewModel, presenter);
		this.viewModel = (JournalViewModel) super.viewModel;

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				form = new JournalForm(JournalView.this);

				// Make sure that both forms reference the same object
				JournalView.super.form = form;
			}
		});
	}

	/**
	 *
	 */
	protected void syncViewModel(final Object arg) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if (arg instanceof JournalViewModel.SyncType) {
					JournalViewModel.SyncType syncType = (JournalViewModel.SyncType) arg;
					switch (syncType) {
						case NODES:
							form.allocationEntryAccount.removeAllItems();
							form.cashEntryAccount.removeAllItems();
							form.equityEntryAccount.removeAllItems();
							form.removeAccountSelector.removeAllItems();
							for (AccountDTO account : viewModel.getAccounts()) {
								form.allocationEntryAccount.addItem(account);
								form.cashEntryAccount.addItem(account);
								form.equityEntryAccount.addItem(account);
								form.removeAccountSelector.addItem(account);
							}

							form.allocationEntryPortfolio.removeAllItems();
							form.equityEntryPortfolio.removeAllItems();
							form.addPortfolioParentSelector.removeAllItems();
							form.removePortfolioSelector.removeAllItems();
							for (PortfolioNodeDTO portfolio : viewModel.getPortfolios()) {
								form.allocationEntryPortfolio.addItem(portfolio);
								form.equityEntryPortfolio.addItem(portfolio);
								form.addPortfolioParentSelector.addItem(portfolio);
								form.removePortfolioSelector.addItem(portfolio);
							}
							break;
						case ENTRIES:
							form.entries.setModel(viewModel.getJournalTable());
							viewModel.getJournalTable().install(form.entries);
							break;
					}
				}
			}
		});
	}

	@Override
	public JournalForm getForm() {
		return form;
	}

	public void setActiveTab(int i) {
		form.entryCreator.setSelectedIndex(i);
	}

	//:-- Listeners for GUI events -------------------------------------------------

	// Allocation entry events
	
	public class OnAllocationEntryDateChanged implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			viewModel.setAllocationEntryDate(((DatePicker) e.getSource()).getDate());
		}
	}

	public class OnAllocationEntryAccountChanged implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			AccountDTO selected = (AccountDTO) ((JComboBox) e.getSource()).getSelectedItem();
			viewModel.setAllocationEntryAccount(selected);
		}
	}

	public class OnAllocationEntryPortfolioChanged implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			PortfolioNodeDTO selected = (PortfolioNodeDTO) ((JComboBox) e.getSource()).getSelectedItem();
			viewModel.setAllocationEntryPortfolio(selected);
		}
	}

	public class OnAllocationEntryAmountChanged implements ChangeListener {

		@Override
		public void stateChanged(ChangeEvent e) {
			viewModel.setAllocationEntryAmount((Double) form.allocationEntryAmount.getValue());
		}
	}

	public class OnAllocationEntryTypeChanged implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			int index = ((JComboBox) e.getSource()).getSelectedIndex();

			if (index == 0) {
				viewModel.setAllocationEntryType(JournalViewModel.AllocationEntryType.ALLOCATION);
			} else {
				viewModel.setAllocationEntryType(JournalViewModel.AllocationEntryType.DEALLOCATION);
			}
		}
	}

	public class OnAllocationEntryCommentChanged implements DocumentListener {

		public void updated(DocumentEvent e) {
			Document document = e.getDocument();
			String comment;

			try {
				comment = document.getText(0, document.getLength());
			} catch (BadLocationException exception) {
				comment = "";
			}

			viewModel.setAllocationEntryComment(comment);
		}

		@Override
		public void insertUpdate(DocumentEvent e) {
			updated(e);
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			updated(e);
		}

		@Override
		public void changedUpdate(DocumentEvent e) {
			updated(e);
		}
	}

	public class OnAllocationEntrySubmitted implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			fireEvent(new Events.AllocationEntrySubmitted(this));
		}
	}
	
	// Cash entry events

	public class OnCashEntryDateChanged implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			viewModel.setCashEntryDate(((DatePicker) e.getSource()).getDate());
		}
	}

	public class OnCashEntryAccountChanged implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			AccountDTO selected = (AccountDTO) ((JComboBox) e.getSource()).getSelectedItem();
			viewModel.setCashEntryAccount(selected);
		}
	}

	public class OnCashEntryAmountChanged implements ChangeListener {

		@Override
		public void stateChanged(ChangeEvent e) {
			viewModel.setCashEntryAmount((Double) form.cashEntryAmount.getValue());
		}
	}

	public class OnCashEntryTypeChanged implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			int index = ((JComboBox) e.getSource()).getSelectedIndex();

			if (index == 0) {
				viewModel.setCashEntryType(JournalViewModel.CashEntryType.DEPOSIT);
			} else {
				viewModel.setCashEntryType(JournalViewModel.CashEntryType.WITHDRAWAL);
			}
		}
	}

	public class OnCashEntryCommentChanged implements DocumentListener {

		public void updated(DocumentEvent e) {
			Document document = e.getDocument();
			String comment;

			try {
				comment = document.getText(0, document.getLength());
			} catch (BadLocationException exception) {
				comment = "";
			}

			viewModel.setCashEntryComment(comment);
		}

		@Override
		public void insertUpdate(DocumentEvent e) {
			updated(e);
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			updated(e);
		}

		@Override
		public void changedUpdate(DocumentEvent e) {
			updated(e);
		}
	}

	public class OnCashEntrySubmitted implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			fireEvent(new Events.CashEntrySubmitted(this));
		}
	}

	// Equity entry events

	public class OnEquityEntryDateChanged implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			viewModel.setEquityEntryDate(((DatePicker) e.getSource()).getDate());
		}
	}

	public class OnEquityEntryAccountChanged implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			AccountDTO selected = (AccountDTO) ((JComboBox) e.getSource()).getSelectedItem();
			viewModel.setEquityEntryAccount(selected);
		}
	}

	public class OnEquityEntryPortfolioChanged implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			PortfolioNodeDTO selected = (PortfolioNodeDTO) ((JComboBox) e.getSource()).getSelectedItem();
			viewModel.setEquityEntryPortfolio(selected);
		}
	}

	public class OnEquityEntryQuantityChanged implements ChangeListener {

		@Override
		public void stateChanged(ChangeEvent e) {
			viewModel.setEquityEntryQuantity((Integer) form.equityEntryQuantity.getValue());
		}
	}

	public class OnEquityEntryPriceChanged implements ChangeListener {

		@Override
		public void stateChanged(ChangeEvent e) {
			viewModel.setEquityEntryPrice((Double) form.equityEntryPrice.getValue());
		}
	}

	public class OnEquityEntryCommissionChanged implements ChangeListener {

		@Override
		public void stateChanged(ChangeEvent e) {
			viewModel.setEquityEntryCommission((Double) form.equityEntryCommission.getValue());
		}
	}

	public class OnEquityEntryTypeChanged implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			int index = ((JComboBox) e.getSource()).getSelectedIndex();

			if (index == 0) {
				viewModel.setEquityEntryType(JournalViewModel.EquityEntryType.BUY);
			} else {
				viewModel.setEquityEntryType(JournalViewModel.EquityEntryType.SELL);
			}
		}
	}

	public class OnEquityEntryCommentChanged implements DocumentListener {

		public void updated(DocumentEvent e) {
			Document document = e.getDocument();
			String comment;

			try {
				comment = document.getText(0, document.getLength());
			} catch (BadLocationException exception) {
				comment = "";
			}

			viewModel.setEquityEntryComment(comment);
		}

		@Override
		public void insertUpdate(DocumentEvent e) {
			updated(e);
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			updated(e);
		}

		@Override
		public void changedUpdate(DocumentEvent e) {
			updated(e);
		}
	}

	public class OnEquityEntryTickerChanged implements DocumentListener {

		public void updated(DocumentEvent e) {
			Document document = e.getDocument();
			String ticker;

			try {
				ticker = document.getText(0, document.getLength());
			} catch (BadLocationException exception) {
				ticker = "";
			}

			viewModel.setEquityEntryTicker(ticker);
		}

		@Override
		public void insertUpdate(DocumentEvent e) {
			updated(e);
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			updated(e);
		}

		@Override
		public void changedUpdate(DocumentEvent e) {
			updated(e);
		}
	}

	public class OnEquityEntrySubmitted implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			fireEvent(new Events.EquityEntrySubmitted(this));
		}
	}

	// Management events

	public class OnAddAccountRequested implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			fireEvent(new Events.AddAccountRequested(this));
		}
	}

	public class OnAddPortfolioRequested implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			fireEvent(new Events.AddPortfolioRequested(this));
		}
	}

	public class OnRemoveAccountRequested implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			fireEvent(new Events.RemoveAccountRequested(this));
		}
	}

	public class OnRemovePortfolioRequested implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			fireEvent(new Events.RemovePortfolioRequested(this));
		}
	}

	public class OnAddAccountNameChanged implements DocumentListener {

		public void updated(DocumentEvent e) {
			Document document = e.getDocument();
			String name;

			try {
				name = document.getText(0, document.getLength());
			} catch (BadLocationException exception) {
				name = "";
			}

			viewModel.setAddAccountName(name);
		}

		@Override
		public void insertUpdate(DocumentEvent e) {
			updated(e);
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			updated(e);
		}

		@Override
		public void changedUpdate(DocumentEvent e) {
			updated(e);
		}
	}

	public class OnAddPortfolioNameChanged implements DocumentListener {

		public void updated(DocumentEvent e) {
			Document document = e.getDocument();
			String name;

			try {
				name = document.getText(0, document.getLength());
			} catch (BadLocationException exception) {
				name = "";
			}

			viewModel.setAddPortfolioName(name);
		}

		@Override
		public void insertUpdate(DocumentEvent e) {
			updated(e);
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			updated(e);
		}

		@Override
		public void changedUpdate(DocumentEvent e) {
			updated(e);
		}
	}

	public class OnAddPortfolioParentChanged implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			PortfolioNodeDTO selected = (PortfolioNodeDTO) ((JComboBox) e.getSource()).getSelectedItem();
			viewModel.setAddPortfolioParent(selected);
		}
	}

	public class OnRemovePortfolioChanged implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			PortfolioNodeDTO selected = (PortfolioNodeDTO) ((JComboBox) e.getSource()).getSelectedItem();
			viewModel.setRemovePortfolio(selected);
		}
	}

	public class OnRemoveAccountChanged implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			AccountDTO selected = (AccountDTO) ((JComboBox) e.getSource()).getSelectedItem();
			viewModel.setRemoveAccount(selected);
		}
	}

}
