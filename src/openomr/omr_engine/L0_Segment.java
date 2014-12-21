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


public class L0_Segment
{
	public int start;	//Start and stop coordinates of L0_Segment
	public int stop;
	
	public boolean hasNote;
	
	private BufferedImage buffImage;
	private BufferedImage dupImage;
	private StaveDetection staveDetection;
	private Staves stave;
	
	//Consider moving this somewhere else
	LinkedList<PitchCalculation> noteList;
	
	//List of all noteheads present in L0_Segment
	public LinkedList<NoteHead> noteCoordList;
	
	//List of all L1_Segments
	private LinkedList<L1_Segment> l1_segmentList;
	
	public L0_Segment()
	{
		
		noteList = new LinkedList<PitchCalculation>();
		l1_segmentList = new LinkedList<L1_Segment>();
		hasNote = false;
	}
	
	public void setParameters(BufferedImage buffImage, BufferedImage dupImage, StaveDetection staveDetection, Staves stave)
	{
		this.buffImage = buffImage;
		this.dupImage = dupImage;
		this.staveDetection = staveDetection;
		this.stave = stave;
	}
	
	public void processL0_Segment()
	{
		//Locate all notehead in segment (if any are present)
		findNoteHeads();
		
		//if this segment has a note, we need to further segment it
		if (hasNote)
		{
			System.out.println("***** L0_Segment *****");
			for (int i=0; i<noteCoordList.size(); i+=1)
			{
				NoteHead noteHeadTemp = noteCoordList.get(i);
				
				//System.out.printf("Stem position: %d\n", noteHeadTemp.stemInfo.stemDirection);
				
				L1_Segment l1_temp = new L1_Segment(noteHeadTemp.x-staveDetection.getStavelineParameters().getN2(), noteHeadTemp.x+staveDetection.getStavelineParameters().getD2(), noteHeadTemp.stemInfo);
				l1_temp.setParameters(buffImage, dupImage, staveDetection, stave);
				l1_temp.doL1_Segment();
				l1_segmentList.add(l1_temp);
				
				if (noteHeadTemp.stemInfo.stemDirection == 1)  //stem is on right
				{
					if (i < noteCoordList.size()-1)
					{	
						int xStart = noteHeadTemp.stemInfo.stemPosition;
						int xStop = noteCoordList.get(i+1).x-9;  //9 is an arbritrary #. Need to find a pattern
						
						if (xStop - xStart > 14) //we don't want to process very small sgements
						{
							L1_Segment l1_temp2 = new L1_Segment(xStart, xStop, new NoteStem());
							l1_temp2.setParameters(buffImage, dupImage, staveDetection, stave);
							l1_temp2.doL1_Segment();
							l1_segmentList.add(l1_temp2);
						}
					}
					else	//There are no more stems in L0_Segment
					{
						int xStart = noteHeadTemp.stemInfo.stemPosition;
						int xStop = stop;

						if (xStop - xStart > 14) //we don't want to process very small sgements
						{
							L1_Segment l1_temp2 = new L1_Segment(xStart, xStop, new NoteStem());
							l1_temp2.setParameters(buffImage, dupImage, staveDetection, stave);
							l1_temp2.doL1_Segment();
							l1_segmentList.add(l1_temp2);
						}
					}
					
				}
				
				else	//stem is on the left
				{
					//System.out.println("Stem is on the right... Still needs to be implemented");
					if (i < noteCoordList.size()-1)
					{
						
					}
				}
			}
		}
		else
		{
			segmentNonNoteSymbols();
		}
	}
	
	private void findNoteHeads()
	{
		
		DrawingTools.drawBox(dupImage, stave.getStart(), stave.getEnd(), start, stop, Color.GREEN);
		
		//Locate all noteheads
		NoteHeadDetection noteHeadDetection = new NoteHeadDetection(buffImage, staveDetection.getStavelineParameters());
		noteCoordList = noteHeadDetection.findNotes(stave.getStart() , stave.getEnd(), start, stop);

		//Traverse linked list containing noteHeads and draw them
		Iterator posIterator = noteCoordList.iterator();
		while (posIterator.hasNext())
		{
			NoteHead pos = (NoteHead) posIterator.next();

			addNote(pos.y + 8, stave.getNoteDistance(), stave.getStave_line(4).getEnd());

			// draw note
			DrawingTools.drawNote(dupImage, pos.x, 20, pos.y, staveDetection.getStavelineParameters().getD2());
		}
		
		
	}
	
