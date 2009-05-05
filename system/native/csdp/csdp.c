#include<stdlib.h>
#include<stdio.h>
#include<assert.h>
#include<declarations.h>
#include"csdp.h"

double *convert_jdoubleArray_to_double_range(JNIEnv * env, jdoubleArray array,
					     int from, int count, int shift)
{
  //	printf("Converting an array from %d %d elements\n", from, count);
	jboolean isCopy;
	jdouble *element =
		(jdouble *) (*env)->GetDoubleArrayElements(env, array, &isCopy);
	int size = (*env)->GetArrayLength(env, array);
	assert(count + from <= size);
	//	printf("Now allocating %d doubles\n", count + shift);
	double *result = malloc(sizeof(double) * (count + shift));
	if(result == NULL) {
		printf("Could not allocate memory\n");
		exit(1);
	}
	int j;
	for (j = 0; j < count; j++) {
		/* dont set anything to array element 0. fortran indexes (1..n) */
	  //	assert(j < count && from + j < size);
		//		printf("Converting element %d\n", j + shift);
		result[j + shift] = element[from + j];
	}
	//	printf("Finshed copying\n");
	if(isCopy == JNI_TRUE) {
		(*env)->ReleaseDoubleArrayElements(env, array, element, JNI_ABORT);
	}
		printf("Returning result\n");
	return result;
}

double *convert_jdoubleArray_to_double(JNIEnv * env, jdoubleArray array, int shift)
{
	int size = (*env)->GetArrayLength(env, array);
	return convert_jdoubleArray_to_double_range(env, array, 0, size, shift);
}

struct blockmatrix convert_double_array_to_blockmatrix_single(JNIEnv * env,
							      jdoubleArray array,
							      int n)
{
	union blockdatarec data;
	data.mat = convert_jdoubleArray_to_double(env, array, 0);
	/* CSDP ignores the first record, thus we have to allocate 2 blocks */
	struct blockrec *rec = malloc(2 * sizeof(struct blockrec));
	rec[1].blockcategory = MATRIX;
	rec[1].data = data;
	rec[1].blocksize = n;
	struct blockmatrix b;
	b.nblocks = 1;
	b.blocks = rec;
	return b;
}

struct blockmatrix convert_double_array_to_blockmatrix(JNIEnv * env,
						       jdoubleArray array,
						       int blockNum,
						       int blockSizes[])
{
	struct blockmatrix b;
	b.nblocks = blockNum;
	/* CSDP ignores the first record, thus we have to allocate one
	   additional block */
	struct blockrec *rec = malloc((blockNum + 1) * sizeof(struct blockrec));
	b.blocks = rec;

	int offset = 0;
	for (int i = 0; i < blockNum; ++i) {
	  int blockLength = blockSizes[i] * blockSizes[i];

	  union blockdatarec data;
	  data.mat = convert_jdoubleArray_to_double_range(env, array,
							  offset, blockLength, 0);
	  rec[i+1].blockcategory = MATRIX;
	  rec[i+1].data = data;
	  rec[i+1].blocksize = blockSizes[i];

	  offset += blockLength;
	}
	return b;
}

struct sparseblock *convert_array_to_sparseblock(int blockSize,
						 jdouble *matrix_data) {
  
  int blockLength = blockSize * blockSize;

  /* now we need to find out howmany non zero entries the matrix contains */
  int ii;
  int nonzero = 0;
  for (ii = 0; ii < blockLength; ii++) {
    if (matrix_data[ii] != 0) {
      nonzero++;
    }
  }

  if (nonzero == 0)
    /* Then we can skip this block */
    return NULL;

  struct sparseblock *result = malloc(sizeof(struct sparseblock));

#ifndef NOSHORTS
#define INDICES_TYPE unsigned short
#else
#define INDICES_TYPE int
#endif
  result->iindices = malloc(sizeof(INDICES_TYPE) * (nonzero + 1));
  result->jindices = malloc(sizeof(INDICES_TYPE) * (nonzero + 1));

  result->entries = malloc(sizeof(double) * (nonzero + 1));

  int curentry = 1;
  for (ii = 0; ii < blockLength; ii++) {
    if (matrix_data[ii] != 0) {
      result->entries[curentry] = matrix_data[ii];
      result->iindices[curentry] = ii / blockSize + 1;
      assert(result->iindices[curentry] <= blockSize);
      result->jindices[curentry] = ii % blockSize + 1;
      assert(result->jindices[curentry] <= blockSize);
      curentry++;
    }
  }

  result->blocksize = blockSize;
  result->numentries = nonzero;
  result->issparse = 1;
  result->nextbyblock = NULL;

  return result;
}

