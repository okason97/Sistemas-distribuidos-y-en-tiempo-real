/* RPC client for simple addition example */

#include <stdio.h>
#include <stdlib.h>
struct operands {
	int x;
	int y;
};
typedef struct operands operands;

int *add_1(operands *argp){
	static int  result;

	printf("Got request: adding %d, %d\n",
	       argp->x, argp->y);

	result = argp->x + argp->y;


	return (&result);
}

int *sub_1(operands *argp){
	static int  result;

	printf("Got request: subtracting %d, %d\n",
	       argp->x, argp->y);

	result = argp->x - argp->y;


	return (&result);
}

int add( int x, int y) {
  operands ops;

  /* Gather everything into a single data structure to send to the server */
  ops.x = x;
  ops.y = y;

  return(*add_1(&ops));
}

/* Wrapper function takes care of calling the RPC procedure */

int sub( int x, int y) {
  operands ops;

  /* Gather everything into a single data structure to send to the server */
  ops.x = x;
  ops.y = y;

  return(*sub_1(&ops));
}

int main( int argc, char *argv[]) {
  int x,y;
  if (argc!=3) {
    fprintf(stderr,"Usage: %s num1 num\n",argv[0]);
    exit(0);
  }

  /* get the 2 numbers that should be added */
  x = atoi(argv[1]);
  y = atoi(argv[2]);

  printf("%d + %d = %d\n",x,y, add(x,y));
  printf("%d - %d = %d\n",x,y, sub(x,y));
  return(0);
}



