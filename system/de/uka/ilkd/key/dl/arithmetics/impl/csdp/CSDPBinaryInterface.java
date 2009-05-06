/***************************************************************************
 *   Copyright (C) 2008 by Jan David Quesel                                *
 *   quesel@informatik.uni-oldenburg.de                                    *
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
package de.uka.ilkd.key.dl.arithmetics.impl.csdp;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StreamTokenizer;
import java.util.Arrays;
import java.util.Scanner;

import de.uka.ilkd.key.dl.options.DLOptionBean;

/**
 * @author jdq TODO Documentation since 05.05.2009
 */
public class CSDPBinaryInterface {

	/**
	 * Input Parameters:
	 * 
	 * @param n
	 *            gives the dimension of the X, C, and Z matrices.
	 * @param k
	 *            gives the number of constraints.
	 * @param blockmatrixC
	 *            gives the C matrix and implicitly defines the block structure
	 *            of the block diagonal matrices.
	 * @param a
	 *            gives the right hand side vector a.
	 * @param constraints
	 *            specifies the problem constraints.
	 * @param constant_offset
	 *            This scalar is added to the primal and dual objective values.
	 * 
	 * 
	 * @param blockmatrixpX
	 *            On input, this parameter gives the initial primal solution X.
	 *            On output, it gives the optimal primal solution X
	 * @param py
	 *            On input, this parameter gives the initial dual solution y. On
	 *            output, it gives the optimal dual solution y
	 * @param blockmatrixpZ
	 *            On input, this parameter gives the initial dual solution Z. On
	 *            output, it gives the optimal dual solution Z
	 * @param ppobj
	 *            gives the optimal primal objective value
	 * @param pdobj
	 *            gives the optimal dual objective value
	 * @return <ul>
	 *         <li>0: Success. Problem is solvable
	 *         <li>1: Success. The problem is primal infeasible.
	 *         <li>2: Success. The problem is dual infeasible.
	 *         <li>3: Partial Success: A solution has been found, but full
	 *         accuracy was not achieved. One or more of primal infeasibility,
	 *         dual infeasibility, or relative duality gap are larger than their
	 *         tolerances, but by a factor of less than 1000.
	 *         <li>4: Failure. Maximum iterations reached.
	 *         <li>5: Failure. Stuck at edge of primal feasibility.
	 *         <li>6: Failure. Stuck at edge of dual infeasibility.
	 *         <li>7: Failure. Lack of progress.
	 *         <li>8: Failure. X, Z, or O was singular.
	 *         <li>9: Failure. Detected NaN or Inf values.
	 *         </ul>
	 * @throws IOException
	 *             if there is a problem creating temporary files
	 */
	public static int easySDP(int n, int k, double[] blockmatrixC, double[] a,
			double[] constraints, double constant_offset,
			double[] blockmatrixpX, double[] py, double[] blockmatrixpZ,
			double[] ppobj, double[] pdobj) throws IOException {
		int Cnblocks = 1;
		File tempFile = File.createTempFile("keymaera-csdp", ".dat-s");
		tempFile.deleteOnExit();
		System.out.println("Writing to " + tempFile.getAbsolutePath());// XXX
		PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(
				tempFile)));
		writer.printf("%d \n", k);
		writer.printf("%d \n", Cnblocks); // number of blocks in C
		// in our case we always use MATRIX with one block
		writer.printf("%d \n", n);
		assert a.length == k;
		for (int i = 0; i < k; i++) {
			writer.printf("%.18e ", a[i]);
		}
		writer.write("\n");
		for (int blk = 1; blk <= Cnblocks; blk++) {
			for (int i = 1; i <= n; i++) {
				for (int j = i; j <= n; j++) {
					double token = blockmatrixC[ijtok(i, j, n)];
					if (token != 0) {
						writer.printf("0 %d %d %d %.18e \n", blk, i, j, token);
					}
				}
			}
		}
		for (int i = 0; i < k; i++) {
			for (int j = i * n * n; j < (i + 1) * n * n; j++) {
				double value = constraints[j];
				// System.out.println(constraints.length + ": " + "constraint["
				// + j + "] = " + value);
				if (value != 0) {
					// the 1 is the block number
					writer.printf("%d %d %d %d %.18e \n", i + 1, 1, (j - i * n
							* n)
							/ n + 1, (j - i * n * n) % n + 1, value);
				}
			}
		}
		writer.flush();
		writer.close();
		File output = File.createTempFile("keymaera-csdp-output", ".sol");
		output.deleteOnExit();
		ProcessBuilder pb = new ProcessBuilder(DLOptionBean.INSTANCE
				.getCsdpBinary().getAbsolutePath(), tempFile.getAbsolutePath(),
				output.getAbsolutePath());
		Process start = pb.start();

		InputStream inputStream = start.getInputStream();
		InputStreamReader reader = new InputStreamReader(inputStream);
		int read = 0;
		while ((read = reader.read()) != -1) {
			System.out.print((char) read);
		}
		try {
			start.waitFor();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (start.exitValue() == 0 || start.exitValue() == 3) {
			FileReader fReader = new FileReader(output);
			Scanner stok = new Scanner(fReader);
			Arrays.fill(blockmatrixpX, 0);
			Arrays.fill(blockmatrixpZ, 0);

			// first we read k doubles
			for (int i = 0; i < k; i++) {
				py[i] = Double.parseDouble(stok.next());
			}
			// the next numbers provide us the matrices pX and pZ
			// the form is the following 1 1 1 1 1.000000004211176741e+00
			// as long as the first number is 1 we are reading values for pX
			// if it changes to 2 we are reading pZ
			// as we initially used one block per matrix, the result should
			// also contain just one block, therefore the second number is
			// always one
			while (stok.hasNext()) {
				int matrixNumber = -1;
				matrixNumber = (int) Integer.parseInt(stok.next());
				int blockNum = Integer.parseInt(stok.next());
				assert blockNum == 1 : "We can only handle result matrices with one block per matrix.";
				int i = (int) Integer.parseInt(stok.next());
				int j = (int) Integer.parseInt(stok.next());
				if (matrixNumber == 1) {
					String next = stok.next();
					double value = Double.parseDouble(next);
					blockmatrixpZ[ijtok(i, j, n)] = value;
					if(i!=j) {
						blockmatrixpZ[ijtok(j, i, n)] = value;	
					}
				} else if (matrixNumber == 2) {
					String next = stok.next();
					double value = Double.parseDouble(next);
					System.out.println("Next is " + next + " gets parsed to " + value);
					System.out.println("(" + i + ", " + j + ", " + n + ") = " + (ijtok(i, j, n)));
					blockmatrixpX[ijtok(i, j, n)] = value;
					if(i!=j) {
						// fill the other triangle as the input is a sparse format for a diagonal matrix
						blockmatrixpX[ijtok(j, i, n)] = value;
					}
				} else {
					throw new IllegalArgumentException(
							"Dont know how to interpret matrix number "
									+ matrixNumber);
				}
			}
			fReader.close();
			
		}
		return start.exitValue();
	}

	/**
	 * @param i
	 * @param j
	 * @param n
	 * @return TODO documentation since 05.05.2009
	 */
	private static int ijtok(int i, int j, int n) {
		return (j - 1) * n + i - 1;
	}
}
