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

import pl.traderate.core.JournalEntryDTO;

import javax.swing.*;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class JournalTable implements TableModel {

	private ArrayList<JournalEntryDTO> entries;
	
	public JournalTable(ArrayList<JournalEntryDTO> entries) {
		this.entries = entries;
	}

	@Override
	public int getRowCount() {
		return entries.size();
	}

	@Override
	public int getColumnCount() {
		return 12;
	}

	@Override
	public Class<?> getColumnClass(int i) {
		switch (i) {
			case 11:
				return Boolean.class;
			default:
				return String.class;
		}
	}

	@Override
	public String getColumnName(int i) {
		switch (i) {
			case 0:
				return "ID";
			case 1:
				return "Data";
			case 2:
				return "Konto";
			case 3:
				return "Portfel";
			case 4:
				return "Kwota";
			case 5:
				return "Typ operacji";
			case 6:
				return "Ticker";
			case 7:
				return "Ilość";
			case 8:
				return "Cena";
			case 9:
				return "Prowizja";
			case 10:
				return "Komentarz";
			case 11:
				return "Usuń";
			default: return "";
		}
	}
	
	public void install(JTable table) {
		table.getColumnModel().getColumn(0).setMinWidth(70);
		table.getColumnModel().getColumn(0).setPreferredWidth(70);
		table.getColumnModel().getColumn(0).setMaxWidth(70);

		TypeCellRenderer typeCellRenderer = new TypeCellRenderer();
		typeCellRenderer.setHorizontalAlignment(JLabel.LEFT);
		table.getColumnModel().getColumn(5).setCellRenderer(typeCellRenderer);
		table.getColumnModel().getColumn(5).setMinWidth(50);
		table.getColumnModel().getColumn(5).setPreferredWidth(50);
		table.getColumnModel().getColumn(5).setMaxWidth(50);

		DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
		rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
		table.getColumnModel().getColumn(0).setCellRenderer(rightRenderer);
		table.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);
		table.getColumnModel().getColumn(7).setCellRenderer(rightRenderer);
		table.getColumnModel().getColumn(8).setCellRenderer(rightRenderer);
		table.getColumnModel().getColumn(9).setCellRenderer(rightRenderer);

		table.setIntercellSpacing(new Dimension(20, 3));

		table.getColumnModel().getColumn(11).setMinWidth(40);
		table.getColumnModel().getColumn(11).setPreferredWidth(40);
		table.getColumnModel().getColumn(11).setMaxWidth(40);
	}

	@Override
	public Object getValueAt(int row, int column) {
		JournalEntryDTO entry = entries.get(row);
		switch (column) {
			case 0:
				return entry.ID;
			case 1:
				return new SimpleDateFormat("dd.MM.yyyy").format(entry.date);
			case 2:
				return entry.account.toString();
			case 3:
				return entry.portfolio == null ? "---" : entry.portfolio.toString();
			case 4:
				return entry.amount == null ? "---" : entry.amount;
			case 5:
				return entry.type;
			case 6:
				return entry.ticker == null ? "---" : entry.ticker;
			case 7:
				return entry.quantity == null ? "---" : entry.quantity;
			case 8:
				return entry.price == null ? "---" : entry.price;
			case 9:
				return entry.commission == null ? "---" : entry.commission;
			case 10:
				return entry.comment;
			case 11:
				return entry.deleteFlag;
			default: return "";
		}
	}

	@Override
	public void setValueAt(Object value, int row, int column) {
		if (column == 11) {
			JournalEntryDTO entry = entries.get(row);
			entry.deleteFlag = (Boolean) value;
		}
	}

	@Override
	public boolean isCellEditable(int row, int column) {
		if (column == 11) return true;
		return false;
	}

	@Override
	public void addTableModelListener(TableModelListener l) {

	}

	@Override
	public void removeTableModelListener(TableModelListener l) {

	}

	public ArrayList<JournalEntryDTO> getEntriesToDelete() {
		ArrayList<JournalEntryDTO> entriesToDelete = new ArrayList<>();
		for (JournalEntryDTO entry : entries) {
			if (entry.deleteFlag) {
				entriesToDelete.add(entry);
			}
		}

		return entriesToDelete;
	}
	
	public class TypeCellRenderer extends DefaultTableCellRenderer {

		public TypeCellRenderer() {
			super();
		}

		@Override
		protected void setValue(Object value) {
			String type = (String) value;
			
			setText("<html><b>" + type + "</b></html>");

			if (type.equals("K")) {
				setToolTipText("Zakup akcji");
			}

			if (type.equals("S")) {
				setToolTipText("Sprzedaż akcji");
			}

			if (type.equals("W+")) {
				setToolTipText("Wpłata (depozyt)");
			}

			if (type.equals("W-")) {
				setToolTipText("Wypłata");
			}

			if (type.equals("A+")) {
				setToolTipText("Alokacja środków pieniężnych (przeniesienie z konta do portfela)");
			}

			if (type.equals("A-")) {
				setToolTipText("Dealokacja środków pieniężnych (przeniesienie z portfela na konto)");
			}
		}
	}
}
