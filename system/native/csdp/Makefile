#CFLAGS=-O3 -ansi -Wall -DNOSHORTS -DDEBUG -g -ggdb -fPIC
#CFLAGS=-O3 -std=c99 -Wall -DNOSHORTS -DDEBUG -fPIC -g -ggdb -fPIC
CFLAGS= -std=c99 -Wall -DNOSHORTS -DDEBUG -fPIC -g -ggdb

all: libcsdp.so
	cp libcsdp.so ../../binary

#libcsdp.so: csdp.o libsdp.a
#	ld -shared -o $@ csdp.o libsdp.a #-llapack -lblas -lm
#	#strip $@

lib%.so: %.c libsdp.a
	cc $(CFLAGS) -shared -L/usr/lib/gcc/x86_64-linux-gnu/4.3.3 -I$(JAVA_HOME)/include -I$(JAVA_HOME)/include/linux -I../Csdp-6.0.1/include -o $@ $< libsdp.a -llapack -lblas -lm 
#	cc $(CFLAGS) -shared -I$(JAVA_HOME)/include -I$(JAVA_HOME)/include/linux -I$(HOME)/workspace/Csdp-6.0.1/include -o $@ $< libsdp.a -llapack -lblas -lm
