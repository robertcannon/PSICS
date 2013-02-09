// Adapted from Michael Thomas Flanagan's class FourierTransform.java which was
// based on Numerical Recipes
// see http://www.ee.ucl.ac.uk/~mflanaga/ for the original version


package org.psics.num.math;

import org.psics.be.E;


@SuppressWarnings("all")
public class FourierTransform {

	private Complex[] complexData = null; // array to hold the input data as a
											// set of Complex numbers
	private Complex[] complexCorr = null; // corresponding array to hold the
											// data to be correlated with first
											// data set
	private boolean complexDataSet = false; // if true - the complex data input
											// array has been filled, if false -
											// it has not.
	private int originalDataLength = 0; // original data length value; the
										// working data length may be altered by
										// deletion or padding
	private int fftDataLength = 0; // working data length - usually the
									// smallest power of two that is either
									// equal to originalDataLength or larger
									// than originalDataLength
	private boolean dataAltered = false; // set to true if originalDataLength
											// altered, e.g. by point deletion
											// or padding.

	private double[] fftData = null; // array to hold a data set of complex
										// numbers arranged as alternating
	// real and imaginary parts, e.g. real_0 imag_0, real_1 imag_1, for the fast
	// Fourier Transform method
	private double[] fftCorr = null; // corresponding array to hold the data
										// to be correlated with first data set
	// private double[] fftResp = null; // corresponding array to hold the
										// response to be convolved with first
										// data set
	private boolean fftDataSet = false; // if true - the fftData array has been
										// filled, if false - it has not.

	private double[] fftDataWindow = null; // array holding fftData array
											// elements multiplied by the
											// windowing weights
	private double[] fftCorrWindow = null; // corresponding array to hold the
											// data to be correlated with first
											// data set

	private int windowOption = 0; // Window Option
	// = 0; no windowing applied (default) - equivalent to option = 1
	// = 1; Rectangular (square, box-car)
	// = 2; Bartlett (triangular)
	// = 3; Welch
	// = 4; Hann (Hanning)
	// = 5; Hamming
	// = 6; Kaiser
	// = 7; Gaussian


	private double kaiserAlpha = 2.0D; // Kaiser window constant, alpha
	private double gaussianAlpha = 2.5D; // Gaussian window constant, alpha
	private double[] weights = null; // windowing weights
	// private boolean windowSet = false; // = true when a windowing option has
										// been chosen, otherwise = false
	private boolean windowApplied = false; // = true when data has been
											// multiplied by windowing weights,
											// otherwise = false
	private double sumOfSquaredWeights = 0.0D; // Sum of the windowing weights

	private Complex[] transformedDataComplex = null; // transformed data set
														// of Complex numbers
	private double[] transformedDataFft = null; // transformed data set of
												// double adjacent real and
												// imaginary parts
	// private boolean fftDone = false; // = false - basicFft has not been
	// called
	// = true - basicFft has been called

	private double[][] powerSpectrumEstimate = null; // first row - array to
														// hold frequencies
	// second row - array to hold estimated power density (psd) spectrum
	private boolean powSpecDone = false; // = false - PowerSpectrum has not
											// been called
	// = true - PowerSpectrum has been called
	private int psdNumberOfPoints = 0; // Number of points in the estimated
										// power spectrum

	private int segmentNumber = 1; // Number of segments into which the data
									// has been split
	private int segmentLength = 0; // Number of of data points in a segment
	private boolean overlap = false; // Data segment overlap option
	// = true; overlap by half segment length - smallest spectral variance per
	// data point
	// good where data already recorded and data reduction is after the process
	// = false; no overlap - smallest spectral variance per conputer operation
	// good for real time data collection where data reduction is computer
	// limited
	private boolean segNumSet = false; // true if segment number has been set
	private boolean segLenSet = false; // true of segment length has been set

	private double deltaT = 1.0D; // Sampling period (needed only for true
									// graphical output)
	private boolean deltaTset = false; // true if sampling period has been set

	private double[][] correlationArray = null; // first row - array to hold
												// time lags
	// second row - correlation between fftDataWindow and fftCorrWindow
	private boolean correlateDone = false; // = false - correlation has not
											// been called
	// = true - correlation has been called

	private double[][] timeFrequency = null; // matrix of time against
												// frequency mean square powers
												// from shoert time FT
	// first row = blank cell followed by time vector
	// first column = blank cell followed by frequency vector
	// each cell is then the mean square amplitude at that frequency and time
	private boolean shortTimeDone = false; // = true when short time Fourier
											// Transform has been performed
	private int numShortFreq = 0; // number of frequency points in short time
									// Fourier transform
	private int numShortTimes = 0; // number of time points in short time
									// Fourier transform


	// No initialisation of the data variables
	public FourierTransform() {

	}


	// Enter a data array of real numbers
	public void setData(double[] realData) {
		originalDataLength = realData.length;
		fftDataLength = nextPowerOfTwo(originalDataLength);
		complexData = Complex.oneDarray(fftDataLength);
		for (int i = 0; i < originalDataLength; i++) {
			complexData[i].setReal(realData[i]);
			complexData[i].setImag(0.0D);
		}
		for (int i = originalDataLength; i < fftDataLength; i++) {
			complexData[i].reset(0.0D, 0.0D);
		}
		complexDataSet = true;

		fftData = new double[2 * fftDataLength];
		int j = 0;
		for (int i = 0; i < fftDataLength; i++) {
			fftData[j] = complexData[i].getReal();
			j++;
			fftData[j] = 0.0D;
			j++;
		}
		fftDataSet = true;

		fftDataWindow = new double[2 * fftDataLength];
		weights = new double[fftDataLength];
		sumOfSquaredWeights = windowData(fftData, fftDataWindow, weights);

		transformedDataFft = new double[2 * fftDataLength];
		transformedDataComplex = Complex.oneDarray(fftDataLength);

		if (segNumSet) {
			setSegmentNumber(segmentNumber);
		} else {
			if (segLenSet) {
				setSegmentLength(segmentLength);
			} else {
				segmentLength = fftDataLength;
			}
		}
	}


