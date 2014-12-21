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


package openomr.imageprocessing;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

public class PhotoResize
{
	public static void main(String args[])
	{

		try
		{

			String path = new String("C:\\Documents and Settings\\Arnaud\\My Documents\\workspace\\SheetMusic\\clefs\\samples\\"); //musical_symbols\\");
			// System.out.print(path+args[0]);
			int symbolCount = 0;

			DataOutputStream fOut = new DataOutputStream(new FileOutputStream(path + "Test.txt"));
			
			//String symbolCollection[] = { "alto", "bass", "crotchet", "crochetrest", "dot", "flat", "minim", "natural", "semiquaver_line", "semiquaver_rest", "sharp", "slur", "treble" };
			//int count = 13;
			
			String symbolCollection[] = { "alto", "bass", "treble" };
			int count = 3;
			
			int symbolCollectionCount[] = new int[count];
			
			for (int i=0; i<count; i+=1)
			{
				symbolCollectionCount[i] = 1;
			}
			
			File fileArray[] = (new File(path)).listFiles();

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
						if (imageFileArray[j].getName().endsWith(".png"))
						{

							String fName = imageFileArray[j].getName();
							for (int k = 0; k < count; k += 1)
							{
								if (fName.startsWith(symbolCollection[k]))
								{
									int len = symbolCollection[k].length();
									if (fName.charAt(len) >= '0' && fName.charAt(len) <= '9')
									{
										//System.out.println(imageFileArray[j].getName());

										BufferedImage buffImage = ImageIO.read(new File(path + currDir + "\\" + fName));

										int wScale = buffImage.getWidth() / 8;
										int hScale = buffImage.getHeight() / 16;
										if (wScale == 0)
											wScale = 1;
										if (hScale == 0)
											hScale = 1;

										//System.out.println("Width: " + buffImage.getWidth() + "  Height: " + buffImage.getHeight());
										//System.out.println("wScale: " + wScale + "  hScale: " + hScale);
										Image scaledInstance = buffImage.getScaledInstance(8, 16, Image.SCALE_DEFAULT);
										PixelGrabber pixGrabber = new PixelGrabber(scaledInstance, 0, 0, 8, 16, true);
										try
										{
											pixGrabber.grabPixels();
											int pixArray[];
											pixArray = (int[]) pixGrabber.getPixels();

											for (int x = 0; x < 128; x += 1)
											{
												if (pixArray[x] == -1)
													fOut.writeBytes("0.0;");
												else
													fOut.writeBytes("1.0;");
											}
											fOut.writeBytes((k+1) + "\n");
											
											//File outImage = new File(path + currDir + "\\" + "resized_" + fName);
											
											//BufferedImage buffOutImage = new BufferedImage(8, 16, BufferedImage.TYPE_BYTE_GRAY);
											//Graphics g = buffOutImage.getGraphics();
											//g.drawImage(scaledInstance, 0, 0, null);
											//ImageIO.write(buffOutImage, "png", outImage);

										} catch (InterruptedException e)
										{
											e.printStackTrace();
										}

										symbolCount += 1;
										localSymbolCount += 1;
									}
								}
							}
						}
					}
					System.out.println("Directory: " + fileArray[i].getName() + " -- " + localSymbolCount);

				}
			}
			fOut.close();
			System.out.println("Symbols found: " + symbolCount);
		} catch (IOException e)
		{
			System.out.println("Could not open file");
		}
	}
}
