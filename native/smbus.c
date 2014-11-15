/* 
   This stub is necessary because JNA won't find inline functions defined in header files.
   There has to be an actual symbol somewhere in a library.
*/


#include <linux/i2c-dev.h>


__s32 smbus_read_byte_data(int file, __u8 command){
	return i2c_smbus_read_byte_data(file,  command);
}



__s32 smbus_write_byte_data(int file, __u8 command,  __u8 value){
	return i2c_smbus_write_byte_data(file,  command, value);
}
                                              
