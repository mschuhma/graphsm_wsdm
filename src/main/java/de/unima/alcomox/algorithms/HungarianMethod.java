// *****************************************************************************
//
// Copyright (c) 2011 Christian Meilicke (University of Mannheim)
//
// Permission is hereby granted, free of charge, to any person
// obtaining a copy of this software and associated documentation
// files (the "Software"), to deal in the Software without restriction,
// including without limitation the rights to use, copy, modify, merge,
// publish, distribute, sublicense, and/or sell copies of the Software,
// and to permit persons to whom the Software is furnished to do so,
// subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included
// in all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
// OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
// WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR
// IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
//
// *********************************************************************************

package de.unima.alcomox.algorithms;

import java.util.ArrayList;


/**
* This class implements the hungarian method (also known as Munkres Algorithm).
*
* Algorithm is based on the description available at
* http://www.netlib.org/utk/lsi/pcwLSI/text/node222.html
* Note that theres an error in "such a zero exists" on the webpage,
* this passage must be placed one line above
*/
public class HungarianMethod  {
	
	private int dimension;
	private double[][] matrix;
	private double[][] omatrix;
	private boolean[][] stars;
	private boolean[][] primes;
	private boolean[] coveredRows;
	private boolean[] coveredCols;
	private boolean solved = false;
	
	// this value makes the algorithm avoid locked cells
	private static final float LOCK_PENALTY = 100.0f;
	
	
	public  void setInputMatrix(double[][] matrix) {
		this.dimension = matrix.length;
		this.matrix = new double[this.dimension][this.dimension];
		this.omatrix = new double[this.dimension][this.dimension];
		this.stars = new boolean[this.dimension][this.dimension];
		this.primes = new boolean[this.dimension][this.dimension];
		this.coveredRows = new boolean[this.dimension];
		this.coveredCols = new boolean[this.dimension];
		for (int i = 0; i < this.dimension; i++) {
			this.coveredRows[i] = false;
			this.coveredCols[i] = false;			
		}
		this.setMatrix(matrix);	
	}
	
	/**
	* Returns a string representation of the hungarian matrix that the algorithm has been worked with.
	* 
	* @return A String representation of the hungarian matrix.
	*/
	public String toString() {
		return toString(false);
	}
	
	/**
	* Returns a string representation of the original input matrix with
	* chosen cells marked with a star.
	* 
	* @return A String representation of the input matrix
	*/
	public String toOString() {
		return toString(true);
	}	
	
	
	/**
	* Returns the aggregated minimum.
	* If the hungarian matrix has not been solved yet, it will be solved first.
	* 
	* @return The aggregated minimum (= sum of the distances all chosen entries).  
	*/
	public double getMinimum() {
		double aggregatedMinimum = 0.0f;
		if (!this.solved) { this.solve(); }
		for (int x = 0; x < this.dimension; x++) {
			for (int y = 0; y < this.dimension; y++) {
				if (stars[x][y]) { aggregatedMinimum += omatrix[x][y]; }
			}
		}	
		// System.out.println("hungarian minimum: " +  aggregatedMinimum);
		return aggregatedMinimum;
	}
	

	
	/**
	* Solves the hungarian matrix. The results is available as stared matrix.
	*/
	public void solve() {
		this.step0();
		this.step1();
		while (!(this.step2())) {
			// step 3 calls step 4 and 5 internally
			this.step3();
		}
		this.solved = true;
	}
	
	
	
	// ****************************************
	// ********** PRIVATE PLAYGORUND **********
	// ****************************************
	
	
	private void setMatrix(double[][] matrix) {
		for (int i = 0; i < matrix.length; i++) {
			// System.out.println();
			for (int j = 0; j < matrix.length; j++) {
				// System.out.print(matrix[i][j] + " ");
				this.matrix[i][j] = matrix[i][j];
				this.omatrix[i][j] = matrix[i][j];
				this.stars[i][j] = false;
				this.primes[i][j] = false;
			}			
		}
	}
	
	private String toString(boolean original) {
		StringBuffer rep = new StringBuffer();
		for (int i = 0; i < this.dimension; i++) {
			rep.append(this.coveredCols[i] ? " ~   " : "     ");
		}
		rep.append("\n");
		for (int i = 0; i < this.dimension; i++) {
			for (int j = 0; j < this.dimension; j++) {
				if (original) { rep.append(this.omatrix[i][j]); }
				else { rep.append(this.matrix[i][j]); }
				if (this.stars[i][j]) { rep.append("* "); }
				else if (this.stars[i][j]) { rep.append("' "); }
				else { rep.append("  "); }
			}
			if (this.coveredRows[i]) { rep.append(" ~"); }
			rep.append("\n");
		}
		return rep.toString();
	}		
	
	private void step1() {
		boolean starable;
		for (int x = 0; x < this.dimension; x++) {
			for (int y = 0; y < this.dimension; y++) {
				if (!(this.stars[x][y]) && this.matrix[x][y] == 0.0) {
					starable = true;
					for (int xcheck = 0; xcheck < this.dimension; xcheck++) {
						if (this.stars[xcheck][y]) { starable = false; }
					}
					for (int ycheck = 0; ycheck < this.dimension; ycheck++) {
						if (this.stars[x][ycheck]) { starable = false; }
					}
					if (starable) {
						this.stars[x][y] = true;
					}
				}
			}
		}
	}
	

