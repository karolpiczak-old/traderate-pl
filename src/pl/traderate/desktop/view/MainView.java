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
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
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

	MainView(MainViewModel viewModel, MainPresenter presenter, final GenericView summaryView, final GenericView journalView) {
		super(viewModel, presenter);
		this.viewModel = (MainViewModel) super.viewModel;

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				form = new MainForm(MainView.this, summaryView, journalView);

				// Make sure that both forms reference the same object
				MainView.super.form = form;

				form.navigationTree.addTreeSelectionListener(new OnNodeSelected());
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

				form.versionText.setText(viewModel.getVersion());
			}
		});
	}

	public void setActiveTab(int i) {
		form.tabs.setSelectedIndex(i);
	}

//:-- Listeners for GUI events -------------------------------------------------

	public class OnManageButtonClicked implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			fireEvent(new Events.NodeManagementRequested(this));
		}
	}

	public class OnNodeSelected implements TreeSelectionListener {

		@Override
		public void valueChanged(TreeSelectionEvent e) {
			TreePath path = e.getNewLeadSelectionPath();

			if (path != null) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();

				// Disable selection on title nodes
				if (node.getUserObject() instanceof String) {
					((JTree) e.getSource()).setSelectionPath(e.getOldLeadSelectionPath());
				} else {
					fireEvent(new Events.NodeChanged(this, node));
				}
			}
		}
	}
}
