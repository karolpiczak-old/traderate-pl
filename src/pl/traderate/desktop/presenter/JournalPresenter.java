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

import pl.traderate.core.TradeRate;
import pl.traderate.core.event.JournalUpdatedModelEvent;
import pl.traderate.core.event.GenericModelEvent;
import pl.traderate.core.exception.*;
import pl.traderate.desktop.event.GenericViewEvent;
import pl.traderate.desktop.view.GenericView;
import pl.traderate.desktop.view.JournalViewModel;

import javax.swing.*;
import java.math.BigDecimal;
import java.util.Date;
import java.util.concurrent.ExecutionException;

public class JournalPresenter extends GenericPresenter {

	protected JFrame parentFrame;
	
	protected JournalViewModel viewModel;

	protected JournalModelEventHandler modelEventHandler;

	public JournalPresenter(TradeRate model) {
		super(model);

		viewModel = new JournalViewModel(this);
		initializeViewModel();

		modelEventHandler = new JournalModelEventHandler();
		model.removeEventListener(super.modelEventHandler);
		model.addEventListener(modelEventHandler);

		// Make sure that both viewModels reference the same object
		super.viewModel = viewModel;
		super.modelEventHandler = modelEventHandler;
	}

	@Override
	protected void initializeViewModel() {
		viewModel.setEntries(model.getEntries());
		viewModel.setAccounts(model.getAccounts());
		viewModel.setPortfolios(model.getAllPortfolioNodes());
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

	protected class JournalModelEventHandler extends GenericModelEventHandler {

		@Override
		public void visitModelEvent(GenericModelEvent e) {
			e.accept(this);
		}

		@Override
		public void handleModelEvent(GenericModelEvent e) {

		}

		@Override
		public void handleModelEvent(JournalUpdatedModelEvent e) {
			viewModel.setEntries(model.getEntries());
		}

	}

//:-- Presenter events ---------------------------------------------------------

	public static class Events {

		public static class NodeTabRequested extends GenericViewEvent {

			public NodeTabRequested(Object source) {
				super(source);
			}

			public void handle(JournalPresenter presenter) {
				presenter.viewModel.setActiveTab(3);
			}
		}

		public static class AllocationEntrySubmitted extends GenericViewEvent {

			public AllocationEntrySubmitted(Object source) {
				super(source);
			}

			public void handle(final JournalPresenter presenter) {
				new SwingWorker<String, Object>() {

					@Override
					public String doInBackground() throws EntryInsertionException, InvalidInputException, ObjectNotFoundException, JournalNotLoadedException {
						int accountID = presenter.viewModel.getAllocationEntryAccount().ID;
						int portfolioID = presenter.viewModel.getAllocationEntryPortfolio().ID;
						Date date = presenter.viewModel.getAllocationEntryDate();
						BigDecimal amount = presenter.viewModel.getAllocationEntryAmount();
						String comment = presenter.viewModel.getAllocationEntryComment();

						JournalViewModel.AllocationEntryType entryType = presenter.viewModel.getAllocationEntryType();
						
						switch (entryType) {
							case ALLOCATION:
								presenter.model.addCashAllocationEntry(accountID, portfolioID, "", date, comment, amount);
								break;
							case DEALLOCATION:
								presenter.model.addCashDeallocationEntry(accountID, portfolioID, "", date, comment, amount);
								break;
						}

						return null;
					}

					@Override
					protected void done() {
						try {
							try {
								get();
							} catch (InterruptedException exception) {
								exception.printStackTrace();
							} catch (ExecutionException exception) {
								throw exception.getCause();
							}
						} catch (EntryInsertionException e) {
							JOptionPane.showMessageDialog(presenter.parentFrame, "Błędna operacja (np. brak pokrycia).", "Błąd operacji", JOptionPane.ERROR_MESSAGE);
						} catch (InvalidInputException e) {
							JOptionPane.showMessageDialog(presenter.parentFrame, "Błędne dane operacji (niepoprawne wartości).", "Błąd operacji", JOptionPane.ERROR_MESSAGE);
						} catch (ObjectNotFoundException e) {
							JOptionPane.showMessageDialog(presenter.parentFrame, "Podany portfel/konto nie zostały odnalezione.", "Błąd operacji", JOptionPane.ERROR_MESSAGE);
						} catch (JournalNotLoadedException e) {
							JOptionPane.showMessageDialog(presenter.parentFrame, "Żaden dziennik nie został załadowany.", "Błąd operacji", JOptionPane.ERROR_MESSAGE);
						} catch (Throwable e) {
							JOptionPane.showMessageDialog(presenter.parentFrame, "Błąd wewnętrzny.", "Błąd operacji", JOptionPane.ERROR_MESSAGE);
						}
					}
				}.execute();
			}
		}

		public static class CashEntrySubmitted extends GenericViewEvent {

			public CashEntrySubmitted(Object source) {
				super(source);
			}

			public void handle(final JournalPresenter presenter) {
				new SwingWorker<String, Object>() {

					@Override
					public String doInBackground() throws EntryInsertionException, InvalidInputException, ObjectNotFoundException, JournalNotLoadedException {
						int accountID = presenter.viewModel.getCashEntryAccount().ID;
						Date date = presenter.viewModel.getCashEntryDate();
						BigDecimal amount = presenter.viewModel.getCashEntryAmount();
						String comment = presenter.viewModel.getCashEntryComment();

						JournalViewModel.CashEntryType entryType = presenter.viewModel.getCashEntryType();

						switch (entryType) {
							case DEPOSIT:
								presenter.model.addCashDepositEntry(accountID, "", date, comment, amount);
								break;
							case WITHDRAWAL:
								presenter.model.addCashWithdrawalEntry(accountID, "", date, comment, amount);
								break;
						}

						return null;
					}

					@Override
					protected void done() {
						try {
							try {
								get();
							} catch (InterruptedException exception) {
								exception.printStackTrace();
							} catch (ExecutionException exception) {
								throw exception.getCause();
							}
						} catch (EntryInsertionException e) {
							JOptionPane.showMessageDialog(presenter.parentFrame, "Błędna operacja (np. brak pokrycia).", "Błąd operacji", JOptionPane.ERROR_MESSAGE);
						} catch (InvalidInputException e) {
							JOptionPane.showMessageDialog(presenter.parentFrame, "Błędne dane operacji (niepoprawne wartości).", "Błąd operacji", JOptionPane.ERROR_MESSAGE);
						} catch (ObjectNotFoundException e) {
							JOptionPane.showMessageDialog(presenter.parentFrame, "Podany portfel/konto nie zostały odnalezione.", "Błąd operacji", JOptionPane.ERROR_MESSAGE);
						} catch (JournalNotLoadedException e) {
							JOptionPane.showMessageDialog(presenter.parentFrame, "Żaden dziennik nie został załadowany.", "Błąd operacji", JOptionPane.ERROR_MESSAGE);
						} catch (Throwable e) {
							JOptionPane.showMessageDialog(presenter.parentFrame, "Błąd wewnętrzny.", "Błąd operacji", JOptionPane.ERROR_MESSAGE);
						}
					}
				}.execute();
			}
		}

		public static class EquityEntrySubmitted extends GenericViewEvent {

			public EquityEntrySubmitted(Object source) {
				super(source);
			}

			public void handle(final JournalPresenter presenter) {
				new SwingWorker<String, Object>() {

					@Override
					public String doInBackground() throws EntryInsertionException, InvalidInputException, ObjectNotFoundException, JournalNotLoadedException, ObjectConstraintsException {
						int accountID = presenter.viewModel.getEquityEntryAccount().ID;
						int portfolioID = presenter.viewModel.getEquityEntryPortfolio().ID;
						Date date = presenter.viewModel.getEquityEntryDate();
						BigDecimal quantity = presenter.viewModel.getEquityEntryQuantity();
						BigDecimal price = presenter.viewModel.getEquityEntryPrice();
						BigDecimal commission = presenter.viewModel.getEquityEntryCommission();
						String comment = presenter.viewModel.getEquityEntryComment();
						String ticker = presenter.viewModel.getEquityEntryTicker();

						JournalViewModel.EquityEntryType entryType = presenter.viewModel.getEquityEntryType();

						switch (entryType) {
							case BUY:
								presenter.model.addBuyEquityTransactionEntry(accountID, portfolioID, "", date, comment, ticker, quantity, price, commission);
								break;
							case SELL:
								presenter.model.addSellEquityTransactionEntry(accountID, portfolioID, "", date, comment, ticker, quantity, price, commission);
								break;
						}

						return null;
					}

					@Override
					protected void done() {
						try {
							try {
								get();
							} catch (InterruptedException exception) {
								exception.printStackTrace();
							} catch (ExecutionException exception) {
								throw exception.getCause();
							}
						} catch (EntryInsertionException e) {
							JOptionPane.showMessageDialog(presenter.parentFrame, "Błędna operacja (np. brak pokrycia).", "Błąd operacji", JOptionPane.ERROR_MESSAGE);
						} catch (InvalidInputException e) {
							JOptionPane.showMessageDialog(presenter.parentFrame, "Błędne dane operacji (niepoprawne wartości).", "Błąd operacji", JOptionPane.ERROR_MESSAGE);
						} catch (ObjectNotFoundException e) {
							JOptionPane.showMessageDialog(presenter.parentFrame, "Podany portfel/konto nie zostały odnalezione.", "Błąd operacji", JOptionPane.ERROR_MESSAGE);
						} catch (JournalNotLoadedException e) {
							JOptionPane.showMessageDialog(presenter.parentFrame, "Żaden dziennik nie został załadowany.", "Błąd operacji", JOptionPane.ERROR_MESSAGE);
						} catch (Throwable e) {
							JOptionPane.showMessageDialog(presenter.parentFrame, "Błąd wewnętrzny.", "Błąd operacji", JOptionPane.ERROR_MESSAGE);
						}
					}
				}.execute();
			}
		}
	}
}
