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

import org.netbeans.swing.outline.DefaultOutlineModel;
import org.netbeans.swing.outline.RowModel;
import pl.traderate.core.HoldingsDTO;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.util.ArrayList;

public class HoldingTable {

	boolean closedMode;

	DefaultOutlineModel outlineModel;

	public HoldingTable(String name, ArrayList<HoldingsDTO.EquityHoldingDTO> holdings, boolean closedMode) {
		this.closedMode = closedMode;

		DefaultMutableTreeNode root = new DefaultMutableTreeNode(name);

		for (HoldingsDTO.EquityHoldingDTO holding : holdings) {
			DefaultMutableTreeNode holdingNode = new DefaultMutableTreeNode(holding);

			for (HoldingsDTO.EquityPositionDTO position: holding.positions) {
				DefaultMutableTreeNode positionNode = new DefaultMutableTreeNode(position);

				for (HoldingsDTO.EquityTradeDTO trade: position.trades) {
					positionNode.add(new DefaultMutableTreeNode(trade));
				}

				holdingNode.add(positionNode);
			}

			root.add(holdingNode);
		}

		outlineModel = (DefaultOutlineModel) DefaultOutlineModel.createOutlineModel(new DefaultTreeModel(root), new HoldingRowModel(), true, "Ticker");
	}

	public DefaultOutlineModel getOutlineModel() {
		return outlineModel;
	}

	public class HoldingRowModel implements RowModel {

		@Override
		public int getColumnCount() {
			return 9;
		}

		@Override
		public Object getValueFor(Object o, int i) {
			Object userObject = ((DefaultMutableTreeNode) o).getUserObject();

			if (userObject instanceof HoldingsDTO.PerformanceDataDTO) {
				HoldingsDTO.PerformanceDataDTO objectWithPerformance = (HoldingsDTO.PerformanceDataDTO) userObject;
				switch (i) {
					case 1:
						return closedMode ? objectWithPerformance.closePrice : objectWithPerformance.lastMarketPrice;
					case 2:
						return closedMode ? objectWithPerformance.closeValue : objectWithPerformance.marketValue;
					case 3:
						return objectWithPerformance.quantity;
					case 4:
						return objectWithPerformance.openPrice;
					case 5:
						return objectWithPerformance.openValue;
					case 6:
						return objectWithPerformance.commission;
					case 7:
						return closedMode ? objectWithPerformance.realizedGain : objectWithPerformance.paperGain;
					case 8:
						return closedMode ? objectWithPerformance.realizedGainPercentage : objectWithPerformance.paperGainPercentage;
					default:
						return null;
				}
			}

			return null;
		}

		@Override
		public Class getColumnClass(int i) {
			switch (i) {
				default: return String.class;
			}
		}

		@Override
		public boolean isCellEditable(Object o, int i) {
			return false;
		}

		@Override
		public void setValueFor(Object o, int i, Object o1) {

		}

		@Override
		public String getColumnName(int i) {
			switch (i) {
				case 0:
					return closedMode ? "Bieżąca cena rynkowa" : "%";
				case 1:
					return closedMode ? "Cena zamknięcia" : "Cena bieżąca";
				case 2:
					return closedMode ? "Wartość zamknięcia" : "Wartość bieżąca";
				case 3:
					return "Ilość";
				case 4:
					return "Cena otwarcia";
				case 5:
					return "Wartość otwarcia";
				case 6:
					return "Prowizja";
				case 7:
					return "Zysk";
				case 8:
					return "Zysk %";
				default: return "";
			}
		}
	}
}