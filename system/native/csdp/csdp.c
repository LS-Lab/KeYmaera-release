#include<stdlib.h>
#include<declarations.h>
#include"csdp.h"

double* convert_jdoubleArray_to_double(JNIEnv* env, jdoubleArray array) {
	jdouble *element = (jdouble*) GetdoubleArrayElements(env, array, 0);
	int size = GetArrayLength(env, array);
	double* result = malloc(sizeof(double)*size);
	int j;
	for (j = 0; j < size; j++) {
		result[j] = element[j];
	}
	return result;
}

double** convert_jdoubleArray_to_double2D(JNIEnv* env, jdoubleArray array) {
#warning implement
	return 0;
}

struct blockmatrix* convert_double_array_to_blockmatrix(JNIEnv* env, jdoubleArray array) {
	union blockdatarec data;
	data.mat = convert_jdoubleArray_to_double(env, array);
	struct blockrec rec;
	rec.blockcategory = MATRIX;
	rec.data = data;
	struct blockmatrix* b = malloc(sizeof(struct blockmatrix));
	b->nblocks = 1;
	b->blocks = &rec;
	return b;
}

void insert_results_bmatrix(JNIEnv* env, jdoubleArray out, struct blockmatrix* in) {
#warning implement
}

void insert_results_array(JNIEnv* env, jdoubleArray out, double* in) {
#warning implement
}

void insert_results_array2D(JNIEnv* env, jdoubleArray out, double** in) {
#warning implement
}

void free_matrix(struct blockmatrix* matrix) {
#warning is this enough to free the double*?
	free(matrix->blocks[0].data.mat);
	free(matrix);
}

JNIEXPORT jint JNICALL
Java_de_uka_ilkd_key_dl_arithmetics_impl_csdp_CSDP_easy_spd(JNIEnv * env,
															jclass clazz,
															jint n, jint k,
															bmatrix C,
															jdoubleArray a,
															cmatrix
															constraints,
															jdouble
															constant_offset,
															bmatrix pX,
															jdoubleArray py,
															bmatrix pZ,
															jdoubleArray
															ppobj,
															jdoubleArray
															pdobj)
{

	struct blockmatrix* _tmpC = convert_double_array_to_blockmatrix(env, C);
	struct blockmatrix* _pX = convert_double_array_to_blockmatrix(env, pX);
	struct blockmatrix* _pZ = convert_double_array_to_blockmatrix(env, pZ);

	struct constraintmatrix* _constraints;

	struct blockmatrix _C;
	_C.nblocks = _tmpC->nblocks;
	_C.blocks = _tmpC->blocks;
	free(_tmpC);

	double* _a = convert_jdoubleArray_to_double(env, a);
	double** _py = convert_jdoubleArray_to_double2D(env, py);
	double* _ppobj = convert_jdoubleArray_to_double(env, ppobj);
	double* _pdobj = convert_jdoubleArray_to_double(env, pdobj);
	int result = easy_sdp((int)n, (int)k, _C, _a, _constraints, (double)constant_offset, _pX, _py, _pZ, _ppobj, _pdobj);
	insert_results_bmatrix(env, pX, _pX);
	insert_results_array2D(env, py, _py);
	insert_results_bmatrix(env, pZ, _pZ);
	insert_results_array(env, ppobj, _ppobj);
	insert_results_array(env, pdobj, _pdobj);

	free_matrix(_pX);
	free_matrix(_pZ);
	free(_a);
	free(_py);
	free(_ppobj);
	free(_pdobj);
	return result;
}