	private boolean step2() {
		int coveredColCounter = 0;
		for (int y = 0; y < this.dimension; y++) {
			for (int x = 0; x < this.dimension; x++) {
				if (this.stars[x][y]) { this.coveredCols[y] = true; }
			}
		}
		for (int y = 0; y < this.dimension; y++) {
			if (this.coveredCols[y]) { coveredColCounter++; }
		}		
		if (this.dimension == coveredColCounter) { return true; }
		else { return false; }
	}	
	
	private void step3() {
		boolean continueStep3 = true;
		while (continueStep3) {
			int[] coord = getUncoveredZ(); 
			if (coord == null) {
				this.step5();	
			}
			else {
				int x = coord[0];
				int y = coord[1];
				primes[x][y] = true;
				int[] zStarCoord = getZStarInRow(x);
				if (zStarCoord == null) {
					step4(coord);
					continueStep3 = false;
				}
				else {
					this.coveredRows[x] = true;
					this.coveredCols[zStarCoord[1]] = false;
				}
			}
		}
	}	
	
	private void step4(int[] coord) {
		int[] zPrimeCoord = new int[2];
		int[] zStarCoord  = null;
		ArrayList<int[]> zSequence = new ArrayList<int[]>();
		zPrimeCoord[0] = coord[0];
		zPrimeCoord[1] = coord[1];	
		zSequence.add(zPrimeCoord);
		while (true) {	
			zStarCoord  = getZStarInCol(zPrimeCoord[1]);
			if (zStarCoord == null) { break; }
			zSequence.add(zStarCoord);
			zPrimeCoord = getZPrimeInRow(zStarCoord[0]);
			zSequence.add(zPrimeCoord);	
		}
		// concvert ' * ' * ' to * ' * ' *
		int[] tempCoord;
		for (int i = 0; i < zSequence.size(); i++) {
			tempCoord = zSequence.get(i); 
			// even
			if (i % 2 == 0) { stars[tempCoord[0]][tempCoord[1]] = true; }
			// odd
			else { stars[tempCoord[0]][tempCoord[1]] = false; }
		}
		this.resetPrimesAndCovers();	
	}
	
	private void resetPrimesAndCovers() {
		for (int x = 0; x < this.dimension; x++) {
			this.coveredCols[x] = false;
			this.coveredRows[x] = false;
			for (int y = 0; y < this.dimension; y++) { primes[x][y] = false; }
		}
	}

	private void step5() {
		double min = this.getUncoveredMin();
		// add to covered rows
		for (int x = 0; x < this.dimension; x++) {
			if (this.coveredRows[x]) {
				for (int y = 0; y < this.dimension; y++) { matrix[x][y] += min; }
			}
		}
		// substract from uncovered cols
		for (int y = 0; y < this.dimension; y++) {
			if (!(this.coveredCols[y])) {
				for (int x = 0; x < this.dimension; x++) {
					matrix[x][y] -= min;
					
				}
			}
		}
	}
	
	private double getUncoveredMin() {
		double min = Double.MAX_VALUE;
		for (int x = 0; x < this.dimension; x++) {
			if (!(this.coveredRows[x])) {
				for (int y = 0; y < this.dimension; y++) {
					if (!(this.coveredCols[y])) { 	
						if (min > this.matrix[x][y]) {
							min = this.matrix[x][y];
						}
					}
				}
			}
		}		
		return min;
	}

	private int[] getUncoveredZ() {
		for (int x = 0; x < this.dimension; x++) {
			if (!(this.coveredRows[x])) {
				for (int y = 0; y < this.dimension; y++) {
					if (!(this.coveredCols[y])) { 	
						if (this.matrix[x][y] == 0.0 && !this.stars[x][y] && !this.primes[x][y]) {
							return new int[]{x,y};
						}
					}
				}
			}
		}		
		return null;
	}
	
	private int[] getZStarInRow(int x) {
		for (int y = 0; y < this.dimension; y++) {
			if (this.stars[x][y]) { return new int[]{x,y}; }
		}	
		return null;
	}

	private int[] getZStarInCol(int y) {
		for (int x = 0; x < this.dimension; x++) {
			if (this.stars[x][y]) { return new int[]{x,y}; }
		}	
		return null;
	}	
	

	private int[] getZPrimeInRow(int x) {
		for (int y = 0; y < this.dimension; y++) {
			if (this.primes[x][y]) { return new int[]{x,y}; }
		}	
		return null;
	}
	
	private void step0() {
		for (int i = 0; i < this.dimension; i++) {
			double rowMin = Float.MAX_VALUE;
			for (int j = 0; j < this.dimension; j++) {
				if (rowMin > this.matrix[i][j]) { rowMin = this.matrix[i][j]; }
			}
			for (int j = 0; j < this.dimension; j++) { this.matrix[i][j] -= rowMin; }
		}	
	}


	public static void main (String args[]) {
		
		double[][] matrix = {
				{2, 3, 1, 5},
				{2, 2, 1, 5},
				{4, 3, 1, 7},
		};
		
		HungarianMethod hm = new HungarianMethod();
		
		hm.setInputMatrix(matrix);
		
		hm.solve();
		
		System.out.println(hm.getMinimum());
	}

}

