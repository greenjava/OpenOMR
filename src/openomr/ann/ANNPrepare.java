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

package openomr.ann;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.*;
import java.io.*;
import java.util.Vector;

import javax.imageio.ImageIO;


/**
 * This class uses the BackProp class to train a neural network of musical symbols.
 * <br>A directory path is provided as a constructor to this class and all sub-directories
 * <br> in that directory are subsequently searched for ".png" files. Those files are then
 * <br> read and scaled as 8x16 (8 pixels wide, 16 pixels high) images and used to train
 * <br> the neural network.
 * @author Arnaud Desaedeleer
 */

public class ANNPrepare
{
	private static final int scaleWidth = 8;
	private static final int scaleHeight = 16;
	private static final int totalPixels = scaleHeight * scaleWidth;   //8 * 16 = 128
	
	private int symbolsUsed;	//# symbols we are using
	
	private Vector symbolCollection;
	
	private int trainingSymbolsFound;
	private int validationSymbolsFound;
	
	private int symbolCollectionCount[];
	private String path;
	private String IMAGE_EXTENSION = ".png";
	
	private double[][] desiredTrainingData;
	private double[][] inputTrainingData;
	private double inputValidationData[][];
	private double desiredValidationData[][];
	private ANNTrainer ann;
	
	
	/**
	 * 
	 * @param dirPath The path to the directory containing all sub-directories with .png files in them
	 */
	@SuppressWarnings("unchecked")
	public ANNPrepare(String dirPath)
	{
		loadSymbols(dirPath);
		
		//# of symbols found in both training and validation directories
		trainingSymbolsFound = countFiles("training");
		validationSymbolsFound = countFiles("validation");
		
		inputTrainingData = new double[trainingSymbolsFound][totalPixels];
		desiredTrainingData = new double[trainingSymbolsFound][symbolsUsed];
		
		inputValidationData = new double[validationSymbolsFound][totalPixels];
		desiredValidationData = new double[validationSymbolsFound][symbolsUsed];
		
		symbolCollectionCount = new int[symbolsUsed];

		for (int i = 0; i < symbolsUsed; i += 1)
			symbolCollectionCount[i] = 0;
	}
	
