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

import java.awt.image.BufferedImage;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ANNFilePrepare
{
	public static void main(String args[])
	{
		try
		{
			DataOutputStream fOut = new DataOutputStream(new FileOutputStream(args[0]));
			System.out.println("There were: " + args.length + " arguments");
			String path = new String("C:\\Documents and Settings\\Arnaud\\My Documents\\workspace\\SheetMusic\\musical_symbols\\clefs\\8x16\\");
			int count=1;
			while (true)
			{
				if (count == args.length)
					break;
				try
				{
					BufferedImage tempImage = ImageIO.read(new File(path + args[count]));
					for (int i=0; i<tempImage.getHeight(); i+=1)
					{
						for (int j=0; j<tempImage.getWidth(); j+=1)
						{
							int pix = tempImage.getRGB(j, i);
							if (pix == -1)
								fOut.writeBytes("0.0;");
							else
								fOut.writeBytes("1.0;");
						}
					}
					fOut.writeBytes("5\r\n");
				} 
				catch (IOException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				count+=1;
			}
			
		} 
		catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
