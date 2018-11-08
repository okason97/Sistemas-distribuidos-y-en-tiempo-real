/* RPC client for addition of variable length array */

#include <stdio.h>
#include <stdlib.h>

typedef struct {
  u_int iarray_len;
  int *iarray_val;
} iarray;

int *vadd_1(iarray *argp){
  static int  result;
  int i;
  
  printf("Got request: adding %d numbers\n",
         argp->iarray_len);

  result=0;
  for (i=0;i<argp->iarray_len;i++)
    result += argp->iarray_val[i];

  return (&result);
}

int vadd( int *x, int n) {
  iarray arr;
  int *result;

  /* Set up the iarray to send to the server */
  arr.iarray_len = n;
  arr.iarray_val = x;

  return(*vadd_1(&arr));
}

int main( int argc, char *argv[]) {
  int *ints,n;
  int i;
  int res;
  if (argc<2) {
    fprintf(stderr,"Usage: %s num1 num2 ...\n",argv[0]);
    exit(0);
  }

  /* get the 2 numbers that should be added */
  n = argc-1;
  ints = (int *) malloc(n * sizeof( int ));
  if (ints==NULL) {
    fprintf(stderr,"Error allocating memory\n");
    exit(0);
  }
  for (i=1;i<argc;i++) {
    ints[i-1] = atoi(argv[i]);
  }

  res = vadd(ints,n);
  printf("%d",ints[0]);
  for (i=1;i<n;i++) 
    printf(" + %d",ints[i]);
  printf(" = %d\n",res);
  return(0);
}