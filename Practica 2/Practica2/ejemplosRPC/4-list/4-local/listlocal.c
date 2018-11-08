/* RPC client for simple addition example */

#include <stdio.h>
#include <stdlib.h>

struct foo {
    int x;
    struct foo *next;
};
typedef struct foo foo;

int *sum_1(foo *argp){
    static int  result;

    result=0;
    while (argp) {
      result += argp->x;
      argp = argp->next;
    }

    return (&result);
}

void printnums( foo *f) {
  
  while (f) {
    printf("%d ",f->x);
    f=f->next;
  }
  printf("\n");
}

void print_sum( foo *head) {
  int *result;

  printf("Sum is %d\n",*sum_1(head)); 
}



int main( int argc, char *argv[]) {
  int n,i;
  foo *f;
  foo *head;
  foo *prev;

  if (argc<2) {
    fprintf(stderr,"Usage: %s num1 num2 ...\n",argv[0]);
    exit(0);
  }

  n = argc-1;
  f = head = (foo *) malloc(sizeof(foo));
  for (i=0;i<n;i++) {
    f->x = atoi(argv[i+1]);
    f->next = (foo *) malloc(sizeof(foo));
    prev=f;
    f = f->next;
  }

  free(prev->next);
  prev->next=NULL;

  printnums(head);
  print_sum(head);
  return(0);
}



