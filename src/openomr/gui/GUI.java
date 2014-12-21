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

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;

import javax.imageio.ImageIO;
import javax.swing.*;

import openomr.ann.ANNBatchModeTester;
import openomr.ann.ANNInterrogator;
import openomr.dataanalysis.XYChart;
import openomr.imageprocessing.DoBlackandWhite;
import openomr.omr_engine.NoteHead;
import openomr.omr_engine.NoteHeadDetection;
import openomr.omr_engine.StaveDetection;
import openomr.omr_engine.StaveParameters;
import openomr.omr_engine.StavePeaks;
import openomr.omr_engine.Staves;
import openomr.omr_engine.XProjection;
import openomr.omr_engine.YProjection;

import org.joone.net.NeuralNet;


public class GUI extends JFrame
{
	private JFileChooser chooser;
	private StaveDetection staveDetection = null;
	private String fileName;
	private BufferedImage buffImage;
	private StaveParameters staveParameters;
	private String fNamePath;
	private YProjection yProj = null;
	private OMREngineJPanel staveDetectionPanel;
	public static String PATH_SEPERATOR;
	private static NeuralNet neuralNetwork;
	private BufferedImage fftImage;
	private JMenuItem fftItem;
	private JMenuItem staveRecogItem;
	private ExtendedJMenuItem recognizeScoreItem;
	private JTabbedPane jTabbedPane;
	private ToolBar toolBar;
	private ExtendedJMenuItem dofftItem;
	private BufferedImage recognizedImage;
	private ExtendedJMenuItem yprojGraphItem;
	private ExtendedJMenuItem noteHeadItem;
	private ExtendedJMenuItem symbolItem;
	private ExtendedJMenuItem staveProjItem;
	private ExtendedJMenuItem fftSave;
	private ExtendedJMenuItem closeItem;
	private ExtendedJMenuItem fileOpen;
	private ExtendedJMenuItem saveImageItem;
	private static ANNInterrogator neuralNetInterrogator;

