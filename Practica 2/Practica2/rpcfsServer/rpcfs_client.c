/*
 * This is sample code generated by rpcgen.
 * These are only templates and you can use them
 * as a guideline for developing your own functions.
 */

#include "rpcfs.h"


void
rpcfs_prog_1(char *host)
{
	CLIENT *clnt;
	read_result  *result_1;
	read_data  read_1_arg;
	ssize_t  *result_2;
	write_data  write_1_arg;

#ifndef	DEBUG
	clnt = clnt_create (host, RPCFS_PROG, RPCFS_VERSION, "udp");
	if (clnt == NULL) {
		clnt_pcreateerror (host);
		exit (1);
	}
#endif	/* DEBUG */

	result_1 = read_1(&read_1_arg, clnt);
	if (result_1 == (read_result *) NULL) {
		clnt_perror (clnt, "call failed");
	}
	result_2 = write_1(&write_1_arg, clnt);
	if (result_2 == (ssize_t *) NULL) {
		clnt_perror (clnt, "call failed");
	}
#ifndef	DEBUG
	clnt_destroy (clnt);
#endif	 /* DEBUG */
}


int
main (int argc, char *argv[])
{
	char *host;

	if (argc < 2) {
		printf ("usage: %s server_host\n", argv[0]);
		exit (1);
	}
	host = argv[1];
	rpcfs_prog_1 (host);
exit (0);
}
