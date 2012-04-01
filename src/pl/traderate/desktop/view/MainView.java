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

import pl.traderate.desktop.presenter.MainPresenter;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static pl.traderate.desktop.presenter.MainPresenter.Events;

public class MainView extends GenericView {

	private MainViewModel viewModel;

	/**
	 * Typecasted reference to the main form.
	 *
	 * Hides <tt>form</tt> from superclass for convenience only.
	 */
	private MainForm form;

	MainView(MainViewModel viewModel, MainPresenter presenter) {
		super(viewModel, presenter);
		this.viewModel = (MainViewModel) super.viewModel;

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				form = new MainForm(MainView.this);

				// Make sure that both forms reference the same object
				MainView.super.form = form;
			}
		});
	}

	/**
	 *
	 */
	protected void syncViewModel() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				form.navigationTree.setModel(viewModel.getNavigationTree());
				for (int i = 0; i < form.navigationTree.getRowCount(); ++i) {
					form.navigationTree.expandRow(i);
				}
			}
		});
	}

//:-- Listeners for GUI events -------------------------------------------------

	public class OnButtonOKClicked implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			fireEvent(new Events.FormSubmitted(this));
		}
	}
}
