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
import pl.traderate.desktop.event.GenericViewEvent;
import pl.traderate.desktop.view.MainViewModel;

import javax.swing.tree.DefaultMutableTreeNode;

public class MainPresenter extends GenericPresenter {

	protected MainViewModel viewModel;

	protected HomeModelEventHandler modelEventHandler;

	// Subpresenters
	protected SummaryPresenter summaryPresenter;

	protected JournalPresenter journalPresenter;

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
	}
	
	@Override
	public void show() {
		super.show();
		summaryPresenter.show();
		journalPresenter.show();
	}

	@Override
	protected void initializeViewModel() {
		viewModel.setRootPortfolioNode(model.getPortfolioNodes());
		viewModel.setAccountNodes(model.getAccounts());
	}
	
	@Override
	public void handleViewEvent(GenericViewEvent e) {
		e.handle(this);
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
		public void handleModelEvent(DataUpdateModelEvent e) {
			// TODO: Implement
		}

	}

//:-- Presenter events ---------------------------------------------------------

	public static class Events {

//		public static class DummyEvent extends GenericViewEvent {
//
//			public DummyEvent(Object source) {
//				super(source);
//			}
//
//			public void handle(MainPresenter presenter) {
//
//				new SwingWorker<String, Object>() {
//
//					@Override
//					public String doInBackground()  {
//
//						return null;
//					}
//
//					@Override
//					protected void done() {
//						try {
//
//						} catch (Exception ignore) {
//
//						}
//					}
//				}.execute();
//			}
//
//		}

		public static class NodeManagementRequested extends GenericViewEvent {

			public NodeManagementRequested(Object source) {
				super(source);
			}

			public void handle(MainPresenter presenter) {
				presenter.journalPresenter.handleViewEvent(new JournalPresenter.Events.NodeTabRequested(this));
				presenter.viewModel.setActiveTab(1);
			}
		}

		public static class NodeChanged extends GenericViewEvent {

			DefaultMutableTreeNode node;
			
			public NodeChanged(Object source, DefaultMutableTreeNode node) {
				super(source);
				this.node = node;
			}

			public void handle(MainPresenter presenter) {
				if (node.getUserObject() instanceof PortfolioNodeDTO) {
					PortfolioDetailsDTO portfolio = presenter.model.getPortfolio(((PortfolioNodeDTO) node.getUserObject()).ID);
					presenter.summaryPresenter.handleViewEvent(new SummaryPresenter.Events.PortfolioSelected(this, portfolio));
				}

				if (node.getUserObject() instanceof AccountDTO) {
					AccountDTO account = presenter.model.getAccount(((AccountDTO) node.getUserObject()).ID);
					presenter.summaryPresenter.handleViewEvent(new SummaryPresenter.Events.AccountSelected(this, account));
				}
			}
		}
	}
}