	// Enter a data array of complex numbers
	public void setData(Complex[] data) {
		originalDataLength = data.length;
		fftDataLength = FourierTransform.nextPowerOfTwo(originalDataLength);
		complexData = Complex.oneDarray(fftDataLength);
		for (int i = 0; i < originalDataLength; i++) {
			complexData[i] = data[i].copy();
		}
		for (int i = originalDataLength; i < fftDataLength; i++)
			complexData[i].reset(0.0D, 0.0D);
		complexDataSet = true;

		fftData = new double[2 * fftDataLength];
		int j = 0;
		for (int i = 0; i < fftDataLength; i++) {
			fftData[j] = complexData[i].getReal();
			j++;
			fftData[j] = complexData[i].getImag();
			j++;
		}
		fftDataSet = true;

		fftDataWindow = new double[2 * fftDataLength];
		weights = new double[fftDataLength];
		sumOfSquaredWeights = windowData(fftData, fftDataWindow, weights);

		transformedDataFft = new double[2 * fftDataLength];
		transformedDataComplex = Complex.oneDarray(fftDataLength);

		if (segNumSet) {
			setSegmentNumber(segmentNumber);
		} else {
			if (segLenSet) {
				setSegmentLength(segmentLength);
			} else {
				segmentLength = fftDataLength;
			}
		}
	}


	// Enter a data array of adjacent alternating real and imaginary parts for
	// fft method, fastFourierTransform
	public void setFftData(double[] fftdata) {
		if (fftdata.length % 2 != 0)
			throw new IllegalArgumentException("data length must be an even number");

		originalDataLength = fftdata.length / 2;
		fftDataLength = FourierTransform.nextPowerOfTwo(originalDataLength);
		fftData = new double[2 * fftDataLength];
		for (int i = 0; i < 2 * originalDataLength; i++)
			fftData[i] = fftdata[i];
		for (int i = 2 * originalDataLength; i < 2 * fftDataLength; i++)
			fftData[i] = 0.0D;
		fftDataSet = true;

		complexData = Complex.oneDarray(fftDataLength);
		int j = -1;
		for (int i = 0; i < fftDataLength; i++) {
			complexData[i].setReal(fftData[++j]);
			complexData[i].setImag(fftData[++j]);
		}
		complexDataSet = true;

		fftDataWindow = new double[2 * fftDataLength];
		weights = new double[fftDataLength];
		sumOfSquaredWeights = windowData(fftData, fftDataWindow, weights);

		transformedDataFft = new double[2 * fftDataLength];
		transformedDataComplex = Complex.oneDarray(fftDataLength);

		if (segNumSet) {
			setSegmentNumber(segmentNumber);
		} else {
			if (segLenSet) {
				setSegmentLength(segmentLength);
			} else {
				segmentLength = fftDataLength;
			}
		}
	}


	// Get the input data array as Complex
	public Complex[] getComplexInputData() {
		if (!complexDataSet) {
			E.warning("complex data set not entered or calculated - null returned");
		}
		return complexData;
	}


	// Get the input data array as adjacent real and imaginary pairs
	public double[] getAlternateInputData() {
		if (!fftDataSet) {
			E.warning("fft data set not entered or calculted - null returned");
		}
		return fftData;
	}


	// Get the windowed input data array as windowed adjacent real and imaginary
	// pairs
	public double[] getAlternateWindowedInputData() {
		if (!fftDataSet) {
			E.warning("fft data set not entered or calculted - null returned");
		}
		if (!fftDataSet) {
			E.warning("fft data set not entered or calculted - null returned");
		}
		if (!windowApplied) {
			E.warning("fft data set has not been multiplied by windowing weights");
		}
		return fftDataWindow;
	}


	public int getOriginalDataLength() {
		return originalDataLength;
	}


	public int getUsedDataLength() {
		return fftDataLength;
	}


	// Set a samplimg period
	public void setDeltaT(double dt) {
		deltaT = dt;
		deltaTset = true;
	}


	// Get the samplimg period
	public double getDeltaT() {
		double ret = 0.0D;
		if (deltaTset) {
			ret = deltaT;
		} else {
			E.warning("detaT has not been set - zero returned");
		}
		return ret;
	}


	// Set a Rectangular window option
	public void setRectangular() {
		windowOption = 1;
		// windowSet = true;
		if (fftDataSet) {
			sumOfSquaredWeights = windowData(fftData, fftDataWindow, weights);
			windowApplied = true;
		}
	}


	// Set a Bartlett window option
	public void setBartlett() {
		windowOption = 2;
		// windowSet = true;
		if (fftDataSet) {
			sumOfSquaredWeights = windowData(fftData, fftDataWindow, weights);
			windowApplied = true;
		}
	}


	// Set a Welch window option
	public void setWelch() {
		windowOption = 3;
		// windowSet = true;
		if (fftDataSet) {
			sumOfSquaredWeights = windowData(fftData, fftDataWindow, weights);
			windowApplied = true;
		}
	}


	// Set a Hann window option
	public void setHann() {
		windowOption = 4;
		// windowSet = true;
		if (fftDataSet) {
			sumOfSquaredWeights = windowData(fftData, fftDataWindow, weights);
			windowApplied = true;
		}
	}


	// Set a Hamming window option
	public void setHamming() {
		windowOption = 5;
		// windowSet = true;
		if (fftDataSet) {
			sumOfSquaredWeights = windowData(fftData, fftDataWindow, weights);
			windowApplied = true;
		}
	}


	// Set a Kaiser window option
	public void setKaiser(double alpha) {
		kaiserAlpha = alpha;
		windowOption = 6;
		// windowSet = true;
		if (fftDataSet) {
			sumOfSquaredWeights = windowData(fftData, fftDataWindow, weights);
			windowApplied = true;
		}
	}


	// Set a Kaiser window option
	// default option for alpha
	public void setKaiser() {
		windowOption = 6;
		// windowSet = true;
		if (fftDataSet) {
			sumOfSquaredWeights = windowData(fftData, fftDataWindow, weights);
			windowApplied = true;
		}
	}


	// Set a Gaussian window option
	public void setGaussian(double alpha) {
		if (alpha < 2.0D) {
			alpha = 2.0D;
			E.info("setGaussian; alpha must be greater than or equal to 2 - alpha has been reset to 2");
		}
		gaussianAlpha = alpha;
		windowOption = 7;
		// windowSet = true;
		if (fftDataSet) {
			sumOfSquaredWeights = windowData(fftData, fftDataWindow, weights);
			windowApplied = true;
		}
	}


	// Set a Gaussian window option
	// default option for alpha
	public void setGaussian() {
		windowOption = 7;
		// windowSet = true;
		if (fftDataSet) {
			sumOfSquaredWeights = windowData(fftData, fftDataWindow, weights);
			windowApplied = true;
		}
	}


	// Remove windowing
	public void removeWindow() {
		windowOption = 0;
		// windowSet = false;
		if (fftDataSet) {
			sumOfSquaredWeights = windowData(fftData, fftDataWindow, weights);
			windowApplied = false;
		}
	}


