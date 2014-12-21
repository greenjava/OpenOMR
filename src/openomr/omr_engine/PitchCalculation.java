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

public class PitchCalculation
{
	public final int KEY_TREBLE = 0;
	public final int KEY_BASS = 1;
	public final int KEY_ALTO = 2;
	
	private int note;
	private int origNote;
	private int duration;
	//private int xPosition;
	
	public PitchCalculation()
	{
		note = 0;
		duration = 3;
		//key = KEY_TREBLE;
		//timesig = 0;
	}
	
	public void setNote(int yPos, int noteDistance, int refPos)
	{
		origNote = (int) Math.floor((refPos - yPos) / (noteDistance/2.0));
		
		//System.out.printf("yPos=%d, refPos=%d, noteDistance=%d, note=%d\n", yPos, refPos, noteDistance, note);
		note = origNote;
		note = calcNotePitch(origNote);
		
		
	}
	
	public void setDuration(int duration)
	{
		this.duration = duration;
	}
	
	public int getDuration()
	{
		return duration;
	}
	
	private int calcNotePitch(int origNote)
	{
		int num = origNote / 7;
		
		int tempNote = origNote;
		if (tempNote < 0)
		{
			while (true)
			{
				if (tempNote > -7)
					break;
				tempNote+=7;
			}
		}
		else if (tempNote > 6)
		{
			while (true)
			{
				if (tempNote < 7)
					break;
				tempNote-=7;
			}
		}
		if (tempNote >= 0 && tempNote <= 6)
		{
			switch (tempNote)
			{
			case 0:	return 0+7*num;
			case 1:	return 1+7*num;
			case 2:	return 3+7*num;
			case 3:	return 5+7*num;
			case 4:	return 7+7*num;
			case 5:	return 8+7*num;
			case 6:	return 10+7*num;
			}
		}
		else if (tempNote < 0 && tempNote >= -6)
		{
			switch (tempNote)
			{
			case -1:	return -2+7*num;
			case -2:	return -4+7*num;
			case -3:	return -6+7*num;
			case -4:	return -8+7*num;
			case -5:	return -9+7*num;
			case -6:	return -11+7*num;
			}
		}
		return -1;
	}
	
	public int getNote()
	{
		return note;
	}
	
	public void printNote()
	{
		int tempNote = origNote;
		if (tempNote < 0)
		{
			while (true)
			{
				if (tempNote > 0)
					break;
				tempNote+=7;
			}
		}
		if (tempNote % 7 == 0)
			System.out.print("E ");
		else if (tempNote % 7 == 1)
			System.out.print("F ");
		else if (tempNote % 7 == 2)
			System.out.print("G ");
		else if (tempNote % 7 == 3)
			System.out.print("A ");
		else if (tempNote % 7 == 4)
			System.out.print("B ");
		else if (tempNote % 7 == 5)
			System.out.print("C ");
		else if (tempNote % 7 == 6)
			System.out.print("D ");
		
		System.out.println("- Duration " + duration);
	}
}
