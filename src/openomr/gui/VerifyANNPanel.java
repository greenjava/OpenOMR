/***************************************************************************
 *   Copyright (C) 2006 by Arnaud Desaedeleer                              *
 *   arnaud@desaedeleer.com                                                *
 *                                                                         *
 *   This file is part of OpenOMR                                          *                                                      
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 *   This program is distributed in the hope that it will be useful,       *
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 *   GNU General Public License for more details.                          *
 *                                                                         *
 *   You should have received a copy of the GNU General Public License     *
 *   along with this program; if not, write to the                         *
 *   Free Software Foundation, Inc.,                                       *
 *   59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.             *
 ***************************************************************************/

package openomr.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

import openomr.ann.ANNInterrogator;
import openomr.ann.ANNPrepare;
import openomr.ann.SymbolConfidence;


public class VerifyANNPanel extends JPanel
{
	GUI gui;
	private JLabel imageSelected;
	public File file;
	private JLabel whichSym;
	private JLabel numCert;
	private JLabel imageWindow;
	private Box vbox2;
	private Box hbox4;
	private String PATH_SEPERATOR = System.getProperty("file.separator");
	
	
	public VerifyANNPanel(GUI gui)
	{
		super();
		setLayout(new BorderLayout());
		this.gui = gui;
		
		Border etched = BorderFactory.createEtchedBorder();
		Border titled = BorderFactory.createTitledBorder(etched, " ANN Tester ");
		setBorder(titled);

		JButton browseButton = new JButton("Browse File");
		browseButton.addActionListener(new BrowserActionListener());
		JLabel imageLabel = new JLabel("Image: ");
		imageSelected = new JLabel("");
		
		Box hbox1 = Box.createHorizontalBox();
		hbox1.add(browseButton);
		hbox1.add(Box.createHorizontalStrut(10));
		hbox1.add(imageLabel);
		hbox1.add(Box.createHorizontalStrut(10));
		hbox1.add(imageSelected);
		hbox1.add(Box.createHorizontalStrut(10));
		hbox1.add(Box.createHorizontalGlue());

		JButton testButton = new JButton("Test");
		testButton.addActionListener(new TestAction());
		Box hbox5 = Box.createHorizontalBox();
		hbox5.add(testButton);
		hbox5.add(Box.createHorizontalGlue());
		
		JLabel sym = new JLabel("Symbol: ");
		whichSym = new JLabel("");
		Box hbox2 = Box.createHorizontalBox();
		hbox2.add(sym);
		hbox2.add(whichSym);
		hbox2.add(Box.createHorizontalGlue());
		
		JLabel cert = new JLabel("% Certainty: ");
		numCert = new JLabel("");
		Box hbox3 = Box.createHorizontalBox();
		hbox3.add(cert);
		hbox3.add(numCert);
		hbox3.add(Box.createHorizontalGlue());

		
		Box vbox1 = Box.createVerticalBox();
		vbox1.add(hbox5);
		vbox1.add(Box.createVerticalStrut(10));
		vbox1.add(hbox2);
		vbox1.add(Box.createVerticalStrut(10));
		vbox1.add(hbox3);
		
		imageWindow = new JLabel("");
		
		hbox4 = Box.createHorizontalBox();
		hbox4.add(vbox1);
		hbox4.add(imageWindow);

		vbox2 = Box.createVerticalBox();
		vbox2.add(hbox1);
		vbox2.add(Box.createVerticalStrut(15));
		vbox2.add(vbox1);
		vbox2.add(Box.createVerticalStrut(15));
		vbox2.add(hbox4);
		
		
		
		add(vbox2, BorderLayout.CENTER);

		
	}
	
	private class BrowserActionListener implements ActionListener
	{
		public void actionPerformed(ActionEvent arg0)
		{
			JFileChooser chooser = new JFileChooser(System.getProperty("user.dir") + PATH_SEPERATOR + "neuralnetwork");
			int option = chooser.showOpenDialog(VerifyANNPanel.this);
			if (option == JFileChooser.APPROVE_OPTION)
			{
				file = chooser.getSelectedFile();
				imageSelected.setText(chooser.getSelectedFile().getName());
			}

			hbox4.remove(imageWindow);
			ImageIcon icon = new ImageIcon(file.getAbsolutePath());
			imageWindow = new JLabel(icon);
			hbox4.add(imageWindow);
		}
	}
	
	private class TestAction implements ActionListener
	{
		public void actionPerformed(ActionEvent arg0)
		{
			ANNInterrogator annIntero = GUI.getANNInterrogator();
			if (annIntero == null)
			{
				//the object does not exist, create one, save it
				annIntero = new ANNInterrogator();
				gui.setANNInterrogator(annIntero);
			}
			
			double data[] = ANNPrepare.getImageData(file.getAbsoluteFile().toString());
			double netData[][] = new double[1][128];
			netData[0] = data;
			SymbolConfidence result = annIntero.interogateNN(netData);
			whichSym.setText(result.getName());
			numCert.setText(Double.toString(result.getRMSE()).substring(0, 6));
		}
	}
}