	// Applies a window to the data
	private double windowData(double[] data, double[] window, double[] weight) {
		int m = data.length;
		int n = m / 2 - 1;
		int j = 0;
		double sum = 0.0D;
		switch (windowOption) {
		// 0. No windowing applied or remove windowing
		case 0:
			// 1. Rectangular
		case 1:
			for (int i = 0; i <= n; i++) {
				weight[i] = 1.0D;
				window[j] = data[j++];
				window[j] = data[j++];
			}
			sum = n + 1;
			break;
		// 2. Bartlett
		case 2:
			for (int i = 0; i <= n; i++) {
				weight[i] = 1.0D - Math.abs((i - n / 2) / n / 2);
				sum += weight[i] * weight[i];
				window[j] = data[j++] * weight[i];
				window[j] = data[j++] * weight[i];
			}
			break;
		// 3. Welch
		case 3:
			for (int i = 0; i <= n; i++) {
				weight[i] = 1.0D - Fmath.square((i - n / 2) / n / 2);
				sum += weight[i] * weight[i];
				window[j] = data[j++] * weight[i];
				window[j] = data[j++] * weight[i];
			}
			break;
		// 4. Hann
		case 4:
			for (int i = 0; i <= n; i++) {
				weight[i] = (1.0D - Math.cos(2.0D * i * Math.PI / n)) / 2.0D;
				sum += weight[i] * weight[i];
				window[j] = data[j++] * weight[i];
				window[j] = data[j++] * weight[i];
			}
			break;
		// 5. Hamming
		case 5:
			for (int i = 0; i <= n; i++) {
				weight[i] = 0.54D + 0.46D * Math.cos(2.0D * i * Math.PI / n);
				sum += weight[i] * weight[i];
				window[j] = data[j++] * weight[i];
				window[j] = data[j++] * weight[i];
			}
			break;
		// 6. Kaiser
		case 6:
			double denom = FourierTransform.modBesselIo(Math.PI * kaiserAlpha);
			double numer = 0.0D;
			for (int i = 0; i <= n; i++) {
				numer = FourierTransform.modBesselIo(Math.PI * kaiserAlpha
						* Math.sqrt(1.0D - Fmath.square(2.0D * i / n - 1.0D)));
				weight[i] = numer / denom;
				sum += weight[i] * weight[i];
				window[j] = data[j++] * weight[i];
				window[j] = data[j++] * weight[i];
			}
			break;
		// 6. Kaiser
		case 7:
			for (int i = 0; i <= n; i++) {
				weight[i] = Math.exp(-0.5D * Fmath.square(gaussianAlpha * (2 * i - n) / n));
				sum += weight[i] * weight[i];
				window[j] = data[j++] * weight[i];
				window[j] = data[j++] * weight[i];
			}
			break;
		}
		return sum;
	}


	// return modified Bessel Function of the zeroth order (for Kaiser window)
	// after numerical Recipe's bessi0
	// - Abramowitz and Stegun coeeficients
	public static double modBesselIo(double arg) {
		double absArg = 0.0D;
		double poly = 0.0D;
		double bessel = 0.0D;

		if ((absArg = Math.abs(arg)) < 3.75) {
			poly = arg / 3.75;
			poly *= poly;
			bessel = 1.0D
					+ poly
					* (3.5156229D + poly
							* (3.08989424D + poly
									* (1.2067492D + poly * (0.2659732 + poly * (0.360768e-1 + poly * 0.45813e-2)))));
		} else {
			bessel = (Math.exp(absArg) / Math.sqrt(absArg))
					* (0.39894228D + poly
							* (0.1328592e-1D + poly
									* (0.225319e-2 + poly
											* (-0.157565e-2 + poly
													* (0.916281e-2 + poly
															* (-0.2057706e-1 + poly
																	* (0.2635537e-1 + poly
																			* (-0.1647633e-1 + poly * 0.392377e-2))))))));
		}
		return bessel;
	}


	// Get the windowing weights
	public double[] getWeights() {
		return weights;
	}


	// set the number of segments
	public void setSegmentNumber(int sNum) {
		segmentNumber = sNum;
		segNumSet = true;
		segLenSet = false;
	}


	// set the segment length
	public void setSegmentLength(int sLen) {
		segmentLength = sLen;
		segLenSet = true;
		segNumSet = false;
	}


