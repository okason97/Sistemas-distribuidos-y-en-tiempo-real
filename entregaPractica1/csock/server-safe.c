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
    int sizeData = 65536;
	struct sockaddr_in serv_addr, cli_addr;
	int n;
    double timetick;
    unsigned char intStr[sizeof(int)];


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
	n = read(newsockfd, &longBuffer, sizeof(longBuffer));
	if (n < 0) 
	     error("ERROR reading from socket");
	stringSize = ntohl(longBuffer);

	char buffer[stringSize];
	char newBuffer[stringSize];
	bzero(buffer,stringSize);
    int received = 0;
	int safeCheck;
    while (received<stringSize){
		bzero(newBuffer,stringSize);
		if (stringSize - received >= sizeData){
			n = read(newsockfd,newBuffer,sizeData+sizeof(int));
		}else{
			n = read(newsockfd,newBuffer,stringSize-received+sizeof(int));
		}
		if (n < 0) 
		    error("ERROR receiving data");

		safeCheck = (newBuffer[0] << 24) | (newBuffer[1] << 16) | (newBuffer[2] << 8) | newBuffer[3];
		if (received == safeCheck){
			printf("safe\n");
			// append the new data to the buffer
			strcat( buffer, newBuffer+sizeof(int));
		}else{
		    error("ERROR receiving data");
		}
        n -= sizeof(int);
		received += n;

		printf("n: %d\n", n);
        intStr[0] = (n>>24) & 0xFF;
        intStr[1] = (n>>16) & 0xFF;
        intStr[2] = (n>>8) & 0xFF;
        intStr[3] = n & 0xFF;
		printf("ack: %d\n", (intStr[0] << 24) | (intStr[1] << 16) | (intStr[2] << 8) | intStr[3]);

        n = write(newsockfd,intStr,sizeof(int));
		if (n < 0) 
		     error("ERROR writing to socket");

		printf("finally: %d\n", received);
    }

	return 0; 
}
