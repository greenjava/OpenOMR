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


package openomr.omr_engine;

public class L2_Segment
{
	private int yPosition; //symbol position along y-axis
	private String symbolName;	//Symbol name as classified by ANN
	private double accuracy;	// % confidence that ANN gave
	
	public L2_Segment(int yPosition, String symbolName, double accuracy)
	{
		this.yPosition = yPosition;
		this.symbolName = symbolName;
		this.accuracy = accuracy;
	}
	
	public void printInfo()
	{
		System.out.printf("Symbol: %s, accuracy: %f\n", symbolName, accuracy);
	}
	
	public int getyPosition()
	{
		return yPosition;
	}
	
	public String getSymbolName()
	{
		return symbolName;
	}
	
	public double getAccuracy()
	{
		return accuracy;
	}
}
