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
import java.util.Iterator;

public class StaveBoundaries
{
	private double MEASURE_PIXELS = 0.9;
	private BufferedImage buffImage;
	private StaveDetection staveDetection;

	public StaveBoundaries(BufferedImage buffImage, StaveDetection staveDetection)
	{
		this.buffImage = buffImage;
		this.staveDetection = staveDetection;
	}

	public void findBoundaries()
	{
		Iterator it = staveDetection.getStaveInfo();
		while (true)
		{
			if (!it.hasNext())
				break;

			Staves stave = (Staves) it.next();

			int start = doLeftBoundaries(stave);
			int end = doRightBoundaries(stave);
			stave.setLR(start, end);
		}
	}

	private int doLeftBoundaries(Staves stave)
	{
		for (int i = 0; i < buffImage.getWidth(); i += 1)
		{
			boolean res = isStaveOnly(stave, i, 1);
			if (!res)
			{
				return i;
			}
		}
		return 0;
	}

	private int doRightBoundaries(Staves stave)
	{
		for (int i = buffImage.getWidth() - 1; i >= 0; i -= 1)
		{
			boolean res = isStaveOnly(stave, i, 1);
			if (!res)
			{
				for (int j = i - 1; j >= 0; j -= 4)
				{
					res = isStaveOnly(stave, j, 4);
					if (res)
						return j;
				}
			}
		}
		return buffImage.getWidth() - 1;
	}

	public void findMeasures()
	{
		Iterator it = staveDetection.getStaveInfo();

		while (true)
		{
			if (!it.hasNext())
				break;

			Staves stave = (Staves) it.next();
			int height = stave.getBottom() - stave.getTop();

			int start = stave.getTop();

			boolean found = false;
			int count = 0;
			int remember = 0;
			for (int i = 0; i < buffImage.getWidth(); i += 1)
			{
				int bCount = 0;
				for (int j = start; j < height + start; j += 1)
				{
					int bPix = buffImage.getRGB(i, j);
					if (bPix != -1)
						bCount += 1;
				}
				if (bCount > height * MEASURE_PIXELS)
				{
					if (!found)
						remember = i;
					found = true;
					count += 1;
				} else
				{
					if (count > 2)
						stave.addVBar(remember);
					found = false;
					count = 0;
				}
			}
		}
	}

	public void findGroupsOfNotes()
	{
		Iterator it = staveDetection.getStaveInfo();
		int count = 0;

		//traverse all staves
		while (true)
		{
			if (!it.hasNext())
				break;
			Staves stave = (Staves) it.next();

			count += 1;

			int SPACING_BEFORE = 2;
			int SPACING_AFTER = 6;
			
			for (int i = 0; i < buffImage.getWidth(); i += SPACING_BEFORE)
			{

				//Do we have a an empty stave spanning accross a distance of SPACING_BEFORE pixels?
				boolean res = isStaveOnly(stave, i, SPACING_BEFORE);

				int start = i;
				int end = i;
				if (!res)
				{
					for (int j = i + 1; j < buffImage.getWidth(); j += /*SPACING_AFTER*/+1)
					{
						//Do we have a an empty stave spanning accross a distance of SPACING_AFTER pixels?
						res = isStaveOnly(stave, j, SPACING_AFTER);
						if (res)
						{
							end = j;
							break;
						}
					}
					i = end;
					//int dAvg = (staveDetection.getStavelineParameters().getD1() + staveDetection.getStavelineParameters().getD2()) / 2;
					if (end-start > 3)//end - start > dAvg)
						stave.addSymbolPos(start - 4, end/* + 4*/);
				}
			}
		}
	}

	public boolean isStaveOnly(Staves stave, int xStart, int step)
	{
		boolean found = false;

		int bCount = 0;
		int start = stave.getTop();
		int stop = stave.getBottom();

		int diff = stop - start;
		start -= diff * 0.5;
		stop += diff * 0.5;

		if (start < 0)
			start = 0;
		if (stop >= buffImage.getHeight())
			stop = buffImage.getHeight() - 1;

		int j=0;
		for (j = 0; j < step; j += 1)
		{
			if (xStart+j >= buffImage.getWidth())
				break;
			for (int i = start; i < stop; i += 1)
			{
				int pix = buffImage.getRGB(xStart + j, i);
				if (pix != -1)
					bCount += 1;
			}
		}

		bCount /= j+1;
		int N1 = staveDetection.getStavelineParameters().getN1();
		int N2 = staveDetection.getStavelineParameters().getN2();
		double nAvg = (N1 + N2) / 2;
		nAvg *= 0.8;
		if (bCount <= nAvg * 5  /* # of stavelines */)
			found = true;
		return found;
	}
}
