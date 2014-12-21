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

package openomr.dataanalysis;

import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class GNUPlotGenerator
{
	private DataOutputStream dataFile = null;
	private int data[];
	private int size;
	private String fname;
	
	public GNUPlotGenerator(String fname, int data[], int size)
	{
		this.data = data;
		this.size = size;
		this.fname = fname;
		
		try
		{
			dataFile = new DataOutputStream(new FileOutputStream(fname));
		} 
		catch (FileNotFoundException e)
		{
			System.out.println("Could not create file: " + fname);
			e.printStackTrace();
		}
	}
	
	public void generateDataFile()
	{
		for (int i=0; i< size; i+=1)
		{
			try
			{
				dataFile.writeBytes(String.valueOf(data[i]) + "\\n");
			} 
			catch (IOException e)
			{
				System.out.println("Error writing to file: " + fname);
				e.printStackTrace();
			}
		}
		try
		{
			dataFile.close();
		} 
		catch (IOException e)
		{
			System.out.println("Could not close file: " + fname);
			e.printStackTrace();
		}
	}
}
