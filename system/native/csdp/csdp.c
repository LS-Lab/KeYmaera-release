#include<stdlib.h>
#include<assert.h>
#include<declarations.h>
#include"csdp.h"

double *convert_jdoubleArray_to_double_range(JNIEnv * env, jdoubleArray array,
											 int from, int count)
{
	jdouble *element = (jdouble *) (*env)->GetDoubleArrayElements(env, array, 0);
	int size = (*env)->GetArrayLength(env, array);
	assert(count - from <= size && from < size);
	double *result = malloc(sizeof(double) * count);
	int j;
	for (j = 0; j <= count; j++) {
		result[j] = element[from + j];
	}
	return result;
}

double *convert_jdoubleArray_to_double(JNIEnv * env, jdoubleArray array)
{
	int size = (*env)->GetArrayLength(env, array);
	return convert_jdoubleArray_to_double_range(env, array, 0, size);
}

struct blockmatrix *convert_double_array_to_blockmatrix(JNIEnv * env,
														jdoubleArray array)
{
	union blockdatarec data;
	data.mat = convert_jdoubleArray_to_double(env, array);
	struct blockrec rec;
	rec.blockcategory = MATRIX;
	rec.data = data;
	struct blockmatrix *b = malloc(sizeof(struct blockmatrix));
	b->nblocks = 1;
	b->blocks = &rec;
	return b;
}

struct constraintmatrix *convert_double_array_to_constraintmatrix(JNIEnv *
																  env, int n,
																  int k,
																  jdoubleArray
																  constraints)
{
	struct constraintmatrix *result = malloc(sizeof(struct constraintmatrix));
	struct sparseblock *block;
	result->blocks = malloc(sizeof(struct sparseblock) * (k + 1));
	result->blocks[0].next = result->blocks + 1;
	block = result->blocks->next;
#ifdef NOSHORTS
#define INDICES_TYPE unsigned short
#else
#define INDICES_TYPE int
#endif
	INDICES_TYPE *iindices = malloc(sizeof(INDICES_TYPE) * n / 2);
	INDICES_TYPE *jindices = malloc(sizeof(INDICES_TYPE) * n / 2);
	INDICES_TYPE i;
	for (i = 1; i <= n / 2 + 1; i++) {
		iindices[i] = i;
		jindices[i] = i;
	}
	for (i = 1; i <= k; i++) {
		block->entries =
			convert_jdoubleArray_to_double_range(env, constraints,
												 (i - 1) * k, n);
		block->iindices = iindices;
		block->jindices = jindices;
		block->blocknum = 1;
		block->blocksize = 1;
		block->numentries = n;
		block->constraintnum = i;
		block->issparse = 1;
		block = block->next;
	}
}

void insert_results_bmatrix(JNIEnv * env, jdoubleArray out,
							struct blockmatrix *in)
{
#warning implement
}

void insert_results_array(JNIEnv * env, jdoubleArray out, double *in)
{
#warning implement
}

void insert_results_array2D(JNIEnv * env, jdoubleArray out, double **in)
{
#warning implement
}

void free_matrix(struct blockmatrix *matrix)
{
#warning is this enough to free the double*?
	free(matrix->blocks[0].data.mat);
	free(matrix);
}

JNIEXPORT jint JNICALL
Java_de_uka_ilkd_key_dl_arithmetics_impl_csdp_CSDP_easySDP(JNIEnv * env,
															jclass clazz,
															jint n, jint k,
															BMATRIX C,
															jdoubleArray a,
															CMATRIX
															constraints,
															jdouble
															constant_offset,
															BMATRIX pX,
															jdoubleArray py,
															BMATRIX pZ,
															jdoubleArray
															ppobj,
															jdoubleArray
															pdobj)
{

	struct blockmatrix *_C = convert_double_array_to_blockmatrix(env, C);
	struct blockmatrix *_pX = convert_double_array_to_blockmatrix(env, pX);
	struct blockmatrix *_pZ = convert_double_array_to_blockmatrix(env, pZ);

	struct constraintmatrix *_constraints =
		convert_double_array_to_constraintmatrix(env, (int) n, (int) k,
												 constraints);

	double *_a = convert_jdoubleArray_to_double(env, a);
	double *_tmppy = convert_jdoubleArray_to_double(env, py);
	double **_py = &_tmppy;
	double *_ppobj = convert_jdoubleArray_to_double(env, ppobj);
	double *_pdobj = convert_jdoubleArray_to_double(env, pdobj);
	int result =
		easy_sdp((int) n, (int) k, *_C, _a, _constraints,
				 (double) constant_offset, _pX, _py, _pZ, _ppobj, _pdobj);
	insert_results_bmatrix(env, pX, _pX);
	insert_results_array2D(env, py, _py);
	insert_results_bmatrix(env, pZ, _pZ);
	insert_results_array(env, ppobj, _ppobj);
	insert_results_array(env, pdobj, _pdobj);

	free_matrix(_C);
	free_matrix(_pX);
	free_matrix(_pZ);
	free(_a);
	free(_py);
	free(_ppobj);
	free(_pdobj);
	return result;
}
