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
import java.util.LinkedList;

import openomr.dataanalysis.GNUPlotGenerator;


public class NoteHeadDetection
{
	private StaveParameters staveParams;
	private int yPos[];
	private int yPosBeforeFilter[];
	private int xProjection[];
	private int xProjectionBeforeFilter[];
	private int yProjection[];
	private YProjection yProj;
	private int height;
	private int width;
	private BufferedImage buffImage;

	public NoteHeadDetection(BufferedImage buffImage, StaveParameters staveParams)
	{
		this.buffImage = buffImage;
		this.staveParams = staveParams;
		yProj = new YProjection(buffImage);

	}

	public LinkedList<NoteHead> findNotes(int startH, int endH, int startW, int endW)
	{
		height = endH - startH + 1;
		width = endW - startW + 1;

		xProjection = new int[width];
		xProjectionBeforeFilter = new int[width];
		yPos = new int[width];
		yPosBeforeFilter = new int[width];

		LinkedList<NoteHead> coordList = new LinkedList<NoteHead>();

		
		//BEGIN --> better note detection, but not yet in use
		for (int i = startW; i < endW; i += 1)
		{
			XProjection(startH, endH, i, startW);
		}
		for (int i=0; i<width; i+=1)
			xProjectionBeforeFilter[i] = xProjection[i];
		//filterXproj();
		filterMeanXProjection();

		//END
		
		for (int i = startW; i < endW; i += 1)
		{
			YProjRLE(startH, endH, i, startW);
		}

		//printYPos(width, 0);
		
		for (int i = 0; i < width; i += 1)
			yPosBeforeFilter[i] = yPos[i];

		//printYPos(width, 0);
		
		filterYpos(startW, startH);

		//printYPos(width, 0);
		
		
		for (int i = 0; i < width; i += 1)
		{
			boolean start = true;
			int xTempPos = 0;
			for (int j = i; j < width; j += 1)
			{
				if (yPos[j] != 0)
				{
					if (start)
						xTempPos = j;
					start = false;
				}
				if (yPos[j] == 0)
				{
					if (!start)
					{
						NoteHead temp = new NoteHead();
						temp.x = startW + xTempPos;
						temp.y = startH + findyPos(xTempPos);
						//System.out.println("Found note @ x: " + xTempPos + " y: " + temp.y + " ::: " + findyPos(xTempPos));
						
						//Locate position of stem (left or right)
						temp.stemInfo = locateStem(temp.x, startH, endH);
						
						//Add note to linked list

						coordList.add(temp);
						i = j;
						break;
					}
					start = true;
				}
			}
		}
		return coordList;
	}

	private int locateStartNote()
	{
		
		return 0;
	}
	
	private NoteStem locateStem(int x, int startH, int endH)
	{
		int xStart = x - 14;
		int xEnd = staveParams.getD1()*2 + xStart;
		
		XProjection xProj = new XProjection(buffImage);
		xProj.calcXProjection(startH, endH, xStart, xEnd);
		
		int xProjArray[] = xProj.getXProjection();
		int max = xProjArray[0];
		int pos = 0;
		for (int i=1; i<xEnd-xStart; i+=1)
		{
			if (xProjArray[i] > max)
			{
				max = xProjArray[i];
				pos = i;
			}
		}
		
		NoteStem stem = new NoteStem();
		stem.stemPosition = pos+xStart;
		
		//System.out.println("xStart: " + xStart + " xEnd: " + xEnd + " Max: " + max + " Pos: " + pos);
		
		//stem is to the right
		if (pos+xStart > x)
		{
			stem.stemDirection = 1;
			return stem;
		}
		//else stem is to the left
		else
		{
			stem.stemDirection = 0;
			return stem;
		}
	}
	
	public void XProjection(int startH, int endH, int i, int startW)
	{
		int N2 = staveParams.getN2();
		int D1 = staveParams.getD1();
		int D2 = staveParams.getD2();

		yProj.calcYProjection(startH, endH, i, i + 1);
		yProjection = yProj.getYProjection();

		boolean start = false;
		int counter = 0;
		int max = 0;
		int tempPos = 0;

		for (int j = 0; j < height; j += 1)
		{
			if (yProjection[j] == 1)
			{
				if (!start)
				{
					tempPos = j;
				}
				start = true;
				counter += 1;
			} else
			{
				if (start)
				{
					if (counter > max)
					{
						max = counter;
					}
				}
				counter = 0;
				start = false;
			}
		}

		if (max > 5) // && max < 4*D2 ) // + 2 * N2) // && max < 2 * N2 + D1)
		{
			xProjection[i - startW] = max;
			return;
		}
		xProjection[i - startW] = 0;
		return;
	}

