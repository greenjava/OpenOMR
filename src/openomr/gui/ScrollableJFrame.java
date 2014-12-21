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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class ScrollableJFrame extends JInternalFrame
{
	private ScrollableJLabel picture;

	public ScrollableJFrame(BufferedImage buffImage, String Fname)
	{
		super(Fname, true, // resizable
				true, // closable
				true, // maximizable
				true);// iconifiable

		setLocation(30, 30);
		setSize(buffImage.getWidth() / 4, buffImage.getHeight() / 4);

		JPanel buttonCorner = new JPanel();

		ImageIcon tempImage = new ImageIcon(buffImage);
		picture = new ScrollableJLabel(tempImage, (int) (Toolkit.getDefaultToolkit().getScreenResolution() / 2.54));
		JScrollPane pictureScrollPane = new JScrollPane(picture);
		pictureScrollPane.setPreferredSize(new Dimension(buffImage.getWidth(), buffImage.getHeight()));
		pictureScrollPane.setViewportBorder(BorderFactory.createLineBorder(Color.black));

		pictureScrollPane.setCorner(JScrollPane.UPPER_LEFT_CORNER, buttonCorner);

		add(pictureScrollPane);
	}
}
