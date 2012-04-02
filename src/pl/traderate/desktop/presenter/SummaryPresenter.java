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
import pl.traderate.core.event.DataUpdateModelEvent;
import pl.traderate.core.event.GenericModelEvent;
import pl.traderate.desktop.event.GenericViewEvent;
import pl.traderate.desktop.view.GenericView;
import pl.traderate.desktop.view.SummaryViewModel;

import javax.swing.*;

public class SummaryPresenter extends GenericPresenter {

	protected SummaryViewModel viewModel;

	protected SummaryModelEventHandler modelEventHandler;

	public SummaryPresenter(TradeRate model) {
		super(model);

		viewModel = new SummaryViewModel(this);
		initializeViewModel();

		modelEventHandler = new SummaryModelEventHandler();
		model.removeEventListener(super.modelEventHandler);
		model.addEventListener(modelEventHandler);

		// Make sure that both viewModels reference the same object
		super.viewModel = viewModel;
		super.modelEventHandler = modelEventHandler;
	}

	@Override
	protected void initializeViewModel() {
		viewModel.setPortfolio(model.getPortfolio(0));
	}

	@Override
	public void handleViewEvent(GenericViewEvent e) {
		e.handle(this);
	}

	public GenericView getView() {
		return viewModel.getView();
	}

//:-- Model events -------------------------------------------------------------

	protected class SummaryModelEventHandler extends GenericModelEventHandler {

		@Override
		public void visitModelEvent(GenericModelEvent e) {
			e.accept(this);
		}

		@Override
		public void handleModelEvent(GenericModelEvent e) {

		}

		@Override
		public void handleModelEvent(DataUpdateModelEvent e) {

		}

	}

//:-- Presenter events ---------------------------------------------------------

	public static class Events {

	}
}
