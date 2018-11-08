#include <stdio.h>
#include <sys/time.h>
#include "timebreak.h"  /* Created for us by rpcgen - has everything we need ! */


int timebreak( CLIENT *clnt, long *sec) {
  int *result;

  result = timebreak_1(sec,clnt);

  if (result==NULL) {
    fprintf(stderr,"Trouble calling remote procedure timebreak\n");
    exit(0);
  }

  return(*result);
}

long int getTimeout(CLIENT *clnt){
  int *result;

  result = connect_1(NULL,clnt);

  if (result==NULL) {
    fprintf(stderr,"Trouble calling remote procedure connect\n");
    exit(0);
  }

  // get timeout
  struct timeval tv;
  clnt_control(clnt, CLGET_TIMEOUT, &tv);
  printf("%li\n", tv.tv_sec);

  long int timeout = tv.tv_sec;

  return(timeout);
}

int main( int argc, char *argv[]) {
  CLIENT *clnt;
  if (argc!=2) {
    fprintf(stderr,"Usage: %s hostname \n",argv[0]);
    exit(0);
  }

  clnt = clnt_create(argv[1], TIMEBREAK_PROG, TIMEBREAK_VERSION, "tcp");

  /* Make sure the create worked */
  if (clnt == (CLIENT *) NULL) {
    clnt_pcreateerror(argv[1]);
    exit(1);
  }

  long int i = getTimeout(clnt);

  timebreak(clnt, &i);
  return(0);
}



