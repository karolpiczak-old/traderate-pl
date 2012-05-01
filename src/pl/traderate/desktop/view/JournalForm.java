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

import javax.swing.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class JournalForm extends GenericForm {

	JournalView view;
	
	JPanel root;

	JTable entries;

	JTabbedPane entryCreator;

	// Allocation tab
	
	JSpinner allocationEntryAmount;

	JComboBox<String> allocationEntryType;

	JComboBox<AccountDTO> allocationEntryAccount;

	JComboBox<PortfolioNodeDTO> allocationEntryPortfolio;

	DatePicker allocationEntryDate;

	JButton allocationEntrySubmitButton;

	JPanel allocationEntryCreatorTab;

	JPanel nodesCreatorTab;

	JTextField allocationEntryComment;
	
	// Cash tab

	JPanel cashEntryCreatorTab;

	DatePicker cashEntryDate;

	JComboBox<AccountDTO> cashEntryAccount;

	JSpinner cashEntryAmount;

	JComboBox<String> cashEntryType;

	JTextField cashEntryComment;

	JButton cashEntrySubmitButton;
	
	// Equity tab

	JPanel equityEntryCreatorTab;

	DatePicker equityEntryDate;

	JComboBox<AccountDTO> equityEntryAccount;

	JComboBox<PortfolioNodeDTO> equityEntryPortfolio;

	JComboBox<String> equityEntryType;

	JTextField equityEntryTicker;

	JSpinner equityEntryQuantity;

	JSpinner equityEntryPrice;

	JSpinner equityEntryCommission;

	JTextField equityEntryComment;

	JButton equityEntrySubmitButton;

	// Management tab

	JButton addAccountButton;

	JButton addPortfolioButton;

	JButton removeAccountButton;

	JButton removePortfolioButton;

	JTextField addAccountName;

	JTextField addPortfolioName;

	JComboBox<PortfolioNodeDTO> addPortfolioParentSelector;

	JComboBox<PortfolioNodeDTO> removePortfolioSelector;

	JComboBox<AccountDTO> removeAccountSelector;

	public JournalForm(GenericView view) {
		super(view);
		this.view = (JournalView) super.view;

		allocationEntryDate.addActionListener(this.view.new OnAllocationEntryDateChanged());
		allocationEntryAccount.addActionListener(this.view.new OnAllocationEntryAccountChanged());
		allocationEntryPortfolio.addActionListener(this.view.new OnAllocationEntryPortfolioChanged());
		allocationEntryAmount.addChangeListener(this.view.new OnAllocationEntryAmountChanged());
		allocationEntryType.addActionListener(this.view.new OnAllocationEntryTypeChanged());
		allocationEntryComment.getDocument().addDocumentListener(this.view.new OnAllocationEntryCommentChanged());
		allocationEntrySubmitButton.addActionListener(this.view.new OnAllocationEntrySubmitted());

		cashEntryDate.addActionListener(this.view.new OnCashEntryDateChanged());
		cashEntryAccount.addActionListener(this.view.new OnCashEntryAccountChanged());
		cashEntryAmount.addChangeListener(this.view.new OnCashEntryAmountChanged());
		cashEntryType.addActionListener(this.view.new OnCashEntryTypeChanged());
		cashEntryComment.getDocument().addDocumentListener(this.view.new OnCashEntryCommentChanged());
		cashEntrySubmitButton.addActionListener(this.view.new OnCashEntrySubmitted());

		equityEntryDate.addActionListener(this.view.new OnEquityEntryDateChanged());
		equityEntryAccount.addActionListener(this.view.new OnEquityEntryAccountChanged());
		equityEntryPortfolio.addActionListener(this.view.new OnEquityEntryPortfolioChanged());
		equityEntryType.addActionListener(this.view.new OnEquityEntryTypeChanged());
		equityEntryTicker.getDocument().addDocumentListener(this.view.new OnEquityEntryTickerChanged());
		equityEntryQuantity.addChangeListener(this.view.new OnEquityEntryQuantityChanged());
		equityEntryPrice.addChangeListener(this.view.new OnEquityEntryPriceChanged());
		equityEntryCommission.addChangeListener(this.view.new OnEquityEntryCommissionChanged());
		equityEntryType.addActionListener(this.view.new OnEquityEntryTypeChanged());
		equityEntryComment.getDocument().addDocumentListener(this.view.new OnEquityEntryCommentChanged());
		equityEntrySubmitButton.addActionListener(this.view.new OnEquityEntrySubmitted());
	}

	void show() {

	}

	private void createUIComponents() {
		entries = new JTable();

		// Cash entry tab
		cashEntryDate = new DatePicker(new Date(), new SimpleDateFormat("dd.MM.yyyy"));

		String[] cashTypes = { "Wpłata", "Wypłata" };
		cashEntryType = new JComboBox<>(cashTypes);

		cashEntryAccount = new JComboBox<>();

		cashEntryAmount = new JSpinner(new SpinnerNumberModel(1000.0, 0.0, 999999999999.99, 1.0));
		cashEntryAmount.setEditor(new JSpinner.NumberEditor(cashEntryAmount, "0.00"));

		// Allocation entry tab
		allocationEntryDate = new DatePicker(new Date(), new SimpleDateFormat("dd.MM.yyyy"));

		String[] allocationTypes = { "Alokacja", "Dealokacja" };
		allocationEntryType = new JComboBox<>(allocationTypes);

		allocationEntryAccount = new JComboBox<>();

		allocationEntryPortfolio = new JComboBox<>();

		allocationEntryAmount = new JSpinner(new SpinnerNumberModel(1000.0, 0.0, 999999999999.99, 1.0));
		allocationEntryAmount.setEditor(new JSpinner.NumberEditor(allocationEntryAmount, "0.00"));
		
		// Equity entry tab
		equityEntryDate = new DatePicker(new Date(), new SimpleDateFormat("dd.MM.yyyy"));

		String[] equityTypes = { "Kupno", "Sprzedaż" };
		equityEntryType = new JComboBox<>(equityTypes);

		equityEntryAccount = new JComboBox<>();

		equityEntryPortfolio = new JComboBox<>();

		equityEntryPrice = new JSpinner(new SpinnerNumberModel(10.0, 0.01, 999999999999.99, 1.0));
		equityEntryPrice.setEditor(new JSpinner.NumberEditor(equityEntryPrice, "0.00"));

		equityEntryCommission = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 999999999999.99, 1.0));
		equityEntryCommission.setEditor(new JSpinner.NumberEditor(equityEntryCommission, "0.00"));

		equityEntryQuantity = new JSpinner(new SpinnerNumberModel(1, 1, 999999999, 1));
		equityEntryQuantity.setEditor(new JSpinner.NumberEditor(equityEntryQuantity, "0"));

		// Management tab
		addPortfolioParentSelector = new JComboBox<>();
		removeAccountSelector = new JComboBox<>();
		removePortfolioSelector = new JComboBox<>();
	}
}
