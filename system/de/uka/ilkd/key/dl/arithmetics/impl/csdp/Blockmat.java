/**
 * 
 */
package de.uka.ilkd.key.dl.arithmetics.impl.csdp;

/**
 * This file contains definitions for the block matrix data structures used in
 * CSDP 3.0. Note that there are an additional set of definitions used for
 * sparse constraint matrices.
 */
public class Blockmat {
	/**
	 * Each block is a diagonal block or a matrix
	 */
	enum blockcat {
		DIAG, MATRIX, PACKEDMATRIX
	}

	/**
	 * A block data record contains a pointer to the actual data for the block.
	 * Note that matrices are stored in Fortran form, while vectors are stored
	 * using indices 1, 2, ..., n.
	 */
	class blockdatarec {
		double[] vec;
		double[] mat;
		boolean isVector;
	}
	
	/**
	 * A block record describes an individual block within a matrix.
	 */
	class blockrec {
	  blockdatarec data;
	  blockcat blockcategory;
	  int blocksize;
	}

	/**
	 * A block matrix contains an entire matrix in block diagonal form.
	 */
	class blockmatrix {
	  int nblocks;
	  blockrec[] blocks;
	}

	/*
	 * Definition for constraint matrices.
	 */

	/**
	 * There's one of these for each nonzero block in each constraint matrix.
	 */
	class sparseblock {
	  sparseblock[] next;
	  sparseblock[] nextbyblock;
	  double[] entries;
	  int[] iindices;
	  int[] jindices;
	  int numentries;
	  int blocknum;
	  int blocksize;
	  int constraintnum;
	  int issparse;
	}
	
	/**
	 * A constraint matrix is just an array of pointers to linked lists of the
	 * constraint blocks.
	 */
	class constraintmatrix {
	  sparseblock[] blocks;
	}

}
