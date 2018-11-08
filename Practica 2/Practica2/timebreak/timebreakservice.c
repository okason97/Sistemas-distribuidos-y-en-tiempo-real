#include <stdio.h>
#include <unistd.h>
#include "timebreak.h"

int *timebreak_1_svc(long *argp, struct svc_req *rqstp){
    static int  result;

    printf("Got request: timeout %li\n",
           *argp);

    sleep((*argp)+1);

    result = 0;

    return (&result);
}

int *connect_1_svc(void *argp, struct svc_req *rqstp){
    static int  result;

    printf("Connected\n");

    result = 0;

    return (&result);
}