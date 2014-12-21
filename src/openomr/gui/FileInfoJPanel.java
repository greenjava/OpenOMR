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
import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

public class FileInfoJPanel extends JPanel
{
	private JLabel fileLabel;
	private JLabel widthLabel;
	private JLabel heightLabel;
	private JLabel n1Label;
	private JLabel n2Label;
	private JLabel d1Label;
	private JLabel d2Label;

	public FileInfoJPanel(GUI gui)
	{
		super();
		setLayout(new BorderLayout());

		Border etched = BorderFactory.createEtchedBorder();
		Border titled = BorderFactory.createTitledBorder(etched, " Stave Boundaries ");
		setBorder(titled);

		JLabel fName = new JLabel("File Name: ");
		fileLabel = new JLabel("");
		fileLabel.setForeground(Color.BLUE);

		JLabel width = new JLabel("Image Width: ");
		widthLabel = new JLabel("");
		widthLabel.setForeground(Color.BLUE);

		JLabel height = new JLabel("Image Height: ");
		heightLabel = new JLabel("");
		heightLabel.setForeground(Color.BLUE);

		JLabel n1 = new JLabel("n1: ");
		n1Label = new JLabel("");
		n1Label.setForeground(Color.BLUE);

		JLabel n2 = new JLabel("n2: ");
		n2Label = new JLabel("");
		n2Label.setForeground(Color.BLUE);

		JLabel d1 = new JLabel("d1: ");
		d1Label = new JLabel("");
		d1Label.setForeground(Color.BLUE);

		JLabel d2 = new JLabel("d2: ");
		d2Label = new JLabel("");
		d2Label.setForeground(Color.BLUE);

		Box vbox = Box.createVerticalBox();

		Box hbox1 = Box.createHorizontalBox();
		hbox1.add(fName);
		hbox1.add(fileLabel);
		hbox1.add(Box.createHorizontalGlue());

		Box hbox2 = Box.createHorizontalBox();
		hbox2.add(width);
		hbox2.add(widthLabel);
		hbox2.add(Box.createHorizontalGlue());
		hbox2.add(height);
		hbox2.add(heightLabel);
		hbox2.add(Box.createHorizontalStrut(250));

		Box hbox3 = Box.createHorizontalBox();
		hbox3.add(n1);
		hbox3.add(n1Label);
		hbox3.add(Box.createHorizontalGlue());
		hbox3.add(n2);
		hbox3.add(n2Label);
		hbox3.add(Box.createHorizontalStrut(250));

		Box hbox4 = Box.createHorizontalBox();
		hbox4.add(d1);
		hbox4.add(d1Label);
		hbox4.add(Box.createHorizontalGlue());
		hbox4.add(d2);
		hbox4.add(d2Label);
		hbox4.add(Box.createHorizontalStrut(250));

		vbox.add(hbox1);
		vbox.add(Box.createVerticalStrut(7));
		vbox.add(hbox2);
		vbox.add(Box.createVerticalStrut(7));
		vbox.add(hbox3);
		vbox.add(Box.createVerticalStrut(7));
		vbox.add(hbox4);

		add(vbox, BorderLayout.NORTH);

	}

	public void setValues(String fName, int width, int height, int n1, int n2, int d1, int d2)
	{
		fileLabel.setText(fName);
		widthLabel.setText(String.valueOf(width));
		heightLabel.setText(String.valueOf(height));
		n1Label.setText(String.valueOf(n1));
		n2Label.setText(String.valueOf(n2));
		d1Label.setText(String.valueOf(d1));
		d2Label.setText(String.valueOf(d2));
	}
}