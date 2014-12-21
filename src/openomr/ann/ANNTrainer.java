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

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;

import openomr.gui.TrainANNJPanel;

import org.joone.engine.FullSynapse;
import org.joone.engine.Layer;
import org.joone.engine.LinearLayer;
import org.joone.engine.Monitor;
import org.joone.engine.NeuralNetEvent;
import org.joone.engine.NeuralNetListener;
import org.joone.engine.SigmoidLayer;
import org.joone.engine.learning.TeachingSynapse;
import org.joone.io.FileOutputSynapse;
import org.joone.io.MemoryInputSynapse;
import org.joone.io.MemoryOutputSynapse;
import org.joone.io.StreamInputSynapse;
import org.joone.net.NeuralNet;
import org.joone.net.NeuralNetValidator;
import org.joone.net.NeuralValidationEvent;
import org.joone.net.NeuralValidationListener;
import org.joone.util.LearningSwitch;


public class ANNTrainer implements NeuralNetListener, NeuralValidationListener
{
	private Monitor monitor;
	private TeachingSynapse trainer;
	
	private double inputTrainingData[][];
	private double desiredTrainingData[][];
	private double inputValidationData[][];
	private double desiredValidationData[][];
	
	private NeuralNet nnet;
	private int trainingPatterns;
	private int validationPatterns;
	private LinearLayer input;
	private SigmoidLayer hidden;
	private SigmoidLayer output;
	private int imageSize;
	private int numOutputs;
	private double lastRMSE;
	private TrainANNJPanel gui;

	public ANNTrainer(double inputTrainingData[][], double desiredTrainingData[][], double inputValidationData[][], double desiredValidationData[][], int trainingPatterns, int validationPatterns, int imageSize, int numOutputs)
	{
		this.inputTrainingData = inputTrainingData;
		this.desiredTrainingData = desiredTrainingData;
		
		this.inputValidationData = inputValidationData;
		this.desiredValidationData = desiredValidationData;
		
		this.trainingPatterns = trainingPatterns;
		this.validationPatterns = validationPatterns;
		this.imageSize = imageSize;
		this.numOutputs = numOutputs;
	}
	
	public void createNeuralNet(TrainANNJPanel gui)
	{
		this.gui = gui;
		lastRMSE = 999;
		
		/* The 3 Layers */
		input = new LinearLayer();
		hidden = new SigmoidLayer();
		output = new SigmoidLayer();
		input.setRows(imageSize);
		hidden.setRows(40);
		output.setRows(numOutputs);
		
		//System.out.println("inputs: " + imageSize + "  numoutputs: " + numOutputs);
		
		/* The Synapses */
		FullSynapse synapse_IH = new FullSynapse(); /* Input -> Hidden conn. */
		FullSynapse synapse_HO = new FullSynapse(); /* Hidden -> Output conn. */
		input.addOutputSynapse(synapse_IH);
		hidden.addInputSynapse(synapse_IH);
		hidden.addOutputSynapse(synapse_HO);
		output.addInputSynapse(synapse_HO);
		
		
		/* The I/O components */
		
		MemoryInputSynapse memInput = new MemoryInputSynapse();
		memInput.setInputArray(inputTrainingData);
		memInput.setFirstRow(1);
		memInput.setAdvancedColumnSelector("1-" + String.valueOf(imageSize)); //"1-128"
		
		MemoryInputSynapse memValInput = new MemoryInputSynapse();
		memValInput.setInputArray(inputValidationData);
		memValInput.setFirstRow(1);
		memValInput.setAdvancedColumnSelector("1-" + String.valueOf(imageSize));
		
		LearningSwitch Ilsw = this.createSwitch(memInput, memValInput);
		input.addInputSynapse(Ilsw);
		
		/* The Trainer and its desired file */
		//desired training data & desired validation data
		
		MemoryInputSynapse memResult = new MemoryInputSynapse();
		memResult.setInputArray(desiredTrainingData);
		memResult.setAdvancedColumnSelector("1-" + String.valueOf(numOutputs));	// # of outputs we have
		
		MemoryInputSynapse memValResult = new MemoryInputSynapse();
		memValResult.setInputArray(desiredValidationData);
		memValResult.setAdvancedColumnSelector("1-" + String.valueOf(numOutputs));
		
		LearningSwitch Dlsw = this.createSwitch(memResult, memValResult);
		trainer = new TeachingSynapse();
		trainer.setDesired(Dlsw);		
		output.addOutputSynapse(trainer);

		nnet = new NeuralNet();
		nnet.addLayer(input, NeuralNet.INPUT_LAYER);
		nnet.addLayer(hidden, NeuralNet.HIDDEN_LAYER);
		nnet.addLayer(output, NeuralNet.OUTPUT_LAYER);
		nnet.setTeacher(trainer);

	}
	
