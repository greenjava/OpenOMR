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
import java.awt.image.BufferedImage;
import java.util.LinkedList;

import openomr.omr_engine.StavePeaks;
import openomr.omr_engine.Staves;


public class DrawingTools
{
	public static void drawNote(BufferedImage dupImage, int x, int dx, int y, int D2)
	{
		for (int j = y; j < y + dx; j += 1)
			for (int k = 0; k < D2; k += 1)
			{
				try
				{

					dupImage.setRGB(x + k, j, 0xFF);
				} catch (ArrayIndexOutOfBoundsException e)
				{
				}
			}
	}
	
	public static void drawBox(BufferedImage dupImage, int top, int bottom, int start, int stop, Color color)
	{
		// Draw left vertical line
		for (int j = top; j <= bottom; j += 1)
		{
			for (int k = 0; k < 2; k += 1)
			{
				try
				{
					dupImage.setRGB(start + k, j, color.getRGB());
				} catch (ArrayIndexOutOfBoundsException e)
				{
				}
			}
		}
		// Draw right vertical line
		for (int j = top; j <= bottom; j += 1)
		{
			for (int k = 0; k < 2; k += 1)
			{
				try
				{
					dupImage.setRGB(stop + k, j, color.getRGB());
				} catch (ArrayIndexOutOfBoundsException e)
				{
				}
			}
		}
		// Draw top horizontal line
		for (int j = start; j < stop; j += 1)
		{
			for (int k = -1; k < 1; k += 1)
			{
				try
				{
					dupImage.setRGB(j, top + k, color.getRGB());
				} catch (ArrayIndexOutOfBoundsException e)
				{
				}
			}
		}
		// Draw bottom horizontal line
		for (int j = start; j < stop; j += 1)
		{
			for (int k = -1; k < 1; k += 1)
			{
				try
				{
					dupImage.setRGB(j, bottom + k, color.getRGB());
				} catch (ArrayIndexOutOfBoundsException e)
				{
				}
			}
		}
	}
	
	public static void drawMeasures(BufferedImage dupImage, Staves stave)
	{
		// displays measure bars on image
		LinkedList<Integer> temp = stave.getVBar();
		int capacity = temp.size();
		for (int i = 0; i < capacity; i += 1)
		{
			for (int j = stave.getStave_line(0).getStart(); j <= stave.getStave_line(4).getEnd(); j += 1)
			{
				for (int k = 0; k < 3; k += 1)
				{
					dupImage.setRGB(Integer.valueOf(temp.get(i)) + k, j, 0xFF0000);
				}
			}
		}
	}

	public static void drawStave(BufferedImage dupImage, int left, int right, Staves stave, Color color)
	{
		// draws staves in red
		for (int j = 0; j < 5; j += 1)
		{
			StavePeaks sPeak = stave.getStave_line(j);
			int start = sPeak.getStart() - 1;
			for (int i = left; i < right; i += 1)
			{
				for (int k = start; k < start + 4; k += 1)
					dupImage.setRGB(i, k, color.getRGB());
			}
		}
	}
}
