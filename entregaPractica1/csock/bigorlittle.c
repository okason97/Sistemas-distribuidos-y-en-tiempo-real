#include <stdlib.h>
#include <stdio.h>

int main(int argc, char *argv[]){
	int num = 1;

	if (*(char *)&num == 1){
	    printf("Little-Endian\n");
	}else{
	    printf("Big-Endian\n");
	}
}