struct constraintmatrix *convert_double_array_to_constraintmatrix(JNIEnv * env,
								  int k,
								  int blockNum,
								  int blockSizes[],
								  jdoubleArray constraints)
{
  /* again we need to allocate one more block */
  struct constraintmatrix *result =
    malloc((k + 1) * sizeof(struct constraintmatrix));
	
  jboolean isCopy;
  jdouble *element =
    (jdouble *) (*env)->GetDoubleArrayElements(env, constraints, &isCopy);

  int offset = 0;
  /* Iterate over all constraints */
  for (int constr = 1; constr <= k; constr++) {

    result[constr].blocks = NULL;

    /* Iterate over all blocks */
    for (int blk = 0; blk < blockNum; ++blk) {
      int blockSize = blockSizes[blk];

      struct sparseblock *block =
	convert_array_to_sparseblock(blockSize, element + offset);

      if (block != NULL) {
	block->blocknum = blk + 1;
	block->constraintnum = constr;

	/* Insert the new block into the list of blocks. The blocks
	   will be generated in reversed order, which is hopefully not
	   a problem for CSDP ...  */
	block->next = result[constr].blocks;
	result[constr].blocks = block;
      }
      
      offset += blockSize * blockSize;
    }
  }

  if(isCopy == JNI_TRUE) {
    (*env)->ReleaseDoubleArrayElements(env, constraints, element, JNI_ABORT);
  }

  return result;
}

void insert_results_bmatrix(JNIEnv * env, jdoubleArray out,
			    struct blockmatrix *in)
{
    jboolean isCopy;
    jdouble* destArrayElems = 
      (*env)->GetDoubleArrayElements(env, out, &isCopy);
    int i,j;
    int index = 0;
    for(j=1; j <= in->nblocks; j++)
      {
	struct blockrec block = in->blocks[j];
	assert (block.blockcategory == MATRIX);
	int size = block.blocksize;
	int length = size * size;
	for(i = 0; i < length; i++)
	  {
	    destArrayElems[index++] = block.data.mat[i];
	  }
      }
    if(isCopy == JNI_TRUE) {
      (*env)->ReleaseDoubleArrayElements(env, out, destArrayElems, 0);
    }
}

void insert_results_array(JNIEnv * env, jdoubleArray out, double *in, int size)
{
    jboolean isCopy;
    jdouble* destArrayElems = 
           (*env)->GetDoubleArrayElements(env, out, &isCopy);
	int i;
	for(i = 0; i < size; i++)
	{
		destArrayElems[i] = in[i+1];
	}
	if(isCopy == JNI_TRUE) {
		(*env)->ReleaseDoubleArrayElements(env, out, destArrayElems, 0);
	}
}

struct constraintmatrix *create_test_constraints();

JNIEXPORT jint JNICALL
	Java_de_uka_ilkd_key_dl_arithmetics_impl_csdp_CSDP_easySDP(env, clazz, jBlockSizes,
								   k, C, a,
								   constraints,
								   constant_offset,
								   pX, py, pZ,
								   ppobj, pdobj)
	 JNIEnv *env;
	 jclass clazz;
	 jintArray jBlockSizes;
	 jint k;
	 jdoubleArray C;
	 jdoubleArray a;
	 jdoubleArray constraints;
	 jdouble constant_offset;
	 jdoubleArray pX;
	 jdoubleArray py;
	 jdoubleArray pZ;
	 jdoubleArray ppobj;
	 jdoubleArray pdobj;
{

	assert(sizeof(double) == sizeof(jdouble));
	assert(sizeof(int) == sizeof(jint));

	jboolean isCopy;
	jint blockNum = (*env)->GetArrayLength(env, jBlockSizes);
	jint* blockSizes = 
	  (*env)->GetIntArrayElements(env, jBlockSizes, &isCopy);

	int n = 0;
	for (int i = 0; i < blockNum; ++i)
	  n += blockSizes[i];

	struct blockmatrix _C =
	  convert_double_array_to_blockmatrix(env, C,
					      blockNum, blockSizes);

	struct constraintmatrix *_constraints =
	  convert_double_array_to_constraintmatrix(env, (int) k,
						   blockNum, blockSizes,
						   constraints);

	double *_a = convert_jdoubleArray_to_double(env, a, 1);

	write_prob("prob-2.dat-s", n, k, _C, _a, _constraints);

	struct blockmatrix X, Z;
	double *y;
	double pobj, dobj;

	printf("n = %d, k = %d\n", n, k);
	initsoln(n, k, _C, _a, _constraints, &X, &y, &Z);
	int result = easy_sdp((int) n, (int) k, _C, _a, _constraints,
						  (double) constant_offset, &X, &y, &Z, &pobj,
						  &dobj);

	write_sol("prob-2.sol", n, k, X, y, Z);

	insert_results_bmatrix(env, pX, &X);
	insert_results_array(env, py, y, k);
	insert_results_bmatrix(env, pZ, &Z);
	insert_results_array(env, ppobj, &pobj, n);
	insert_results_array(env, pdobj, &dobj, k);

	free_prob(n, k, _C, _a, _constraints, X, y, Z);

	if(isCopy == JNI_TRUE) {
	  (*env)->ReleaseIntArrayElements(env, jBlockSizes, blockSizes, 0);
	}

	return result;
}


