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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;

/**
 *
 */
abstract class JournalEntry implements Serializable, Identifiable {

	protected int ID;

	protected Date date;

	protected String comment;

	protected static int numberOfJournalEntriesCreated;

	protected Account account;

	protected ArrayList<Tag> tags;

	// TODO: Check for tags defensive copying when implementing tags
	protected JournalEntry(Account account, ArrayList<Tag> tags, Date date, String comment) {
		ID = numberOfJournalEntriesCreated++;

		this.account = account;
		this.tags = tags;
		this.date = new Date(date.getTime());
		this.comment = comment;
	}

	public abstract void apply(Account account) throws EntryInsertionException;

	public abstract void apply(JournalEntryDTO journalEntryDTO);

	public Date getDate() {
		return new Date(date.getTime());
	}

	@Override
	public int getID() {
		return ID;
	}

	static void resetIDIncrement() {
		numberOfJournalEntriesCreated = 0;
	}

	public Account getAccount() {
		return account;
	}

	public void attach() throws EntryInsertionException {
		account.addEntry(this);
	}

	public void detach() throws EntryInsertionException {
		account.removeEntry(this);
	}

	public static class DateComparator implements Comparator<JournalEntry> {

		@Override
		public int compare(JournalEntry o1, JournalEntry o2) {
			return o1.getDate().compareTo(o2.getDate());
		}
	}
}
