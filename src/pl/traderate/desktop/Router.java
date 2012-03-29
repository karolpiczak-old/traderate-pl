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
import pl.traderate.core.exception.EntryInsertionException;
import pl.traderate.core.exception.JournalNotLoadedException;
import pl.traderate.core.exception.ObjectNotFoundException;
import pl.traderate.desktop.presenter.MainPresenter;

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
		MainPresenter presenter = new MainPresenter(model);
		presenter.show();

		model.createJournal("Dziennik", "Jan Kowalski");

		try {
			model.addAccount("Test");
			model.addPortfolio("Test", 0);
			model.addCashDepositEntry(0, "tag1", new GregorianCalendar(2011, 1, 1).getTime(), "Komentarz", new BigDecimal("100.00"));
			model.addAccount("Test2");
			//model.addCashWithdrawalEntry(0, "tag1", new GregorianCalendar(2012, 1, 3).getTime(), "Komentarz", new BigDecimal("50.00"));
			model.addCashDepositEntry(0, "tag1", new GregorianCalendar(2012, 1, 2).getTime(), "Komentarz", new BigDecimal("200.00"));
			//model.addCashWithdrawalEntry(0, "tag1", new GregorianCalendar(2011, 1, 3).getTime(), "Komentarz", new BigDecimal("30.00"));
			//model.removeEntry(1);
			model.addCashAllocationEntry(0, 0, "tag2", new GregorianCalendar(2012, 1, 3).getTime(), "Komentarz", new BigDecimal("100.00"));
			model.addCashAllocationEntry(0, 0, "tag2", new GregorianCalendar(2011, 1, 3).getTime(), "Komentarz", new BigDecimal("100.00"));
			//model.addCashAllocationEntry(0, 0, "tag2", new GregorianCalendar(2010, 1, 3).getTime(), "Komentarz", new BigDecimal("50.00"));
			model.addCashDeallocationEntry(0, 0, "tag2", new GregorianCalendar(2012, 1, 4).getTime(), "Komentarz", new BigDecimal("100.00"));
			model.addAccount("Test3");
		} catch (JournalNotLoadedException | ObjectNotFoundException | EntryInsertionException e) {
			e.printStackTrace();
		}
	}
}
