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

import pl.traderate.desktop.presenter.JournalPresenter;

import javax.swing.*;

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
	protected void syncViewModel() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {

			}
		});
	}

	@Override
	public JournalForm getForm() {
		return form;
	}

	//:-- Listeners for GUI events -------------------------------------------------


}
