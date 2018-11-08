/* A simple server in the internet domain using TCP
   The port number is passed as an argument */
#include <stdio.h>
#include <sys/types.h> 
#include <sys/socket.h>
#include <netinet/in.h>
#include <stdlib.h>
#include <strings.h>
#include <unistd.h>
#include <math.h>
#include <string.h>
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
    exit(1);
}

int main(int argc, char *argv[])
{
	int sockfd, newsockfd, portno, clilen, bufferLength;
	int longBuffer, longNewBuffer;
	int stringSize;
	struct sockaddr_in serv_addr, cli_addr;
	int n;
    double timetick;


	if (argc < 2) {
		fprintf(stderr,"ERROR, no port provided\n");
		exit(1);
	}
	sockfd = socket(AF_INET, SOCK_STREAM, 0);
	if (sockfd < 0) 
		error("ERROR opening socket");
	bzero((char *) &serv_addr, sizeof(serv_addr));
	portno = atoi(argv[1]);
	serv_addr.sin_family = AF_INET;
	serv_addr.sin_addr.s_addr = INADDR_ANY;
	serv_addr.sin_port = htons(portno);
	if (bind(sockfd, (struct sockaddr *) &serv_addr,
		sizeof(serv_addr)) < 0) 
		error("ERROR on binding");
	listen(sockfd,5);
	clilen = sizeof(cli_addr);
	newsockfd = accept(sockfd, 
			(struct sockaddr *) &cli_addr, 
			&clilen);
	if (newsockfd < 0) 
		error("ERROR on accept");

	// read the size of the data to receive
	longBuffer = 0;
    timetick = dwalltime(); //Empieza a controlar el tiempo
	n = read(newsockfd, &longBuffer, sizeof(longBuffer));
	if (n < 0) 
	     error("ERROR reading from socket");
    printf("read, %f\n", dwalltime()-timetick);
	stringSize = ntohl(longBuffer);

	char buffer[stringSize];
	char newBuffer[stringSize];
	bzero(buffer,stringSize);
    int received = 0;
    while (received<stringSize){
		bzero(newBuffer,stringSize);
	    timetick = dwalltime(); //Empieza a controlar el tiempo
		n = read(newsockfd,newBuffer,stringSize-received);
		if (n < 0) 
		     error("ERROR writing to socket");
	    printf("read, %f\n", dwalltime()-timetick);
		// append the new data to the buffer
		strcat( buffer, newBuffer );
		received += n;
    }

	return 0; 
}
