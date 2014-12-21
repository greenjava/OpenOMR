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

import java.awt.image.BufferedImage;
import java.lang.Math;

/**
 * 
 * @author Arnaud Desaedeleer
 * @author Stephen Murrell
 */

public class FFT
{
	private final int BRIGHT_THRESHOLD = 200;
	private final double pi = Math.acos(-1);
	private final double twopi = 2 * pi;
	private int size;
	private BufferedImage fftImage;
	private Complex c1;
	private Coordinates minCord;
	private Coordinates maxCord;

	public FFT(BufferedImage buffImage, int size)
	{
		this.size = size;
		c1 = new Complex(size);

		// init complex
		for (int i = 0; i < size; i += 1)
		{
			for (int j = 0; j < size; j += 1)
			{
				c1.setImag(i, j, 0.0);
				c1.setReal(i, j, 0.0);
			}
		}

		int w = size;
		int h = size;
		if (buffImage.getWidth() < size)
			w = buffImage.getWidth();
		if (buffImage.getHeight() < size)
			h = buffImage.getHeight();

		// Create new buffered image for FFT output
		this.fftImage = new BufferedImage(size / 2, size / 2, BufferedImage.TYPE_INT_RGB);

		for (int i = 0; i < h /* height */; i += 1)
		{
			for (int j = 0; j < w /* width */; j += 1)
			{
				int v = 0;
				try
				{
					v = buffImage.getRGB(j, i);
				} catch (ArrayIndexOutOfBoundsException e)
				{
					System.out.println("Out of bounds -- buffImage.getRGB(x, y, rgb)");
					System.exit(1);
				}
				RGB rgb = new RGB();
				rgb = DecodeColor(v);
				HLS hls = new HLS();
				hls = RGBtoHLS(rgb);
				hls.h = (int) (hls.h * twopi);
				c1.setReal(i, j, hls.l);
				c1.setImag(i, j, 0.0);
			}
		}
	}

	public void doFFT()
	{
		FFT2D(c1, size, size, 1);
		double scale = maxof(c1, size, size);
		do_halfmag(c1, size, size, scale, 0, 0);
	}

	public double getRotationAngle()
	{
		return Math.atan((double) (maxCord.x - minCord.x) / (double) (maxCord.y - minCord.y));
	}

	public BufferedImage getFFTImage()
	{
		return fftImage;
	}

	private void FFT_meth(int dir, double x[], double y[], int n)
	{
		int nn = 1 << n;
		int i2 = nn >> 1;
		for (int i = 0, j = 0; i < nn - 1; i += 1)
		{
			if (i < j)
			{
				double tx = x[i];
				double ty = y[i];
				x[i] = x[j];
				y[i] = y[j];
				x[j] = tx;
				y[j] = ty;
			}
			int k = i2;
			while (k <= j)
			{
				j -= k;
				k >>= 1;
			}
			j += k;
		}
		double c1 = -1.0, c2 = 0.0;
		int l2 = 1;
		for (int l = 0; l < n; l += 1)
		{
			int l1 = l2;
			l2 <<= 1;
			double u1 = 1.0, u2 = 0.0;
			for (int j = 0; j < l1; j += 1)
			{
				for (int i = j; i < nn; i += l2)
				{
					int i1 = i + l1;
					double t1 = u1 * x[i1] - u2 * y[i1];
					double t2 = u1 * y[i1] + u2 * x[i1];
					x[i1] = x[i] - t1;
					y[i1] = y[i] - t2;
					x[i] += t1;
					y[i] += t2;
				}
				double z = u1 * c1 - u2 * c2;
				u2 = u1 * c2 + u2 * c1;
				u1 = z;
			}
			c2 = Math.sqrt((1.0 - c1) / 2.0);
			if (dir == 1)
				c2 = -c2;
			c1 = Math.sqrt((1.0 + c1) / 2.0);
		}
		if (dir == 1)
		{
			for (int i = 0; i < nn; i += 1)
			{
				x[i] /= (double) nn;
				y[i] /= (double) nn;
			}
		}
	}

	private void FFT2D(Complex c, int nx, int ny, int dir)
	{
		int lognx = logbase2(nx), logny = logbase2(ny);
		if (lognx == 0 || logny == 0)
		{
			System.out.printf("invalid matrix size: %d x %d, must be powers of 2\n", nx, ny);
			System.exit(1);
		}
		int size = nx;
		if (ny > size)
			size = ny;
		double real[] = new double[size];
		double imag[] = new double[size];
		if (real == null || imag == null)
		{
			System.out.printf("memory allocation failure\n");
			System.exit(1);
		}
		for (int j = 0; j < ny; j += 1)
		{
			for (int i = 0; i < nx; i += 1)
			{
				real[i] = c.getReal(i, j);
				imag[i] = c.getImag(i, j);
			}
			FFT_meth(dir, real, imag, lognx);
			for (int k = 0; k < nx; k += 1)
			{
				c.setReal(k, j, real[k]);
				c.setImag(k, j, imag[k]);
			}
		}
		for (int i = 0; i < nx; i += 1)
		{
			for (int j = 0; j < ny; j += 1)
			{
				real[j] = c.getReal(i, j);
				imag[j] = c.getImag(i, j);
			}
			FFT_meth(dir, real, imag, logny);
			for (int k = 0; k < ny; k += 1)
			{
				c.setReal(i, k, real[k]);
				c.setImag(i, k, imag[k]);
			}
		}
	}

