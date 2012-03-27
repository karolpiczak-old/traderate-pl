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
import pl.traderate.core.event.*;
import pl.traderate.desktop.event.GenericViewEvent;
import pl.traderate.desktop.view.MainViewModel;

import javax.swing.*;
import java.util.Date;

public class MainPresenter extends GenericPresenter {

	protected MainViewModel viewModel;

	protected HomeModelEventHandler modelEventHandler;

	public MainPresenter(TradeRate model) {
		super(model);

		viewModel = new MainViewModel(this);
		updateViewModel();

		modelEventHandler = new HomeModelEventHandler();
		model.removeEventListener(super.modelEventHandler);
		model.addEventListener(modelEventHandler);

		// Make sure that both viewModels reference the same object
		super.viewModel = viewModel;
		super.modelEventHandler = modelEventHandler;
	}

	@Override
	protected void updateViewModel() {
		viewModel.setVersion("1.0.0");
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
			System.out.println("GenericModelEvent at HomePresenter");
		}

		@Override
		public void handleModelEvent(UpdateModelEvent e) {
			System.out.println("UpdateModelEvent at HomePresenter");
		}

	}

//:-- Presenter events ---------------------------------------------------------

	public static class Events {

		public static class FormSubmitted extends GenericViewEvent {

			public FormSubmitted(Object source) {
				super(source);
			}

			public void handle(MainPresenter presenter) {
				presenter.viewModel.setVersion(new Date().toString());

				new SwingWorker<String, Object>() {

					@Override
					public String doInBackground()  {

						return null;
					}

					@Override
					protected void done() {
						try {

						} catch (Exception ignore) {

						}
					}
				}.execute();
			}

		}

	}
}
