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

package pl.traderate.desktop.presenter;

import pl.traderate.core.AccountDTO;
import pl.traderate.core.PortfolioDetailsDTO;
import pl.traderate.core.PortfolioNodeDTO;
import pl.traderate.core.TradeRate;
import pl.traderate.core.event.*;
import pl.traderate.core.exception.JournalLoadException;
import pl.traderate.core.exception.JournalNotLoadedException;
import pl.traderate.core.exception.JournalSaveException;
import pl.traderate.core.exception.ObjectNotFoundException;
import pl.traderate.desktop.event.GenericViewEvent;
import pl.traderate.desktop.view.GenericView;
import pl.traderate.desktop.view.MainViewModel;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.io.File;
import java.util.concurrent.ExecutionException;

public class MainPresenter extends GenericPresenter {

	protected MainViewModel viewModel;

	protected HomeModelEventHandler modelEventHandler;

	// Subpresenters
	protected SummaryPresenter summaryPresenter;

	protected JournalPresenter journalPresenter;

	protected JFrame parentFrame;

	public MainPresenter(TradeRate model) {
		super(model);

		summaryPresenter = new SummaryPresenter(model);
		journalPresenter = new JournalPresenter(model);

		viewModel = new MainViewModel(this, summaryPresenter.getView(), journalPresenter.getView());
		initializeViewModel();

		modelEventHandler = new HomeModelEventHandler();
		model.removeEventListener(super.modelEventHandler);
		model.addEventListener(modelEventHandler);

		// Make sure that both viewModels reference the same object
		super.viewModel = viewModel;
		super.modelEventHandler = modelEventHandler;

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				journalPresenter.setParentFrame(viewModel.getView().getForm().getFrame());
				setParentFrame(viewModel.getView().getForm().getFrame());
			}
		});
	}

		@Override
	public void show() {
		super.show();
		summaryPresenter.show();
		journalPresenter.show();
	}

	@Override
	protected void initializeViewModel() {
		try {
			viewModel.setRootPortfolioNode(model.getPortfolioNodes());
			viewModel.setAccountNodes(model.getAccounts());
			viewModel.setJournalName(model.getJournalName());
			viewModel.setJournalOwner(model.getJournalOwner());
		} catch (JournalNotLoadedException ignored) {

		}
	}
	
	@Override
	protected void purgeViewModel() {
		viewModel.purgeRootPortfolioNode();
		viewModel.purgeAccountNodes();
		viewModel.purgeJournalName();
		viewModel.purgeJournalOwner();
	}

	@Override
	public void handleViewEvent(GenericViewEvent e) {
		e.handle(this);
	}

	public GenericView getView() {
		return viewModel.getView();
	}

	public void setParentFrame(JFrame parentFrame) {
		this.parentFrame = parentFrame;
	}

//:-- Model events -------------------------------------------------------------

	protected class HomeModelEventHandler extends GenericPresenter.GenericModelEventHandler {

		@Override
		public void visitModelEvent(GenericModelEvent e) {
			e.accept(this);
		}

		@Override
		public void handleModelEvent(GenericModelEvent e) {
			// TODO: Implement
		}

		@Override
		public void handleModelEvent(JournalUpdatedModelEvent e) {
			viewModel.setJournalUnsaved(true);
		}

		@Override
		public void handleModelEvent(NodesUpdatedModelEvent e) {
			viewModel.setJournalUnsaved(true);
			try {
				viewModel.setRootPortfolioNode(model.getPortfolioNodes());
				viewModel.setAccountNodes(model.getAccounts());
			} catch (JournalNotLoadedException e1) {
				e1.printStackTrace();
			}
		}

		@Override
		public void handleModelEvent(QuoteUpdatedModelEvent e) {

		}

		@Override
		public void handleModelEvent(JournalClosedModelEvent e) {
			viewModel.setJournalUnsaved(false);
			purgeViewModel();
			viewModel.setInterfaceLock(true);
		}

		@Override
		public void handleModelEvent(JournalCreatedModelEvent e) {
			viewModel.setJournalUnsaved(true);
			initializeViewModel();
			viewModel.setInterfaceLock(false);
		}

		@Override
		public void handleModelEvent(JournalOpenedModelEvent e) {
			initializeViewModel();
			viewModel.setInterfaceLock(false);
		}

		@Override
		public void handleModelEvent(JournalSavedModelEvent e) {
			viewModel.setJournalUnsaved(false);
		}
	}