	public GUI()
	{
		setTitle("Sheet Music");
		// Toolkit toolKit = Toolkit.getDefaultToolkit();
		// Dimension screenSize = toolKit.getScreenSize();
		setSize(800, 950);

	    PATH_SEPERATOR = System.getProperty("file.separator");

		// ******************//
		// MENU //
		// ******************//

		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		JMenu fileMenu = new ExtendedJMenu("File");
		menuBar.add(fileMenu);

		fileOpen = new ExtendedJMenuItem("Open");
		fileMenu.add(fileOpen);
		fileOpen.addActionListener(new FileOpenListener());

		chooser = new JFileChooser();
		final ExtensionFileFilter extFilter = new ExtensionFileFilter();
		extFilter.addExtension("jpg");
		extFilter.addExtension("jpeg");
		extFilter.addExtension("gif");
		extFilter.addExtension("bmp");
		extFilter.addExtension("png");
		extFilter.setDescription("Image files");
		chooser.setFileFilter(extFilter);

		closeItem = new ExtendedJMenuItem("Close");
		fileMenu.add(closeItem);
		closeItem.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent arg0)
			{
				jTabbedPane.removeAll();
				fileOpen.setEnabled(true);
				closeItem.setEnabled(false);
				setImageRecognisedOptions(false);
				dofftItem.setEnabled(false);
				staveRecogItem.setEnabled(false);
				toolBar.setFFTEnabled(false);
				toolBar.setReconitionEnabled(false);
				toolBar.setOpenEnabled(true);
			}
			
		});
		closeItem.setEnabled(false);
		
		JMenuItem exitItem = new ExtendedJMenuItem("Exit");
		fileMenu.add(exitItem);
		exitItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent event)
			{
				System.exit(1);
			}
		});
		chooser.setAccessory(new ImagePreviewer(chooser));
		
		JMenu fftMenu = new ExtendedJMenu("FFT");
		menuBar.add(fftMenu);

		dofftItem = new ExtendedJMenuItem("Do FFT");
		dofftItem.addActionListener(new FFTAction());
		dofftItem.setEnabled(false);
		fftMenu.add(dofftItem);

		fftItem = new ExtendedJMenuItem("View FFT");
		fftItem.addActionListener(new FFTImageViewerListener());
		fftItem.setEnabled(false);
		fftMenu.add(fftItem);

		fftSave = new ExtendedJMenuItem("Save FFT");
		fftSave.addActionListener(new FFTSaver());
		fftSave.setEnabled(false);
		fftMenu.add(fftSave);
		
		JMenu recognitionMenu = new ExtendedJMenu("Recognition");
		menuBar.add(recognitionMenu);

		staveRecogItem = new ExtendedJMenuItem("Recognize Score");
		staveRecogItem.addActionListener(new RecogniseScoreAction());
		staveRecogItem.setEnabled(false);
		recognitionMenu.add(staveRecogItem);

		recognizeScoreItem = new ExtendedJMenuItem("View Recognized Score");
		recognizeScoreItem.addActionListener(new ImageViewerListener());
		recognizeScoreItem.setEnabled(false);
		recognitionMenu.add(recognizeScoreItem);

		saveImageItem = new ExtendedJMenuItem("Save Image");
		saveImageItem.addActionListener(new ActionListener()
				{

					public void actionPerformed(ActionEvent arg0)
					{
						try
						{
							saveImage(recognizedImage, "recogImage");
						} catch (IOException e)
						{
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
					}
			
				});
		recognizeScoreItem.setEnabled(true);
		recognitionMenu.add(saveImageItem);
		
		JMenu analysisMenu = new ExtendedJMenu("Analysis");
		menuBar.add(analysisMenu);

		yprojGraphItem = new ExtendedJMenuItem("Y-proj Graph");
		yprojGraphItem.addActionListener(new YProjListener());
		yprojGraphItem.setEnabled(false);
		analysisMenu.add(yprojGraphItem);

		noteHeadItem = new ExtendedJMenuItem("NoteHead Graph");
		noteHeadItem.addActionListener(new NoteHeadGraph());
		noteHeadItem.setEnabled(false);
		analysisMenu.add(noteHeadItem);
		
		symbolItem = new ExtendedJMenuItem("Symbol X-Porjection");
		symbolItem.addActionListener(new SymbolItemGraph());
		symbolItem.setEnabled(false);
		analysisMenu.add(symbolItem);
		
		staveProjItem = new ExtendedJMenuItem("Stave Y-Projection");
		staveProjItem.addActionListener(new StaveYProjection());
		staveProjItem.setEnabled(false);
		analysisMenu.add(staveProjItem);
		
		
		JMenu annMenu = new ExtendedJMenu("ANN");
		menuBar.add(annMenu);
		
		JMenuItem trainerItem = new ExtendedJMenuItem("ANN Trainer");
		trainerItem.addActionListener(new TrainerListener());
		annMenu.add(trainerItem);
		
		JMenuItem testerItem = new ExtendedJMenuItem("ANN Tester");
		testerItem.addActionListener(new TesterListener());
		annMenu.add(testerItem);
		
		JMenuItem batchTesterItem = new ExtendedJMenuItem("Batch mode testing");
		batchTesterItem.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent arg0)
					{
						ANNBatchModeTester annTest = new ANNBatchModeTester();
						annTest.prepareInputData();
					}
			
				});
		annMenu.add(batchTesterItem);

		// ******************//
		// END MENU //
		// ******************//

		Container contents = getContentPane();

		ToolBar toolBar = new ToolBar(this);
		contents.add(toolBar, BorderLayout.NORTH);

		jTabbedPane = new JTabbedPane();
		
		
		//Load neural network
		
		neuralNetInterrogator = new ANNInterrogator();
		
		/*try{
			neuralNetwork = new Trainer("." + PATH_SEPERATOR + "musical_symbols" + PATH_SEPERATOR + "training" + PATH_SEPERATOR);
			neuralNetwork.restoreNeuralNet();
		}
		catch (IOException e)
		{
			System.out.println("Could not locate file for Neural Network");
		}
		catch (ClassNotFoundException e)
		{
			System.out.println("Could not find class for Neural Network");
		}*/

	}

	private class FFTImageViewerListener implements ActionListener
	{

		public void actionPerformed(ActionEvent event)
		{
			JLabel lab = new JLabel(new ImageIcon(fftImage));
			jTabbedPane.addTab("FFT Image", lab);
		}
	}
	
	public class FFTAction implements ActionListener
	{
		public void actionPerformed(ActionEvent arg0)
		{
			JDialog jDialog = new JDialog();
			jDialog.setSize(150, 150);
			jDialog.setLocation(getWidth()/2, getHeight()/2);
			
			FFTJPanel fftPanel = new FFTJPanel(GUI.this, jDialog);
			
			Container contents = jDialog.getContentPane();
			contents.add(fftPanel);
			jDialog.setVisible(true);
			
		}
		
	}
	
	private class FFTSaver implements ActionListener
	{

		public void actionPerformed(ActionEvent arg0)
		{
			BufferedOutputStream outBuff;
			try
			{
				outBuff = new BufferedOutputStream(new FileOutputStream(new File("fft_image.png")));
				ImageIO.write(fftImage, "png", outBuff);
				outBuff.close();
			} catch (FileNotFoundException e)
			{
				e.printStackTrace();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		
	}

	private class RecogniseScoreAction implements ActionListener
	{

		public void actionPerformed(ActionEvent arg0)
		{
			JDialog recogDialog = new JDialog();
			recogDialog.setSize(300, 300);
			recogDialog.setLocation(getWidth()/2, getHeight()/2);
			
			OMREngineJPanel spanel = new OMREngineJPanel(GUI.this);
			spanel.setDefaultValues(0, getImage().getWidth());
			
			Container contents = recogDialog.getContentPane();
			
			Box vbox = Box.createVerticalBox();
			vbox.add(spanel);
			
			contents.add(vbox);
			
			recogDialog.setVisible(true);
		}
		
	}
	
	
	private class TrainerListener implements ActionListener
	{

		public void actionPerformed(ActionEvent event)
		{
			JDialog jDialog = new JDialog();
			jDialog.setSize(390, 200);
			jDialog.setLocation(getWidth()/4, getHeight()/4);
			
			TrainANNJPanel annPanel = new TrainANNJPanel(GUI.this);
			
			Container contents = jDialog.getContentPane();
			contents.add(annPanel);
			
			jDialog.setVisible(true);
		}

	}
	
	private class TesterListener implements ActionListener
	{

		public void actionPerformed(ActionEvent event)
		{
			JDialog jDialog = new JDialog();
			jDialog.setSize(350, 300);
			jDialog.setLocation(getWidth()/4, getHeight()/4);
			
			VerifyANNPanel annPanel = new VerifyANNPanel(GUI.this);
			
			Container contents = jDialog.getContentPane();
			contents.add(annPanel);
			
			jDialog.setVisible(true);
		}

	}
	
	private class YProjListener implements ActionListener
	{

		public void actionPerformed(ActionEvent event)
		{
			XYChart yProjChart = new XYChart(yProj.getYProjection(), buffImage.getHeight(), "YProj");

			JPanel buttonCorner = new JPanel();

			ImageIcon tempImage = new ImageIcon(yProjChart.getChart(buffImage.getWidth(), buffImage.getHeight()));
			ScrollableJLabel picture = new ScrollableJLabel(tempImage, (int) (Toolkit.getDefaultToolkit().getScreenResolution() / 2.54));
			JScrollPane pictureScrollPane = new JScrollPane(picture);
			pictureScrollPane.setPreferredSize(new Dimension(buffImage.getWidth(), buffImage.getHeight()));
			pictureScrollPane.setViewportBorder(BorderFactory.createLineBorder(Color.black));

			pictureScrollPane.setCorner(JScrollPane.UPPER_LEFT_CORNER, buttonCorner);

			jTabbedPane.addTab("Yproj Graph", pictureScrollPane);
		}

	}

	private class NoteHeadGraph implements ActionListener
	{

		private JComboBox symbolCombo;
		private JComboBox staveCombo;
		private JButton okButton;

		public void actionPerformed(ActionEvent event)
		{

			JDialog noteHeadDialog = new JDialog();
			noteHeadDialog.setSize(100, 150);

			JLabel staveLabel = new JLabel("Stave #: ");

			staveCombo = new JComboBox();
			symbolCombo = new JComboBox();

			staveCombo.addActionListener(new staveComboListener());

			for (int i = 0; i < staveDetection.getNumStavesFound(); i += 1)
			{
				staveCombo.addItem(String.valueOf(i));
			}

			JLabel symbolLabel = new JLabel("Symbol #: ");

			okButton = new JButton("Ok");
			okButton.addActionListener(new OkAction());

			Container contents = noteHeadDialog.getContentPane();

			Box hbox1 = Box.createHorizontalBox();
			hbox1.add(staveLabel);
			hbox1.add(staveCombo);

			Box hbox2 = Box.createHorizontalBox();
			hbox2.add(symbolLabel);
			hbox2.add(symbolCombo);
			hbox2.add(Box.createHorizontalGlue());

			Box vbox = Box.createVerticalBox();
			vbox.add(Box.createVerticalStrut(7));
			vbox.add(hbox1);
			vbox.add(Box.createVerticalStrut(7));
			vbox.add(hbox2);
			vbox.add(Box.createVerticalStrut(7));
			vbox.add(okButton);
			vbox.add(Box.createVerticalStrut(7));

			contents.add(vbox);
			noteHeadDialog.setVisible(true);
		}

		private class staveComboListener implements ActionListener
		{

			public void actionPerformed(ActionEvent arg0)
			{
				String valSel = (String) staveCombo.getSelectedItem();

				LinkedList<Staves> staveList = staveDetection.getStaveList();
				Staves stave = (Staves) staveList.get(Integer.valueOf(valSel));
				LinkedList<openomr.omr_engine.L0_Segment> symbolPos = stave.getSymbolPos();

				if (symbolCombo.getItemCount() != 0)
				{
					symbolCombo.removeAllItems();
				}

				for (int i = 0; i < symbolPos.size(); i += 1)
					symbolCombo.addItem(String.valueOf(i));
			}

		}

		private class OkAction implements ActionListener
		{

			private JButton previousButton;
			private JButton nextButton;
			private JPanel imagePanel;
			private JScrollPane scrollPanel;
			private int val;
			private int symbol;
			
			public void actionPerformed(ActionEvent arg0)
			{
				String valSel = (String) staveCombo.getSelectedItem();
				String symbolSel = (String) symbolCombo.getSelectedItem();
				val = Integer.valueOf(valSel);
				symbol = Integer.valueOf(symbolSel);
				
				imagePanel = new JPanel();
				doPanel();
				scrollPanel = new JScrollPane(imagePanel);
				jTabbedPane.addTab("NoteDetection Graph", scrollPanel);
			}
			
			private void doPanel()
			{
				LinkedList<Staves> staveList = staveDetection.getStaveList();
				Staves stave = (Staves) staveList.get(val);
				LinkedList<openomr.omr_engine.L0_Segment> symbolPos = stave.getSymbolPos();

				if (symbolPos.get(symbol).hasNote)
					System.out.println("This symbol has a note somewhere");
				
				NoteHeadDetection noteHeadDetection = new NoteHeadDetection(buffImage, staveDetection.getStavelineParameters());

				int start = ((openomr.omr_engine.L0_Segment) symbolPos.get(symbol)).start;
				int stop = ((openomr.omr_engine.L0_Segment) symbolPos.get(symbol)).stop;

				int height = stave.getBottom() - stave.getTop() + 160;
				LinkedList<NoteHead> posList = noteHeadDetection.findNotes(stave.getStart(), stave.getEnd(), start, stop);

				System.out.println("Top: " + (stave.getTop() - 80));
				System.out.println("Symbol: " + symbol);

				
				XYChart yProjChart = new XYChart(noteHeadDetection.getYposBeforeFilter(), stop - start, "Y Projection");
				ImageIcon yPosImage = new ImageIcon(yProjChart.getChart(400, 400));
				JLabel yPosLabel = new JLabel(yPosImage);

				XYChart yProjFilterChart = new XYChart(noteHeadDetection.getYPos(), stop - start, "Y Projection After Filer");
				ImageIcon yPosFilterImage = new ImageIcon(yProjFilterChart.getChart(400, 400));
				JLabel yPosFilterLabel = new JLabel(yPosFilterImage);

				ImageIcon subImage = new ImageIcon(recognizedImage.getSubimage(start, stave.getStart(), stop - start, stave.getEnd()-stave.getStart()));
				JLabel subImageLabel = new JLabel(subImage);

				XYChart xProjBeforeChart = new XYChart(noteHeadDetection.getXProjectionBeforeFilter(), stop - start, "X Projection");
				ImageIcon xProjBeforeImage = new ImageIcon(xProjBeforeChart.getChart(400, 400));
				JLabel xProjBeforeLabel = new JLabel(xProjBeforeImage);
				
				XYChart xProjChart = new XYChart(noteHeadDetection.getXProjection(), stop - start, "X Projection");
				ImageIcon xProjImage = new ImageIcon(xProjChart.getChart(400, 400));
				JLabel xProjLabel = new JLabel(xProjImage);

				
				
				
				Box hbox = Box.createHorizontalBox();
				hbox.add(yPosLabel);
				hbox.add(Box.createVerticalStrut(20));
				hbox.add(yPosFilterLabel);

				Box hbox2 = Box.createHorizontalBox();
				hbox2.add(subImageLabel);
				hbox2.add(Box.createVerticalStrut(20));
				hbox2.add(xProjBeforeLabel);
				hbox2.add(Box.createVerticalStrut(20));
				hbox2.add(xProjLabel);

				previousButton = new JButton("previous");
				previousButton.addActionListener(new PreviousAction());
				nextButton = new JButton("next");
				nextButton.addActionListener(new NextAction());

				Box hbox3 = Box.createHorizontalBox();
				hbox3.add(previousButton);
				hbox3.add(nextButton);

				Box vbox = Box.createVerticalBox();
				vbox.add(hbox);
				vbox.add(Box.createVerticalStrut(20));
				vbox.add(hbox2);
				vbox.add(Box.createVerticalStrut(20));
				vbox.add(hbox3);

				imagePanel.add(vbox);
				imagePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
			}

			private class PreviousAction implements ActionListener
			{

				public void actionPerformed(ActionEvent arg0)
				{
					if (symbol == 0)
					{
						if (val !=0)
						{
							val-=1;
							LinkedList<Staves> staveList = staveDetection.getStaveList();
							Staves stave = (Staves) staveList.get(val);
							LinkedList<openomr.omr_engine.L0_Segment> symbolPos = stave.getSymbolPos();
							symbol = symbolPos.size()-1;
						}
					}
					else
						symbol-=1;
					
					imagePanel.removeAll();
					doPanel();
					jTabbedPane.repaint();
				}

			}

			private class NextAction implements ActionListener
			{

				public void actionPerformed(ActionEvent arg0)
				{
					LinkedList<Staves> staveList = staveDetection.getStaveList();
					Staves stave = (Staves) staveList.get(val);
					LinkedList<openomr.omr_engine.L0_Segment> symbolPos = stave.getSymbolPos();
					if (symbol == symbolPos.size()-1)
					{
						if (staveList.size()-1 != val)
						{
							val+=1;
							symbol = 0;
						}
					}
					else
						symbol+=1;
					
					imagePanel.removeAll();
					doPanel();
					jTabbedPane.repaint();
				}

			}

		}
	}

	private class SymbolItemGraph implements ActionListener
	{

		private JComboBox symbolCombo;
		private JComboBox staveCombo;
		private JButton okButton;

		public void actionPerformed(ActionEvent event)
		{

			JDialog noteHeadDialog = new JDialog();
			noteHeadDialog.setSize(100, 150);

			JLabel staveLabel = new JLabel("Stave #: ");

			staveCombo = new JComboBox();
			symbolCombo = new JComboBox();

			staveCombo.addActionListener(new staveComboListener());

			for (int i = 0; i < staveDetection.getNumStavesFound(); i += 1)
			{
				staveCombo.addItem(String.valueOf(i));
			}

			JLabel symbolLabel = new JLabel("Symbol #: ");

			okButton = new JButton("Ok");
			okButton.addActionListener(new OkAction());

			Container contents = noteHeadDialog.getContentPane();

			Box hbox1 = Box.createHorizontalBox();
			hbox1.add(staveLabel);
			hbox1.add(staveCombo);

			Box hbox2 = Box.createHorizontalBox();
			hbox2.add(symbolLabel);
			hbox2.add(symbolCombo);
			hbox2.add(Box.createHorizontalGlue());

			Box vbox = Box.createVerticalBox();
			vbox.add(Box.createVerticalStrut(7));
			vbox.add(hbox1);
			vbox.add(Box.createVerticalStrut(7));
			vbox.add(hbox2);
			vbox.add(Box.createVerticalStrut(7));
			vbox.add(okButton);
			vbox.add(Box.createVerticalStrut(7));

			contents.add(vbox);
			noteHeadDialog.setVisible(true);
		}

		private class staveComboListener implements ActionListener
		{

			public void actionPerformed(ActionEvent arg0)
			{
				String valSel = (String) staveCombo.getSelectedItem();

				LinkedList<Staves> staveList = staveDetection.getStaveList();
				Staves stave = (Staves) staveList.get(Integer.valueOf(valSel));
				LinkedList<openomr.omr_engine.L0_Segment> symbolPos = stave.getSymbolPos();

				if (symbolCombo.getItemCount() != 0)
				{
					symbolCombo.removeAllItems();
				}

				for (int i = 0; i < symbolPos.size(); i += 1)
					symbolCombo.addItem(String.valueOf(i));
			}

		}

		private class OkAction implements ActionListener
		{

			private JButton previousButton;
			private JButton nextButton;
			private JPanel imagePanel;
			private JScrollPane scrollPanel;
			private int val;
			private int symbol;
			
			public void actionPerformed(ActionEvent arg0)
			{
				String valSel = (String) staveCombo.getSelectedItem();
				String symbolSel = (String) symbolCombo.getSelectedItem();
				val = Integer.valueOf(valSel);
				symbol = Integer.valueOf(symbolSel);
				
				imagePanel = new JPanel();
				doPanel();
				scrollPanel = new JScrollPane(imagePanel);
				jTabbedPane.addTab("NoteDetection Graph", scrollPanel);
			}
			
			private void doPanel()
			{
				LinkedList<Staves> staveList = staveDetection.getStaveList();
				Staves stave = (Staves) staveList.get(val);
				LinkedList<openomr.omr_engine.L0_Segment> symbolPos = stave.getSymbolPos();

				if (!symbolPos.get(symbol).hasNote)
					System.out.println("DOES NOT HAVE NOTE");
				
				//NoteHeadDetection noteHeadDetection = new NoteHeadDetection(buffImage, staveDetection.getStavelineParameters());

				int start = ((openomr.omr_engine.L0_Segment) symbolPos.get(symbol)).start;
				int stop = ((openomr.omr_engine.L0_Segment) symbolPos.get(symbol)).stop;

				//int height = stave.getBottom() - stave.getTop() + 160;
				//LinkedList<Coordinates> posList = noteHeadDetection.findNotes(stave.getStart(), stave.getEnd(), start, stop);

				YProjection yProj = new YProjection(buffImage);
				yProj.calcYProjection(stave.getStart(), stave.getEnd(), start, stop);

				XYChart yProjChart = new XYChart(stave.getEnd() - stave.getStart(), yProj.getYProjection(), "Y Projection");
				ImageIcon yPosImage = new ImageIcon(yProjChart.getChart(400, 400));
				JLabel yPosLabel = new JLabel(yPosImage);

				XProjection xProj = new XProjection(buffImage);
				xProj.calcXProjection(stave.getStart(), stave.getEnd(), start, stop);

				XYChart xProjChart = new XYChart(xProj.getXProjection(), stop - start,  "X Projection");
				ImageIcon xPosImage = new ImageIcon(xProjChart.getChart(400, 400));
				JLabel xPosLabel = new JLabel(xPosImage);
				
				ImageIcon subImage = new ImageIcon(recognizedImage.getSubimage(start - 10, stave.getStart(), stop - start + 20, stave.getEnd()-stave.getStart()));
				JLabel subImageLabel = new JLabel(subImage);

				Box hbox = Box.createHorizontalBox();
				hbox.add(yPosLabel);
				hbox.add(Box.createHorizontalStrut(50));
				hbox.add(xPosLabel);

				Box hbox2 = Box.createHorizontalBox();
				hbox2.add(subImageLabel);

				previousButton = new JButton("previous");
				previousButton.addActionListener(new PreviousAction());
				nextButton = new JButton("next");
				nextButton.addActionListener(new NextAction());

				Box hbox3 = Box.createHorizontalBox();
				hbox3.add(previousButton);
				hbox3.add(nextButton);

				Box vbox = Box.createVerticalBox();
				vbox.add(hbox);
				vbox.add(Box.createVerticalStrut(20));
				vbox.add(hbox2);
				vbox.add(Box.createVerticalStrut(20));
				vbox.add(hbox3);

				imagePanel.add(vbox);
				imagePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
			}

			private class PreviousAction implements ActionListener
			{

				public void actionPerformed(ActionEvent arg0)
				{
					if (symbol == 0)
					{
						if (val !=0)
						{
							val-=1;
							LinkedList<Staves> staveList = staveDetection.getStaveList();
							Staves stave = (Staves) staveList.get(val);
							LinkedList<openomr.omr_engine.L0_Segment> symbolPos = stave.getSymbolPos();
							symbol = symbolPos.size()-1;
						}
					}
					else
						symbol-=1;
					
					imagePanel.removeAll();
					doPanel();
					jTabbedPane.repaint();
				}

			}

			private class NextAction implements ActionListener
			{

				public void actionPerformed(ActionEvent arg0)
				{
					LinkedList<Staves> staveList = staveDetection.getStaveList();
					Staves stave = (Staves) staveList.get(val);
					LinkedList<openomr.omr_engine.L0_Segment> symbolPos = stave.getSymbolPos();
					if (symbol == symbolPos.size()-1)
					{
						if (staveList.size()-1 != val)
						{
							val+=1;
							symbol = 0;
						}
					}
					else
						symbol+=1;
					
					imagePanel.removeAll();
					doPanel();
					jTabbedPane.repaint();
				}

			}

		}
	}
	
	
	private class StaveYProjection implements ActionListener
	{
		private JComboBox staveCombo;
		private JButton okButton;

		public void actionPerformed(ActionEvent event)
		{

			JDialog noteHeadDialog = new JDialog();
			noteHeadDialog.setSize(100, 150);

			JLabel staveLabel = new JLabel("Stave #: ");

			staveCombo = new JComboBox();
			
			//staveCombo.addActionListener(new staveComboListener());

			for (int i = 0; i < staveDetection.getNumStavesFound(); i += 1)
			{
				staveCombo.addItem(String.valueOf(i));
			}
			
			
			//JLabel symbolLabel = new JLabel("Symbol #: ");

			okButton = new JButton("Ok");
			okButton.addActionListener(new OkAction());

			Container contents = noteHeadDialog.getContentPane();

			Box hbox1 = Box.createHorizontalBox();
			hbox1.add(staveLabel);
			hbox1.add(staveCombo);

			Box hbox2 = Box.createHorizontalBox();
			hbox2.add(Box.createHorizontalGlue());

			Box vbox = Box.createVerticalBox();
			vbox.add(Box.createVerticalStrut(7));
			vbox.add(hbox1);
			vbox.add(Box.createVerticalStrut(7));
			vbox.add(hbox2);
			vbox.add(Box.createVerticalStrut(7));
			vbox.add(okButton);
			vbox.add(Box.createVerticalStrut(7));

			contents.add(vbox);
			noteHeadDialog.setVisible(true);
		}

		private class OkAction implements ActionListener
		{

			private JButton previousButton;
			private JButton nextButton;
			private JPanel imagePanel;
			private JScrollPane scrollPanel;
			private int val;
			private int symbol;
			
			public void actionPerformed(ActionEvent arg0)
			{
				String valSel = (String) staveCombo.getSelectedItem();
				val = Integer.valueOf(valSel);

				imagePanel = new JPanel();
				doPanel();
				scrollPanel = new JScrollPane(imagePanel);
				jTabbedPane.addTab("Stave YProjection", scrollPanel);
			}
			
			private void doPanel()
			{
				LinkedList<Staves> staveList = staveDetection.getStaveList();
				Staves stave = (Staves) staveList.get(val);
				
				//System.out.printf("left %d, right %d, top %d, bottom %d", stave.getLeft(), stave.getRight(), stave.getStart(), stave.getEnd());
				
				XProjection xproj = new XProjection(buffImage);
				xproj.calcXProjection(stave.getStart(), stave.getEnd(), stave.getLeft(), stave.getRight());
				
				
				XYChart yProjChart = new XYChart(xproj.getXProjection(), stave.getRight() - stave.getLeft(), "Y Projection");
				ImageIcon yPosImage = new ImageIcon(yProjChart.getChart(stave.getRight() - stave.getLeft(), 400));
				JLabel yPosLabel = new JLabel(yPosImage);

				ImageIcon subImage = new ImageIcon(recognizedImage.getSubimage(stave.getLeft() - 10, stave.getStart(), stave.getRight() - stave.getLeft() + 20, stave.getEnd()-stave.getStart()));
				JLabel subImageLabel = new JLabel(subImage);

				Box hbox = Box.createHorizontalBox();
				hbox.add(yPosLabel);

				Box hbox2 = Box.createHorizontalBox();
				hbox2.add(Box.createHorizontalStrut(70));
				hbox2.add(subImageLabel);

				previousButton = new JButton("previous");
				previousButton.addActionListener(new PreviousAction());
				nextButton = new JButton("next");
				nextButton.addActionListener(new NextAction());

				Box hbox3 = Box.createHorizontalBox();
				hbox3.add(previousButton);
				hbox3.add(nextButton);

				Box vbox = Box.createVerticalBox();
				vbox.add(hbox);
				vbox.add(Box.createVerticalStrut(20));
				vbox.add(hbox2);
				vbox.add(Box.createVerticalStrut(20));
				vbox.add(hbox3);

				imagePanel.add(vbox);
				imagePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
			}

			private class PreviousAction implements ActionListener
			{

				public void actionPerformed(ActionEvent arg0)
				{
					if (symbol == 0)
					{
						if (val !=0)
						{
							val-=1;
							LinkedList<Staves> staveList = staveDetection.getStaveList();
							Staves stave = (Staves) staveList.get(val);
							LinkedList<openomr.omr_engine.L0_Segment> symbolPos = stave.getSymbolPos();
							symbol = symbolPos.size()-1;
						}
					}
					else
						symbol-=1;
					
					imagePanel.removeAll();
					doPanel();
					jTabbedPane.repaint();
				}

			}

			private class NextAction implements ActionListener
			{

				public void actionPerformed(ActionEvent arg0)
				{
					LinkedList<Staves> staveList = staveDetection.getStaveList();
					Staves stave = (Staves) staveList.get(val);
					LinkedList<openomr.omr_engine.L0_Segment> symbolPos = stave.getSymbolPos();
					if (symbol == symbolPos.size()-1)
					{
						if (staveList.size()-1 != val)
						{
							val+=1;
							symbol = 0;
						}
					}
					else
						symbol+=1;
					
					imagePanel.removeAll();
					doPanel();
					jTabbedPane.repaint();
				}

			}

		}
	}
	
	private class ImageViewerListener implements ActionListener
	{

		public void actionPerformed(ActionEvent event)
		{
			JPanel buttonCorner = new JPanel();

			ImageIcon tempImage = new ImageIcon(recognizedImage);
			ScrollableJLabel picture = new ScrollableJLabel(tempImage, (int) (Toolkit.getDefaultToolkit().getScreenResolution() / 2.54));
			JScrollPane pictureScrollPane = new JScrollPane(picture);
			pictureScrollPane.setPreferredSize(new Dimension(buffImage.getWidth(), buffImage.getHeight()));
			pictureScrollPane.setViewportBorder(BorderFactory.createLineBorder(Color.black));

			pictureScrollPane.setCorner(JScrollPane.UPPER_LEFT_CORNER, buttonCorner);

			jTabbedPane.addTab("Recognized Image", pictureScrollPane);
		}

	}

	private class FileOpenListener implements ActionListener
	{
		public void actionPerformed(ActionEvent event)
		{
			FileOpenAction();
		}
	}

	public void FileOpenAction()
	{
		chooser.setCurrentDirectory(new File("." + PATH_SEPERATOR + "testImages"));
		ExtensionFileFilter filter = new ExtensionFileFilter();
		filter.addExtension("jpg");
		filter.addExtension("jpeg");
		filter.addExtension("gif");
		filter.addExtension("bmp");
		filter.addExtension("png");

		chooser.setFileFilter(filter);

		int result = chooser.showOpenDialog(GUI.this);
		if (result == JFileChooser.APPROVE_OPTION)
		{
			String fname = chooser.getSelectedFile().getPath();
			fNamePath = chooser.getSelectedFile().getParent();
			fileName = chooser.getSelectedFile().getName();
			try
			{
				BufferedImage tempImage = ImageIO.read(new File(fname));
				
				buffImage = tempImage;

				int scale = 4;
				JLabel dispImage = new JLabel(new ImageIcon(buffImage.getScaledInstance(buffImage.getWidth() / scale, buffImage.getHeight() / scale, Image.SCALE_FAST)));
				getContentPane().add(jTabbedPane);
				jTabbedPane.addTab("Original Score", dispImage);


				// Make image black and white
				//DoBlackandWhite doBW = new DoBlackandWhite(buffImage);
				//doBW.doBW();

				staveParameters = new StaveParameters(buffImage);
				staveParameters.calcParameters();
				
				System.out.printf("n1=%d, n2=%d, d1=%d, d2=%d\n", staveParameters.getN1(), staveParameters.getN2(), staveParameters.getD1(), staveParameters.getD2());
				
				//Enable some buttons
				toolBar.setFFTEnabled(true);
				toolBar.setReconitionEnabled(true);
				toolBar.setOpenEnabled(false);
				
				//FFT Items
				dofftItem.setEnabled(true);
				
				staveRecogItem.setEnabled(true);
				
				closeItem.setEnabled(true);
				fileOpen.setEnabled(false);
				
				//readStaveInfo();

				//System.out.println("Stave info -- N1=" + staveParameters.getN1() + " N2=" + staveParameters.getN2() + " D1=" + staveParameters.getD1() + " D2=" + staveParameters.getD2());

			} catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}

	public String getFileName()
	{
		int stop = 0;
		int len = fileName.length();

		for (int i = 0; i < len; i += 1)
		{
			if (fileName.charAt(i) == '.')
			{
				stop = i;
				break;
			}
		}

		return new String(fileName.toCharArray(), 0, stop);
	}

	public void saveImage(BufferedImage buffImage, String name) throws IOException
	{
		File outFFT = new File(name + ".png");
		ImageIO.write(buffImage, "png", outFFT);
	}

	public OMREngineJPanel getStaveDetectionPanel()
	{
		return staveDetectionPanel;
	}

	public void setFFTImage(BufferedImage fftImage)
	{
		this.fftImage = fftImage;
	}

	public void setStaveDetectionPanel(OMREngineJPanel staveDetectionPanel)
	{
		this.staveDetectionPanel = staveDetectionPanel;
	}

	public void setImage(BufferedImage buffImage)
	{
		this.buffImage = buffImage;
	}

	public BufferedImage getImage()
	{
		return buffImage;
	}

	public void setStaveDetection(StaveDetection staveDetection)
	{
		this.staveDetection = staveDetection;
	}

	public StaveDetection getStaveDetection()
	{
		return staveDetection;
	}

	public void setFFTView(boolean val)
	{
		fftItem.setEnabled(val);
	}

	public void setImageRecognisedOptions(boolean val)
	{
		recognizeScoreItem.setEnabled(val);
		yprojGraphItem.setEnabled(val);
		noteHeadItem.setEnabled(val);
		symbolItem.setEnabled(val);
		staveProjItem.setEnabled(val);
		toolBar.setPlayEnbabled(true);
	}

	public void setBoundaryImage(BufferedImage dupImage)
	{
		this.recognizedImage = dupImage;
	}

	public StaveParameters getStaveLineParameters()
	{
		return staveParameters;
	}

	/*public void saveStaveInfo()
	{
		DataOutputStream staveOutFile = null;
		try
		{
			staveOutFile = new DataOutputStream(new FileOutputStream(fNamePath + PATH_SEPERATOR + getFileName() + ".stave"));
		} catch (FileNotFoundException e)
		{
			System.out.println("Could not open output file: " + fNamePath + PATH_SEPERATOR + getFileName() + ".stave");
		}
		try
		{
			staveOutFile.writeDouble(staveDetection.getStaveThreshold());
			staveOutFile.writeDouble(staveDetection.getPeakThreshold());

			staveOutFile.writeInt(yProj.getStartH());
			staveOutFile.writeInt(yProj.getEndH());
			staveOutFile.writeInt(yProj.getStartW());
			staveOutFile.writeInt(yProj.getEndW());

			staveOutFile.writeInt(staveDetection.getNumStavesFound());
			Iterator it = staveDetection.getStaveInfo();
			while (true)
			{
				if (!it.hasNext())
					break;
				Staves stave = (Staves) it.next();
				stave.saveStaveInfo(staveOutFile);
			}

			staveOutFile.close();
		} catch (IOException e)
		{
			System.out.println("Could not open output file: " + fNamePath + PATH_SEPERATOR + getFileName() + ".stave");
			e.printStackTrace();
		}
	}*/

	public void setYproj(YProjection yProj)
	{
		this.yProj = yProj;
	}

	public YProjection getYProj()
	{
		return yProj;
	}

	/*
	public void readStaveInfo() throws IOException
	{
		DataInputStream staveInfile = null;

		try
		{
			staveInfile = new DataInputStream(new FileInputStream(new File(fNamePath + PATH_SEPERATOR + getFileName() + ".stave")));
			yProj = new YProjection(buffImage);

			double staveThreshold = staveInfile.readDouble();
			double peakThreshold = staveInfile.readDouble();
			yProj.calcYProjection(staveInfile.readInt(), staveInfile.readInt(), staveInfile.readInt(), staveInfile.readInt());

			staveDetection = new StaveDetection(yProj, staveParameters);

			staveDetection.setStaveThreshold(staveThreshold);
			staveDetection.setPeakThreshold(peakThreshold);

			int numStaves = staveInfile.readInt();
			staveDetection.setNumStave(numStaves);

			LinkedList<Staves> staves = staveDetection.getStaveList();
			for (int i = 0; i < numStaves; i += 1)
			{
				Staves stave = new Staves(i);
				stave.setStaveNumber(staveInfile.readInt());
				for (int j = 0; j < 5; j += 1)
				{
					StavePeaks stavePeak = new StavePeaks(staveInfile.readInt(), staveInfile.readInt());
					int stART = staveInfile.readInt();
					stavePeak.setStartEnd(stART, staveInfile.readInt());
					stave.addStaveline(j, stavePeak);
					staves.add(stave);
				}

			}

			// staveDetectionPanel.loadValues(staveDetection);
		} catch (IOException e)
		{
			// Don't care, file ".stave" does not exist.
		}

	}
	*/
	
	public void setToolBar(ToolBar toolBar)
	{
		this.toolBar = toolBar;
	}

	public ToolBar getToolbar()
	{
		return toolBar;
	}
	
	public void setNeuralNetwork(NeuralNet neuralNetwork)
	{
		GUI.neuralNetwork = neuralNetwork;
	}
	
	public static NeuralNet getNeuralNetwork()
	{
		return neuralNetwork;
	}

	public static ANNInterrogator getANNInterrogator()
	{
		return neuralNetInterrogator;
	}
	
	public void setANNInterrogator(ANNInterrogator neuralNetInterrogator)
	{
		GUI.neuralNetInterrogator = neuralNetInterrogator;
	}
	
	
	
	public void saveFFT(boolean val)
	{
		fftSave.setEnabled(val);
	}
	
}
