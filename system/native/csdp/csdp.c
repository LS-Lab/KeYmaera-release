#include<stdlib.h>
#include<assert.h>
#include<declarations.h>
#include"csdp.h"

double *convert_jdoubleArray_to_double_range(JNIEnv * env, jdoubleArray array,
											 int from, int count)
{
	jdouble *element =
		(jdouble *) (*env)->GetDoubleArrayElements(env, array, 0);
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
														jdoubleArray array,
														int n)
{
	union blockdatarec data;
	data.mat = convert_jdoubleArray_to_double(env, array);
	/* CSDP ignores the first record, thus we have to allocate 2 blocks */
	struct blockrec *rec = malloc(2 * sizeof(struct blockrec));
	rec[1].blockcategory = MATRIX;
	rec[1].data = data;
	rec[1].blocksize = n;
	struct blockmatrix *b = malloc(sizeof(struct blockmatrix));
	b->nblocks = 1;
	b->blocks = rec;
	return b;
}

struct constraintmatrix *convert_double_array_to_constraintmatrix(JNIEnv *
																  env, int n,
																  int k,
																  jdoubleArray
																  constraints)
{
	/* again we need to allocate one more block */
	struct constraintmatrix *result =
		malloc((k+1) * sizeof(struct constraintmatrix));
	int l;
	for (l = 1; l <= k; l++) {
		result[l].blocks = malloc(sizeof(struct sparseblock));
		struct sparseblock *block= result[l].blocks;
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
		double *tmp = convert_jdoubleArray_to_double_range(env, constraints,
														   (l - 1) * k, n);
		int size = n - (l - 1) * k;
		block->entries = malloc(sizeof(double) * size + 1);
		int j = 1;
		for (j = 1; j <= size; j++) {
			block->entries[j] = tmp[j - 1];
		}
		free(tmp);
		block->iindices = iindices;
		block->jindices = jindices;
		block->blocknum = 1;
		block->blocksize = 1;
		block->numentries = n;
		block->constraintnum = l;
		block->issparse = 1;
		block->next = 0;
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
														   jdoubleArray pdobj)
{

	struct blockmatrix *_C = convert_double_array_to_blockmatrix(env, C, n);
	struct blockmatrix *_pX = convert_double_array_to_blockmatrix(env, pX, n);
	struct blockmatrix *_pZ = convert_double_array_to_blockmatrix(env, pZ, n);

	struct constraintmatrix *_constraints =
		convert_double_array_to_constraintmatrix(env, (int) n, (int) k,
												 constraints);

	double *tmp = convert_jdoubleArray_to_double(env, a);
	int size = (*env)->GetArrayLength(env, a);
	double *_a = malloc(sizeof(double) * (size + 1));
	int i = 1;
	for (i = 1; i <= size; i++) {
		_a[i] = tmp[i - 1];
	}
	free(tmp);
	tmp = convert_jdoubleArray_to_double(env, py);
	size = (*env)->GetArrayLength(env, py);
	double **_py = malloc(sizeof(double *));
	*_py = malloc(sizeof(double) * size + 1);
	i = 1;
	for (i = 1; i <= size; i++) {
		_py[0][i] = tmp[i - 1];
	}
	free(tmp);
	tmp = convert_jdoubleArray_to_double(env, ppobj);
	size = (*env)->GetArrayLength(env, ppobj);
	double *_ppobj = malloc(sizeof(double) * size + 1);
	i = 1;
	for (i = 1; i <= size; i++) {
		_ppobj[i] = tmp[i - 1];
	}
	free(tmp);
	tmp = convert_jdoubleArray_to_double(env, ppobj);
	size = (*env)->GetArrayLength(env, pdobj);
	double *_pdobj = convert_jdoubleArray_to_double(env, pdobj);
	i = 1;
	for (i = 1; i <= size; i++) {
		_pdobj[i] = tmp[i - 1];
	}
	free(tmp);
	int result = easy_sdp((int) n, (int) k, *_C, _a, _constraints,
						  (double) constant_offset, _pX, _py, _pZ, _ppobj,
						  _pdobj);
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
