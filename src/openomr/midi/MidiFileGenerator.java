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

import javax.sound.midi.*;

/**
 * This class represents an Old Song that we might want to record for our
 * listening pleasure.
 */
public abstract class MidiFileGenerator
{
	private final Sequencer sequencer;
	private final Track track;
	private final int resolution;
	private int pos;

	/**
	 * @param key
	 *            is the note that this starts with. 60 is middle C.
	 * @param tempo
	 *            is measured in beats per second
	 */
	public MidiFileGenerator(int key, int tempo, int resolution) throws MidiUnavailableException, InvalidMidiDataException
	{
		this.resolution = resolution;
		Sequence sequence = new Sequence(Sequence.PPQ, resolution);
		track = sequence.createTrack();
		//makeSong(key);
		sequencer = MidiSystem.getSequencer();
		sequencer.open();
		sequencer.setSequence(sequence);
		sequencer.setTempoInBPM(tempo);
	}

	public void start()
	{
		sequencer.start();
	}

	//protected abstract void makeSong(int key) throws InvalidMidiDataException;

	protected void add(int note) throws InvalidMidiDataException
	{
		add(note, 1);
	}

	protected synchronized void add(int note, int length) throws InvalidMidiDataException
	{
		addStartEvent(note);
		pos += length;
		addStopEvent(note);
	}

	protected synchronized void addSilence(int length)
	{
		pos += length;
	}

	/**
	 * A piano teacher once told me that the first note in a bar should be
	 * emphasized.
	 * 
	 * We assume that resolution has already been set and that we have the
	 * "this" monitor.
	 */
	private int volume()
	{
		return ((pos % resolution) == 0) ? 100 : 70;
	}

	/**
	 * We assume that we are holding the "this" monitor
	 */
	private void addStartEvent(int note) throws InvalidMidiDataException
	{
		ShortMessage message = new ShortMessage();
		message.setMessage(ShortMessage.NOTE_ON, 0, note, volume());
		track.add(new MidiEvent(message, pos));
	}

	/**
	 * We assume that we are holding the "this" monitor
	 */
	private void addStopEvent(int note) throws InvalidMidiDataException
	{
		ShortMessage message = new ShortMessage();
		message.setMessage(ShortMessage.NOTE_OFF, 0, note, 0);
		track.add(new MidiEvent(message, pos));
	}
}