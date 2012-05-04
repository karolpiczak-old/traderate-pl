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

import org.pushingpixels.substance.api.renderers.SubstanceDefaultTreeCellRenderer;
import pl.traderate.core.PortfolioNodeDTO;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;

public class NodesTable {
	public static class NodeRenderer extends SubstanceDefaultTreeCellRenderer {

		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
			SubstanceDefaultTreeCellRenderer component = (SubstanceDefaultTreeCellRenderer) super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);

// TODO: Fancy cell renderer customization
//			if (value instanceof DefaultMutableTreeNode) {
//				if (((DefaultMutableTreeNode) value).getUserObject() instanceof PortfolioNodeDTO) {
//					PortfolioNodeDTO portfolio = (PortfolioNodeDTO) ((DefaultMutableTreeNode) value).getUserObject();
//				}
//			}

			return component;
		}

		@Override
		public Dimension getPreferredSize() {
			Dimension dimension = super.getPreferredSize();
			return new Dimension(dimension.width, dimension.height + 10);
		}
	}
}