/*
 * Test DATA
 */
struct blockmatrix create_test_blockmatrix()
{
	struct blockmatrix C;

	/*
	 * First, allocate storage for the C matrix.  We have three blocks, but
	 * because C starts arrays with index 0, we have to allocate space for
	 * four blocks- we'll waste the 0th block.  Notice that we check to 
	 * make sure that the malloc succeeded.
	 */

	C.nblocks = 1;
	C.blocks = (struct blockrec *) malloc(4 * sizeof(struct blockrec));
	if (C.blocks == NULL) {
		printf("Couldn't allocate storage for C!\n");
		exit(1);
	};

	/*
	 * Setup the first block.
	 */

	C.blocks[1].blockcategory = MATRIX;
	C.blocks[1].blocksize = 7;
	C.blocks[1].data.mat = (double *) malloc(7 * 7 * sizeof(double));
	if (C.blocks[1].data.mat == NULL) {
		printf("Couldn't allocate storage for C!\n");
		exit(1);
	};

	/*
	 * Put the entries into the first block.
	 */

	C.blocks[1].data.mat[ijtok(1, 1, 7)] = 2.0;
	C.blocks[1].data.mat[ijtok(1, 2, 7)] = 1.0;
	C.blocks[1].data.mat[ijtok(1, 3, 7)] = 0.0;
	C.blocks[1].data.mat[ijtok(1, 4, 7)] = 0.0;
	C.blocks[1].data.mat[ijtok(1, 5, 7)] = 0.0;
	C.blocks[1].data.mat[ijtok(1, 6, 7)] = 0.0;
	C.blocks[1].data.mat[ijtok(1, 7, 7)] = 0.0;

	C.blocks[1].data.mat[ijtok(2, 1, 7)] = 1.0;
	C.blocks[1].data.mat[ijtok(2, 2, 7)] = 2.0;
	C.blocks[1].data.mat[ijtok(2, 3, 7)] = 0.0;
	C.blocks[1].data.mat[ijtok(2, 4, 7)] = 0.0;
	C.blocks[1].data.mat[ijtok(2, 5, 7)] = 0.0;
	C.blocks[1].data.mat[ijtok(2, 6, 7)] = 0.0;
	C.blocks[1].data.mat[ijtok(2, 7, 7)] = 0.0;

	C.blocks[1].data.mat[ijtok(3, 1, 7)] = 0.0;
	C.blocks[1].data.mat[ijtok(3, 2, 7)] = 0.0;
	C.blocks[1].data.mat[ijtok(3, 3, 7)] = 3.0;
	C.blocks[1].data.mat[ijtok(3, 4, 7)] = 0.0;
	C.blocks[1].data.mat[ijtok(3, 5, 7)] = 1.0;
	C.blocks[1].data.mat[ijtok(3, 6, 7)] = 0.0;
	C.blocks[1].data.mat[ijtok(3, 7, 7)] = 0.0;