	// check and set up the segments
	private void checkSegmentDetails() {
		if (!fftDataSet) {
			throw new IllegalArgumentException("No fft data has been entered or calculated");
		}
		if (fftDataLength < 2) {
			throw new IllegalArgumentException("More than one point, MANY MORE, are needed");
		}

		// check if data number is even
		if (fftDataLength % 2 != 0) {
			E.info("Number of data points must be an even number");
			E.info("last point deleted");
			fftDataLength -= 1;
			dataAltered = true;
		}

		// check segmentation with no overlap
		if (segNumSet && !overlap) {
			if (fftDataLength % segmentNumber == 0) {
				int segL = fftDataLength / segmentNumber;
				if (FourierTransform.checkPowerOfTwo(segL)) {
					segmentLength = segL;
					segLenSet = true;
				} else {
					E.info("segment length is not an integer power of two");
					E.info("segment length reset to total data length, i.e. no segmentation");

					segmentNumber = 1;
					segmentLength = fftDataLength;
					segLenSet = true;
				}
			} else {
				E.info("total data length divided by the number of segments is not an integer");
				E.info("segment length reset to total data length, i.e. no segmentation");

				segmentNumber = 1;
				segmentLength = fftDataLength;
				segLenSet = true;
			}
		}

		if (segLenSet && !overlap) {
			if (fftDataLength % segmentLength == 0) {
				if (FourierTransform.checkPowerOfTwo(segmentLength)) {
					segmentNumber = fftDataLength / segmentLength;
					segNumSet = true;
				} else {
					E.info("segment length is not an integer power of two");
					E.info("segment length reset to total data length, i.e. no segmentation");

					segmentNumber = 1;
					segmentLength = fftDataLength;
					segNumSet = true;
				}
			} else {
				E.info("total data length divided by the segment length is not an integer");
				E.info("segment length reset to total data length, i.e. no segmentation");

				segmentNumber = 1;
				segmentLength = fftDataLength;
				segNumSet = true;
			}
		}

		// check segmentation with overlap
		if (segNumSet && overlap) {
			if (fftDataLength % (segmentNumber + 1) == 0) {
				int segL = 2 * fftDataLength / (segmentNumber + 1);
				if (FourierTransform.checkPowerOfTwo(segL)) {
					segmentLength = segL;
					segLenSet = true;
				} else {
					E.info("segment length is not an integer power of two");
					E.info("segment length reset to total data length, i.e. no segmentation");

					segmentNumber = 1;
					segmentLength = fftDataLength;
					segLenSet = true;
					overlap = false;
				}
			} else {
				E.info("total data length divided by the number of segments plus one is not an integer");
				E.info("segment length reset to total data length, i.e. no segmentation");

				segmentNumber = 1;
				segmentLength = fftDataLength;
				segLenSet = true;
				overlap = false;
			}
		}

		if (segLenSet && overlap) {
			E.info("seg len and overlap " + fftDataLength + " " + segmentLength);
			if ((2 * fftDataLength) % segmentLength == 0) {
				if (FourierTransform.checkPowerOfTwo(segmentLength)) {
					segmentNumber = (2 * fftDataLength) / segmentLength - 1;
					segNumSet = true;
				} else {
					E.info("segment length is not an integer power of two");
					E.info("segment length reset to total data length, i.e. no segmentation");

					segmentNumber = 1;
					segmentLength = fftDataLength;
					segNumSet = true;
					overlap = false;
				}
			} else {
				E.info("twice the total data length divided by the segment length is not an integer");
				E.info("segment length reset to total data length, i.e. no segmentation");

				segmentNumber = 1;
				segmentLength = fftDataLength;
				segNumSet = true;
				overlap = false;
			}
		}

		if (!segNumSet && !segLenSet) {
			segmentNumber = 1;
			segNumSet = true;
			overlap = false;
		}

		if (overlap && segmentNumber < 2) {
			E.info("Overlap is not possible with less than two segments.");
			E.info("Overlap option has been reset to 'no overlap' i.e. to false.");
			overlap = false;
			segmentNumber = 1;
			segNumSet = true;

		}

		// check no segmentation option
		int segLno = 0;
		int segNno = 0;
		int segLov = 0;
		int segNov = 0;

		if (segmentNumber == 1) {
			// check if data number is a power of two
			if (!FourierTransform.checkPowerOfTwo(fftDataLength)) {
				boolean test0 = true;
				boolean test1 = true;
				boolean test2 = true;
				int newL = 0;
				int ii = 2;
				// not a power of two - check segmentation options
				// no overlap option
				while (test0) {
					newL = fftDataLength / ii;
					if (FourierTransform.checkPowerOfTwo(newL) && (fftDataLength % ii) == 0) {
						test0 = false;
						segLno = newL;
						segNno = ii;
					} else {
						if (newL < 2) {
							test1 = false;
							test0 = false;
						} else {
							ii++;
						}
					}
				}
				test0 = true;
				ii = 2;
				// overlap option
				while (test0) {
					newL = 2 * (fftDataLength / (ii + 1));
					if (FourierTransform.checkPowerOfTwo(newL) && (fftDataLength % (ii + 1)) == 0) {
						test0 = false;
						segLov = newL;
						segNov = ii;
					} else {
						if (newL < 2) {
							test2 = false;
							test0 = false;
						} else {
							ii++;
						}
					}
				}
				// compare overlap and no overlap options
				boolean setSegment = true;
				int segL = 0;
				int segN = 0;
				boolean ovrlp = false;
				if (test1) {
					if (test2) {
						if (segLov > segLno) {
							segL = segLov;
							segN = segNov;
							ovrlp = true;
						} else {
							segL = segLno;
							segN = segNno;
							ovrlp = false;
						}
					} else {
						segL = segLno;
						segN = segNno;
						ovrlp = false;
					}
				} else {
					if (test2) {
						segL = segLov;
						segN = segNov;
						ovrlp = true;
					} else {
						setSegment = false;
					}
				}

				// compare segmentation and zero padding
				if (setSegment && (originalDataLength - segL <= fftDataLength - originalDataLength)) {
					E.info("Data length is not an integer power of two");
					E.info("Data cannot be transformed as a single segment");
					System.out.print("The data has been split into " + segN + " segments of length " + segL);
					if (ovrlp) {
						E.info(" with 50% overlap");
					} else {
						E.info(" with no overlap");
					}
					segmentLength = segL;
					segmentNumber = segN;
					overlap = ovrlp;

				} else {
					E.info("Data length is not an integer power of two");
					if (dataAltered) {
						E.info("Deleted point has been restored and the data has been padded " +
								"with zeros to give a power of two length");

					} else {
						E.info("Data has been padded with zeros to give a power of two length");
					}

				}
			}
		}
	}



	public int getSegmentNumber() {
		return segmentNumber;
	}


	public int getSegmentLength() {
		return segmentLength;
	}


	public void setOverlapOption(boolean overlapOpt) {
		overlap = overlapOpt;
	}


	public boolean getOverlapOption() {
		return overlap;
	}


	// calculate the number of data points given the:
	// segment length (segLen), number of segments (segNum)
	// and the overlap option (overlap: true - overlap, false - no overlap)
	public static int calcDataLength(boolean overlap, int segLen, int segNum) {
		if (overlap) {
			return (segNum + 1) * segLen / 2;
		} else {
			return segNum * segLen;
		}
	}


	// Method for performing a Fast Fourier Transform
	public void transform() {

		// set up data array
		int isign = 1;
		if (!fftDataSet)
			throw new IllegalArgumentException("No data has been entered for the Fast Fourier Transform");
		if (originalDataLength != fftDataLength) {
			E.info("Fast Fourier Transform data length ," + originalDataLength + ", is not an integer power of two");
			E.warning("Data has been padded with zeros to fill to nearest integer power of two length "
					+ fftDataLength);
		}

		// Perform fft
		double[] hold = new double[fftDataLength * 2];
		for (int i = 0; i < fftDataLength * 2; i++)
			hold[i] = fftDataWindow[i];
		basicFft(hold, fftDataLength, isign);
		for (int i = 0; i < fftDataLength * 2; i++)
			transformedDataFft[i] = hold[i];

		// fill transformed data arrays
		for (int i = 0; i < fftDataLength; i++) {
			transformedDataComplex[i].reset(transformedDataFft[2 * i], transformedDataFft[2 * i + 1]);
		}
	}


	// Method for performing an inverse Fast Fourier Transform
	public void inverse() {

		// set up data array
		int isign = -1;
		if (!fftDataSet)
			throw new IllegalArgumentException("No data has been entered for the inverse Fast Fourier Transform");
		if (originalDataLength != fftDataLength) {
			E.info("Fast Fourier Transform data length ," + originalDataLength + ", is not an integer power of two");
			E.warning("Data has been padded with zeros to fill to nearest integer power of two length "
					+ fftDataLength);
		}

		// Perform inverse fft
		double[] hold = new double[fftDataLength * 2];
		for (int i = 0; i < fftDataLength * 2; i++)
			hold[i] = fftDataWindow[i];
		basicFft(hold, fftDataLength, isign);

		for (int i = 0; i < fftDataLength * 2; i++)
			transformedDataFft[i] = hold[i] / fftDataLength;

		// fill transformed data arrays
		for (int i = 0; i < fftDataLength; i++) {
			transformedDataComplex[i].reset(transformedDataFft[2 * i], transformedDataFft[2 * i + 1]);
		}
	}