	public void loadSymbols(String dirPath)
	{
		path = dirPath;

		//Load symbol names from input file
		symbolsUsed = 0;
		symbolCollection = new Vector();
		try
		{
			BufferedReader dataIn = new BufferedReader(new InputStreamReader(new FileInputStream(new File(path+"list.txt"))));
			while (true)
			{
				try
				{
					String temp = dataIn.readLine();
					if (temp == null)
						break;
					symbolCollection.add(temp);
					symbolsUsed+=1;
				} catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		} catch (FileNotFoundException e)
		{
			System.out.println("Symbol file not found!");
			e.printStackTrace();
		}
	}
	
	public ANNTrainer prepareNetwork()
	{
		int trainingSymCount = prepareInputData("training", inputTrainingData, desiredTrainingData);
		int validationSymCount = prepareInputData("validation", inputValidationData, desiredValidationData);
		
		ann = new ANNTrainer(inputTrainingData, desiredTrainingData, inputValidationData, desiredValidationData, trainingSymCount, validationSymCount, totalPixels, symbolsUsed);
		return ann;
	}
	
	/**
	 * Prepare data for neural network. This method must be invoked before the trainNetwork()
	 * method is invoked
	 */
	private int prepareInputData(String dir, double inputData[][], double desiredData[][])
	{
		//System.out.println("Path:" + dir);
		int symbolCount=0;
		File fileArray[] = (new File(path+openomr.gui.GUI.PATH_SEPERATOR+dir)).listFiles();
		for (int i = 0; i < fileArray.length; i += 1)
		{
			if (fileArray[i].isDirectory() == true)
			{
				int localSymbolCount = 0;
				// process each directory
				String currDir = fileArray[i].getName();
				File imageFileArray[] = fileArray[i].listFiles();
				for (int j = 0; j < imageFileArray.length; j += 1)
				{
					if (imageFileArray[j].getName().endsWith(IMAGE_EXTENSION))  //Accept only .png files
					{
						String fName = imageFileArray[j].getName();
						for (int k = 0; k < symbolsUsed; k += 1)
						{
							if (fName.startsWith((String)symbolCollection.get(k)))
							{
								int len = ((String)symbolCollection.get(k)).length();
								if (fName.charAt(len) >= '0' && fName.charAt(len) <= '9')
								{
																		
									//Get image data and fill "inputs" array
									double data[] = getImageData(path+dir+openomr.gui.GUI.PATH_SEPERATOR + currDir + openomr.gui.GUI.PATH_SEPERATOR + fName);
									for (int n = 0; n < totalPixels; n += 1)
										inputData[symbolCount][n] = data[n];
									
									//prepare target array
									for (int n=0; n<symbolsUsed; n+=1)
									{
										if (n==k)
											desiredData[symbolCount][n] = 1;
										else
											desiredData[symbolCount][n] = 0;
									}
									
									symbolCount += 1;
									localSymbolCount += 1;
								}
							}
						}
					}
				}
			}
		}
		return symbolCount;
	}
		
	/*
	public void testFile(String name)
	{
		
		double data[] = getImageData(name);
		double newData[][] = new double[1][128];
		newData[0] = data;
		
		ann.interogateNN(newData);
	}
	*/
	
	private int countFiles(String dir)
	{
		File fileArray[] = (new File(path+openomr.gui.GUI.PATH_SEPERATOR+dir)).listFiles();
		int fileCount = 0;
		
		for (int i = 0; i < fileArray.length; i += 1)
		{
			if (fileArray[i].isDirectory() == true)
			{
				//String currDir = fileArray[i].getName();
				File imageFileArray[] = fileArray[i].listFiles();
				for (int j = 0; j < imageFileArray.length; j += 1)
				{
					if (imageFileArray[j].getName().endsWith(".png"))
					{
						String fName = imageFileArray[j].getName();
						for (int k = 0; k < symbolsUsed; k += 1)
						{
							if (fName.startsWith((String)symbolCollection.get(k)))
							{
								int len = ((String)symbolCollection.get(k)).length();
								if (fName.charAt(len) >= '0' && fName.charAt(len) <= '9')
								{
									fileCount += 1;
								}
							}
						}
					}
				}
			}
		}
		return fileCount;
	}
	
	public static double[] prepareImage(BufferedImage buffImage)
	{
				
		double inputs[] = new double[totalPixels];
		Image scaledInstance = buffImage.getScaledInstance(scaleWidth, scaleHeight, Image.SCALE_DEFAULT);
		PixelGrabber pixGrabber = new PixelGrabber(scaledInstance, 0, 0, scaleWidth, scaleHeight, true);
		try
		{
			pixGrabber.grabPixels();
			int pixArray[];
			pixArray = (int[]) pixGrabber.getPixels();
			for (int x = 0; x < totalPixels; x += 1)
			{
				Color col = new Color(pixArray[x]);
				float[] hsb = Color.RGBtoHSB(col.getRed(), col.getGreen(), col.getBlue(), null);
				inputs[x] = hsb[2]; //get luminosity
			}
		} catch (InterruptedException e)
		{
			System.out.println("Error grabbing pixel");
			e.printStackTrace();
		}
		
		return inputs;
	}
	
	public static double[] getImageData(String fName)
	{
		BufferedImage buffImage = null;
		try
		{
			buffImage = ImageIO.read(new File(fName));
		} catch (IOException e1)
		{
			System.out.println("Could not open file: " + fName);
			e1.printStackTrace();
		}
		
		double inputs[] = new double[totalPixels];
		
		Image scaledInstance = buffImage.getScaledInstance(scaleWidth, scaleHeight, Image.SCALE_DEFAULT);
		PixelGrabber pixGrabber = new PixelGrabber(scaledInstance, 0, 0, scaleWidth, scaleHeight, true);
		try
		{
			pixGrabber.grabPixels();
			int pixArray[];
			pixArray = (int[]) pixGrabber.getPixels();
			for (int x = 0; x < totalPixels; x += 1)
			{
				Color col = new Color(pixArray[x]);
				float[] hsb = Color.RGBtoHSB(col.getRed(), col.getGreen(), col.getBlue(), null);
				inputs[x] = hsb[2]; //get luminosity
			}
		} catch (InterruptedException e)
		{
			System.out.println("Error grabbing pixel");
			e.printStackTrace();
		}
				
		return inputs;
	}
}