	C.blocks[1].data.mat[ijtok(4, 1, 7)] = 0.0;
	C.blocks[1].data.mat[ijtok(4, 2, 7)] = 0.0;
	C.blocks[1].data.mat[ijtok(4, 3, 7)] = 0.0;
	C.blocks[1].data.mat[ijtok(4, 4, 7)] = 2.0;
	C.blocks[1].data.mat[ijtok(4, 5, 7)] = 0.0;
	C.blocks[1].data.mat[ijtok(4, 6, 7)] = 0.0;
	C.blocks[1].data.mat[ijtok(4, 7, 7)] = 0.0;

	C.blocks[1].data.mat[ijtok(5, 1, 7)] = 0.0;
	C.blocks[1].data.mat[ijtok(5, 2, 7)] = 0.0;
	C.blocks[1].data.mat[ijtok(5, 3, 7)] = 1.0;
	C.blocks[1].data.mat[ijtok(5, 4, 7)] = 0.0;
	C.blocks[1].data.mat[ijtok(5, 5, 7)] = 3.0;
	C.blocks[1].data.mat[ijtok(5, 6, 7)] = 0.0;
	C.blocks[1].data.mat[ijtok(5, 7, 7)] = 0.0;

	C.blocks[1].data.mat[ijtok(6, 1, 7)] = 0.0;
	C.blocks[1].data.mat[ijtok(6, 2, 7)] = 0.0;
	C.blocks[1].data.mat[ijtok(6, 3, 7)] = 0.0;
	C.blocks[1].data.mat[ijtok(6, 4, 7)] = 0.0;
	C.blocks[1].data.mat[ijtok(6, 5, 7)] = 0.0;
	C.blocks[1].data.mat[ijtok(6, 6, 7)] = 0.0;
	C.blocks[1].data.mat[ijtok(6, 7, 7)] = 0.0;

	C.blocks[1].data.mat[ijtok(7, 1, 7)] = 0.0;
	C.blocks[1].data.mat[ijtok(7, 2, 7)] = 0.0;
	C.blocks[1].data.mat[ijtok(7, 3, 7)] = 0.0;
	C.blocks[1].data.mat[ijtok(7, 4, 7)] = 0.0;
	C.blocks[1].data.mat[ijtok(7, 5, 7)] = 0.0;
	C.blocks[1].data.mat[ijtok(7, 6, 7)] = 0.0;
	C.blocks[1].data.mat[ijtok(7, 7, 7)] = 0.0;

	return C;
}

struct constraintmatrix *create_test_constraints()
{
	struct constraintmatrix *constraints;


	/*
	 * The next major step is to setup the two constraint matrices A1 and A2.
	 * Again, because C indexing starts with 0, we have to allocate space for
	 * one more constraint.  constraints[0] is not used.
	 */

	constraints =
		(struct constraintmatrix *) malloc((2 + 1) *
										   sizeof(struct constraintmatrix));
	if (constraints == NULL) {
		printf("Failed to allocate storage for constraints!\n");
		exit(1);
	};

	/*
	 * Setup the A1 matrix.  Note that we start with block 3 of A1 and then
	 * do block 1 of A1.  We do this in this order because the blocks will
	 * be inserted into the linked list of A1 blocks in reverse order.  
	 */

	/*
	 * Terminate the linked list with a NULL pointer.
	 */

	constraints[1].blocks = NULL;

	/*
	 * Now, we handle block 3 of A1.
	 */

	/*
	 * blockptr will be used to point to blocks in constraint matrices.
	 */

	struct sparseblock *blockptr;

	/*
	 * Allocate space for block 3 of A1.
	 */

	blockptr = (struct sparseblock *) malloc(sizeof(struct sparseblock));
	if (blockptr == NULL) {
		printf("Allocation of constraint block failed!\n");
		exit(1);
	};

	/*
	 * Initialize block 1.
	 */

	printf("Now creating constraint 1\n");

	blockptr->blocknum = 1;
	blockptr->blocksize = 7;
	blockptr->constraintnum = 1;
	blockptr->next = NULL;
	blockptr->nextbyblock = NULL;
	blockptr->entries = (double *) malloc((5 + 1) * sizeof(double));
	if (blockptr->entries == NULL) {
		printf("Allocation of constraint block failed!\n");
		exit(1);
	};
	blockptr->iindices = (int *) malloc((5 + 1) * sizeof(int));
	if (blockptr->iindices == NULL) {
		printf("Allocation of constraint block failed!\n");
		exit(1);
	};
	blockptr->jindices = (int *) malloc((5 + 1) * sizeof(int));
	if (blockptr->jindices == NULL) {
		printf("Allocation of constraint block failed!\n");
		exit(1);
	};

