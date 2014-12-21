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

package openomr.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;

import openomr.fft.FFT;
import openomr.imageprocessing.RotateImage;


class FFTJPanel extends JPanel
{

	private static final long serialVersionUID = -3322528190777266489L;
	private JButton doFFT;
	private BufferedImage buffImage;
	private JTextField fftSize;
	private GUI gui;
	JDialog jDialog;

	public FFTJPanel(GUI gui, JDialog jDialog)
	{
		super();
		this.gui = gui;
		this.jDialog = jDialog;

		setLayout(new BorderLayout());

		Border etched = BorderFactory.createEtchedBorder();
		Border titled = BorderFactory.createTitledBorder(etched, " FFT ");
		setBorder(titled);

		fftSize = new JTextField("512", 4);
		JLabel fftSizeLabel = new JLabel("FFT Window Size:");

		doFFT = new JButton("Do FFT");
		doFFT.addActionListener(new FFTPanelActionListener());

		Box hbox2 = Box.createHorizontalBox();
		hbox2.add(fftSizeLabel);
		hbox2.add(fftSize);
		Box hbox3 = Box.createHorizontalBox();

		Box hbox4 = Box.createHorizontalBox();
		hbox4.add(doFFT);
		hbox4.add(Box.createHorizontalStrut(20));

		Box vbox1 = Box.createVerticalBox();
		vbox1.add(hbox2);
		vbox1.add(Box.createVerticalStrut(7));
		vbox1.add(hbox3);
		vbox1.add(Box.createVerticalStrut(7));
		vbox1.add(hbox4);

		add(vbox1, BorderLayout.NORTH);
	}

	public void setButtonActive(Boolean val)
	{
		doFFT.setEnabled(val);
		repaint();
	}

	private class FFTPanelActionListener implements ActionListener
	{
		private FFT fft;

		public void actionPerformed(ActionEvent arg0)
		{
			buffImage = gui.getImage();
			fft = new FFT(buffImage, Integer.valueOf(fftSize.getText()));
			fft.doFFT();
			double rotAngle = fft.getRotationAngle();
			System.out.println("Rot angle: " + (rotAngle*180/3.14));
			gui.setFFTView(true);
			gui.saveFFT(true);

			RotateImage rotImage = new RotateImage(buffImage, fft.getRotationAngle());
			buffImage = rotImage.tilt();
			
			//replace old image with new rotated image
			gui.setImage(buffImage);
			
			gui.setFFTImage(fft.getFFTImage());
			
			jDialog.setVisible(false);
		}

	}
}