//:-- Presenter events ---------------------------------------------------------

	public static class Events {

		public static class NodeManagementRequested extends GenericViewEvent {

			public NodeManagementRequested(Object source) {
				super(source);
			}

			@Override
			public void handle(MainPresenter presenter) {
				presenter.journalPresenter.handleViewEvent(new JournalPresenter.Events.NodeTabRequested(this));
				presenter.viewModel.setActiveTab(1);
			}
		}

		public static class NewJournal extends GenericViewEvent {

			String name;
			
			String owner;
			
			public NewJournal(Object source, String name, String owner) {
				super(source);
				this.name = name;
				this.owner = owner;
			}

			@Override
			public void handle(final MainPresenter presenter) {
				new SwingWorker<String, Object>() {

					@Override
					public String doInBackground() {
						presenter.model.createJournal(name, owner);

						return null;
					}

					@Override
					protected void done() {
						try {
							get();
							presenter.viewModel.setJournalFile(null);
						} catch (InterruptedException | ExecutionException ignored) {

						}
					}
				}.execute();
			}
		}

		public static class OpenJournal extends GenericViewEvent {

			File file;

			public OpenJournal(Object source, File file) {
				super(source);
				this.file = file;
			}

			@Override
			public void handle(final MainPresenter presenter) {
				new SwingWorker<String, Object>() {

					@Override
					public String doInBackground() throws JournalLoadException {
						presenter.model.openJournal(file);
						return null;
					}

					@Override
					protected void done() {
						try {
							try {
								get();
								presenter.viewModel.setJournalFile(file);
							} catch (InterruptedException ignored) {

							} catch (ExecutionException e) {
								throw e.getCause();
							}
						} catch (JournalLoadException e) {
							JOptionPane.showMessageDialog(presenter.parentFrame, "Odczyt pliku dziennika nie powiódł się.", "Błąd odczytu", JOptionPane.ERROR_MESSAGE);
						} catch (Throwable ignored) {

						}
					}
				}.execute();
			}
		}

		public static class SaveJournal extends GenericViewEvent {

			File file;

			public SaveJournal(Object source, File file) {
				super(source);
				this.file = file;
			}

			@Override
			public void handle(final MainPresenter presenter) {
				new SwingWorker<String, Object>() {

					@Override
					public String doInBackground() throws JournalSaveException {
						try {
							presenter.model.saveJournal(file);
						} catch (JournalNotLoadedException e) {
							e.printStackTrace();
						}
						return null;
					}

					@Override
					protected void done() {
						try {
							try {
								get();
								presenter.viewModel.setJournalFile(file);
							} catch (InterruptedException ignored) {

							} catch (ExecutionException e) {
								throw e.getCause();
							}
						} catch (JournalSaveException e) {
							JOptionPane.showMessageDialog(presenter.parentFrame, "Zapis dziennika nie powiódł się.", "Błąd zapisu", JOptionPane.ERROR_MESSAGE);
						} catch (Throwable ignored) {

						}
					}
				}.execute();
			}
		}

		public static class CloseJournal extends GenericViewEvent {

			public CloseJournal(Object source) {
				super(source);
			}

			@Override
			public void handle(final MainPresenter presenter) {
				new SwingWorker<String, Object>() {

					@Override
					public String doInBackground() {
						try {
							presenter.model.closeJournal();
						} catch (JournalNotLoadedException e) {
							e.printStackTrace();
						}
						return null;
					}

					@Override
					protected void done() {
						try {
							get();
							presenter.viewModel.setJournalFile(null);
						} catch (InterruptedException | ExecutionException ignored) {

						}
					}
				}.execute();
			}
		}

		public static class QuoteUpdateRequested extends GenericViewEvent {

			public QuoteUpdateRequested(Object source) {
				super(source);
			}

			@Override
			public void handle(final MainPresenter presenter) {
				new SwingWorker<String, Object>() {

					@Override
					public String doInBackground() {
						try {
							presenter.model.updateQuotes();
						} catch (JournalNotLoadedException e) {
							return null;
						}
						return null;
					}
				}.execute();
			}
		}

		public static class NodeChanged extends GenericViewEvent {

			DefaultMutableTreeNode node;

			public NodeChanged(Object source, DefaultMutableTreeNode node) {
				super(source);
				this.node = node;
			}

			@Override
			public void handle(MainPresenter presenter) {
				if (node.getUserObject() instanceof PortfolioNodeDTO) {
					PortfolioDetailsDTO portfolio = null;
					try {
						portfolio = presenter.model.getPortfolio(((PortfolioNodeDTO) node.getUserObject()).ID);
					} catch (ObjectNotFoundException | JournalNotLoadedException e) {
						e.printStackTrace();
					}
					presenter.summaryPresenter.handleViewEvent(new SummaryPresenter.Events.PortfolioSelected(this, portfolio));
				}

				if (node.getUserObject() instanceof AccountDTO) {
					AccountDTO account = null;
					try {
						account = presenter.model.getAccount(((AccountDTO) node.getUserObject()).ID);
					} catch (ObjectNotFoundException | JournalNotLoadedException e) {
						e.printStackTrace();
					}
					presenter.summaryPresenter.handleViewEvent(new SummaryPresenter.Events.AccountSelected(this, account));
				}
			}
		}
	}
}
