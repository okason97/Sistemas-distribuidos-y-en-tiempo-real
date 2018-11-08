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
    printf("buffer size: %i\n",longBufferLong);

    // prepare the data to send
    longBuffer = htonl(longBufferLong);

    printf("buffer size: %i\n",longBufferLong);

    // send the total size of the buffer
    n = write(sockfd,&longBuffer,sizeof(longBuffer));
    if (n < 0) 
         error("ERROR writing to socket");
    printf("write: %i\n", longBufferLong);

    longBuffer = 0;

    // receive the amount of buffer received by the server (now it's 0)
    n = read(sockfd, &longBuffer, sizeof(longBuffer));
    if (n > 0) {
        longBufferLong = ntohl(longBuffer);
        printf("read: %i\n", longBufferLong);
//        printf("confirmed buffer: %i\n", longBufferLong);
    }
    else {
       error("ERROR reading from socket");
    }

    // while the server doesn't have the full buffer
    if (longBufferLong < size-1){
	    char slice[size-longBufferLong];
		while (longBufferLong < size-1){
            printf("in of while\n");
            // create a slice with the remaining data to be sent
            bzero(slice,size-longBufferLong);
			for(int j=longBufferLong;j<size;j++)
				slice[j-longBufferLong] = buffer[j];
            // send the slice
            sliceLen = strlen(slice);
		    n = write(sockfd,slice,sliceLen);
		    if (n < 0) 
		        error("ERROR writing to socket");		
            printf("write: %i\n", sliceLen);
//            printf("\nsent: %i\n", sliceLen);

            // read the remaining data to be sent
            n = read(sockfd, &longBuffer, sizeof(longBuffer));
            if (n > 0) {
                longBufferLong = ntohl(longBuffer);
                printf("read: %i\n", longBufferLong);
//                printf("confirmed buffer: %i\n", longBufferLong);
            }
            else {
               error("ERROR reading from socket");
            }
            printf("out of while\n");
		}
	}

    bzero(buffer,size);
    // read the final message
    n = read(sockfd,buffer,size-1);
    if (n < 0) 
         error("ERROR reading from socket");
    printf("%s\n",buffer);

    return 0;
}