	private void segmentNonNoteSymbols()
	{
		YProjection yProj = new YProjection(buffImage);
		yProj.calcYProjection(stave.getStart(), stave.getEnd(), start, stop);

		int yProjection[] = yProj.getYProjection();

		boolean start = false;
		int count = 0;
		int startPos = 0;
		// System.out.println("Symbol #" + i);

		for (int j = 0; j < stave.getEnd() - stave.getStart(); j += 1)
		{
			if (yProjection[j] != 0)
			{
				start = true;

				if (count == 0)
					startPos = j;

				count += 1;
			} else
			{
				if (start && count > staveDetection.getStavelineParameters().getN2())
				{
					DrawingTools.drawBox(dupImage, stave.getStart() + startPos, stave.getStart() + j, this.start, this.stop, Color.MAGENTA);
				}
				start = false;
				count = 0;
			}
		}
	}	
	
	public void addNote(int yPos, int noteDistance, int refPos)
	{
		//Cannot calculate pitch here as we need more info about note segments...
		PitchCalculation temp = new PitchCalculation();
		temp.setNote(yPos, noteDistance, refPos);
		noteList.add(temp);
		
		
		hasNote = true;
	}
	
	public void calculateNoteDuration()
	{
		int notesProcessed = 0;
		for (int i=0; i<l1_segmentList.size(); i+=1)
		{
			L1_Segment l1_temp = l1_segmentList.get(i);
			if (l1_temp.getStemInfo().stemDirection == 1 && i < l1_segmentList.size()-1) //Stem to the right
			{
				//Therefore we need next L1_Segment to determine duration of note
				l1_temp = l1_segmentList.get(i+1);
				if (l1_temp.getWidth() > 4)
				{
					LinkedList<L2_Segment> l2_temp = l1_temp.getL2_Segment();
					for (int j=0; j<l2_temp.size(); j+=1)
					{
						if (l2_temp.get(j).getSymbolName().equals("quaver_line") || l2_temp.get(j).getSymbolName().equals("quaver"))
						{
							if (l2_temp.get(j).getAccuracy() > 0.2)
							{
								noteList.get(notesProcessed).setDuration(2);
								break;
							}
						}
						else if (l2_temp.get(j).getSymbolName().equals("semiquaver_line")  || l2_temp.get(j).getSymbolName().equals("semiquaver"))
						{
							if (l2_temp.get(j).getAccuracy() > 0.2)
							{
								noteList.get(notesProcessed).setDuration(1);
								break;
							}
						}
					}
					notesProcessed+=1;
				}
			}
			else if (l1_temp.getStemInfo().stemDirection == 1 && l1_temp.getWidth() > 4)
			{
				l1_temp = l1_segmentList.get(i);
				LinkedList<L2_Segment> l2_temp = l1_temp.getL2_Segment();
				for (int j=0; j<l2_temp.size(); j+=1)
				{
					if (l2_temp.get(j).getSymbolName().equals("quaver_line")  || l2_temp.get(j).getSymbolName().equals("quaver"))
					{
						if (l2_temp.get(j).getAccuracy() > 0.2)
						{
							noteList.get(notesProcessed).setDuration(2);
							break;
						}
					}
					else if (l2_temp.get(j).getSymbolName().equals("semiquaver_line")  || l2_temp.get(j).getSymbolName().equals("semiquaver"))
					{
						if (l2_temp.get(j).getAccuracy() > 0.2)
						{
							noteList.get(notesProcessed).setDuration(1);
							break;
						}
					}
				}
				notesProcessed+=1;
			}
				
		}
	}
	
	public LinkedList<PitchCalculation> getNotes()
	{
		return noteList;
	}
}