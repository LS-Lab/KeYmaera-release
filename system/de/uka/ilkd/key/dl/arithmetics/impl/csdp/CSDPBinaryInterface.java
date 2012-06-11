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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
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
	public static int easySDP(int[] blocksizes, int k, double[] blockmatrixC,
			double[] a, double[] constraints, double constant_offset,
			double[] blockmatrixpX, double[] py, double[] blockmatrixpZ,
			double[] ppobj, double[] pdobj) throws IOException {
		final File tempFile = File.createTempFile("keymaera-csdp", ".dat-s");
		tempFile.deleteOnExit();
		System.out.println("Writing to " + tempFile.getAbsolutePath());// XXX
		final PrintWriter writer = new PrintWriter(new BufferedWriter(
				new FileWriter(tempFile)));
		writer.printf("%d \n", k);
		writer.printf("%d \n", blocksizes.length);
		for (int i = 0; i < blocksizes.length; i++) {
			writer.printf("%d ", blocksizes[i]);
		}
		writer.printf("\n");
		assert a.length == k;
		for (int i = 0; i < k; i++) {
			writer.printf("%.18e ", a[i]);
		}
		writer.write("\n");
		int offset = 0;
		for (int blk = 1; blk <= blocksizes.length; blk++) {
			int blkSize = blocksizes[blk - 1];
			for (int i = 1; i <= blkSize; i++) {
				for (int j = i; j <= blkSize; j++) {
					final double token = blockmatrixC[ijtok(i, j, blkSize)
							+ offset];
					if (token != 0) {
						writer.printf("0 %d %d %d %.18e \n", blk, i, j, token);
					}
				}
			}
			offset += blkSize * blkSize;
		}
		offset = 0;
		for (int i = 0; i < k; i++) {
			for (int blk = 1; blk <= blocksizes.length; blk++) {
				int curBlockMatSize = blocksizes[blk - 1] * blocksizes[blk - 1];
				for (int j = 0; j < curBlockMatSize; j++) {
					final double value = constraints[j + offset];
					// System.out.println(constraints.length + ": " +
					// "constraint["
					// + j + "] = " + value);
					if (value != 0) {
						writer.printf("%d %d %d %d %.18e \n", i + 1, blk, j
								/ blocksizes[blk - 1] + 1, j
								% blocksizes[blk - 1] + 1, value);
					}
				}
				offset += curBlockMatSize;
			}
		}
		writer.flush();
		writer.close();
		final File output = File.createTempFile("keymaera-csdp-output", ".sol");
		output.deleteOnExit();
		final ProcessBuilder pb = new ProcessBuilder(DLOptionBean.INSTANCE
				.getCsdpBinary().getAbsolutePath(), tempFile.getAbsolutePath(),
				output.getAbsolutePath());
		final Process start = pb.start();

		final InputStream inputStream = start.getInputStream();
		final InputStreamReader reader = new InputStreamReader(inputStream);
		int read = 0;
		while ((read = reader.read()) != -1) {
			System.out.print((char) read);
		}
        try {
            start.waitFor();

            if (start.exitValue() == 0 || start.exitValue() == 3) {
                final FileReader fReader = new FileReader(output);
                final Scanner stok = new Scanner(fReader);
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
                    if (matrixNumber == 1) {
                        addLineToMatrix(blockmatrixpZ, blocksizes, stok);
                    } else if (matrixNumber == 2) {
                        addLineToMatrix(blockmatrixpX, blocksizes, stok);
                    } else {
                        throw new IllegalArgumentException(
                                "Dont know how to interpret matrix number "
                                        + matrixNumber);
                    }
                }
                fReader.close();

            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new IllegalStateException("Interrupted", e);
        } finally {
            if (start != null) {
                start.destroy();
            }
        }
		return start.exitValue();
	}

	/**
	 * @param mat
	 * @param stok
	 *            TODO documentation since 06.05.2009
	 */
	private static void addLineToMatrix(double[] mat, int[] blocksizes,
			Scanner stok) {
		final int blockNum = Integer.parseInt(stok.next());
		final int i = (int) Integer.parseInt(stok.next());
		final int j = (int) Integer.parseInt(stok.next());
		final String next = stok.next();
		final double value = Double.parseDouble(next);
		int offset = 0;
		for (int off = 0; off < blockNum - 1; off++) {
			offset += blocksizes[off] * blocksizes[off];
		}
		mat[ijtok(i, j, blocksizes[blockNum - 1]) + offset] = value;
		if (i != j) {
			// fill the other triangle as the input is a sparse
			// format for a diagonal matrix
			mat[ijtok(j, i, blocksizes[blockNum - 1]) + offset] = value;
		}
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
