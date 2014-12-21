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

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Vector;

public class ANNBatchModeTester
{
	private static final int scaleWidth = 8;
	private static final int scaleHeight = 16;
	private static final int totalPixels = scaleHeight * scaleWidth;   //8 * 16 = 128
	
	private String neuralNetName = "NeuralNetwork.ann";
	private String symbolFileName = "list.txt";
	private String outputFileName = "testing_results.csv";
	private String PATH_SEPERATOR = System.getProperty("file.separator");
	private String directory;
	private Vector<String> symbolCollection;
	private String IMAGE_EXTENSION = ".png";
	private int symbolsUsed;
	private DataOutputStream dataOut;
	
	public ANNBatchModeTester()
	{
		directory = directory = System.getProperty("user.dir") + PATH_SEPERATOR + "neuralnetwork";
		symbolsUsed = loadSymbols(directory);
		
		try
		{
			dataOut = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(System.getProperty("user.dir") + PATH_SEPERATOR + "neuralnetwork" + PATH_SEPERATOR + outputFileName)));
		} 
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		
	}
	
	public int prepareInputData()
	{
		String dir = "testing";
		//System.out.println("Path:" + dir);
		int symbolCount=0;
		File fileArray[] = (new File(directory+PATH_SEPERATOR+dir)).listFiles();
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
									double data[] = ANNPrepare.getImageData(directory+ PATH_SEPERATOR+dir+PATH_SEPERATOR + currDir + openomr.gui.GUI.PATH_SEPERATOR + fName);
									
									double annData[][] = new double[1][128];
									annData[0] = data;
									
									ANNInterrogator annIntero = new ANNInterrogator();
									SymbolConfidence symConf = annIntero.interogateNN(annData);
									
									
									try
									{
										dataOut.writeBytes((String)symbolCollection.get(k)+ ", " + symConf.getName() + ", " + symConf.getRMSE() + "\n");
									} 
									catch (IOException e)
									{
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									
									
									//prepare target array
									/*for (int n=0; n<symbolsUsed; n+=1)
									{
										if (n==k)
											desiredData[symbolCount][n] = 1;
										else
											desiredData[symbolCount][n] = 0;
									}
									*/
									symbolCount += 1;
									localSymbolCount += 1;
								}
							}
						}
					}
				}
			}
		}
		
		try
		{
			dataOut.close();
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return symbolCount;
	}
	
	@SuppressWarnings("unchecked")
	private int loadSymbols(String dir)
	{
		//Load symbol names from input file
		int symbolsUsed = 0;
		symbolCollection = new Vector();
		try
		{
			BufferedReader dataIn = new BufferedReader(new InputStreamReader(new FileInputStream(new File(dir+PATH_SEPERATOR+symbolFileName))));
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
		return symbolsUsed;
	}
}
