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

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.LinkedList;

import openomr.ann.ANNPrepare;
import openomr.ann.SymbolConfidence;
import openomr.gui.DrawingTools;
import openomr.gui.GUI;


public class L1_Segment
{

	private int xStart; // Start and stop coordinates of L1_Segment
	private int xStop;
	private NoteStem stemInfo;

	private BufferedImage buffImage;
	private BufferedImage dupImage;
	private StaveDetection staveDetection;
	private Staves stave;

	// Linked list of L2_Segments
	private LinkedList<L2_Segment> l2_Segments;

	public L1_Segment(int xStart, int xStop, NoteStem stemInfo)
	{
		this.xStart = xStart;
		this.xStop = xStop;
		l2_Segments = new LinkedList<L2_Segment>();
		this.stemInfo = stemInfo;
	}

	public void setParameters(BufferedImage buffImage, BufferedImage dupImage, StaveDetection staveDetection, Staves stave)
	{
		this.buffImage = buffImage;
		this.dupImage = dupImage;
		this.staveDetection = staveDetection;
		this.stave = stave;
	}

	public void doL1_Segment()
	{
		//System.out.printf("L1_Segment: xStart=%d and xStop=%d\n", xStart, xStop);
		
		if (stemInfo.stemDirection != -1)
		{
			segmentVertically();
		}
		else
		{
			DrawingTools.drawBox(dupImage, stave.getStart(), stave.getEnd(), xStart, xStop, Color.PINK);
			segmentVertically();
		}
	}
	
	private void segmentVertically()
	{
		String stem = null;
		if (stemInfo.stemDirection == 0)
			stem = new String("Left");
		else if (stemInfo.stemDirection == 1)
			stem = new String("Right");
		else
			stem = new String("No note in this segment");
		
		//System.out.println("Stem position: " + stem);

		YProjection yProj = new YProjection(buffImage);
		yProj.calcYProjection(stave.getStart(), stave.getEnd(), xStart, xStop);

		//yProj.printYProjection();
		
		int yProjection[] = yProj.getYProjection();

		// Threshold function to remove stems
		for (int j = 0; j < stave.getEnd() - stave.getStart(); j += 1)
			if (yProjection[j] < staveDetection.getStavelineParameters().getN2())
				yProjection[j] = 0;

		// Remove stavelines from projection
		for (int j = 0; j < 5; j += 1)
		{
			StavePeaks sPeak = stave.getStave_line(j);
			int stavelineStart = sPeak.getStart() - 1;

			//System.out.println("stavelineStart: " + (stavelineStart-stave.getStart()));
			for (int n = stavelineStart - stave.getStart(); n < (stavelineStart - stave.getStart()) + staveDetection.getStavelineParameters().getN2(); n += 1)
			{
				//System.out.println("n=" + n);
				try
				{
				yProjection[n] = 0;
				}
				catch (ArrayIndexOutOfBoundsException e)
				{
					System.out.println("FIX THIS SOON");
				}
			}
		}

		/*System.out.println("Y-PROJECTION START");
		for (int i=0; i<stave.getEnd() - stave.getStart(); i+=1)
			System.out.println(yProjection[i]);
		System.out.println("Y-PROJECTION END");
		*/
		
		boolean start = false;
		int startPos = 0;

		int wcount = 0;
		for (int j = 0; j < stave.getEnd() - stave.getStart(); j += 1)
		{
			// Case when we have a non-zero element in projection
			if (yProjection[j] > 0)
			{
				//System.out.println("*** Start at " + j);
				wcount = 0;
				if (!start)
					startPos = j;
				start = true;
			}

			else
			{

				wcount += 1;
				int heightCheck = (j - wcount) - startPos;
				//System.out.println("Height check = " + heightCheck + "  wcount= " + wcount);
				if ((wcount > staveDetection.getStavelineParameters().getD1() || (j ==stave.getEnd() - stave.getStart() -1)) && heightCheck > staveDetection.getStavelineParameters().getN2())
				{
					if (start)
					{
						int yStart = stave.getStart() + startPos- 6;
						int yEnd = 6+stave.getStart() + j - wcount - (stave.getStart() + startPos);
						
						if (stemInfo.stemDirection == 1 || stemInfo.stemDirection == 0)
						{
							int xDiff = xStop-xStart;
							xStart-=xDiff*0.1;
						}
						//xStart -= 6;
						//xStop +=3;
						//xStart -=6;
						
						
						//System.out.printf("xStart=%d, xEnd=%d, yStart=%d, yEnd=%d\n", xStart, xStop, yStart, yEnd);
						
						//Draw L2_Segment
						DrawingTools.drawBox(dupImage, yStart, yStart+yEnd, xStart, xStop, Color.ORANGE);

						
						//Test neural network
						double data[] = ANNPrepare.prepareImage(buffImage.getSubimage(xStart+4, yStart, xStop - xStart+3, yEnd+6));
						
						//GUI.getNeuralNetwork().testNet(neuralData);
						double neuralData[][] = new double[1][128];
						neuralData[0] = data;
						SymbolConfidence result = GUI.getANNInterrogator().interogateNN(neuralData);
						
						//Uncomment when NN is taken care of
						L2_Segment l2_temp = new L2_Segment(yStart, result.getName(), result.getRMSE());
						l2_temp.printInfo();
						l2_Segments.add(l2_temp);

						//System.out.printf("Symbol: %s  Percentage: %f\n", res.symbolName, res.percentage);

						start = false;
						wcount = 0;
					}
				}
			}
		}
	}
	
	public NoteStem getStemInfo()
	{
		return stemInfo;
	}
	
	public LinkedList<L2_Segment> getL2_Segment()
	{
		return l2_Segments;
	}
	
	public int getWidth()
	{
		return xStop - xStart;
	}
}