	/*
	 * We have 1 nonzero entry in the upper triangle of block 3 of A1.
	 */

	blockptr->numentries = 5;

	/*
	 * The entry in the 1,1 position of block 3 of A1 is 1.0
	 */

	blockptr->iindices[1] = 1;
	blockptr->jindices[1] = 1;
	blockptr->entries[1] = 3.0;

	blockptr->iindices[2] = 2;
	blockptr->jindices[2] = 1;
	blockptr->entries[2] = 1.0;

	blockptr->iindices[3] = 2;
	blockptr->jindices[3] = 2;
	blockptr->entries[3] = 3.0;

	blockptr->iindices[4] = 1;
	blockptr->jindices[4] = 2;
	blockptr->entries[4] = 1.0;

	blockptr->iindices[5] = 6;
	blockptr->jindices[5] = 6;
	blockptr->entries[5] = 1.0;

	blockptr->next = constraints[1].blocks;
	constraints[1].blocks = blockptr;

	/*
	 * Setup the A2 matrix.  This time, there are nonzero entries in block 3
	 * and block 2.  We start with block 3 of A2 and then do block 1 of A2. 
	 */

	/*
	 * Terminate the linked list with a NULL pointer.
	 */

	constraints[2].blocks = NULL;

	/*
	 * First, we handle block 3 of A2.
	 */

	/*
	 * Allocate space for block 3 of A2.
	 */

	blockptr = (struct sparseblock *) malloc(sizeof(struct sparseblock));
	if (blockptr == NULL) {
		printf("Allocation of constraint block failed!\n");
		exit(1);
	};

	/*
	 * Initialize block 3.
	 */

	printf("Now creating constraint 2\n");

	blockptr->blocknum = 1;
	blockptr->blocksize = 7;
	blockptr->constraintnum = 2;
	blockptr->next = NULL;
	blockptr->nextbyblock = NULL;
	blockptr->entries = (double *) malloc((6 + 1) * sizeof(double));
	if (blockptr->entries == NULL) {
		printf("Allocation of constraint block failed!\n");
		exit(1);
	};
	blockptr->iindices = (int *) malloc((6 + 1) * sizeof(int));
	if (blockptr->iindices == NULL) {
		printf("Allocation of constraint block failed!\n");
		exit(1);
	};
	blockptr->jindices = (int *) malloc((6 + 1) * sizeof(int));
	if (blockptr->jindices == NULL) {
		printf("Allocation of constraint block failed!\n");
		exit(1);
	};

	/*
	 * We have 1 nonzero entry in the upper triangle of block 3 of A2.
	 */

	blockptr->numentries = 6;


	/*
	 * The entry in the 2,2 position of block 3 of A2 is 1.0
	 */

	blockptr->iindices[1] = 3;
	blockptr->jindices[1] = 3;
	blockptr->entries[1] = 3.0;

	blockptr->iindices[2] = 3;
	blockptr->jindices[2] = 5;
	blockptr->entries[2] = 1.0;

	blockptr->iindices[3] = 5;
	blockptr->jindices[3] = 3;
	blockptr->entries[3] = 1.0;

	blockptr->iindices[4] = 4;
	blockptr->jindices[4] = 4;
	blockptr->entries[4] = 4.0;

	blockptr->iindices[5] = 5;
	blockptr->jindices[5] = 5;
	blockptr->entries[5] = 5.0;

	blockptr->iindices[6] = 7;
	blockptr->jindices[6] = 7;
	blockptr->entries[6] = 1.0;

	/*
	 * Insert block 3 into the linked list of A2 blocks.  
	 */

	blockptr->next = constraints[2].blocks;
	constraints[2].blocks = blockptr;


	printf("Now outputting constraints\n");

	int counter;
	for (counter = 1; counter <= 2; counter++) {
		struct sparseblock *p = constraints[counter].blocks;
		printf
			("\n counter: %d, blocknum: %d, constraintnum: %d, blocksize: %d\n",
			 counter, p->blocknum, p->constraintnum, p->blocksize);
	}
	return constraints;
}

