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

/** The <code> StaveParameters </code> is a class responsible for calculating two important paramters
 * in a BufferedImage. Those two parameters are the thickness of a stave line and the distance between
 * two stave lines. Given those two parameters, a lower and upper threshold value is determined.
 * <p>
 * The <code> StaveParameters </code> class is used as follows:
 * <p> 
 * <code>
 * StaveParameters sParams = new StaveParameters(buffImage); <br>
 * sParams.calcParameters();
 * </code>
 * <p>
 * Calling the calcParameters() method will calculate values for the following private fields:
 * <p>
 *<code>int n1</code> - The lower threshold value for the thickness of a stave line <br>
 *<code>int n2</code> - The upper threshold value for the thickness of a stave line <br>
 *<code>int d1</code> - The lower threshold value for the distance between two stave lines <br>
 *<code>int d2</code> - The upper threshold value for the distance between two stave lines <br>
 *
 * @author Arnaud
 * @version 1.0
 */

public class StaveParameters
{
	private BufferedImage buffImage;
	private int len = 100;
	private int wPixels[], bPixels[];
	private int height, width;
	final private int wThreshold = 2;
	final private int bThreshold = 0;

	private int n1 = 0;
	private int n2 = 0;
	private int d1 = 0;
	private int d2 = 0;

	/**
	 * 
	 * @param buffImage
	 * 
	 * Initialises the StaveParameters class with buffImage
	 */
	
	public StaveParameters(BufferedImage buffImage)
	{
		this.buffImage = buffImage;
		wPixels = new int[len];
		bPixels = new int[len];
		height = buffImage.getHeight();
		width = buffImage.getWidth();

		//calcRLE(0, width - 1);
		//calcParams();
	}

	
	/**
	 * This method will calculate the parameters N1, N2, D1 and D2
	 *
	 */
	
	public void calcParameters()
	{
		calcRLE(0, width - 1);
		calcParams();
	}
	
	
	/**
	 * Returns the value n1 which is the lower threshold value for the thickness of a stave line
	 * @return n1
	 */
	
	public int getN1()
	{
		return n1;
	}

	
	/**
	 * Returns the value n2 which is the upper threshold value for the thickness of a stave line
	 * @return n2
	 */
	
	public int getN2()
	{
		return n2;
	}

	
	/**
	 * Returns the value d1 which is the lower threshold value for the distance between two stave lines
	 * @return d1
	 */
	
	public int getD1()
	{
		return d1;
	}

	
	/**
	 * Returns the value d2 which is the lower threshold value for the distance between two stave lines
	 * @return d2
	 */
	
	public int getD2()
	{
		return d2;
	}
	
	
	/**
	 * Prints the two int[] arrays containing the RLE of the black pixels and white pixels respectively
	 *
	 */

	public void printHistogram()
	{
		for (int i = 0; i < len; i += 1)
			System.out.println(bPixels[i] + ", " + wPixels[i]);
	}
	
	
	/*	PRIVATE METHODS */
	
	private void calcRLE(int startW, int endW)
	{
		for (int i = startW; i < endW; i += 1)
		{
			int bcnt = 0, wcnt = 0;
			boolean stop = false;

			for (int j = 0; j < height - 1; j += 1)
			{
				int color = buffImage.getRGB(i, j);

				if (color != -1 && !stop) // -1 == white pixel
				{
					bcnt += 1;
					color = buffImage.getRGB(i, j + 1);
					if (color == -1)
					{
						stop = true;
						if (bcnt > bThreshold && bcnt < 100)
							bPixels[bcnt] += 1;
						bcnt = 0;
					}
				} else
				{
					wcnt += 1;
					color = buffImage.getRGB(i, j + 1);
					if (color != -1)
					{
						stop = false;
						if (wcnt > wThreshold && wcnt < 100)
							wPixels[wcnt] += 1;
						wcnt = 0;
					}
				}
			}
		}
	}
	
	private int calcParams()
	{
		int wMax = wPixels[0];
		int bMax = bPixels[0];
		int wPos = 0;
		int bPos = 0;
		int bThird;
		int wThird;

		// find max value in wPixels and bPixels
		for (int i = 0; i < len - 1; i += 1)
		{
			if (wPixels[i] > wMax)
			{
				wMax = wPixels[i];
				wPos = i;
			}
			if (bPixels[i] > bMax)
			{
				bMax = bPixels[i];
				bPos = i;
			}
		}

		// calculate 1/3 of black pixels and white pixels max height
		bThird = bMax / 3;
		wThird = wMax / 3;

		// interpolate to find xMin and xMax
		n1 = interpMin(bPos, bThird, bPixels) - 1;
		n2 = interpMax(bPos, bThird, bPixels) + 1;
		d1 = interpMin(wPos, wThird, wPixels);
		d2 = interpMax(wPos, wThird, wPixels);

		return 1;
	}

	private int interpMin(int pos, int third, int pixels[])
	{
		int temp = 0;
		for (int i = pos; i > 0; i--)
		{
			if (pixels[i] < third)
			{
				// need to interpolate
				temp = ((third - pixels[i + 1]) / (pixels[i] - pixels[i + 1])) + i + 1;
				break;
			}
		}
		return temp;
	}

	private int interpMax(int pos, int third, int pixels[])
	{
		int temp = 0;
		for (int i = pos; i < len - 2; i++)
		{
			if (pixels[i] < third)
			{
				// need to interpolate
				temp = ((third - pixels[i]) / (pixels[i - 1] - pixels[i])) + i + 1;
				break;
			}
		}
		return temp;
	}

	/*	END PRIVATE METHODS */
	
	
	/*
	public int[] getWPixles()
	{
		return wPixels;
	}

	public int[] getBPixles()
	{
		return bPixels;
	}
	*/
}
