all: libcsdp.so

libcsdp.so: csdp.o
	ld -shared -o $@ csdp.o libsdp.a
	strip $@

%.o: %.c
	gcc -fPIC -c -I$(JAVA_HOME)/include -I$(JAVA_HOME)/include/linux -I$(HOME)/local/src/Csdp-6.0.1/include -o $@ $<
