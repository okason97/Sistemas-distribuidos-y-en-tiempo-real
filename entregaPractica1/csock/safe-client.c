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

//Para calcular tiempo
double dwalltime(){
  double sec;
  struct timeval tv;

  gettimeofday(&tv,NULL);
  sec = tv.tv_sec + tv.tv_usec/1000000.0;
  return sec;
}

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
    int size = (int)pow(10,6);
    int longBufferLong, sliceLen;
//    int size = 256;
    double timetick;

    char buffer[size];
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

    timetick = dwalltime(); //Empieza a controlar el tiempo
    // send the total size of the buffer
    n = write(sockfd,&longBuffer,sizeof(longBuffer));
    if (n < 0) 
         error("ERROR writing to socket");
    printf("write, %f\n", dwalltime()-timetick);

    int sent = 0;
    while (sent<longBufferLong){
        timetick = dwalltime(); //Empieza a controlar el tiempo
		n = write(sockfd,buffer+sent,longBufferLong-sent);
		if (n < 0) 
		     error("ERROR writing to socket");
        printf("write, %f\n", dwalltime()-timetick);
		sent += n;
    }

    return 0;
}
