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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.util.Vector;

import org.joone.engine.Layer;
import org.joone.io.MemoryInputSynapse;
import org.joone.io.MemoryOutputSynapse;
import org.joone.net.NeuralNet;

public class ANNInterrogator
{
	private NeuralNet nnet;
	private int numOutputs=15;
	private int imageSize=128;
	private String neuralNetName = "NeuralNetwork.ann";
	private String symbolFileName = "list.txt";
	private String PATH_SEPERATOR = System.getProperty("file.separator");
	private String directory;
	private Vector<String> symbolCollection;
	
	public ANNInterrogator()
	{
		try
		{
			directory = System.getProperty("user.dir") + PATH_SEPERATOR + "neuralnetwork";
			FileInputStream stream = new FileInputStream(directory + PATH_SEPERATOR + neuralNetName);
			//System.out.println(directory + PATH_SEPERATOR + neuralNetName);
			ObjectInputStream objIn = new ObjectInputStream(stream);

			nnet = (NeuralNet)objIn.readObject();
			//numOutputs = (int)objIn.readInt();
			//imageSize = (int)objIn.readInt();

			loadSymbols(directory);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
	}
	
	public ANNInterrogator(NeuralNet nnet)
	{
		this.nnet = nnet;
		loadSymbols(directory);
	}
	
	public SymbolConfidence interogateNN(double data[][])
	{		
		Layer input = nnet.getInputLayer();
		input.removeAllInputs();
		MemoryInputSynapse memInp = new MemoryInputSynapse();
		memInp.setFirstRow(1);
		memInp.setAdvancedColumnSelector("1-"  + String.valueOf(imageSize));
		input.addInputSynapse(memInp);
		memInp.setInputArray(data);
		
		Layer output = nnet.getOutputLayer();
		output.removeAllOutputs();

		MemoryOutputSynapse memOut = new MemoryOutputSynapse();
		output.addOutputSynapse(memOut);

		nnet.getMonitor().setTotCicles(1);
		nnet.getMonitor().setTrainingPatterns(1);
		nnet.getMonitor().setLearning(false);
		nnet.start();
		nnet.getMonitor().Go();

		double[] pattern = memOut.getNextPattern();
		
		int pos=0;
		double max = pattern[0];
		for (int i=1; i <  numOutputs; ++i) 
		{
			if (pattern[i] > max)
			{
				pos = i;
				max = pattern[i];
			}
		}
		
		SymbolConfidence symbolConf = new SymbolConfidence(symbolCollection.get(pos), pattern[pos]);
		nnet.stop();
		return symbolConf;
	}
	
	
	@SuppressWarnings("unchecked")
	private void loadSymbols(String dir)
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
	}
}
