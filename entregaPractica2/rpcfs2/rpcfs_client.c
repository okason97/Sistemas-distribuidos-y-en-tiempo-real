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
	memcpy(w_data.buf, buf, n_byte);

	/* Call the client stub created by rpcgen */
	result = write_1(&w_data,clnt);
	if (result==NULL) {
		fprintf(stderr,"Trouble calling remote procedure\n");
		exit(0);
	}
	return(*result);
}

int get_file_size( CLIENT *clnt, char file_name[]) {
  int *result;

  /* Call the client stub created by rpcgen */
  result = filesize_1(&file_name,clnt);
  if (result==NULL) {
    fprintf(stderr,"Trouble calling remote procedure\n");
    exit(0);
  }
  return(*result);
}

int main( int argc, char *argv[]) {
	CLIENT *clnt;
	int fileSize;
	char file_name[128];
	char copy_file_name[128];
	read_result result;
	int bytes_read = 0;

	if (argc!=4) {
	fprintf(stderr,"Usage: %s hostname file_name copy_file_name\n",argv[0]);
	exit(0);
	}

	clnt = clnt_create(argv[1], RPCFS_PROG, RPCFS_VERSION, "udp");

	if (clnt == (CLIENT *) NULL) {
	clnt_pcreateerror(argv[1]);
	exit(1);
	}

	strcpy(file_name, argv[2]);
	strcpy(copy_file_name, argv[3]);

	fileSize = get_file_size(clnt, file_name);

    FILE* fd = fopen(file_name, "a");

	result = server_read(clnt, fileSize, 0, file_name);
	bytes_read += result.bytes_read;
	fwrite(result.buf, 1, result.bytes_read, fd);
	server_write(clnt, copy_file_name, result.bytes_read, result.buf);

	while(bytes_read < fileSize){
		result = server_read(clnt, fileSize-bytes_read, bytes_read, file_name);
		bytes_read += result.bytes_read;
		fwrite(result.buf, 1, result.bytes_read, fd);
		server_write(clnt, copy_file_name, result.bytes_read, result.buf);
	}

	fclose(fd);
	return(0);
}