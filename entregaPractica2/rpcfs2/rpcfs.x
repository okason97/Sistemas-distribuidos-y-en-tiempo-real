%#include <sys/types.h>

#define VERSION_NUMBER 1

struct read_data {
  int n_byte;
  int offset;
  char file_name[128];
};

struct read_result {
  int bytes_read;
  char buf[1024];
};

struct write_data {
  char file_name[128];
  int n_byte;
  char buf[1024];
};

program RPCFS_PROG {
   version RPCFS_VERSION {
     read_result READ(read_data) = 1;
     int FILESIZE(string) = 2;
     int WRITE(write_data) = 3;
   } = VERSION_NUMBER;
} = 555555555;