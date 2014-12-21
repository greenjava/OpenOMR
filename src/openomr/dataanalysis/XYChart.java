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

import java.awt.image.BufferedImage;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class XYChart
{
	private JFreeChart chart;
	
	public XYChart(int data[], int size, String name)
	{
		XYSeries series = new XYSeries(name);
		for (int i=0; i<size; i+=1)
			series.add(i, data[i]);
		XYDataset xyDataset = new XYSeriesCollection(series);
        
		chart = ChartFactory.createXYAreaChart(name, "width", "# Pixels", xyDataset, PlotOrientation.VERTICAL, true, false, false);
		
	}
	
	public XYChart(int size, int data[], String name)
	{
		XYSeries series = new XYSeries(name);
		for (int i=0; i<size; i+=1)
			series.add(i, data[i]);
		XYDataset xyDataset = new XYSeriesCollection(series);
        
		chart = ChartFactory.createXYAreaChart(name, "width", "# Pixels", xyDataset, PlotOrientation.HORIZONTAL, true, false, false);
		
	}
	
	public BufferedImage getChart(int x, int y)
	{
		return chart.createBufferedImage(x, y);
	}
}
