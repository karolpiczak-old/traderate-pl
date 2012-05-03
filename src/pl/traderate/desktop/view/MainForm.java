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

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.File;

public class MainForm extends GenericForm {

	MainView view;
	
	GenericView summaryView;

	GenericView journalView;
	
	JFrame frame;

	JPanel root;

	JPanel statusbar;

	JPanel toolbar;

	JPanel mainPanel;

	JPanel navigationPanel;

	JProgressBar progressBar;

	JTree navigationTree;

	JTabbedPane tabs;

	JLabel topNavigationLabel;

	JButton manageTreeButton;

	JLabel versionText;

	JButton updateButton;

	JButton newJournalButton;

	JButton openJournalButton;

	JButton saveJournalButton;

	JButton closeJournalButton;

	JButton saveAsJournalButton;

	JLabel journalName;

	JFileChooser fileChooser;

	MainForm(GenericView view, GenericView summaryView, GenericView journalView) {
		super(view);
		this.view = (MainView) super.view;

		this.summaryView = summaryView;
		this.journalView = journalView;

		frame = new JFrame("Main");
		frame.setMinimumSize(new Dimension(640, 550));
		frame.setPreferredSize(new Dimension(1200, 800));
		frame.setContentPane(root);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		
		fileChooser = new JFileChooser();
		FileFilter xmlFilter = new XMLFileFilter();
		fileChooser.addChoosableFileFilter(xmlFilter);
		fileChooser.setFileFilter(xmlFilter);

		manageTreeButton.addActionListener(this.view.new OnManageButtonClicked());
		newJournalButton.addActionListener(this.view.new OnNewButtonClicked());
		openJournalButton.addActionListener(this.view.new OnOpenButtonClicked());
		saveJournalButton.addActionListener(this.view.new OnSaveButtonClicked());
		saveAsJournalButton.addActionListener(this.view.new OnSaveAsButtonClicked());
		closeJournalButton.addActionListener(this.view.new OnCloseButtonClicked());
		updateButton.addActionListener(this.view.new OnUpdateButtonClicked());
	}

	void show() {
		frame.setVisible(true);

	}

	private void createUIComponents() {
		statusbar = new JPanel();
		statusbar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIManager.getDefaults().getColor("mid")));
		
		tabs = new JTabbedPane();
		tabs.addTab("Podsumowanie", ((SummaryForm) summaryView.getForm()).root);
		tabs.addTab("Dziennik", ((JournalForm) journalView.getForm()).root);

		navigationTree = new JTree();
		navigationTree.setModel(new DefaultTreeModel(new DefaultMutableTreeNode("---")));
	}

	public JFrame getFrame() {
		return frame;
	}

	public static class XMLFileFilter extends FileFilter {

		@Override
		public boolean accept(File file) {
			if (file.isDirectory()) {
				return true;
			}

			String extension = getExtension(file);

			if (extension != null) {
				if (extension.equals("xml")) return true;
			}

			return false;
		}

		@Override
		public String getDescription() {
			return "Pliki dziennika (.xml)";
		}

		public String getExtension(File file) {
			String extension = null;
			String name = file.getName();
			int pos = name.lastIndexOf('.');

			if (pos > 0 &&  pos < name.length() - 1) {
				extension = name.substring(pos + 1).toLowerCase();
			}

			return extension;
		}
	}
}
