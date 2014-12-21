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

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.LinkedList;

public class Staves
{
	private StavePeaks stave_line[];
	private LinkedList<Integer> vBars;
	private LinkedList<L0_Segment> symbolPos;
	private int top;
	private int bottom;
	private int staveNumber;
	private int left;
	private int right;
	private int noteDistance;
	private int start;	//where we start to look for notes VERTICALLY
	private int end; //where we stop looking for notes VERTICALLY
	
	public Staves(int staveNumber)
	{
		stave_line = new StavePeaks[5];
		this.staveNumber = staveNumber;
		vBars = new LinkedList<Integer>();
		symbolPos = new LinkedList<openomr.omr_engine.L0_Segment>();
	}

	public void setStart(int start)
	{
		this.start = start;
	}

	public void setEnd(int end)
	{
		this.end = end;
	}
	
	public int getStart()
	{
		return start;
	}
	
	public int getEnd()
	{
		return end;
	}
	
	public void setLR(int l, int r)
	{
		this.left = l;
		this.right = r;
	}

	public void addStaveline(int pos, StavePeaks staveLine)
	{
		stave_line[pos] = staveLine;
		if (pos == 0)
			top = staveLine.getStart() - 5;
		if (pos == 4)
			bottom = staveLine.getEnd() + 5;
	}

	public int getTop()
	{
		return top;
	}
	
	public int getBottom()
	{
		return bottom;
	}
	
	public void printStavesInfo(DataOutputStream staveInfo)
	{
		try
		{
			staveInfo.writeBytes("Stave # " + staveNumber + "\n");

			for (int i = 0; i < 5; i += 1)
			{
				staveInfo.writeBytes("Staveline # " + i + " StartPos: " + stave_line[i].getStart() + " EndPos: " + stave_line[i].getEnd() + "\n");
			}
		} catch (IOException e)
		{
			System.out.println("Coult not write to file in method printStavesInfo");
			e.printStackTrace();
		}
	}

	public StavePeaks getStave_line(int number)
	{
		return stave_line[number];
	}

	public void setStave_line(int number, StavePeaks stavePeak)
	{
		stave_line[number] = stavePeak;
	}

	public int getLeft()
	{
		return left;
	}

	public int getRight()
	{
		return right;
	}

	public void setStaveNumber(int number)
	{
		this.staveNumber = number;
	}

	public void addVBar(int xPos)
	{
		vBars.add(Integer.valueOf(xPos));
	}

	public LinkedList<Integer> getVBar()
	{
		return vBars;
	}

	public LinkedList<openomr.omr_engine.L0_Segment> getSymbolPos()
	{
		return symbolPos;
	}
	
	public void addSymbolPos(int start, int stop)
	{
		L0_Segment temp = new L0_Segment();
		temp.start = start;
		temp.stop = stop;
		symbolPos.add(temp);
	}
	
	public void saveStaveInfo(DataOutputStream staveOutFile)
	{
		/*
		 * FORMAT OF .stave FILE Stave Threshold, Peak Threshold Stave # Stave 1 -
		 * 5 --> Peak Value Position, Peak Value, Stave Start X-cord, Stave End
		 * X-cord
		 */
		try
		{
			staveOutFile.writeInt(staveNumber);
			for (int i = 0; i < 5; i += 1)
			{
				staveOutFile.writeInt(stave_line[i].getPos());
				staveOutFile.writeInt(stave_line[i].getValue());
				staveOutFile.writeInt(stave_line[i].getStart());
				staveOutFile.writeInt(stave_line[i].getEnd());
			}
		} catch (IOException e)
		{
			System.out.println("Could not write to outfile");
			e.printStackTrace();
		}
	}
	
	public void setNoteDistance(int noteDistance)
	{
		this.noteDistance = noteDistance; 
	}
	
	public int getNoteDistance()
	{
		return noteDistance;
	}
}
