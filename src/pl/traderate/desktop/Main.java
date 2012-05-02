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

package pl.traderate.desktop;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

public class Main {

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(new TradeRateLookAndFeel());
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}

		Font defaultFont = null;
		try {
			defaultFont = Font.createFont(Font.TRUETYPE_FONT, (Main.class.getResourceAsStream("/pl/traderate/desktop/Roboto-Medium.ttf")));
		} catch (IOException | FontFormatException e) {
			e.printStackTrace();
		}

		Font boldFont = null;
		try {
			boldFont = Font.createFont(Font.TRUETYPE_FONT, (Main.class.getResourceAsStream("/pl/traderate/desktop/Roboto-Bold.ttf")));
		} catch (IOException | FontFormatException e) {
			e.printStackTrace();
		}

		UIManager.put("Button.font", defaultFont.deriveFont(12.0F));
		UIManager.put("CheckBox.font", defaultFont.deriveFont(12.0F));
		UIManager.put("CheckBoxMenuItem.font", defaultFont.deriveFont(12.0F));
		UIManager.put("ColorChooser.font", defaultFont.deriveFont(12.0F));
		UIManager.put("ComboBox.font", defaultFont.deriveFont(12.0F));
		UIManager.put("EditorPane.font", defaultFont.deriveFont(12.0F));
		UIManager.put("Label.font", defaultFont.deriveFont(12.0F));
		UIManager.put("List.font", defaultFont.deriveFont(12.0F));
		UIManager.put("Menu.font", defaultFont.deriveFont(12.0F));
		UIManager.put("MenuBar.font", defaultFont.deriveFont(12.0F));
		UIManager.put("MenuItem.font", defaultFont.deriveFont(12.0F));
		UIManager.put("OptionPane.font", defaultFont.deriveFont(12.0F));
		UIManager.put("Panel.font", boldFont.deriveFont(12.0F));
		UIManager.put("PasswordField.font", defaultFont.deriveFont(12.0F));
		UIManager.put("PopupMenu.font", defaultFont.deriveFont(12.0F));
		UIManager.put("ProgressBar.font", defaultFont.deriveFont(12.0F));
		UIManager.put("RadioButton.font",defaultFont.deriveFont(12.0F));
		UIManager.put("RadioButtonMenuItem.font", defaultFont.deriveFont(12.0F));
		UIManager.put("ScrollPane.font", defaultFont.deriveFont(12.0F));
		UIManager.put("TabbedPane.font", defaultFont.deriveFont(12.0F));
		UIManager.put("Table.font", defaultFont.deriveFont(12.0F));
		UIManager.put("TableHeader.font", defaultFont.deriveFont(12.0F));
		UIManager.put("TextArea.font", defaultFont.deriveFont(12.0F));
		UIManager.put("TextField.font", defaultFont.deriveFont(12.0F));
		UIManager.put("TextPane.font", defaultFont.deriveFont(12.0F));
		UIManager.put("TitledBorder.font", defaultFont.deriveFont(12.0F));
		UIManager.put("ToggleButton.font", defaultFont.deriveFont(12.0F));
		UIManager.put("ToolBar.font", defaultFont.deriveFont(12.0F));
		UIManager.put("ToolTip.font", defaultFont.deriveFont(12.0F));
		UIManager.put("Tree.font", defaultFont.deriveFont(12.0F));
		UIManager.put("Viewport.font", defaultFont.deriveFont(12.0F));
		UIManager.put("Label.font", defaultFont.deriveFont(12.0F));

		Router.getInstance().goHome();
	}
}
