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

import pl.traderate.core.exception.AccountRecalcException;
import pl.traderate.core.exception.EntryInsertionException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

/**
 *
 */
class Account {

	private final int id;

	private static int numberOfAccountsCreated;

	private String name;

	private final ArrayList<JournalEntry> entries;

	private final ArrayList<Holding> holdings;

	private Date latestEntryDate;

	private BigDecimal cashBalance;

	Account(String name) {
		id = numberOfAccountsCreated++;
		setName(name);
		entries = new ArrayList<JournalEntry>();
		holdings = new ArrayList<Holding>();

		cashBalance = new BigDecimal("0");
		latestEntryDate = new Date(0L);
	}

	public void applyEntry(JournalEntry entry) {

	}

	public void applyEntry(CashDepositEntry entry) {
		cashBalance = cashBalance.add(entry.getAmount());
	}

	public void applyEntry(CashWithdrawalEntry entry) throws EntryInsertionException {
		BigDecimal newBalance = cashBalance.subtract(entry.getAmount());

		if (newBalance.compareTo(new BigDecimal("0")) < 0) {
			throw new EntryInsertionException();
		}

		cashBalance = newBalance;
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

	public void addEntry(JournalEntry entry) throws EntryInsertionException {

		// Checks if entry date is newer or equal to latestEntryDate
		if (entry.getDate().compareTo(latestEntryDate) >= 0) {
			entry.apply(this);
			entries.add(entry);
			latestEntryDate = entry.getDate();
		} else {
			try {
				entries.add(entry);
				recalc();
			} catch (AccountRecalcException e) {
				entries.remove(entry);
				try {
					recalc();
				} catch (AccountRecalcException e2) {
					throw new InternalError();
				}
				throw new EntryInsertionException();
			}
		}
	}

	private void wipeCalculations() {
		holdings.clear();
		cashBalance = new BigDecimal("0");
	}

	private void recalc() throws AccountRecalcException {
		wipeCalculations();

		ArrayList<JournalEntry> sortedEntries = new ArrayList<JournalEntry>(entries);
		Collections.sort(sortedEntries, new JournalEntry.DateComparator());

		for (JournalEntry entry : sortedEntries) {
			try {
				entry.apply(this);
			} catch (EntryInsertionException e) {
				throw new AccountRecalcException();
			}
		}
	}
}