	// Base method for performing a Fast Fourier Transform
	// Based on the Numerical Recipes procedure four1
	// If isign is set to +1 this method replaces fftData[0 to 2*nn-1] by its
	// discrete Fourier Transform
	// If isign is set to -1 this method replaces fftData[0 to 2*nn-1] by nn
	// times its inverse discrete Fourier Transform
	// nn MUST be an integer power of 2. This is not checked for in this method,
	// fastFourierTransform(...), for speed.
	// If not checked for by the calling method, e.g. powerSpectrum(...) does,
	// the method checkPowerOfTwo() may be used to check
	// The real and imaginary parts of the data are stored adjacently
	// i.e. fftData[0] holds the real part, fftData[1] holds the corresponding
	// imaginary part of a data point
	// data array and data array length over 2 (nn) transferred as arguments
	// result NOT returned to transformedDataFft
	// Based on the Numerical Recipes procedure four1
	public void basicFft(double[] data, int nn, int isign) {
		double dtemp = 0.0D, wtemp = 0.0D, tempr = 0.0D, tempi = 0.0D;
		double theta = 0.0D, wr = 0.0D, wpr = 0.0D, wpi = 0.0D, wi = 0.0D;
		int istep = 0, m = 0, mmax = 0;
		int n = nn << 1;
		int j = 1;
		int jj = 0;
		for (int i = 1; i < n; i += 2) {
			jj = j - 1;
			if (j > i) {
				int ii = i - 1;
				dtemp = data[jj];
				data[jj] = data[ii];
				data[ii] = dtemp;
				dtemp = data[jj + 1];
				data[jj + 1] = data[ii + 1];
				data[ii + 1] = dtemp;
			}
			m = n >> 1;
			while (m >= 2 && j > m) {
				j -= m;
				m >>= 1;
			}
			j += m;
		}
		mmax = 2;
		while (n > mmax) {
			istep = mmax << 1;
			theta = isign * (6.28318530717959D / mmax);
			wtemp = Math.sin(0.5D * theta);
			wpr = -2.0D * wtemp * wtemp;
			wpi = Math.sin(theta);
			wr = 1.0D;
			wi = 0.0D;
			for (m = 1; m < mmax; m += 2L) {
				for (int i = m; i <= n; i += istep) {
					int ii = i - 1;
					jj = ii + mmax;
					tempr = wr * data[jj] - wi * data[jj + 1];
					tempi = wr * data[jj + 1] + wi * data[jj];
					data[jj] = data[ii] - tempr;
					data[jj + 1] = data[ii + 1] - tempi;
					data[ii] += tempr;
					data[ii + 1] += tempi;
				}
				wr = (wtemp = wr) * wpr - wi * wpi + wr;
				wi = wi * wpr + wtemp * wpi + wi;
			}
			mmax = istep;
		}
	}


	public Complex[] getTransformedDataAsComplex() {
		return transformedDataComplex;
	}


	// Get the transformed data array as adjacent real and imaginary pairs
	public double[] getTransformedDataAsAlternate() {
		return transformedDataFft;
	}


	// Performs and returns results a fft power spectrum density (psd)
	// estimation
	// of unsegmented, segmented or segemented and overlapped data
	// data in array fftDataWindow
	public double[][] powerSpectrum() {

		checkSegmentDetails();

		psdNumberOfPoints = segmentLength / 2;
		powerSpectrumEstimate = new double[2][psdNumberOfPoints];

		if (!overlap && segmentNumber < 2) {
			// Unsegmented and non-overlapped data

			// set up data array
			int isign = 1;
			if (!fftDataSet)
				throw new IllegalArgumentException("No data has been entered for the Fast Fourier Transform");
			if (!FourierTransform.checkPowerOfTwo(fftDataLength))
				throw new IllegalArgumentException("Fast Fourier Transform data length ," + fftDataLength
						+ ", is not an integer power of two");

			// perform fft
			double[] hold = new double[fftDataLength * 2];
			for (int i = 0; i < fftDataLength * 2; i++)
				hold[i] = fftDataWindow[i];
			basicFft(hold, fftDataLength, isign);
			for (int i = 0; i < fftDataLength * 2; i++)
				transformedDataFft[i] = hold[i];

			// fill transformed data arrays
			for (int i = 0; i < fftDataLength; i++) {
				transformedDataComplex[i].reset(transformedDataFft[2 * i], transformedDataFft[2 * i + 1]);
			}

			// obtain weighted mean square amplitudes
			powerSpectrumEstimate[1][0] = Fmath.square(hold[0]) + Fmath.square(hold[1]);
			for (int i = 1; i < psdNumberOfPoints; i++) {
				powerSpectrumEstimate[1][i] = Fmath.square(hold[2 * i]) + Fmath.square(hold[2 * i + 1])
						+ Fmath.square(hold[2 * segmentLength - 2 * i])
						+ Fmath.square(hold[2 * segmentLength - 2 * i + 1]);
			}

			// Normalise
			for (int i = 0; i < psdNumberOfPoints; i++) {
				powerSpectrumEstimate[1][i] = 2.0D * powerSpectrumEstimate[1][i]
						/ (fftDataLength * sumOfSquaredWeights);
			}

			// Calculate frequencies
			for (int i = 0; i < psdNumberOfPoints; i++) {
				powerSpectrumEstimate[0][i] = (double) i / ((double) segmentLength * deltaT);
			}
		} else {
			// Segmented or segmented and overlapped data
			powerSpectrumEstimate = powerSpectrumSeg();
		}

		powSpecDone = true;

		return powerSpectrumEstimate;
	}


