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

package openomr.fft;

/**
 * The <code> Complex </code> class stores information about complex numbers.
 * <p>
 * 
 * 
 * @author Arnaud Desaedeleer
 * @author Stephen Murrell
 */

public class Complex
{
	private double real[][];
	private double imag[][];

	public Complex(int size)
	{
		real = new double[size][size];
		imag = new double[size][size];
	}

	public void setReal(int x, int y, double real)
	{
		this.real[x][y] = real;
	}

	public void setImag(int x, int y, double imag)
	{
		this.imag[x][y] = imag;
	}

	public double getReal(int x, int y)
	{
		return real[x][y];
	}

	public double getImag(int x, int y)
	{
		return imag[x][y];
	}
}
