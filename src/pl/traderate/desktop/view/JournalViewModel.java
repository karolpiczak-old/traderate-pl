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

import pl.traderate.core.JournalEntryDTO;
import pl.traderate.desktop.presenter.JournalPresenter;

import java.util.ArrayList;

public class JournalViewModel extends GenericViewModel {

	protected JournalView view;

	protected ArrayList<JournalEntryDTO> entries;
	
	protected JournalTable journalTable;

	public JournalViewModel(JournalPresenter presenter) {
		super(presenter);

		view = new JournalView(this, presenter);
		addObserver(view);

		// Make sure that both views reference the same object
		super.view = view;
	}

	public JournalView getView() {
		return view;
	}

	public void setEntries(ArrayList<JournalEntryDTO> entries) {
		this.entries = entries;
		journalTable = new JournalTable(this.entries);

		notifyChange();
	}

	public JournalTable getJournalTable() {
		return journalTable;
	}

	public void setActiveTab(int i) {
		view.setActiveTab(i);
	}
}