	// Performs and returns results a fft power spectrum density (psd)
	// estimation of segmented or segemented and overlaped data
	// Data in fftDataWindow array
	// Private method for PowerSpectrum (see above)
	private double[][] powerSpectrumSeg() {

		// set up segment details
		int segmentStartIndex = 0;
		int segmentStartIncrement = segmentLength;
		if (overlap)
			segmentStartIncrement /= 2;
		double[] data = new double[2 * segmentLength]; // holds data and
														// transformed data for
														// working segment
		psdNumberOfPoints = segmentLength / 2; // number of PSD points
		double[] segPSD = new double[psdNumberOfPoints]; // holds psd for
															// working segment
		double[][] avePSD = new double[2][psdNumberOfPoints]; // first row -
																// frequencies
		// second row - accumaltes psd for averaging and then the averaged psd

		// initialis psd array and transform option
		for (int j = 0; j < psdNumberOfPoints; j++)
			avePSD[1][j] = 0.0D;
		int isign = 1;

		// loop through segments
		for (int i = 1; i <= segmentNumber; i++) {

			// collect segment data
			for (int j = 0; j < 2 * segmentLength; j++)
				data[j] = fftData[segmentStartIndex + j];

			// window data
			if (i == 1) {
				sumOfSquaredWeights = windowData(data, data, weights);
			} else {
				int k = 0;
				for (int j = 0; j < segmentLength; j++) {
					data[k] = data[k] * weights[j];
					data[++k] = data[k] * weights[j];
					++k;
				}
			}

			// perform fft on windowed segment
			basicFft(data, segmentLength, isign);

			// obtain weighted mean square amplitudes
			segPSD[0] = Fmath.square(data[0]) + Fmath.square(data[1]);
			for (int j = 1; j < psdNumberOfPoints; j++) {
				segPSD[j] = Fmath.square(data[2 * j]) + Fmath.square(data[2 * j + 1])
						+ Fmath.square(data[2 * segmentLength - 2 * j])
						+ Fmath.square(data[2 * segmentLength - 2 * j + 1]);
			}

			// Normalise
			for (int j = 0; j < psdNumberOfPoints; j++) {
				segPSD[j] = 2.0D * segPSD[j] / (segmentLength * sumOfSquaredWeights);
			}

			// accumalate for averaging
			for (int j = 0; j < psdNumberOfPoints; j++)
				avePSD[1][j] += segPSD[j];

			// increment segment start index
			segmentStartIndex += segmentStartIncrement;
		}

		// average all segments
		for (int j = 0; j < psdNumberOfPoints; j++)
			avePSD[1][j] /= segmentNumber;

		// Calculate frequencies
		for (int i = 0; i < psdNumberOfPoints; i++) {
			avePSD[0][i] = (double) i / ((double) segmentLength * deltaT);
		}

		return avePSD;
	}


	// Get the power spectrum
	public double[][] getpowerSpectrumEstimate() {
		if (!powSpecDone)
			E.info("getpowerSpectrumEstimate - powerSpectrum has not been called - null returned");
		return powerSpectrumEstimate;
	}


	// get the number of power spectrum frequency points
	public int getNumberOfPsdPoints() {
		return psdNumberOfPoints;
	}


	// Return correlation of data already entered with data passed as this
	// method's argument
	// data must be real
	public double[][] correlate(double[] data) {
		int nLen = data.length;
		if (!fftDataSet)
			throw new IllegalArgumentException("No data has been previously entered");
		if (nLen != originalDataLength)
			throw new IllegalArgumentException("The two data sets to be correlated are of different length");
		if (!FourierTransform.checkPowerOfTwo(nLen))
			throw new IllegalArgumentException(
					"The length of the correlation data sets is not equal to an integer power of two");

		complexCorr = Complex.oneDarray(nLen);
		for (int i = 0; i < nLen; i++) {
			complexCorr[i].setReal(data[i]);
			complexCorr[i].setImag(0.0D);
		}

		fftCorr = new double[2 * nLen];
		int j = -1;
		for (int i = 0; i < nLen; i++) {
			fftCorr[++j] = data[i];
			fftCorr[++j] = 0.0D;
		}

		return correlation(nLen);
	}


	// Return correlation of data1 and data2 passed as this method's arguments
	// data must be real
	public double[][] correlate(double[] data1, double[] data2) {
		int nLen = data1.length;
		int nLen2 = data2.length;
		if (nLen != nLen2)
			throw new IllegalArgumentException("The two data sets to be correlated are of different length");
		if (!FourierTransform.checkPowerOfTwo(nLen))
			throw new IllegalArgumentException(
					"The length of the correlation data sets is not equal to an integer power of two");

		fftDataLength = nLen;
		complexData = Complex.oneDarray(fftDataLength);
		for (int i = 0; i < fftDataLength; i++) {
			complexData[i].setReal(data1[i]);
			complexData[i].setImag(0.0D);
		}

		fftData = new double[2 * fftDataLength];
		int j = 0;
		for (int i = 0; i < fftDataLength; i++) {
			fftData[j] = data1[i];
			j++;
			fftData[j] = 0.0D;
			j++;
		}
		fftDataSet = true;

		fftDataWindow = new double[2 * fftDataLength];
		weights = new double[fftDataLength];
		sumOfSquaredWeights = windowData(fftData, fftDataWindow, weights);

		transformedDataFft = new double[2 * fftDataLength];
		transformedDataComplex = Complex.oneDarray(fftDataLength);

		complexCorr = Complex.oneDarray(nLen);
		for (int i = 0; i < nLen; i++) {
			complexCorr[i].setReal(data2[i]);
			complexCorr[i].setImag(0.0D);
		}

		fftCorr = new double[2 * nLen];
		j = -1;
		for (int i = 0; i < nLen; i++) {
			fftCorr[++j] = data2[i];
			fftCorr[++j] = 0.0D;
		}

		return correlation(nLen);
	}


	// Returns the correlation of the data in fftData and fftCorr
	private double[][] correlation(int nLen) {

		fftDataWindow = new double[2 * nLen];
		fftCorrWindow = new double[2 * nLen];
		weights = new double[nLen];

		sumOfSquaredWeights = windowData(fftData, fftDataWindow, weights);
		windowData(fftCorr, fftCorrWindow, weights);

		// Perform fft on first set of stored data
		int isign = 1;
		double[] hold1 = new double[2 * nLen];
		for (int i = 0; i < nLen * 2; i++)
			hold1[i] = fftDataWindow[i];
		basicFft(hold1, nLen, isign);

		// Perform fft on second set of stored data
		isign = 1;
		double[] hold2 = new double[2 * nLen];
		for (int i = 0; i < nLen * 2; i++)
			hold2[i] = fftCorrWindow[i];
		basicFft(hold2, nLen, isign);

		// multiply hold1 by complex congugate of hold2
		double[] hold3 = new double[2 * nLen];
		int j = 0;
		for (int i = 0; i < nLen; i++) {
			hold3[j] = (hold1[j] * hold2[j] + hold1[j + 1] * hold2[j + 1]) / nLen;
			hold3[j + 1] = (-hold1[j] * hold2[j + 1] + hold1[j + 1] * hold2[j]) / nLen;
			j += 2;
		}

		// Inverse transform -> correlation
		isign = -1;
		basicFft(hold3, nLen, isign);

		// fill correlation array
		for (int i = 0; i < 2 * nLen; i++)
			transformedDataFft[i] = hold3[i];
		correlationArray = new double[2][nLen - 1];
		j = 0;
		for (int i = nLen / 2 + 1; i < nLen; i++) {
			correlationArray[1][j] = hold3[2 * i] / nLen;
			j++;
		}
		for (int i = 0; i < nLen / 2; i++) {
			correlationArray[1][j] = hold3[2 * i] / nLen;
			j++;
		}

		// calculate time lags
		correlationArray[0][0] = -(double) (nLen / 2 - 1) * deltaT;
		for (int i = 1; i < nLen - 1; i++) {
			correlationArray[0][i] = correlationArray[0][i - 1] + deltaT;
		}

		correlateDone = true;
		return correlationArray;
	}


