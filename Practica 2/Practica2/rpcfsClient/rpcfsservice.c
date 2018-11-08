#include <stdio.h>
#include <fcntl.h> // for open
#include <unistd.h> // for close
#include "rpcfs.h"

read_result *read_1_svc(read_data *argp, struct svc_req *rqstp){
	static read_result result;

    printf("Got request: reading from %s, %i bytes with %i offset \n",
           argp->file_name, argp->n_byte, argp->offset);

    int fd = open(argp->file_name, O_RDONLY);

    if(fd < 0){
        result.bytes_read = 0;
        return (&result);        
    }

    result.bytes_read = pread(fd, result.buf, argp->n_byte, argp->offset);
    close(fd);

	return (&result);
}



int *write_1_svc(write_data *argp, struct svc_req *rqstp){
    static int result;

    printf("Got request: writing to %s, %i bytes\n",
           argp->file_name, argp->n_byte);

    int fd = open(argp->file_name, O_WRONLY|O_CREAT, 0777);
    if(fd < 0){
        result = 0;
        return (&result);        
    }

    result = write(fd, argp->buf, argp->n_byte);
    close(fd);

    return (&result);
}