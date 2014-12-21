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

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

import javax.swing.*;

/* ScrollablePicture.java is used by ScrollDemo.java. */

public class ResizeableJLabel extends JLabel
{
	private BufferedImage buffImage;
	private int scale = 4;

	public ResizeableJLabel(BufferedImage buffImage, int m)
	{
		super();
		this.buffImage = buffImage;
		if (buffImage == null)
		{
			setText("No picture found.");
			setHorizontalAlignment(CENTER);
			setOpaque(true);
			setBackground(Color.white);
		}

		ImageIcon imageIcon = new ImageIcon(buffImage.getScaledInstance(buffImage.getWidth() / scale, buffImage.getHeight() / scale, Image.SCALE_FAST ));
		setIcon(imageIcon);
	}

	public void reSize(MouseEvent e)
	{
		ImageIcon imageIcon = new ImageIcon(buffImage.getScaledInstance(e.getX() - 15, e.getY() - 38, Image.SCALE_FAST));
		setIcon(imageIcon);
		repaint();

	}
}