	private void YProjRLE(int startH, int endH, int i, int startW)
	{
		int D1 = staveParams.getD1();
		int D2 = staveParams.getD2();
		int N2 = staveParams.getN2();
		yProj.calcYProjection(startH, endH, i, i + 1);
		yProjection = yProj.getYProjection();

		boolean start = false;
		int counter = 0;
		int max = 0;
		int tempPos = 0;
		int tempMaxPos = 0;

		// filter yProjection. Get rid of values greater than D2
		filteryProj();

		for (int j = 0; j < height; j += 1)
		{
			if (yProjection[j] == 1)
			{
				if (!start)
				{
					tempPos = j;
				}
				start = true;
				counter += 1;
			} else
			{
				if (start)
				{
					if (counter > max)
					{
						max = counter;
						tempMaxPos = tempPos;

					}
				}
				counter = 0;
				start = false;
			}
		}

		if (max > 2 * N2)// && max < D2 + 2 * N2) // && max < 2 * N2 + D1)
		{
			// xProjection[i - startW] = max;
			if (max > D1 && max < D2 + 2 * N2) // This will get rid of two
				// notes directly above one
				// another
				yPos[i - startW] = tempMaxPos;
			else
				yPos[i - startW] = 0;
			return;
		}
		// xProjection[i - startW] = 0;
		yPos[i - startW] = 0;
		return;
	}

	private void filteryProj()
	{
		boolean start = false;
		int N2 = staveParams.getN2();
		int D2 = staveParams.getD2();
		int counter = 0;
		int tempPos = 0;
		for (int j = 0; j < height; j += 1)
		{
			if (yProjection[j] == 1)
			{
				if (!start)
				{
					tempPos = j;
				}
				start = true;
				counter += 1;
			} else
			{
				if (start)
				{
					if (counter > D2 + 2* N2)
					{
						for (int i = tempPos; i < j; i += 1)
							yProjection[i] = 0;
					}
				}
				counter = 0;
				start = false;
			}
		}
	}

	private int findyPos(int tempPos)
	{
		int N2 = staveParams.getN2();
		if (tempPos + N2 >= width)
			return yPos[tempPos];
		if (yPos[tempPos + N2] != 0)
			return yPos[tempPos + N2];
		for (int i = tempPos; i >= tempPos - 2 * N2; i -= 1)
		{
			if (i < 0)
				break;
			if (yPos[i] != 0)
				if (yPos[i + N2] != 0)
					return yPos[i + N2];
		}
		for (int i = tempPos; i < tempPos + 2 * N2; i += 1)
		{
			if (i >= width || i + N2 >= width)
				break;
			if (yPos[i] != 0)
				if (yPos[i + N2] != 0)
					return yPos[i + N2]; // make sure we get a peak value
		}

		for (int i = tempPos; i >= tempPos - 2 * N2; i -= 1)
			if (yPos[i] != 0)
				return yPos[i];
		for (int i = tempPos; i < tempPos + 2 * N2; i += 1)
			if (yPos[i] != 0)
				return yPos[i];
		return 0;
	}

	private void filterXproj()
	{
		int N2 = staveParams.getN2();
		int D2 = staveParams.getD2();
		int D1 = staveParams.getD1();

		// first find all local maximas. Then find all peaks in array
		for (int i = 1; i < width - 1; i += 1)
		{
			int forward = 0;
			int backward = 0;
			if (xProjection[i - 1] < xProjection[i] && xProjection[i] >= xProjection[i + 1])
			{
				int val = (int) (xProjection[i] * 0.5);

				// search backwards
				for (int j = i - 1; j >= 0; j -= 1)
				{
					if (xProjection[j] < val)
					{
						backward = j;
						break;
					}
				}

				// search forwards
				for (int j = i + 1; j < width - 1; j += 1)
				{
					if (xProjection[j] < val)
					{
						forward = j - 1;
						break;
					}
				}

				int distance = forward - backward;
				if (distance < D1) // Flatten
																	// the graph
																	// if
																	// distance
				// < 3/2 N2
				{
					// System.out.println("Flatten...");
					for (int j = i; j >= backward; j -= 1)
						xProjection[j] = 0;
					for (int j = i + 1; j < forward; j += 1)
						xProjection[j] = 0;
				}
			}

		}
	}

