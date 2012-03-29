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

import pl.traderate.core.exception.EntryInsertionException;
import pl.traderate.core.exception.PortfolioRecalcException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

/**
 *
 */
class Portfolio {

	private final int id;
	
	private static int numberOfPortfoliosCreated;

	private String name;

	private final ArrayList<PortfolioEntry> entries;

	/**
	 * Reference to the parent portfolio.
	 *
	 * <tt>null</tt> if portfolio has no ancestors. Should be the case only for the
	 * single unique global portfolio.
	 */
	private Portfolio parent;

	private final ArrayList<Portfolio> children;

	private final ArrayList<Holding> holdings;

	private Date latestEntryDate;

	Portfolio(String name) {
		id = numberOfPortfoliosCreated++;
		setName(name);
		entries = new ArrayList<PortfolioEntry>();
		children = new ArrayList<Portfolio>();
		holdings = new ArrayList<Holding>();

		latestEntryDate = new Date(0L);
	}

	Portfolio(String name, Portfolio parent) {
		this(name);

		this.parent = parent;
		parent.children.add(this);
	}

	public void applyEntry(PortfolioEntry entry) throws EntryInsertionException {
//		// TODO: Implement
//		throw new EntryInsertionException();

	}

	public int getID() {
		return id;
	}

	String getName() {
		return name;
	}

	void setName(String name) {
		this.name = name;
	}

	public void addEntry(PortfolioEntry entry) throws EntryInsertionException {

		// Checks if entry date is newer or equal to latestEntryDate
		if (entry.getDate().compareTo(latestEntryDate) >= 0) {
			entry.apply(this);
			entries.add(entry);
			latestEntryDate = entry.getDate();
		} else {
			try {
				entries.add(entry);
				recalc();
			} catch (PortfolioRecalcException e) {
				entries.remove(entry);
				try {
					recalc();
				} catch (PortfolioRecalcException e2) {
					throw new InternalError();
				}
				throw new EntryInsertionException();
			}
		}
	}

	private void wipeCalculations() {
		holdings.clear();
	}

	private void recalc() throws PortfolioRecalcException {
		wipeCalculations();

		ArrayList<PortfolioEntry> sortedEntries = new ArrayList<PortfolioEntry>(entries);
		Collections.sort(sortedEntries, new JournalEntry.DateComparator());

		for (PortfolioEntry entry : sortedEntries) {
			try {
				entry.apply(this);
			} catch (EntryInsertionException e) {
				throw new PortfolioRecalcException();
			}
		}
	}
}
