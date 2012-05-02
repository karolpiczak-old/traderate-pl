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

package pl.traderate.desktop;

import pl.traderate.core.TradeRate;
import pl.traderate.core.exception.*;
import pl.traderate.desktop.presenter.MainPresenter;

import javax.swing.*;
import java.math.BigDecimal;
import java.util.Date;
import java.util.GregorianCalendar;

public final class Router {

	private final static Router instance = new Router();
	
	private TradeRate model;

	/**
	 * Restricted constructor.
	 *
	 * Prevents direct instantiation.
	 */
	private Router() {
		model = TradeRate.getInstance();
	}

	public static Router getInstance() {
		return instance;
	}

	public void goHome() {
		try {
		model.createJournal("Test", "Test");

		model.addAccount("Test account #1");              // ID: 0
		model.addAccount("Test account #2");              // ID: 1

		model.addPortfolio("Test portfolio #1", 0);       // ID: 1
		model.addPortfolio("Test portfolio #2", 0);       // ID: 2
		model.addPortfolio("Test portfolio #3", 0);       // ID: 3
		model.addPortfolio("Test portfolio #1.1", 1);     // ID: 4
		model.addPortfolio("Test portfolio #2.1", 2);     // ID: 5
		model.addPortfolio("Test portfolio #3.1", 3);     // ID: 6
		model.addPortfolio("Test portfolio #1.2", 1);     // ID: 7
		model.addPortfolio("Test portfolio #1.2.1", 7);   // ID: 8

		model.addCashDepositEntry(0, "Example tag", new GregorianCalendar(2000, 0, 1).getTime(), "Some comment", new BigDecimal("10000.00"));
		model.addCashDepositEntry(1, "Example tag", new GregorianCalendar(2000, 0, 1).getTime(), "Some comment", new BigDecimal("10000.00"));

		model.addCashAllocationEntry(0, 1, "Example tag", new GregorianCalendar(2000, 0, 2).getTime(), "Some comment", new BigDecimal("1000.00"));
		model.addCashAllocationEntry(0, 2, "Example tag", new GregorianCalendar(2000, 0, 2).getTime(), "Some comment", new BigDecimal("1000.00"));
		model.addCashAllocationEntry(1, 3, "Example tag", new GregorianCalendar(2000, 0, 2).getTime(), "Some comment", new BigDecimal("1000.00"));
		model.addCashAllocationEntry(1, 4, "Example tag", new GregorianCalendar(2000, 0, 2).getTime(), "Some comment", new BigDecimal("1000.00"));

		// Check buy entries with random date order
		model.addBuyEquityTransactionEntry(0, 1, "Example tag", new GregorianCalendar(2000, 0, 3).getTime(), "Some comment", "TICKER-A", new BigDecimal("10"), new BigDecimal("5.00"), new BigDecimal("10.00"));
		model.addBuyEquityTransactionEntry(0, 1, "Example tag", new GregorianCalendar(2000, 1, 4).getTime(), "Some comment", "TICKER-A", new BigDecimal("10"), new BigDecimal("10.00"), new BigDecimal("5.00"));
		model.addBuyEquityTransactionEntry(0, 1, "Example tag", new GregorianCalendar(2000, 0, 2).getTime(), "Some comment", "TICKER-A", new BigDecimal("10"), new BigDecimal("15.00"), new BigDecimal("0.00"));

		model.addBuyEquityTransactionEntry(0, 2, "Example tag", new GregorianCalendar(2000, 0, 2).getTime(), "Some comment", "TICKER-A", new BigDecimal("10"), new BigDecimal("15.00"), new BigDecimal("0.00"));

		model.addSellEquityTransactionEntry(0, 1, "Example tag", new GregorianCalendar(2001, 0, 5).getTime(), "Some comment", "TICKER-A", new BigDecimal("5"), new BigDecimal("20.00"), new BigDecimal("0.00"));
		model.addSellEquityTransactionEntry(0, 1, "Example tag", new GregorianCalendar(2000, 0, 6).getTime(), "Some comment", "TICKER-A", new BigDecimal("3"), new BigDecimal("10.00"), new BigDecimal("0.00"));

		model.addSellEquityTransactionEntry(0, 2, "Example tag", new GregorianCalendar(2001, 0, 5).getTime(), "Some comment", "TICKER-A", new BigDecimal("5"), new BigDecimal("20.00"), new BigDecimal("0.00"));

		model.addBuyEquityTransactionEntry(0, 2, "Example tag", new GregorianCalendar(2000, 0, 2).getTime(), "Some comment", "KGHM", new BigDecimal("1"), new BigDecimal("100.00"), new BigDecimal("3.00"));
		} catch (ObjectNotFoundException | ObjectConstraintsException | EntryInsertionException | JournalNotLoadedException | InvalidInputException e) {
			e.printStackTrace();
		}

		MainPresenter presenter = new MainPresenter(model);
		presenter.show();

		new SwingWorker<String, Object>() {

			@Override
			public String doInBackground() {
				try {
					model.updateQuotes();
				} catch (JournalNotLoadedException e) {
					e.printStackTrace();
				}
				return null;
			}
		}.execute();
	}
}