	private void filterMeanXProjection()
	{
		final double UPPER_BOUND = 1.2;
		final double LOWER_BOUND = 0.8;
		int start = 0;
		int stop = 0;
		//System.out.println("Width: " + width);
		for (int i = 0; i < width; i += 1)
		{
			if (xProjection[i] != 0)
			{
				start = i;
				for (int j = i + 1; j < width; j += 1)
				{
					if (xProjection[j] == 0)
					{
						stop = j;
						break;
					}
				}

				// find average values
				//System.out.println("Start: " + start + "  stop: " + stop);
				float average = 0;
				for (int j = start; j < stop; j += 1)
					average += xProjection[j];
				average = average / (float) (stop - start);
				int upperB = (int) Math.round(average * UPPER_BOUND);
				int lowerB = (int) Math.round(average * LOWER_BOUND);
				//System.out.println("average: " + average);
				//System.out.println("Upper: " + upperB + "  lower: " + lowerB);
				for (int j = start; j < stop; j += 1)
					if (xProjection[j] < lowerB || xProjection[j] > upperB)
						xProjection[j] = 0;

				i = stop - 1;
			}
		}
		for (int i = 0; i < width; i += 1)
		{
			if (xProjection[i] != 0)
			{
				start = i;
				for (int j = i + 1; j < width; j += 1)
				{
					if (xProjection[j] == 0)
					{
						stop = j;
						break;
					}
				}

				if ((stop - start) < staveParams.getD1()*0.3)
				{
					for (int j = start; j < stop; j += 1)
						xProjection[j] = 0;
				}
				i = stop - 1;
			}
		}
	}

	private void filterYpos(int startW, int startH)
	{
		int N2 = staveParams.getN2();
		int D2 = staveParams.getD2();

		// first find all local maximas. Then find all peaks in array
		for (int i = 1; i < width - 1; i += 1)
		{
			int forward = 0;
			int backward = 0;
			if (yPos[i - 1] < yPos[i] && yPos[i] >= yPos[i + 1])
			{
				int val = (int) (yPos[i] * 0.5);

				// search backwards
				for (int j = i - 1; j >= 0; j -= 1)
				{
					if (yPos[j] < val)
					{
						backward = j;
						break;
					}
				}

				// search forwards
				for (int j = i + 1; j < width - 1; j += 1)
				{
					if (yPos[j] < val)
					{
						forward = j - 1;
						break;
					}
				}

				int distance = forward - backward;
				if (/*(3 * N2) / 2 > distance ||*/ distance < D2 / 2) // Flatten
																	// the graph
																	// if
																	// distance
				// < 3/2 N2
				{
					// System.out.println("Flatten...");
					for (int j = i; j >= backward; j -= 1)
						yPos[j] = 0;
					for (int j = i + 1; j < forward; j += 1)
						yPos[j] = 0;
				}
			}

		}
	}

	public void generateGNUPlotFile(String fname, int data[], int arrSize)
	{
		GNUPlotGenerator yPosdataPlot = new GNUPlotGenerator(fname, data, arrSize);
		yPosdataPlot.generateDataFile();
	}

	public int[] getYPos()
	{
		return yPos;
	}

	public int[] getYposBeforeFilter()
	{
		return yPosBeforeFilter;
	}

	public int[] getXProjection()
	{
		return xProjection;
	}

	public int[] getXProjectionBeforeFilter()
	{
		return xProjectionBeforeFilter;
	}
	
	public void printYPos(int width, int startH)
	{
		System.out.println("START YPOS -- Height = " + height);
		for (int i = 0; i < width; i += 1)
		{
			if (yPos[i] > 0)
				System.out.println(height - yPos[i]);
			else
				System.out.println(0);
		}
		System.out.println("END YPOS");

	}

	public void printXProj(int width)
	{
		System.out.println("START XProj");
		for (int i = 0; i < width; i += 1)
			System.out.println(xProjection[i]);
		System.out.println("END XProj");
	}
}
