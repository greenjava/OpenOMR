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


package openomr.midi;

import java.util.LinkedList;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiUnavailableException;

import openomr.omr_engine.L0_Segment;
import openomr.omr_engine.PitchCalculation;
import openomr.omr_engine.Staves;


public class ScoreGenerator extends MidiFileGenerator 
{
	private LinkedList<Staves> staveList;
	public ScoreGenerator(LinkedList<Staves> staveList) throws MidiUnavailableException, InvalidMidiDataException
	{
		// key, tempo, resolution
		super(60, 30, 8);
		this.staveList = staveList;
	}
	
	public void makeSong(int key) throws InvalidMidiDataException
	{
		for (int j=0; j<staveList.size(); j+=1)
		{
			Staves tempStave = staveList.get(j);
			LinkedList<L0_Segment> tempSymbol = tempStave.getSymbolPos();
			for (int k=0; k<tempSymbol.size(); k+=1)
			{
				L0_Segment tempPos = tempSymbol.get(k);
				LinkedList<PitchCalculation> notes = tempPos.getNotes();
				for (int i=0; i<notes.size(); i+=1)
				{
					PitchCalculation tempNote = notes.get(i);
					tempNote.printNote();
					//System.out.println("Note: " + (tempNote.getNote()+64));
					add(tempNote.getNote()+64, tempNote.getDuration());
				}
				//System.out.println("End Symbol");
			}
			//System.out.println("End Stave");
		}
	}   
}