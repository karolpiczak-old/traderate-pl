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

package pl.traderate.core;

/**
 * Main application class.
 *
 * Singleton encapsulating all business logic.
 */
public final class TradeRate {

	private final static TradeRate instance = new TradeRate();

	/**
	 * Currently open journal.
	 *
	 * Per design only one journal can be opened at a time.
	 */
	private Journal journal;

	/**
	 * Restricted constructor.
	 *
	 * Prevents direct instantiation.
	 */
	private TradeRate() {

	}

	public static TradeRate getInstance() {
		return instance;
	}

	public void createJournal(String name, String owner) {
		journal = new Journal(name, owner);
	}

	public void openJournal() {

	}

	public void saveJournal() {

	}

	public void closeJournal() {
		saveJournal();
		journal = null;
	}

	public Journal getJournal() {
		return journal;
	}

	public void test() {
		createJournal("Dziennik testowy", "Jan Kowalski");
		
		journal.addAccount(new Account("Rachunek maklerski #1"));
		journal.addAccount(new Account("Rachunek instrumentów pochodnych"));
		journal.addAccount(new Account("Rachunek maklerski #2"));
		journal.addPortfolio(new Portfolio("Portfel testowy #1", journal.getPortfolios().get(0)));
//		journal.addEntry();
//		journal.addEntry();
	}
}
