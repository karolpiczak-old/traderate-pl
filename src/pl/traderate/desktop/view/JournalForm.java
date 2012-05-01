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

	JSpinner allocationEntryAmount;

	JComboBox<String> allocationEntryType;

	JComboBox<AccountDTO> allocationEntryAccount;

	JComboBox<PortfolioNodeDTO> allocationEntryPortfolio;

	DatePicker allocationEntryDate;

	JButton allocationEntrySubmitButton;

	JPanel equityEntryCreatorTab;

	JPanel cashEntryCreatorTab;

	JPanel allocationEntryCreatorTab;

	JPanel nodesCreatorTab;

	JTextField allocationEntryComment;

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
	}

	void show() {

	}

	private void createUIComponents() {
		entries = new JTable();

		allocationEntryDate = new DatePicker(new Date(), new SimpleDateFormat("dd.MM.yyyy"));

		String[] allocationTypes = { "Alokacja", "Dealokacja" };
		allocationEntryType = new JComboBox<String>(allocationTypes);

		allocationEntryAccount = new JComboBox<AccountDTO>();

		allocationEntryPortfolio = new JComboBox<PortfolioNodeDTO>();

		allocationEntryAmount = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 999999999999.99, 1.0));
		allocationEntryAmount.setEditor(new JSpinner.NumberEditor(allocationEntryAmount, "0.00"));
	}
}
