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

import java.util.Iterator;
import java.util.LinkedList;

/**
 * The <code> StaveDetection </code> class is responsible for detecting all the staves present in a BufferedImage. 
 * Since the stave detection algorithm needs relies on knowing the stave parameters (n1, n2, d1 and d1) along with
 * the Y-Projection of the image, they need to be passed in the constructor.
 * <p>
 * The <code> StaveDetection </code> class is used as follows:
 * <p> 
 * <code>
 * StaveDetection sDetection = new StaveDetection(yProj, sParams); <br>
 * sDetection.setParameters(0.80, 0.80); //This is optional as the StaveThreshold and PeakThreshold are both defaulted to 0.75 <br>
 * sDetection.locateStaves(); <br>
 * sDetection.calcNoteDistance(); <br>
 * </code>
 * 
 * 
 * @author Arnaud Desaedeleer
 * @version 1.0
 */

public class StaveDetection
{
	private double STAVE_THRESHOLD;
	private double PEAK_THRESHOLD;
	private int height;
	private int bPixels[];
	private LinkedList<StavePeaks> localMaxList;
	private StaveParameters staveParams;
	private LinkedList<Staves> staveList;
	private int stavesfound;

	public StaveDetection(YProjection yproj, StaveParameters staveParams)
	{
		bPixels = yproj.getYProjection();
		this.height = yproj.getHeight();
		this.staveParams = staveParams;
		localMaxList = new LinkedList<StavePeaks>();
		staveList = new LinkedList<Staves>();
		stavesfound = 0;
		
		STAVE_THRESHOLD = 0.75;
		PEAK_THRESHOLD = 0.75;
		
	}

	/**
	 * This method will locate all the staves present in an image. <p>
	 *
	 *It does so by looking for five equidistant peaks in the Y-Projection. In order for it to detect a stave, 
	 *the spacing between the peaks must be in the range d1 to d2 and the peaks must be in the range n1 to n2.
	 */
	
	public void locateStaves()
	{
		findLocalMaximums();
		stavesfound = 0;
		// process each local maxima one at a time
		Iterator iter = localMaxList.iterator();
		while (true)
		{
			if (!iter.hasNext())
			{
				break;
			}

			StavePeaks temp = (StavePeaks) iter.next();
			// Percentage of local maximum used as threshold to search for other
			// neighbouring peaks
			int val = (int) (temp.getValue() * STAVE_THRESHOLD);

			// search backwards bPixel array for values >= to threshold value
			int count = 1;
			for (int i = temp.getPos() - 1; i >= 0; i -= 1)
			{
				if (bPixels[i] < val || count >= staveParams.getN2() / 2)
				{
					temp.SetStart(temp.getPos() - count + 1);
					break;
				}
				count += 1;
			}

			// search forwards bPixel array for values >= to threshold value
			int count2 = 1;
			for (int i = temp.getPos() + 1; i < height; i += 1)
			{
				if (bPixels[i] < val || count2 >= staveParams.getN2() / 2)
				{
					temp.SetEnd(temp.getPos() + count2 - 1);
					break;
				}
				count2 += 1;
			}

			// calculate the width of potential staveline
			count += count2 - 1;
			if (count > staveParams.getN2() || count < staveParams.getN1())
			{
				// remove any items that are thicker or narrower than staveline
				// parameters N1 and N2
				iter.remove();
			}
		}

		// We now want to locate all the staves by processing each item in
		// linked list
		iter = localMaxList.iterator();
		boolean found = false;
		StavePeaks first = null;
		StavePeaks nextPeak = null;
		int min = 0;
		int max = 0;
		int linecount = 1;
		Staves temp;
		temp = new Staves(stavesfound);
		while (true)
		{
			if (!iter.hasNext())
				break;

			if (!found)
			{
				// We haven't found a staveline, get next item in list to
				// process
				first = (StavePeaks) iter.next();
				// min and max values to determine acceptable distance between
				// end of stave and beginning of next stave
				min = first.getEnd() + staveParams.getD1();
				max = first.getEnd() + staveParams.getD2();

				temp = new Staves(stavesfound);
				temp.addStaveline(0, first);
			} else
			{
				// We could have a stave, find the next staveline
				min = nextPeak.getEnd() + staveParams.getD1();
				max = nextPeak.getEnd() + staveParams.getD2();
				first = nextPeak;
			}

			// Use a second iterator and get it to same position as first
			Iterator iter2 = localMaxList.iterator();
			iter2 = getToSamePosition(iter2, first);

			nextPeak = findNextStave(min, max, first.getValue(), iter2);
			if (nextPeak == null)
			{
				// Current item in list has been determined not to be part of a
				// stave
				// Reset found to false and linecount to 1
				found = false;
				linecount = 1;
			} else
			{
				// Current item could be part of a stave
				linecount += 1;
				if (linecount < 6)
					temp.addStaveline(linecount - 1, nextPeak);

				// When linecount == 5, we have found a stave
				if (linecount == 5)
				{
					stavesfound += 1;

					// advance iter to nextPeak
					iter = getToSamePosition(iter, nextPeak);
					found = false;

					// add stave to linked list
					staveList.add(temp);
					
					//print location of bottom staveline
					//System.out.println("Bottom stave: " + temp.getStave(4).getEnd());
				}
				found = true;
			}
		}
	}

	
	/**
	 * This method calculates the distance between two notes for each stave found and places
	 * the result in <code> int noteDistance </code> field in the <code> Staves </code> class. The distance
	 * between two notes is an important parameter as it will be used by the Midi generator engine to determine
	 * the pitch of notes.
	 */
	
