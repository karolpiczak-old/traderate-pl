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
import java.io.File;
import java.math.BigDecimal;

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
	protected void syncViewModel(final Object arg) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if (arg instanceof MainViewModel.SyncType) {
					MainViewModel.SyncType syncType = (MainViewModel.SyncType) arg;
					switch (syncType) {
						case NODES:
							form.navigationTree.setModel(viewModel.getNavigationTree());
							for (int i = 0; i < form.navigationTree.getRowCount(); ++i) {
								form.navigationTree.expandRow(i);
							}
						case META:
							form.versionText.setText(viewModel.getVersion());
							if (viewModel.getJournalName() == null || viewModel.getJournalOwner() == null) {
								form.journalName.setText("[Brak otwartego dziennika]");
								form.frame.setTitle("TradeRate.pl");
							} else {
								if (viewModel.isJournalUnsaved()) {
									form.journalName.setText(viewModel.getJournalName() + " (" + viewModel.getJournalOwner() + ") *");
									form.frame.setTitle("TradeRate.pl » " + viewModel.getJournalName() + "*");
								} else {
									form.journalName.setText(viewModel.getJournalName() + " (" + viewModel.getJournalOwner() + ")");
									form.frame.setTitle("TradeRate.pl » " + viewModel.getJournalName());
								}
							}
							break;
						case LOCK:
							if (viewModel.isInterfaceLocked()) {
								form.saveJournalButton.setEnabled(false);
								form.saveAsJournalButton.setEnabled(false);
								form.closeJournalButton.setEnabled(false);
								form.updateButton.setEnabled(false);
							} else {
								if (viewModel.isJournalUnsaved()) {
									form.saveJournalButton.setEnabled(true);
									form.saveAsJournalButton.setEnabled(true);
									form.journalName.setText(viewModel.getJournalName() + " (" + viewModel.getJournalOwner() + ") *");
									form.frame.setTitle("TradeRate.pl » " + viewModel.getJournalName() + "*");
								} else {
									form.saveJournalButton.setEnabled(false);
									form.saveAsJournalButton.setEnabled(false);
									form.journalName.setText(viewModel.getJournalName() + " (" + viewModel.getJournalOwner() + ")");
									form.frame.setTitle("TradeRate.pl » " + viewModel.getJournalName());
								}
								form.closeJournalButton.setEnabled(true);
								form.updateButton.setEnabled(true);
							}
							break;
						case INFO:
							if (viewModel.getSelectedName() == null) {
								form.infoNodeName.setText("---");
								form.infoNodeName.setEnabled(false);
								form.infoNodeCash.setText("---");
								form.infoNodeCash.setEnabled(false);
								form.infoNodeHoldings.setText("---");
								form.infoNodeHoldings.setEnabled(false);
								form.infoNodeValue.setText("---");
								form.infoNodeValue.setEnabled(false);
							} else {
								form.infoNodeName.setText(viewModel.getSelectedName());
								form.infoNodeName.setEnabled(true);
								form.infoNodeCashLabel.setText(viewModel.getSelectedType() == MainViewModel.SelectedType.ACCOUNT ? "Niezaalokowane/całkowite środki pieniężne:" : "Dostępne/zagregowane środki pieniężne:");
								form.infoNodeCash.setText(viewModel.getSelectedCash() + " / " + viewModel.getSelectedAggregatedCash());
								form.infoNodeCash.setEnabled(true);
								form.infoNodeHoldings.setText(viewModel.getSelectedCurrentValue() == null ? "---" : viewModel.getSelectedCurrentValue() + " / " + viewModel.getSelectedPaperGain());
								form.infoNodeHoldings.setEnabled(true);
								
								String value;
								if (viewModel.getSelectedValue() == null || viewModel.getSelectedValueChange() == null) {
									value = "---";
								} else {
									value = viewModel.getSelectedValue().toPlainString();
									if (viewModel.getSelectedValueChange().compareTo(BigDecimal.ZERO) > 0) {
										value = value + " (+" + viewModel.getSelectedValueChange().toPlainString() + ")";
									} else {
										value = value + " (" + viewModel.getSelectedValueChange().toPlainString() + ")";
									}
								}
								form.infoNodeValue.setText(value);
								form.infoNodeValue.setEnabled(true);
							}
							break;
					}
				}
			}

		});
	}

	public void setActiveTab(int i) {
		form.tabs.setSelectedIndex(i);
	}

	@Override
	public MainForm getForm() {
		return form;
	}

	public boolean confirmUnsaved() {
		if (viewModel.isJournalUnsaved()) {
			if (JOptionPane.showConfirmDialog(form.frame, "Czy na pewno kontynuować bez zapisania pliku dziennika?", "Niezachowane zmiany", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE) == 0) {
				return true;
			}
			return false;
		} else {
			return true;
		}
	}

//:-- Listeners for GUI events -------------------------------------------------

	public class OnManageButtonClicked implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			fireEvent(new Events.NodeManagementRequested(this));
		}
	}

	public class OnUpdateButtonClicked implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			fireEvent(new Events.QuoteUpdateRequested(this));
		}
	}

	public class OnNewButtonClicked implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			if (confirmUnsaved()) {
				String name = (String) JOptionPane.showInputDialog(form.frame, "<html>Podaj <b>nazwę</b> tworzonego dziennika:</html>", "Nowy dziennik", JOptionPane.PLAIN_MESSAGE);
				String owner = (String) JOptionPane.showInputDialog(form.frame, "<html>Podaj <b>autora</b> tworzonego dziennika:</html>", "Nowy dziennik", JOptionPane.PLAIN_MESSAGE);

				if (name != null && owner != null) {
					fireEvent(new Events.NewJournal(this, name, owner));
				}
			}
		}
	}

	public class OnOpenButtonClicked implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			if (confirmUnsaved()) {
				int state = form.fileChooser.showOpenDialog(form.openJournalButton);

				if (state == JFileChooser.APPROVE_OPTION) {
					File file = form.fileChooser.getSelectedFile();
					fireEvent(new Events.OpenJournal(this, file));
				}
			}
		}
	}

	public class OnSaveButtonClicked implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			File file = viewModel.getJournalFile();

			if (file != null) {
				fireEvent(new Events.SaveJournal(this, file));
			} else {
				new OnSaveAsButtonClicked().actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, ""));
			}
		}
	}

	public class OnSaveAsButtonClicked implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			int state = form.fileChooser.showSaveDialog(form.saveAsJournalButton);

			if (state == JFileChooser.APPROVE_OPTION) {
				File file = form.fileChooser.getSelectedFile();

				if (file.exists()) {
					if (JOptionPane.showConfirmDialog(form.frame, "Czy na pewno nadpisać wybrany plik?\n(" + file + ")", "Zapisz dziennik", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE) != 0) {
						return;
					}
				} else {
					if (form.fileChooser.getFileFilter() instanceof MainForm.XMLFileFilter) {
						if (!file.getPath().toLowerCase().endsWith(".xml")) {
							file = new File(file.getPath() + ".xml");
						}
					}
				}

				fireEvent(new Events.SaveJournal(this, file));
			}
		}
	}

	public class OnCloseButtonClicked implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			if (confirmUnsaved()) {
				fireEvent(new Events.CloseJournal(this));
			}
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