/********************************************************************* 
 * Begin external test methods
********************************************************************/
JNIEXPORT jint JNICALL
	Java_de_uka_ilkd_key_dl_arithmetics_impl_csdp_CSDP_test2(env, clazz, n, k,
															 C, a,
															 constraints,
															 constant_offset,
															 pX, py, pZ,
															 ppobj, pdobj)
	 JNIEnv *env;
	 jclass clazz;
	 jint n;
	 jint k;
	 jdoubleArray C;
	 jdoubleArray a;
	 jdoubleArray constraints;
	 jdouble constant_offset;
	 jdoubleArray pX;
	 jdoubleArray py;
	 jdoubleArray pZ;
	 jdoubleArray ppobj;
	 jdoubleArray pdobj;
{

	struct blockmatrix _C = convert_double_array_to_blockmatrix_single(env, C, n);
	/*struct blockmatrix _C = create_test_blockmatrix(); */

	/*struct constraintmatrix *_constraints =
	   convert_double_array_to_constraintmatrix(env, (int) n, (int) k,
	   constraints); */
	struct constraintmatrix *_constraints = create_test_constraints();

	double *_a = convert_jdoubleArray_to_double(env, a, 1);

	write_prob("prob-2.dat-s", n, k, _C, _a, _constraints);

	struct blockmatrix X, Z;
	double *y;
	double pobj, dobj;

	initsoln(n, k, _C, _a, _constraints, &X, &y, &Z);
	printf("Now starting easy sdp\n");
	int result = easy_sdp((int) n, (int) k, _C, _a, _constraints,
						  (double) constant_offset, &X, &y, &Z, &pobj,
						  &dobj);

	printf("result is: %d\n", result);

	/*insert_results_bmatrix(env, pX, _pX);
	   insert_results_array2D(env, py, _py);
	   insert_results_bmatrix(env, pZ, _pZ); */
	/*insert_results_array(env, ppobj, _ppobj);
	   insert_results_array(env, pdobj, _pdobj); */

	printf("freeing problemdata\n");

	free_prob(n, k, _C, _a, _constraints, X, y, Z);
	return result;
}

JNIEXPORT jint JNICALL
Java_de_uka_ilkd_key_dl_arithmetics_impl_csdp_CSDP_test(JNIEnv * env,
														jclass clazz)
{
	/*
	 * The problem and solution data.
	 */

	struct blockmatrix C;
	double *b;
	struct constraintmatrix *constraints;

	/*
	 * Storage for the initial and final solutions.
	 */

	struct blockmatrix X, Z;
	double *y;
	double pobj, dobj;


	/*
	 * A return code for the call to easy_sdp().
	 */

	int ret;

	/*
	 * The first major task is to setup the C matrix and right hand side b.
	 */

	C = create_test_blockmatrix();


	/*
	 * Allocate storage for the right hand side, b.
	 */

	b = (double *) malloc((2 + 1) * sizeof(double));
	if (b == NULL) {
		printf("Failed to allocate storage for a!\n");
		exit(1);
	};

	/*
	 * Fill in the entries in b.
	 */

	b[1] = 1.0;
	b[2] = 2.0;


	constraints = create_test_constraints();



	/*
	 * At this point, we have all of the problem data setup.
	 */

	/*
	 * Write the problem out in SDPA sparse format.
	 */

	write_prob("prob.dat-s", 7, 2, C, b, constraints);

	printf("After write_prob");

	/*
	 * Create an initial solution.  This allocates space for X, y, and Z,
	 * and sets initial values.
	 */

	initsoln(7, 2, C, b, constraints, &X, &y, &Z);

	printf("After initsol");
	int counter;
	for (counter = 1; counter <= 2; counter++) {
		struct sparseblock *p = constraints[counter].blocks;
		printf
			("\n counter: %d, blocknum: %d, constraintnum: %d, blocksize: %d\n",
			 counter, p->blocknum, p->constraintnum, p->blocksize);
	}

	/*
	 * Solve the problem.
	 */

	ret = easy_sdp(7, 2, C, b, constraints, 0.0, &X, &y, &Z, &pobj, &dobj);

	if (ret == 0)
		printf("The objective value is %.7e \n", (dobj + pobj) / 2);
	else
		printf("SDP failed.\n");

	/*
	 * Write out the problem solution.
	 */

	write_sol("prob.sol", 7, 2, X, y, Z);

	/*
	 * Free storage allocated for the problem and return.
	 */

	free_prob(7, 2, C, b, constraints, X, y, Z);
	return 0;
}