	public void calcNoteDistance()
	{
		for (int i=0; i<staveList.size(); i+=1)
		{
			int dist = 0;
			Staves temp = staveList.get(i);
			for (int j=0; j<4; j+=1)
			{
				int avg = (temp.getStave_line(j).getStart() + temp.getStave_line(j).getEnd())/2;
				int avg2 = (temp.getStave_line(j+1).getStart() + temp.getStave_line(j+1).getEnd())/2;
				dist += avg2 - avg;
			}
			dist /= 4;
			temp.setNoteDistance(dist);
		}
	}
	
	
	/**
	 * This method returns an Iterator to the Staves class containing all the information about the staves found
	 * by the locateStaves() method.
	 * @return staveList
	 */
	
	public Iterator getStaveInfo()
	{
		return staveList.iterator();
	}

	
	/**
	 * This method returns a pointer to a LinkedList (Staves) class which stores information about the staves
	 * found by the locateStaves() method.
	 * @return staveList
	 */
	public LinkedList<Staves> getStaveList()
	{
		return staveList;
	}
	
	
	/**
	 * Return the number of staves found in the image
	 * @return stavesFound
	 */
	
	public int getNumStavesFound()
	{
		return stavesfound;
	}

	
	/**
	 * This method enables to override the default STAVE_THRESHOLD and PEAK_THRESHOLD values. 
	 * @param STAVE_THRESHOLD
	 * @param PEAK_THRESHOLD
	 */
	
	public void setParameters(double STAVE_THRESHOLD, double PEAK_THRESHOLD)
	{
		this.STAVE_THRESHOLD = STAVE_THRESHOLD;
		this.PEAK_THRESHOLD = PEAK_THRESHOLD;
	}

	
	public double getStaveThreshold()
	{
		return STAVE_THRESHOLD;
	}

	public StaveParameters getStavelineParameters()
	{
		return staveParams;
	}
	
	public double getPeakThreshold()
	{
		return PEAK_THRESHOLD;
	}

	public void setStaveThreshold(double STAVE_THRESHOLD)
	{
		this.STAVE_THRESHOLD = STAVE_THRESHOLD;
	}

	public void setPeakThreshold(double PEAK_THRESHOLD)
	{
		this.PEAK_THRESHOLD = PEAK_THRESHOLD;
	}

	/*
	public void printList()
	{
		Iterator iter = localMaxList.iterator();
		while (true)
		{
			if (iter.hasNext())
			{
				StavePeaks temp = (StavePeaks) iter.next();
				System.out.println("Local Max: " + temp.getValue() + " Position: " + temp.getPos());
			} else
				break;
		}
	}
	*/
	
	
	
	// Find local maximums in bPixel array
	// place all local maximums in locaMaxList linked list
	// Eg: 1, 3, 5, 3, 2, 8, 3, 5, 9, 2 --> would return 5, 8, 9
	private void findLocalMaximums()
	{
		for (int i = 1; i < height - 1; i += 1)
		{
			int pixA = bPixels[i - 1];
			int pixB = bPixels[i + 1];
			int pix = bPixels[i];
			if (pix >= pixA && pix > pixB)
			{
				StavePeaks temp = new StavePeaks(i, pix);
				localMaxList.add(temp);
			}
		}
	}
	
	
	// Method to advance an iterator to same position as object StavePeaks in
	// linked list
	private Iterator getToSamePosition(Iterator it1, StavePeaks tester)
	{
		while (true)
		{
			if (!it1.hasNext())
				break;
			StavePeaks t = (StavePeaks) it1.next();
			if (t.equals(tester))
				break;
		}
		return it1;
	}

	// Method to find a potential staveline in linked list
	private StavePeaks findNextStave(int min, int max, int val, Iterator iter)
	{
		if (!iter.hasNext())
			return null;
		StavePeaks next = (StavePeaks) iter.next();

		while (true)
		{
			if (!iter.hasNext())
				break;

			// If starting position of next item in list > max, then we can't
			// consider that item to be part of stave
			if (next.getStart() > max)
				return null;

			// If start is in between min and max, we can consider that item
			else if (next.getStart() >= min && next.getStart() <= max)
			{
				// consider this stave only if previous staveline maximum is >=
				// than 2/3 of current maximum
				// 2/3 seems to be a value that works for all test cases
				int val1 = (int) (next.getValue() * PEAK_THRESHOLD);
				if (val >= val1)
					return next;
			}
			next = (StavePeaks) iter.next();
		}

		// Found nothing, return null
		return null;
	}
}
