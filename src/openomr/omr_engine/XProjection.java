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


package openomr.omr_engine;

import java.awt.image.BufferedImage;

/**
 * The <code> XProjection </code> class will calculate the X-Projection of an image. The constructor
 * is given a <code> BufferedImage </code> and then the <code> calcXProjection </method> is invoked
 * to calculate the X-Projection of the <code> BufferedImage </code>.
 * <p>
 * The <code> XProjection </code> class is used as follows:
 * <p> 
 * <code>
 * XProjection xProj = new XProjection(BufferedImage); <br>
 * xProj.calcXProjection(startH, endH, startW, endW); <br>
 * </code>
 * <p>
 * Calling the <code> calcXProjection </code> method will place the X-Projection of the <code> BufferedImage </code>
 * in a int[] array which can be obtained by calling the <code> getYProjection </code> method.
 * <p>
 * 
 * @author Arnaud Desaedeleer
 * @version 1.0
 */

public class XProjection
{
	private int xProjection[];
	private int size;
	private BufferedImage buffImage;
	
	public XProjection(BufferedImage buffImage)
	{
		this.buffImage = buffImage;
	}
	
	/**
	 * Cacluate the X-Projection of the BufferedImage
	 * @param startH Desired start Y-Coordinate of the BufferedImage
	 * @param endH Desired end Y-Coordinate of the BufferedImage
	 * @param startW Desired start X-Coordinate of the BufferedImage
	 * @param endW Desired end X-Coordinate of the BufferedImage
	 */
	
	public void calcXProjection(int startH, int endH, int startW, int endW)
	{
		int size = Math.abs(endW - startW) + 1;
		//System.out.println("Size: " + size);
		this.size = size;
		xProjection = new int[size];
		
		for (int i = startW; i < endW; i += 1)
		{
			for (int j = startH; j < endH; j += 1)
			{
				int color = 0;
				try
				{
					color = buffImage.getRGB(i, j);
				}
				catch (ArrayIndexOutOfBoundsException e)
				{
					
				}
				if (color != -1) //if black pixel
				{
					xProjection[i-startW] += 1;
				}
			}
		}
	}
	
	/**
	 * Returns the resulting X-Projection of the BufferedImage
	 * @return xProjection
	 */
	
	public int[] getXProjection()
	{
		return xProjection;
	}
	
	
	/**
	 * Prints the X-Projection of the BufferedImage
	 *
	 */
	public void printXProjection()
	{
		System.out.println("X Projection");
		for (int i=0; i<size; i+=1)
		{
			System.out.println(xProjection[i]);
		}
		System.out.println("END X Projection");
	}
	
}
