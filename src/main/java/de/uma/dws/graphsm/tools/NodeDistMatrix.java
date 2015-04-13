package de.uma.dws.graphsm.tools;

public class NodeDistMatrix {
	
	
	/**
	 * 
	 * @param matrix Distance matrix of Double[][]
	 * @param maxValue Cut off value that will be set for all values above
	 * @param normalize Set to true to divide all values by maxValue 
	 * @return matrix 
	 */
	public static double[][] normalizeValues(Double[][] matrix, Double maxValue, boolean normalize) {
		
		//Method can not handle matrix without any values. This case has to be dealt with outside this method
		assert !allValuesNull(matrix);
		
		//Deaktived normalizing if the normalization value is 0.0 
		//This is usually the case when no path where found (represented by null) 
		//but some identical nodes (represented by 0.0)
		boolean maxValueNull = maxValue.equals(0d); 
		if (maxValueNull) {
//			System.out.println("Warning, maxValue = 0");
//			System.out.println(NodeDistMatrix.printMatrix(matrix));
			assert normalize;
			maxValue = 1d;
		}
		
		double[][] m = new double[matrix.length][matrix[0].length];
		
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[i].length; j++) {
				if (matrix[i][j] == null || matrix[i][j]  > maxValue) {
					m[i][j] = maxValue;
				}
				else {
					m[i][j] = matrix[i][j];
				}
				if (normalize)
					m[i][j] = m[i][j] / maxValue;
			}
		}	
		
//		if (maxValueNull)
//			System.out.println(NodeDistMatrix.printMatrix(m));
		
		return m;
	}
	
	public static String printMatrix(double[][] matrix) {
		
		StringBuffer bf = new StringBuffer();
		
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[i].length; j++) {
				bf.append(matrix[i][j] + " ");
			}
			
			bf.append("\n");
		}	
		return bf.toString();
	}
	
	public static String printMatrix(Double[][] matrix) {
		
		StringBuffer bf = new StringBuffer();
		
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[i].length; j++) {
				bf.append(matrix[i][j] + " ");
			}
			
			bf.append("\n");
		}	
		return bf.toString();
	}
	
	public static boolean allValuesNull(Double[][] matrix) {
			
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[i].length; j++) {
				if (matrix[i][j] != null)
					return false;				
			}
		}	
		return true;
	}
	
	public static int getCntOfValues(Double[][] matrix, Double valueToBeCounted) {
		
		int cnt = 0;
		
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[i].length; j++) {
				if (matrix[i][j] != null && matrix[i][j].equals(valueToBeCounted))
					cnt++;				
			}
		}	
		return cnt;
	}
	
	public static int getCntOfNullValues(Double[][] matrix) {
		
		int cnt = 0;
		
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[i].length; j++) {
				if (matrix[i][j] == null)
					cnt++;				
			}
		}	
		return cnt;
	}

}
