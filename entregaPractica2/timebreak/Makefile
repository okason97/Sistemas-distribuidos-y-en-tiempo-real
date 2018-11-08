.c.o:
	gcc -c -Wall -DRPC_SVC_FG $<

all: client server

client: timebreakclient.o timebreak_clnt.o timebreak_xdr.o
	gcc -o client timebreakclient.o timebreak_clnt.o timebreak_xdr.o -lnsl

server: timebreakservice.o timebreak_svc.o timebreak_xdr.o
	gcc -o server timebreakservice.o timebreak_svc.o timebreak_xdr.o -lrpcsvc -lnsl

timebreak.h: timebreak.x
	rpcgen -C timebreak.x

clean: 
	rm *.o timebreak.h timebreak_svc.* timebreak_clnt.* timebreak_xdr.*

timebreakclient.c: timebreak.h
timebreakservice.c: timebreak.h
