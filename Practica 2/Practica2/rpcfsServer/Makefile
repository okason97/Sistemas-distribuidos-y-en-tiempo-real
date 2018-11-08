.c.o:
	gcc -c -Wall -DRPC_SVC_FG $<

all: client server

client: rpcfsclient.o rpcfs_clnt.o rpcfs_xdr.o
	gcc -o client rpcfsclient.o rpcfs_clnt.o rpcfs_xdr.o -lnsl


server: rpcfsservice.o rpcfs_svc.o rpcfs_xdr.o
	gcc -o server rpcfsservice.o rpcfs_svc.o rpcfs_xdr.o -lrpcsvc -lnsl

rpcfs.h: rpcfs.x
	rpcgen -C rpcfs.x

clean: 
	rm *.o rpcfs.h rpcfs_svc.* rpcfs_clnt.* rpcfs_xdr.*

rpcfsclient.c: rpcfs.h
rpcfsservice.c: rpcfs.h
