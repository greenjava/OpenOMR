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

import java.io.*;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

import openomr.ann.ANNPrepare;
import openomr.ann.ANNTrainer;

public class TrainANNJPanel extends JPanel
{
	private GUI gui;
	private String baseDir;
	private JLabel dirSel;
	private JButton trainButton;
	public TrainAction trainAction;
	private ANNPrepare annPrep;
	private ANNTrainer ann;
	private JLabel trainingRMSE;
	private JLabel validationRMSE;
	private JLabel epochsCounter;
	private String PATH_SEPERATOR = System.getProperty("file.separator");

	public TrainANNJPanel(GUI gui)
	{
		super();
		setLayout(new BorderLayout());
		this.gui = gui;

		Border etched = BorderFactory.createEtchedBorder();
		Border titled = BorderFactory.createTitledBorder(etched, " ANN Trainer ");
		setBorder(titled);

		JButton dirButton = new JButton("Browse");
		JLabel dir = new JLabel("Base directroy: ");
		dirSel = new JLabel("");
		dirButton.addActionListener(new BrowserActionListener());

		Box hbox1 = Box.createHorizontalBox();
		hbox1.add(dirButton);
		hbox1.add(Box.createHorizontalStrut(10));
		hbox1.add(dir);
		hbox1.add(Box.createHorizontalStrut(10));
		hbox1.add(dirSel);
		hbox1.add(Box.createHorizontalStrut(10));
		hbox1.add(Box.createHorizontalGlue());

		JLabel trainRMSELabel = new JLabel("Training RMSE: ");
		trainingRMSE = new JLabel("0.0");
		
		JLabel valRMSELabel = new JLabel("Validation RMSE: ");
		validationRMSE = new JLabel("0.0");

		JLabel epochs = new JLabel("Epochs: ");
		epochsCounter = new JLabel("0");

		Box hbox2 = Box.createHorizontalBox();
		hbox2.add(trainRMSELabel);
		hbox2.add(trainingRMSE);
		hbox2.add(Box.createHorizontalStrut(80));
		hbox2.add(valRMSELabel);
		hbox2.add(validationRMSE);
		hbox2.add(Box.createHorizontalGlue());

		Box hbox5 = Box.createHorizontalBox();
		hbox5.add(epochs);
		hbox5.add(epochsCounter);
		hbox5.add(Box.createHorizontalGlue());

		trainButton = new JButton("Train");
		trainButton.addActionListener(new TrainAction());
		JButton stopButton = new JButton("Stop");
		stopButton.addActionListener(new StopAction());
		JButton saveButton = new JButton("Save");
		saveButton.addActionListener(new SaveAction());

		Box hbox4 = Box.createHorizontalBox();
		hbox4.add(trainButton);
		hbox4.add(stopButton);
		hbox4.add(saveButton);

		Box vbox = Box.createVerticalBox();
		vbox.add(hbox1);
		vbox.add(Box.createVerticalStrut(10));
		vbox.add(hbox2);
		vbox.add(Box.createVerticalStrut(10));
		vbox.add(hbox5);
		vbox.add(Box.createVerticalStrut(10));
		vbox.add(hbox4);

		add(vbox, BorderLayout.CENTER);
	}

	private class BrowserActionListener implements ActionListener
	{
		public void actionPerformed(ActionEvent arg0)
		{
			JFileChooser chooser = new JFileChooser(new File(System.getProperty("user.dir")));
			//System.out.println("."+GUI.PATH_SEPERATOR);
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int option = chooser.showOpenDialog(TrainANNJPanel.this);
			if (option == JFileChooser.APPROVE_OPTION)
			{
				baseDir = chooser.getSelectedFile().getPath() + GUI.PATH_SEPERATOR;
				dirSel.setText(chooser.getSelectedFile().getName());
			}
		}
	}
	
	public void setTrainingRMSE(double value)
	{
		trainingRMSE.setText(String.valueOf(value).substring(0, 9));
	}
	
	public void setValidationRMSE(double value)
	{
		validationRMSE.setText(String.valueOf(value).substring(0, 9));
	}

	public void setEpoch(int value)
	{
		epochsCounter.setText(String.valueOf(value));
	}
	
	private class TrainAction implements ActionListener
	{
		public void actionPerformed(ActionEvent arg0)
		{
			annPrep = new ANNPrepare(baseDir);
			ann = annPrep.prepareNetwork();
			ann.createNeuralNet(TrainANNJPanel.this);
			ann.trainNeuralNet(true);

		}
		
	}

	private class StopAction implements ActionListener
	{
		public void actionPerformed(ActionEvent arg0)
		{
			ann.trainNeuralNet(false);
			gui.setNeuralNetwork(ann.getNeuralNet());
			//annPrep.testFile("C:\\Documents and Settings\\Arnaud\\My Documents\\workspace\\SheetMusic\\musical_symbols\\esempio1\\crotchet1.png");
			//annPrep.testFile("C:\\Documents and Settings\\Arnaud\\My Documents\\workspace\\SheetMusic\\musical_symbols\\esempio1\\natural5.png");
			//annPrep.testFile("C:\\Documents and Settings\\Arnaud\\My Documents\\workspace\\SheetMusic\\musical_symbols\\esempio1\\sharp3.png");
			//annPrep.testFile("C:\\Documents and Settings\\Arnaud\\My Documents\\workspace\\SheetMusic\\musical_symbols\\esempio1\\quaver_line6.png");
		}
	}

	private class SaveAction implements ActionListener
	{
		public void actionPerformed(ActionEvent arg0)
		{
			//trainer.saveNeuralNet();
			JFileChooser saveChooser = new JFileChooser(new File(System.getProperty("user.dir") + PATH_SEPERATOR + "neuralnetwork"));
			int option = saveChooser.showSaveDialog(TrainANNJPanel.this);
			if (option == JFileChooser.APPROVE_OPTION)
			{
				File file = saveChooser.getSelectedFile();
				ann.saveNeuralNet(file.getAbsoluteFile().toString());
			}
			else
			{
				//canceled
			}
		}
	}
}
