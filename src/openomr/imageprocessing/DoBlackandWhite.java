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


package openomr.imageprocessing;

import java.awt.Color;
import java.awt.image.BufferedImage;

public class DoBlackandWhite
{
	private BufferedImage buffImage;
	
	public DoBlackandWhite(BufferedImage buffImage)
	{
		this.buffImage = buffImage;
	}
	
	public void doBW()
	{
		for (int i=0; i<buffImage.getHeight(); i+=1)
			for (int j=0; j<buffImage.getWidth(); j+=1)
			{
				int pix = buffImage.getRGB(j, i);
				//if it's not a black or white pixel, set it to white
				if (pix != Color.WHITE.getRGB() && pix != Color.BLACK.getRGB())
					buffImage.setRGB(j, i, Color.BLACK.getRGB());
			}
		
		for (int i=1; i<buffImage.getWidth()-1; i+=1)
			for (int j=1; j<buffImage.getHeight()-1; j+=1)
			{
				if (buffImage.getRGB(i, j) == 0)
				{
					int p1 = buffImage.getRGB(i-1, j-1);
					int p2 = buffImage.getRGB(i-1, j);
					int p3 = buffImage.getRGB(i-1, j+1);
					int p4 = buffImage.getRGB(i, j-1);
					int p5 = buffImage.getRGB(i, j+1);
					int p6 = buffImage.getRGB(i-1, j-1);
					int p7 = buffImage.getRGB(i-1, j);
					int p8 = buffImage.getRGB(i-1, j+1);
				
					if (p1==-1 && p2==-1 && p3==-1 && p4==-1 && p5==-1 && p6==-1 && p7==-1 && p8==-1)
					{
						buffImage.setRGB(i, j, -1);
					}
				}
			}
	}
}
