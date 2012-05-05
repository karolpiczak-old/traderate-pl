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

import pl.traderate.core.AccountDTO;
import pl.traderate.core.CashAllocationsDTO;
import pl.traderate.core.JournalEntryDTO;
import pl.traderate.core.PortfolioDetailsDTO;

import javax.swing.*;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class AllocationTable implements TableModel {

	private Type type;
	
	private CashAllocationsDTO cashAllocations;
	
	public AllocationTable(Object node) {
		type = null;
		cashAllocations = null;
		if (node instanceof AccountDTO) {
			type = Type.ACCOUNT;
			cashAllocations = ((AccountDTO) node).cashAllocationsDTO;
		}
		if (node instanceof PortfolioDetailsDTO) {
			type = Type.PORTFOLIO;
			cashAllocations = ((PortfolioDetailsDTO) node).cashAllocationsDTO;
		}
	}

	@Override
	public int getRowCount() {
		if (cashAllocations == null) return 0;
		return cashAllocations.allocations.size();
	}

	@Override
	public int getColumnCount() {
		return 2;
	}

	@Override
	public Class<?> getColumnClass(int column) {
		switch (column) {
			default:
				return String.class;
		}
	}

	@Override
	public String getColumnName(int column) {
		switch (column) {
			case 0:
				return type == Type.ACCOUNT ? "Portfel docelowy" : "Konto źródłowe";
			case 1:
				return "Kwota";
			default: return "";
		}
	}
	
	public static void install(JTable table) {
		DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
		rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
		table.getColumnModel().getColumn(1).setCellRenderer(rightRenderer);

		table.setRowHeight(20);
		table.setIntercellSpacing(new Dimension(20, 3));
	}

	@Override
	public Object getValueAt(int row, int column) {
		CashAllocationsDTO.Allocation allocation = cashAllocations.allocations.get(row);
		switch (column) {
			case 0:
				return allocation.name;
			case 1:
				return allocation.amount.toPlainString();
			default: return "";
		}
	}

	@Override
	public void setValueAt(Object value, int row, int column) {

	}

	@Override
	public boolean isCellEditable(int row, int column) {
		return false;
	}

	@Override
	public void addTableModelListener(TableModelListener l) {

	}

	@Override
	public void removeTableModelListener(TableModelListener l) {

	}
	
	public enum Type {
		ACCOUNT,
		PORTFOLIO
	}
}
