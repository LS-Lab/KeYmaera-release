#ifndef CSDP_JDQ_H
#define CSDP_JDQ_H

#include<jni.h>

#define BMATRIX jdoubleArray
#define CMATRIX jdoubleArray

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
															pdobj);

JNIEXPORT jint JNICALL
Java_de_uka_ilkd_key_dl_arithmetics_impl_csdp_CSDP_test2(JNIEnv * env,
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
															pdobj);

JNIEXPORT jint JNICALL
Java_de_uka_ilkd_key_dl_arithmetics_impl_csdp_CSDP_test(JNIEnv * env,
														   jclass clazz);

#endif
