CFLAGS=-O3 -ansi -Wall -DNOSHORTS 

all: libcsdp.so

#libcsdp.so: csdp.o libsdp.a
#	ld -shared -o $@ csdp.o libsdp.a #-llapack -lblas -lm
#	#strip $@

lib%.so: %.c libsdp.a
	cc $(CFLAGS) -g -fPIC -shared -I$(JAVA_HOME)/include -I$(JAVA_HOME)/include/linux -I$(HOME)/local/src/Csdp-6.0.1/include -o $@ $< libsdp.a -llapack -lblas -lm
