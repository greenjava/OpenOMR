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
import java.util.Iterator;
import java.util.LinkedList;

import openomr.gui.DrawingTools;
import openomr.imageprocessing.CopyImage;

import org.joone.net.NeuralNet;


public class DetectionProcessor
{
	private BufferedImage buffImage;
	private StaveDetection staveDetection;
	private BufferedImage dupImage;
	
	public DetectionProcessor(BufferedImage buffImage, StaveDetection staveDetection, NeuralNet neuralNetwork)
	{
		this.buffImage = buffImage;
		this.staveDetection = staveDetection;
	}

	public void processAll()
	{
		setTopBottomBoundaries();
		setBoundaries();

		dupImage = (new CopyImage(buffImage)).getCopyOfImage();

		findAllSymbols();
	}

	// This method sets the upper and lower foundaries for each stave found
	private void setTopBottomBoundaries()
	{
		Staves st = (Staves) staveDetection.getStaveList().get(0);
		st.setStart(st.getTop() - 80);
		int dist = 0;
		for (int i = 0; i < staveDetection.getStaveList().size() - 1; i += 1)
		{
			Staves temp1 = (Staves) staveDetection.getStaveList().get(i);
			Staves temp2 = (Staves) staveDetection.getStaveList().get(i + 1);
			dist = (temp2.getTop() - temp1.getBottom()) / 2;
			temp1.setEnd(temp1.getBottom() + dist - 5);
			temp2.setStart(temp2.getTop() - dist + 5);
		}
		st = (Staves) staveDetection.getStaveList().get(staveDetection.getStaveList().size() - 1);
		st.setEnd(buffImage.getHeight() - 5);
	}

	private void setBoundaries()
	{
		// Locate stave boundaries, measures and L0_Segments
		StaveBoundaries staveBoundaries = new StaveBoundaries(buffImage, staveDetection);
		staveBoundaries.findBoundaries();
		// staveBoundaries.findMeasures();
		staveBoundaries.findGroupsOfNotes();
	}

	private void findAllSymbols()
	{
		int staveCount = 0;

		Iterator it = staveDetection.getStaveInfo();
		while (true)
		{
			if (!it.hasNext())
				break;

			// Process all staves found
			Staves stave = (Staves) it.next();

			staveCount += 1;

			DrawingTools.drawStave(dupImage, stave.getLeft(), stave.getRight(), stave, Color.RED);
//			DrawingTools.drawMeasures(dupImage, stave);

			// Linked list with all L0_Segments
			LinkedList<openomr.omr_engine.L0_Segment> l0_segmentList = stave.getSymbolPos();
			int capacity = l0_segmentList.size();
			//System.out.println("Stave " + staveCount);
			
			//Process each L0 segment present in current stave
			for (int i = 0; i < capacity; i += 1)
			{
				//System.out.printf("*** Segment: %d ***\n", i);
				l0_segmentList.get(i).setParameters(buffImage, dupImage, staveDetection, stave);
				l0_segmentList.get(i).processL0_Segment();
				l0_segmentList.get(i).calculateNoteDuration();
			}
		}
	}

	public BufferedImage getDupImage()
	{
		return dupImage;
	}
}
