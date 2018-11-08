/* RPC client for simple addition example */

#include <stdio.h>
#include <fcntl.h> // for open
#include <unistd.h> // for close
#include "rpcfs.h"  /* Created for us by rpcgen - has everything we need ! */

/* Wrapper function takes care of calling the RPC procedure */

read_result server_read( CLIENT *clnt, int n_byte, int offset, char file_name[]) {
  read_data r_data;
  read_result *result;

  /* Gather everything into a single data structure to send to the server */
  r_data.n_byte = n_byte;
  r_data.offset = offset;
  strcpy(r_data.file_name, file_name);

  /* Call the client stub created by rpcgen */
  result = read_1(&r_data,clnt);
  if (result==NULL) {
    fprintf(stderr,"Trouble calling remote procedure\n");
    exit(0);
  }
  return(*result);
}

/* Wrapper function takes care of calling the RPC procedure */

int server_write( CLIENT *clnt, char file_name[], int n_byte, char buf[]) {
  write_data w_data;

  int *result;

  /* Gather everything into a single data structure to send to the server */
  strcpy(w_data.file_name, file_name);
  w_data.n_byte = n_byte;
  strcpy(w_data.buf, buf);

  /* Call the client stub created by rpcgen */
  result = write_1(&w_data,clnt);
  if (result==NULL) {
    fprintf(stderr,"Trouble calling remote procedure\n");
    exit(0);
  }
  return(*result);
}


int main( int argc, char *argv[]) {
  CLIENT *clnt;
  int n_byte, offset, fd;
  char file_name[128];
  char copy_file_name[128];
  read_result result;

  if (argc!=6) {
    fprintf(stderr,"Usage: %s hostname file_name n_byte offset copy_file_name\n",argv[0]);
    exit(0);
  }

  clnt = clnt_create(argv[1], RPCFS_PROG, RPCFS_VERSION, "udp");

  if (clnt == (CLIENT *) NULL) {
    clnt_pcreateerror(argv[1]);
    exit(1);
  }

  strcpy(file_name, argv[2]);
  n_byte = atoi(argv[3]);
  offset = atoi(argv[4]);
  strcpy(copy_file_name, argv[5]);

  result = server_read(clnt, n_byte, offset, file_name);
  fd = open(file_name, O_WRONLY|O_CREAT, 0777);
  if(fd < 0)
      return 1;        

  write(fd, result.buf, result.bytes_read);

  server_write(clnt, copy_file_name, result.bytes_read, result.buf);

  close(fd);
  return(0);
}