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
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;

import openomr.omr_engine.DetectionProcessor;
import openomr.omr_engine.StaveDetection;
import openomr.omr_engine.StaveParameters;
import openomr.omr_engine.YProjection;


class OMREngineJPanel extends JPanel
{
	private JButton recogScore;
	private JTextField YprojWstart;
	private JTextField YprojWend;
	private BufferedImage buffImage;
	private JLabel staveFoundLabel;
	private JTextField staveTVal;
	private JTextField peakTVal;
	private GUI gui;

	public OMREngineJPanel(GUI gui)
	{
		super();
		setLayout(new BorderLayout());

		this.gui = gui;
		gui.setStaveDetectionPanel(this);

		Border etched = BorderFactory.createEtchedBorder();
		Border titled = BorderFactory.createTitledBorder(etched, " OMR Engine ");
		setBorder(titled);

		staveTVal = new JTextField("0.75", 4);
		JLabel staveTLabel = new JLabel("Stave Threshold: ");
		peakTVal = new JTextField("0.75", 4);
		JLabel peakTLabel = new JLabel("Peak Threshold: ");

		YprojWstart = new JTextField("0", 4);
		JLabel YprojWstartLabel = new JLabel("Width Start: ");
		YprojWend = new JTextField("0", 4);
		JLabel YprojWendLabel = new JLabel("Width End: ");

		JLabel stavesLabel = new JLabel("# of Staves Found:");
		staveFoundLabel = new JLabel("-");
		staveFoundLabel.setForeground(Color.BLUE);

		recogScore = new JButton("Recognise Score");
		recogScore.addActionListener(new StaveDetectionPanelActionListener());

		Box hbox2 = Box.createHorizontalBox();
		hbox2.add(staveTLabel);
		hbox2.add(staveTVal);

		Box hbox3 = Box.createHorizontalBox();
		hbox3.add(peakTLabel);
		hbox3.add(peakTVal);

		Box hbox6 = Box.createHorizontalBox();
		hbox6.add(YprojWstartLabel);
		hbox6.add(YprojWstart);

		Box hbox7 = Box.createHorizontalBox();
		hbox7.add(YprojWendLabel);
		hbox7.add(YprojWend);

		Box hbox8 = Box.createHorizontalBox();
		hbox8.add(stavesLabel);
		hbox8.add(staveFoundLabel);

		Box hbox9 = Box.createHorizontalBox();
		hbox9.add(recogScore);

		Box vbox1 = Box.createVerticalBox();
		vbox1.add(hbox2);
		vbox1.add(Box.createVerticalStrut(7));
		vbox1.add(hbox3);
		vbox1.add(Box.createVerticalStrut(7));
		vbox1.add(hbox6);
		vbox1.add(Box.createVerticalStrut(7));
		vbox1.add(hbox7);
		vbox1.add(Box.createVerticalStrut(7));
		vbox1.add(hbox8);
		vbox1.add(Box.createVerticalStrut(7));
		vbox1.add(hbox9);
		add(vbox1, BorderLayout.NORTH);
	}

	public void setButtonActive(Boolean val)
	{
		recogScore.setEnabled(val);
		repaint();
	}

	public void setDefaultValues(int wStart, int wEnd)
	{
		YprojWstart.setText(String.valueOf(wStart));
		YprojWend.setText(String.valueOf(wEnd));
	}

	class StaveDetectionPanelActionListener implements ActionListener
	{

		public void actionPerformed(ActionEvent e)
		{
			buffImage = gui.getImage();

			YProjection yproj = new YProjection(buffImage);
			yproj.calcYProjection(0, buffImage.getHeight(), Integer.valueOf(YprojWstart.getText()), Integer.valueOf(YprojWend.getText()));
			//yproj.printYProjection();
			gui.setYproj(yproj);

			StaveParameters params = gui.getStaveLineParameters(); 
			params.calcParameters();
			
			//params.printHistogram();
			
			StaveDetection staveDetection = new StaveDetection(yproj, params);
			staveDetection.setParameters(Double.valueOf(staveTVal.getText()), Double.valueOf(peakTVal.getText()));
			staveDetection.locateStaves();
			staveFoundLabel.setText(String.valueOf(staveDetection.getNumStavesFound()));

			if (staveDetection.getNumStavesFound() > 0)
			{
			//calculate ditsance between notes
			staveDetection.calcNoteDistance();
			
			
			gui.setStaveDetection(staveDetection);
			
			DetectionProcessor detection = new DetectionProcessor(buffImage, staveDetection, GUI.getNeuralNetwork());
			detection.processAll();

			gui.setBoundaryImage(detection.getDupImage());
			
			gui.setImageRecognisedOptions(true);
			}
			
			//Dispose JDialog box
			//gui.getToolbar().getRecogDialog().setVisible(false);
						
		}
	}
}