	private int logbase2(int n)
	{
		int p = 0, v = 1;
		while (true)
		{
			if (v == n)
				return p;
			if (v <= 0)
				return 0;
			v <<= 1;
			p += 1;
		}
	}

	private double mag(Complex c, int i, int j)
	{
		return Math.sqrt(c.getReal(i, j) * c.getReal(i, j) + c.getImag(i, j) * c.getImag(i, j));
	}

	private double maxof(Complex c, int sz1, int sz2)
	{
		double ans = 0.0;
		for (int i = 3; i < sz1 - 3; i += 1)
		{
			for (int j = 3; j < sz2 - 3; j += 1)
			{
				double curr = mag(c, i, j);
				if (curr > ans)
					ans = curr;
			}
		}
		if (ans == 0.0)
			return 1.0;
		return ans;
	}

	private void quarter(Complex c, int start1, int end1, int start2, int end2, double scale, int x, int y)
	{
		for (int i = start1; i < end1; i += 1)
		{
			int hi = i >> 1;
			for (int j = start2; j < end2; j += 1)
			{
				int hj = j >> 1;
				double m = mag(c, i, j) / scale;
				int n = (int) (Math.sqrt(m) * 255.0);
				if (n > BRIGHT_THRESHOLD)
				{
					if (minCord.y > y + hi)
					{
						minCord.set(x + hj, y + hi, n);
					}

					if (maxCord.y < y + hi)
					{
						maxCord.set(x + hj, y + hi, n);
					}

					// set THRESHOLD pixels to red
					// fftImage.setRGB(x + hj, y + hi, 0xFF0000);
				} 
				else
					fftImage.setRGB(x + hj, y + hi, (n << 16) | (n << 8) | (n));
			}
		}
	}

	private void do_halfmag(Complex c, int sz1, int sz2, double scale, int x0, int y0)
	{
		minCord = new Coordinates(size / 4);
		maxCord = new Coordinates(0);
		int half = sz1 / 2;
		quarter(c, half, sz1, 0, half, scale, y0 + size / 4, x0 - size / 4);
		quarter(c, 0, half, 0, half, scale, y0 + size / 4, x0 + size / 4);
		quarter(c, half, sz1, half, sz2, scale, y0 - size / 4, x0 - size / 4);
		quarter(c, 0, half, half, sz2, scale, y0 - size / 4, x0 + size / 4);
	}

	class Coordinates
	{
		public int x, y;
		public int value;

		public Coordinates(int y)
		{
			this.value = 0;
			this.x = 0;
			this.y = y;
		}

		public void set(int x, int y, int value)
		{
			this.value = value;
			this.x = x;
			this.y = y;
		}
	}

	class RGB
	{
		public double r, g, b;

		public RGB()
		{
			r = 0.0;
			b = 0.0;
			b = 0.0;
		}
	}

	class HLS
	{
		double h, l, s;

		public HLS()
		{
			h = 0.0;
			l = 0.0;
			s = 0.0;
		}
	}

	private RGB DecodeColor(int c)
	{
		RGB rgb = new RGB();
		rgb.r = (c & 255) / 255.0;
		rgb.g = ((c >> 8) & 255) / 255.0;
		rgb.b = ((c >> 16) & 255) / 255.0;
		return rgb;
	}

	private HLS RGBtoHLS(RGB rgb)
	{
		HLS hls = new HLS();
		final double deg60 = 1.0 / 6.0;
		double bst = rgb.r;
		if (rgb.g > bst)
			bst = rgb.g;
		if (rgb.b > bst)
			bst = rgb.b;
		double dst = rgb.r;
		if (rgb.g < dst)
			dst = rgb.g;
		if (rgb.b < dst)
			dst = rgb.b;
		double twol = bst + dst;
		hls.l = twol / 2.0;
		double delta = bst - dst;
		if (twol == 0.0 || twol == 2.0)
			hls.s = 0.0;
		else if (twol <= 1.0)
			hls.s = delta / twol;
		else
			hls.s = delta / (2.0 - twol);
		double h = 0;
		if (delta == 0.0)
			h = 0.0;
		else if (rgb.r >= rgb.g && rgb.r >= rgb.b)
			h = (rgb.g - rgb.b) / delta;
		else if (rgb.g >= rgb.r && rgb.g >= rgb.b)
			h = 2.0 + (rgb.b - rgb.r) / delta;
		else
			h = 4.0 + (rgb.r - rgb.g) / delta;
		if (h < 0.0)
			h += 6.0;
		hls.h = h * deg60;
		return hls;
	}
}
