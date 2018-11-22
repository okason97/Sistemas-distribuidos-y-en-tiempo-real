#include <stdio.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <netdb.h> 
#include <stdlib.h>
#include <unistd.h>
#include <strings.h>
#include <string.h>
#include <math.h>
#include <sys/time.h>

void error(char *msg)
{
    perror(msg);
    exit(0);
}

int main(int argc, char *argv[])
{
    int sockfd, portno, n;
    struct sockaddr_in serv_addr;
    struct hostent *server;
    int size = (int)pow(10,5);
    int longBufferLong, sliceLen;
    double timetick;
    int sizeData = 65536;
    char intStr[sizeof(int)*2];
    char buffer[size];
    char data[size+sizeof(int)];
    int longBuffer;

    if (argc < 3) {
       fprintf(stderr,"usage %s hostname port\n", argv[0]);
       exit(0);
    }
    portno = atoi(argv[2]);
    sockfd = socket(AF_INET, SOCK_STREAM, 0);
    if (sockfd < 0) 
        error("ERROR opening socket");
    server = gethostbyname(argv[1]);
    if (server == NULL) {
        fprintf(stderr,"ERROR, no such host\n");
        exit(0);
    }
    bzero((char *) &serv_addr, sizeof(serv_addr));
    serv_addr.sin_family = AF_INET;
    bcopy((char *)server->h_addr, 
         (char *)&serv_addr.sin_addr.s_addr,
         server->h_length);
    serv_addr.sin_port = htons(portno);
    if (connect(sockfd,(struct sockaddr *)&serv_addr,sizeof(serv_addr)) < 0) 
        error("ERROR connecting");

    // initialize sending buffer
    bzero(buffer,size);
    for (int i=0; i<size-1;i++){
       buffer[i] = 'a';
    }
	longBufferLong = strlen(buffer);

    // prepare the data to send
    longBuffer = htonl(longBufferLong);

    // send the total size of the buffer
    n = write(sockfd,&longBuffer,sizeof(longBuffer));
    if (n < 0) 
         error("ERROR writing to socket");
    int sent = 0;
    while (sent<longBufferLong){
        intStr[0] = (sent>>24) & 0xFF;
        intStr[1] = (sent>>16) & 0xFF;
        intStr[2] = (sent>>8) & 0xFF;
        intStr[3] = sent & 0xFF;
        memcpy(data, intStr, sizeof(intStr));
        memcpy(data+sizeof(intStr), buffer+sent, longBufferLong-sent);
        if(longBufferLong-sent >= sizeData){
    		n = write(sockfd,data,sizeData+sizeof(int));
        }else{
    		n = write(sockfd,data,longBufferLong-sent+sizeof(int));
        }
		if (n < 0) 
		     error("ERROR writing to socket");
		sent = sent + n - sizeof(int);
        printf("%d\n", sent);
    }
    return 0;
}
