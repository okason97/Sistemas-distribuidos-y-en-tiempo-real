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

void error(char *msg)
{
    perror(msg);
    exit(1);
}

int main(int argc, char *argv[])
{
	int sockfd, newsockfd, portno, clilen, bufferLength;
	int size = (int)pow(10,6);
//	int size = 256;
	char buffer[size];
	int longBuffer, longNewBuffer;
	int stringSize;
	struct sockaddr_in serv_addr, cli_addr;
	int n;

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
	if (n > 0) {
		stringSize = ntohl(longBuffer);
	    printf("read: %i\n",  stringSize);        
	}
	else {
	   error("ERROR reading from socket");
	}

//    printf("Message should be of size: %i\n",  stringSize);

	// tell the client that you haven't received any data yet and you are ready to receive it
    longBuffer = htonl(0);
	n = write(newsockfd,&longBuffer,sizeof(longBuffer));
	if (n < 0) error("ERROR writing to socket");
//    printf("Here is the buffer of size: %i\n",  0);        
    printf("write: %i\n",  0);        

	// read the data sent by the client
	bzero(buffer,size);
	n = read(newsockfd,buffer,size-1);
	if (n < 0) error("ERROR reading from socket");
	bufferLength = strlen(buffer);
//    printf("Here is the message of size: %i\n", bufferLength);	    
    printf("read: %i\n",  bufferLength);        
	// while you don't have al the data
	if (bufferLength < stringSize){
		int sizeNewBuffer = stringSize-bufferLength;
		char newBuffer[sizeNewBuffer];
		while (bufferLength < stringSize){
			// send to the client the amount of data that you have already received
		    longBuffer = htonl(bufferLength);
			n = write(newsockfd,&longBuffer,sizeof(longBuffer));
			if (n < 0) error("ERROR writing to socket");
		    printf("write: %i\n",  bufferLength);        

			// receive another chunk of data
			bzero(newBuffer,sizeNewBuffer);
			n = read(newsockfd,newBuffer,sizeNewBuffer);
			if (n < 0) error("ERROR reading from socket");
			longNewBuffer = strlen(newBuffer);

    	    printf("read: %i\n", longNewBuffer);	    
//    	    printf("Here is the message of size: %i\n", longNewBuffer);	    

			// append the new data to the buffer
            for (int i=0;i<longNewBuffer;i++){
                buffer[bufferLength+i] = newBuffer[i];
            }
            bufferLength = strlen(buffer);

//            printf("Here is the buffer of size: %i\n",  bufferLength);       
		}
	}
	// tell the client that you have received all the data
    longBuffer = htonl(bufferLength);
	n = write(newsockfd,&longBuffer,sizeof(longBuffer));
	if (n < 0) error("ERROR writing to socket");
    printf("write: %i\n",  bufferLength);        

	// say goodbye
	n = write(newsockfd,"I got your message",18);
	if (n < 0) error("ERROR writing to socket");
    printf("write: I got your message");        

    printf("finishing\n");
	return 0; 
}