	// Get the correlation
	public double[][] getCorrelation() {
		if (!correlateDone) {
			E.info("getCorrelation - correlation has not been called - no correlation returned");
		}
		return correlationArray;
	}


	// Performs a fft power spectrum density (psd) estimation
	// on a moving window throughout the original data set
	// returning the results as a frequency time matrix
	// windowLength is the length of the window in time units
	public double[][] shortTime(double windowTime) {
		int windowLength = (int) Math.round(windowTime / deltaT);
		if (!checkPowerOfTwo(windowLength)) {
			int low = lastPowerOfTwo(windowLength);
			int high = nextPowerOfTwo(windowLength);

			if ((windowLength - low) <= (high - windowLength)) {
				windowLength = low;
				if (low == 0)
					windowLength = high;
			} else {
				windowLength = high;
			}
			E.info("Method - shortTime");
			E.info("Window length, provided as time, " + windowTime
					+ ", did not convert to an integer power of two data points");
			E.info("A value of " + ((windowLength - 1) * deltaT) + " was substituted");
		}

		return shortTime(windowLength);
	}


	// Performs a fft power spectrum density (psd) estimation
	// on a moving window throughout the original data set
	// returning the results as a frequency time matrix
	// windowLength is the number of points in the window
	public double[][] shortTime(int windowLength) {

		if (!FourierTransform.checkPowerOfTwo(windowLength))
			throw new IllegalArgumentException("Moving window data length ," + windowLength
					+ ", is not an integer power of two");
		if (!fftDataSet)
			throw new IllegalArgumentException("No data has been entered for the Fast Fourier Transform");
		if (windowLength > originalDataLength)
			throw new IllegalArgumentException("The window length, " + windowLength
					+ ", is greater than the data length, " + originalDataLength + ".");

		// if no window option has been set - default = Gaussian with alpha =
		// 2.5
		if (windowOption == 0)
			setGaussian();
		// set up time-frequency matrix
		// first row = blank cell followed by time vector
		// first column = blank cell followed by frequency vector
		// each cell is then the mean square amplitude at that frequency and
		// time
		numShortTimes = originalDataLength - windowLength + 1;
		numShortFreq = windowLength / 2;
		timeFrequency = new double[numShortFreq + 1][numShortTimes + 1];
		timeFrequency[0][0] = 0.0D;
		timeFrequency[0][1] = (double) (windowLength - 1) * deltaT / 2.0D;
		for (int i = 2; i <= numShortTimes; i++) {
			timeFrequency[0][i] = timeFrequency[0][i - 1] + deltaT;
		}
		for (int i = 0; i < numShortFreq; i++) {
			timeFrequency[i + 1][0] = (double) i / ((double) windowLength * deltaT);
		}

		// set up window details
		segmentLength = windowLength;
		int windowStartIndex = 0;
		double[] data = new double[2 * windowLength]; // holds data and
														// transformed data for
														// working window
		double[] winPSD = new double[numShortFreq]; // holds psd for working
													// window
		int isign = 1;

		// loop through time shifts
		for (int i = 1; i <= numShortTimes; i++) {

			// collect window data
			for (int j = 0; j < 2 * windowLength; j++)
				data[j] = fftData[windowStartIndex + j];

			// window data
			if (i == 1) {
				sumOfSquaredWeights = windowData(data, data, weights);
			} else {
				int k = 0;
				for (int j = 0; j < segmentLength; j++) {
					data[k] = data[k] * weights[j];
					data[++k] = data[k] * weights[j];
					++k;
				}
			}

			// perform fft on windowed segment
			basicFft(data, windowLength, isign);

			// obtain weighted mean square amplitudes
			winPSD[0] = Fmath.square(data[0]) + Fmath.square(data[1]);
			for (int j = 1; j < numShortFreq; j++) {
				winPSD[j] = Fmath.square(data[2 * j]) + Fmath.square(data[2 * j + 1])
						+ Fmath.square(data[2 * windowLength - 2 * j])
						+ Fmath.square(data[2 * windowLength - 2 * j + 1]);
			}

			// Normalise and place in time-frequency matrix
			for (int j = 0; j < numShortFreq; j++) {
				timeFrequency[j + 1][i] = 2.0D * winPSD[j] / (windowLength * sumOfSquaredWeights);
			}

			// increment segment start index
			windowStartIndex += 2;
		}

		shortTimeDone = true;
		return timeFrequency;
	}


	// Return time frequency matrix
	public double[][] getTimeFrequencyMatrix() {
		if (!shortTimeDone)
			throw new IllegalArgumentException("No short time Fourier transform has been performed");
		return timeFrequency;
	}


	// Return number of times in short time Fourier transform
	public int getShortTimeNumberOfTimes() {
		if (!shortTimeDone)
			throw new IllegalArgumentException("No short time Fourier transform has been performed");
		return numShortTimes;
	}


	// Return number of frequencies in short time Fourier transform
	public int getShortTimeNumberOfFrequencies() {
		if (!shortTimeDone)
			throw new IllegalArgumentException("No short time Fourier transform has been performed");
		return numShortFreq;
	}


	// Return number of points in short time Fourier transform window
	public int getShortTimeWindowLength() {
		if (!shortTimeDone)
			throw new IllegalArgumentException("No short time Fourier transform has been performed");
		return segmentLength;
	}


	// returns nearest power of two that is equal to or lower than argument
	// length
	public static int lastPowerOfTwo(int len) {

		boolean test0 = true;
		while (test0) {
			if (FourierTransform.checkPowerOfTwo(len)) {
				test0 = false;
			} else {
				len--;
			}
		}
		return len;
	}


	// returns nearest power of two that is equal to or higher than argument
	// length
	public static int nextPowerOfTwo(int len) {

		boolean test0 = true;
		while (test0) {
			if (FourierTransform.checkPowerOfTwo(len)) {
				test0 = false;
			} else {
				len++;
			}
		}
		return len;
	}


	// Checks whether the argument n is a power of 2
	public static boolean checkPowerOfTwo(int n) {
		boolean test = true;
		int m = n;
		while (test && m > 1) {
			if ((m % 2) != 0) {
				test = false;
			} else {
				m /= 2;
			}
		}
		return test;
	}


