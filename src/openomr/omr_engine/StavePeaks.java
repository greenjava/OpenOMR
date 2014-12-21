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

public class StavePeaks
{
	private int peakPos;
	private int peakValue;
	private int start;
	private int end;

	public StavePeaks(int pos, int value)
	{
		this.peakPos = pos;
		this.peakValue = value;
	}

	public void SetStart(int start)
	{
		this.start = start;
	}

	public void SetEnd(int end)
	{
		this.end = end;
	}

	public void setStartEnd(int start, int end)
	{
		this.start = start;
		this.end = end;
	}

	// method to test whether two objects are equal
	public boolean equals(StavePeaks x)
	{
		if (x.peakPos == this.peakPos && x.peakValue == this.peakValue && x.start == this.start && x.end == this.end)
			return true;
		return false;
	}

	public int getPos()
	{
		return peakPos;
	}

	public int getValue()
	{
		return peakValue;
	}

	public int getStart()
	{
		return start;
	}

	public int getEnd()
	{
		return end;
	}
}
