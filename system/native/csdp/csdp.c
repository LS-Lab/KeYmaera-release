#include<stdlib.h>
#include<declarations.h>
#include"csdp.h"

struct blockmatrix* convert_double_array_to_blockmatrix(JNIEnv* env, jdoubleArray array) {
	jdouble *element = (jdouble*) GetdoubleArrayElements(env, array, 0);
	int size = GetArrayLength(env, array);
	double *localArrayCopy = (double*) malloc(size * sizeof(double));
	int j;
	for (j = 0; j < size; j++) {
		localArrayCopy[j] = element[j];
	}
	union blockdatarec data;
	data.mat = localArrayCopy;
	struct blockrec rec;
	rec.blockcategory = MATRIX;
	rec.data = data;
	struct blockmatrix* b = malloc(sizeof(struct blockmatrix));
	b->nblocks = 1;
	b->blocks = &rec;
	return b;
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
															jdoubleArray pY,
															bmatrix pZ,
															jdoubleArray
															ppobj,
															jdoubleArray
															pdobj)
{

	struct blockmatrix* matrix = convert_double_array_to_blockmatrix(env, C);
	free(matrix->blocks[0].data.mat);
	free(matrix);
	return 0;
}

