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

import java.awt.image.*;

/**
 * The <code> YProjection </code> class will calculate the Y-Projection of an image. The constructor
 * is given a <code> BufferedImage </code> and then the <code> calcYProjection </method> is invoked
 * to calculate the Y-Projection of the <code> BufferedImage </code>.
 * <p>
 * The <code> YProjection </code> class is used as follows:
 * <p> 
 * <code>
 * YProjection yProj = new YProjection(BufferedImage); <br>
 * yProj.calcYProjection(startH, endH, startW, endW); <br>
 * </code>
 * <p>
 * Calling the <code> calcYProjection </code> method will place the Y-Projection of the <code> BufferedImage </code>
 * in a int[] array which can be obtained by calling the <code> getYProjection </code> method.
 * <p>
 * 
 * @author Arnaud Desaedeleer
 * @version 1.0
 */

public class YProjection
{
	private BufferedImage buffImage;
	private int height;
	private int yProjection[];
	
	public YProjection(BufferedImage buffImage)
	{
		this.buffImage = buffImage;
		height = buffImage.getHeight();
	}
	
	
	/**
	 * Cacluate the Y-Projection of the BufferedImage
	 * @param startH Desired start Y-Coordinate of the BufferedImage
	 * @param endH Desired end Y-Coordinate of the BufferedImage
	 * @param startW Desired start X-Coordinate of the BufferedImage
	 * @param endW Desired end X-Coordinate of the BufferedImage
	 */

	public void calcYProjection(int startH, int endH, int startW, int endW)
	{
		int height = endH - startH + 1;
		this.height = height;
		yProjection = new int[height];

		for (int i = startH; i < endH; i += 1)
		{
			for (int j = startW; j < endW; j += 1)
			{
				int color;
				try
				{
					color = buffImage.getRGB(j, i);
					if (color == -1) // white pixel
					{
						// Do nothing
					} else
					{
						yProjection[i - startH] += 1;
					}	
				}
				catch (ArrayIndexOutOfBoundsException e)
				{
					//ABSOLUTELY NEED TO FIX THIS
					//System.out.println("Array out of bounds exception -- calcYProjection");
					//System.out.println("Values:  i=" + i + "  j=" + j);
				}
			}
		}
	}

	/**
	 * Returns the resulting Y-Projection of the BufferedImage
	 * @return yProjection
	 */
	
	public int[] getYProjection()
	{
		return yProjection;
	}
	
	
	/**
	 * Prints the Y-Projection of the BufferedImage
	 *
	 */
	
	public void printYProjection()
	{
		System.out.println("Y-Projection");
		for (int i = 0; i < height; i += 1)
		{
			System.out.println(yProjection[i]);
		}
		System.out.println("END Y-Projection");
	}
	
	
	/**
	 * Return the size of the yProjection array. In other words, <code> height = endH - startH </code>
	 * @return height
	 */
	
	public int getHeight()
	{
		return height;
	}
	
	
	
	//CONSIDER DELETING THESE METHODS
	
	/*public void writeYProjectionFile(DataOutputStream yProjOutput)
	{
		// findStaves();
		// System.out.println("********* Y-Projection **********");
		for (int i = 0; i < height; i += 1)
		{
			try
			{
				yProjOutput.writeBytes(String.valueOf(yProjection[i]));
				yProjOutput.writeBytes("\n");
			} catch (IOException e)
			{
				System.out.println("Could not write to outfile in method printY");
				e.printStackTrace();
			}

		}
		// System.out.println(bPixels[i]);
		// System.out.println("********* END Y-Projection **********");
	}
*/
	
	/*
	public int getStartW()
	{
		return startW;
	}

	public int getEndW()
	{
		return endW;
	}

	public int getStartH()
	{
		return startH;
	}

	public int getEndH()
	{
		return endH;
	}
	*/
}
