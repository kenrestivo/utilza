TARGET= libsmbus.so
CC= gcc
CFLAGS= -Wall -O3  -g  -I. 
LDFLAGS= -shared
SRCS= smbus.c

#
#

${TARGET}: ${SRCS}  $(INCLUDES)
	${CC} ${CFLAGS} ${LDFLAGS} -o  ${TARGET} ${SRCS}

clean::
	rm -f ${TARGET} *core *.so


## TODO: install this instead in the jar resources
install: $(TARGET)
	sudo cp $(TARGET) /usr/lib/
