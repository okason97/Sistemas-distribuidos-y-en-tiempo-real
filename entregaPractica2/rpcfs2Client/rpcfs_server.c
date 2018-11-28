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

    if (argp->n_byte < sizeof(result.buf)){
        result.bytes_read = pread(fd, result.buf, argp->n_byte, argp->offset);
    }else{
        result.bytes_read = pread(fd, result.buf, sizeof(result.buf), argp->offset);
    }
    if (result.bytes_read > sizeof(result.buf)) result.bytes_read = sizeof(result.buf);
    close(fd);

	return (&result);
}

int *filesize_1_svc(char **fileName, struct svc_req *rqstp){
	static int result;

    printf("Got request: reading size from %s\n",
           *fileName);

    FILE* fd = fopen(*fileName, "r");

    if(fd < 0){
        result = 0;
        return (&result);        
    }

    fseek(fd, 0L, SEEK_END);
    result = ftell(fd);
    fclose(fd);

	return (&result);
}

int *write_1_svc(write_data *argp, struct svc_req *rqstp){
    static int result;

    printf("Got request: writing to %s, %i bytes\n",
           argp->file_name, argp->n_byte);

    FILE* fd = fopen(argp->file_name, "a");
    
    result = fwrite(argp->buf, 1, argp->n_byte, fd);

    fclose(fd);

    return (&result);
}