	// Checks whether the argument n is an integer times a integer power of 2
	// returns integer multiplier if true
	// returns zero if false
	public static int checkIntegerTimesPowerOfTwo(int n) {
		boolean testOuter1 = true;
		boolean testInner1 = true;
		boolean testInner2 = true;
		boolean testReturn = true;

		int m = n;
		int j = 1;
		int mult = 0;

		while (testOuter1) {
			testInner1 = FourierTransform.checkPowerOfTwo(m);
			if (testInner1) {
				testReturn = true;
				testOuter1 = false;
			} else {
				testInner2 = true;
				while (testInner2) {
					m /= ++j;
					if (m < 1) {
						testInner2 = false;
						testInner1 = false;
						testOuter1 = false;
						testReturn = false;
					} else {
						if ((m % 2) == 0)
							testInner2 = false;
					}
				}
			}
		}
		if (testReturn)
			mult = j;
		return mult;
	}

}


/*
 *
 *
 *
 *  // Print the short time Fourier transform to a text file public void
 * printShortTime(String filename) { if(!shortTimeDone) {
 * E.info("printShortTime- shortTime has not been called - no file printed"); }
 * else{ FileOutput fout = new FileOutput(filename); fout.println("Short Time
 * Fourier Transform Output File from FourierTransform");
 * fout.dateAndTimeln(filename); String title = "Window:
 * "+windowNames[windowOption]; if(windowOption==6)title += ", alpha =
 * "+kaiserAlpha; if(windowOption==7)title += ", alpha = "+gaussianAlpha;
 * fout.println(title); fout.printtab("Data length = ");
 * fout.println(originalDataLength); fout.printtab("Delta T = ");
 * fout.println(deltaT); fout.printtab("Window length (points) = ");
 * fout.println(segmentLength); fout.printtab("Window length (time units) = ");
 * fout.println((segmentLength-1)*deltaT); fout.printtab("Number of frequency
 * points = "); fout.println(numShortFreq); fout.printtab("Number of time points =
 * "); fout.println(numShortTimes);
 *  // Average points if output would be greater than a text file line length
 * boolean checkAve = false; int newTp = numShortTimes; int maxN = 100; int nAve =
 * numShortTimes/maxN; int nLast = numShortTimes % maxN; if(numShortTimes>127) {
 * checkAve = true; if(nLast>0) { nAve += 1; newTp = maxN; nLast = numShortTimes -
 * nAve*(newTp-1); } else{ newTp = maxN; nLast = nAve; } if(nLast!=nAve) {
 * fout.println("In the output below, each of the first " + (newTp-2) + "
 * magnitude points, along the time axis, is the average of " + nAve + "
 * calculated points"); fout.println("The last point is the average of " + nLast + "
 * calculated points"); } else{ fout.println("In the output below, each
 * magnitude point is the average of " + nAve + " calculated points"); }
 * fout.println("The data, without averaging, may be accessed using the method
 * getTimeFrequencyMatrix()"); } fout.println();
 *
 * fout.println("first row = times"); fout.println("first column =
 * frequencies"); fout.println("all other cells = mean square amplitudes at the
 * corresponding time and frequency"); if(checkAve) { double sum = 0.0D; int
 * start = 1; int workingAve = nAve; for(int i=0; i<=numShortFreq;i++) {
 * fout.printtab(Fmath.truncate(timeFrequency[i][0], 4)); start = 1; for(int
 * j=1; j<=newTp; j++) { workingAve = nAve; if(j==newTp)workingAve = nLast;
 * sum=0.0D; for(int k=start; k<=(start+workingAve-1); k++) { sum +=
 * timeFrequency[i][k]; } sum /= workingAve; fout.printtab(Fmath.truncate(sum,
 * 4)); start += workingAve; } fout.println(); } } else{ for(int i=0; i<=numShortFreq;i++) {
 * for(int j=0; j<=newTp; j++) {
 * fout.printtab(Fmath.truncate(timeFrequency[i][j], 4)); } fout.println(); } }
 * fout.close(); } }
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *  // Print the power spectrum to a text file public void
 * printPowerSpectrum(String filename) { if(!powSpecDone)powerSpectrum();
 *
 * FileOutput fout = new FileOutput(filename); fout.println("Power Spectrum
 * Density Estimate Output File from FourierTransform");
 * fout.dateAndTimeln(filename); String title = "Window:
 * "+windowNames[windowOption]; if(windowOption==6)title += ", alpha =
 * "+kaiserAlpha; if(windowOption==7)title += ", alpha = "+gaussianAlpha;
 * fout.println(title); fout.printtab("Number of segments = ");
 * fout.println(segmentNumber); fout.printtab("Segment length = ");
 * fout.println(segmentLength); if(segmentNumber>1) { if(overlap) {
 * fout.printtab("Segments overlap by 50%"); } else{ fout.printtab("Segments do
 * not overlap"); } }
 *
 * fout.println(); printWarnings(fout);
 *
 * fout.printtab("Frequency"); fout.println("Mean Square");
 * fout.printtab("(cycles per"); fout.println("Amplitude"); if(deltaTset) {
 * fout.printtab("unit time)"); } else{ fout.printtab("gridpoint)"); }
 * fout.println(" "); int n = powerSpectrumEstimate[0].length; for(int i=0; i<n;
 * i++) { fout.printtab(Fmath.truncate(powerSpectrumEstimate[0][i], 4));
 * fout.println(Fmath.truncate(powerSpectrumEstimate[1][i], 4)); } fout.close(); }
 *
 *
 *
 *  // Print the correlation to a text file public void printCorrelation(String
 * filename) { if(!correlateDone) { E.info("printCorrelation - correlate has not
 * been called - no file printed"); } else{ FileOutput fout = new
 * FileOutput(filename); fout.println("Correlation Output File from
 * FourierTransform"); fout.dateAndTimeln(filename); String title = "Window:
 * "+windowNames[windowOption]; if(windowOption==6)title += ", alpha =
 * "+kaiserAlpha; if(windowOption==7)title += ", alpha = "+gaussianAlpha;
 * fout.println(title); fout.printtab("Data length = ");
 * fout.println(fftDataLength); fout.println();
 *
 * fout.printtab("Time lag"); fout.println("Correlation"); if(deltaTset) {
 * fout.printtab("/unit time"); } else{ fout.printtab("/grid interval)"); }
 * fout.println("Coefficient");
 *
 * int n = correlationArray[0].length; for(int i=0; i<n; i++) {
 * fout.printtab(Fmath.truncate(correlationArray[0][i], 4));
 * fout.println(Fmath.truncate(correlationArray[1][i], 4)); } fout.close(); } }
 *
 *
 *
 *
 *
 */


