#include <jni.h>
#include <stdio.h>
#include <string.h>
#include "org_psics_pnative_FileFPSICS.h"


extern void __fpsics__runonce(char *str, int *len, double *cputime);

JNIEXPORT jdouble JNICALL
Java_org_psics_pnative_FileFPSICS_fpsics(JNIEnv *env, jobject obj, jstring fname)
 {
     const char *str;
     double cputime;

     str = (*env)->GetStringUTFChars(env, fname, NULL);
     if (str == NULL) {
         return -1; /* OutOfMemoryError already thrown */
     }
     int i;
     int namelength = strlen(str);
     char fxdname[256];
     for (i = 0; i < namelength; i++) {
     	fxdname[i] = str[i];
     }
     (*env)->ReleaseStringUTFChars(env, fname, str);

/*
 	 __fpsics__runonce(fxdname, &namelength, &cputime);
*/
	 return cputime;
 }


 int main(int na, char** argv) {
    return 0;
 }

