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

package openomr.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.util.LinkedList;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiUnavailableException;
import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;

import openomr.midi.MidiFileGenerator;
import openomr.midi.ScoreGenerator;
import openomr.omr_engine.L0_Segment;
import openomr.omr_engine.PitchCalculation;
import openomr.omr_engine.StaveDetection;
import openomr.omr_engine.Staves;


public class ToolBar extends JPanel
{
	private GUI gui;
	private RecognitionAction recognitionAction;
	private FFTAction fftAction;
	private PlayAction playAction;
	private JDialog recogDialog;
	private OpenAction openAction;
	
	public ToolBar(GUI gui)
	{
		super();
		setLayout(new BorderLayout());
		
		this.gui = gui;
		gui.setToolBar(this);
		
		Box box = Box.createHorizontalBox();
		
		JToolBar toolbar = new JToolBar("Tools");
		
		recognitionAction = new RecognitionAction("Right", new ImageIcon("icons/GreenFlag.png"), "Perform Recognition", 'R');
		fftAction = new FFTAction("Center", new ImageIcon("icons/Fft.png"), "Do FFT", 'C');
		playAction = new PlayAction("Right", new ImageIcon("icons/Play.png"), "Play Recognized Score", 'R');
		openAction = new OpenAction("Left", new ImageIcon("icons/Open.png"), "Open File", 'L');
		toolbar.add(openAction);
		toolbar.add(fftAction);
		toolbar.add(recognitionAction);
		toolbar.add(playAction);
		box.add(toolbar);
		box.add(Box.createHorizontalGlue());
		
		add(box, BorderLayout.CENTER);
	}
	
	private class RecognitionAction extends AbstractAction
	{
		public RecognitionAction(String text, Icon icon, String description, char accelerator)
		{
			super(text, icon);
			setEnabled(false);
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(accelerator, java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
			putValue(SHORT_DESCRIPTION, description);
		}

		public void actionPerformed(ActionEvent arg0)
		{
			recogDialog = new JDialog();
			recogDialog.setSize(300, 300);
			recogDialog.setLocation(gui.getWidth()/2, gui.getHeight()/2);
			
			OMREngineJPanel spanel = new OMREngineJPanel(gui);
			spanel.setDefaultValues(0, gui.getImage().getWidth());
			
			Container contents = recogDialog.getContentPane();
			
			Box vbox = Box.createVerticalBox();
			vbox.add(spanel);
			
			contents.add(vbox);
			
			recogDialog.setVisible(true);
		}
	}
	
	private class OpenAction extends AbstractAction
	{
		public OpenAction(String text, Icon icon, String description, char accelerator)
		{
			super(text, icon);
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(accelerator, java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
			putValue(SHORT_DESCRIPTION, description);
		}

		public void actionPerformed(ActionEvent arg0)
		{
			gui.FileOpenAction();
		}
	}

	private class PlayAction extends AbstractAction
	{
		public PlayAction(String text, Icon icon, String description, char accelerator)
		{
			super(text, icon);
			setEnabled(false);
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(accelerator, java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
			putValue(SHORT_DESCRIPTION, description);
		}

		public void actionPerformed(ActionEvent arg0)
		{
			ScoreGenerator scoreGen = null;
			try
			{
				StaveDetection staveDetection = gui.getStaveDetection();
				LinkedList<Staves> staveList = staveDetection.getStaveList();
				scoreGen = new ScoreGenerator(staveList);
				scoreGen.makeSong(64);
				scoreGen.start();
			} 
			catch (MidiUnavailableException e)
			{
				e.printStackTrace();
			} 
			catch (InvalidMidiDataException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	private class FFTAction extends AbstractAction
	{
		public FFTAction(String text, Icon icon, String description, char accelerator)
		{
			super(text, icon);
			setEnabled(false);
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(accelerator, java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
			putValue(SHORT_DESCRIPTION, description);
		}

		public void actionPerformed(ActionEvent arg0)
		{
			JDialog jDialog = new JDialog();
			jDialog.setSize(150, 150);
			jDialog.setLocation(gui.getWidth()/2, gui.getHeight()/2);
			
			FFTJPanel fftPanel = new FFTJPanel(gui, jDialog);
			
			Container contents = jDialog.getContentPane();
			contents.add(fftPanel);
			
			jDialog.setVisible(true);
		}
	}
	
	public void setFFTEnabled(boolean val)
	{
		fftAction.setEnabled(val);
	}
	
	public void setReconitionEnabled(boolean val)
	{
		recognitionAction.setEnabled(val);
	}
	
	public void setPlayEnbabled(boolean val)
	{
		playAction.setEnabled(val);
	}
	
	public void setOpenEnabled(boolean val)
	{
		openAction.setEnabled(val);
	}
	
	public JDialog getRecogDialog()
	{
		return recogDialog;
	}
}