	public void trainNeuralNet(boolean val)
	{
		if (val)
		{
			monitor = nnet.getMonitor();
			monitor.setLearningRate(0.6);
			monitor.setMomentum(0.3);
			
			//System.out.println("trainingPatterns: " + trainingPatterns + "  validationPatterns: " + validationPatterns);
			
			monitor.setTrainingPatterns(trainingPatterns); /* # of rows in the input file */
			monitor.setValidationPatterns(validationPatterns);
			monitor.setTotCicles(10000); /* How many times the net must be trained*/
			monitor.setLearning(true); /* The net must be trained */
			monitor.addNeuralNetListener(this);
	
			nnet.start();
			nnet.getMonitor().Go();
			System.out.println("Done");
		}
		else
			nnet.stop();
	}

	public NeuralNet getNeuralNet()
	{
		return nnet;
	}
	
	public void interogateNN(double data[][])
	{
		//System.out.println("About to interogate NN");
		//double inputArray[][] = {{0, 1}};
		
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
		System.out.print("*** NeuralNet Iterogation ***");
		for (int i=0; i <  numOutputs; ++i) 
		{
			System.out.println(pattern[i] + " ");
		}
		nnet.stop();
		System.out.println("Finished");
	}
	
	
	public void saveNeuralNet(String fileName)
	{
		try
		{
			FileOutputStream stream = new FileOutputStream(fileName);
			ObjectOutputStream objOut = new ObjectOutputStream(stream);
			objOut.writeObject(nnet);
			//objOut.write(numOutputs);
			//objOut.write(imageSize);
			objOut.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	
	public void netStarted(NeuralNetEvent arg0)
	{
		// TODO Auto-generated method stub
		
	}

	public void cicleTerminated(NeuralNetEvent e)
	{
		Monitor mon = (Monitor)e.getSource();
		
		int cycle = mon.getCurrentCicle()+1;
		
		gui.setEpoch(mon.getTotCicles()-cycle);
		
		if (cycle %20 == 0)
		{
			//System.out.println("Epoch #" + (mon.getTotCicles() - cycle));
			//System.out.print(mon.getGlobalError());
			gui.setTrainingRMSE(mon.getGlobalError());
			
			nnet.getMonitor().setExporting(true);
			NeuralNet newNet = nnet.cloneNet();
			nnet.getMonitor().setExporting(false);
			
			newNet.removeAllListeners();
			
			NeuralNetValidator nnv = new NeuralNetValidator(newNet);
			nnv.addValidationListener(this);
			nnv.start();
			
		}
		
	}

	public void netStopped(NeuralNetEvent arg0)
	{
		
	}

	public void errorChanged(NeuralNetEvent e)
	{
		// TODO Auto-generated method stub
		//Monitor mon = (Monitor)e.getSource();
		//System.out.println("New Error: " + mon.getGlobalError());
	}

	public void netStoppedError(NeuralNetEvent arg0, String arg1)
	{
		// TODO Auto-generated method stub
		
	}
	
	
	private LearningSwitch createSwitch(StreamInputSynapse IT, StreamInputSynapse IV)
	{
		LearningSwitch lsw = new LearningSwitch();
		lsw.addTrainingSet(IT);
		lsw.addValidationSet(IV);
		return lsw;
	}

	public void netValidated(NeuralValidationEvent e)
	{
		NeuralNet NN = (NeuralNet)e.getSource();
		if (NN.getMonitor().getGlobalError() < lastRMSE)
			lastRMSE = NN.getMonitor().getGlobalError();
		else
		{
			System.out.println("We are going to stop at: " + (NN.getMonitor().getTotCicles() - NN.getMonitor().getCurrentCicle() + 1));
			NN.getMonitor().Stop();
		}
		//System.out.println(", " + NN.getMonitor().getGlobalError());
		gui.setValidationRMSE(NN.getMonitor().getGlobalError());
	}
	
}
