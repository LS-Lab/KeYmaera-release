#ifndef CSDP_JDQ_H
#define CSDP_JDQ_H

#include<jni.h>

typedef jdoubleArray bmatrix;
typedef jdoubleArray cmatrix;

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
															pdobj);

#endif
