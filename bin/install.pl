#!/usr/bin/perl
use strict;
use warnings;

my @files = ("antlr.jar", "recoder.jar", "javacc.jar", "dresden-ocl-demo.jar", "jargs.jar", "log4j.jar", "xerces.jar");

mkdir("key-ext-jars") if not -d "key-ext-jars";

chdir "key-ext-jars";
foreach (@files) {
	system("wget http://i12www.ira.uka.de/~bubel/nightly/webstart/lib/$_") if not -f $_;
}

@files = ("keymaera.jar", "orbital-ext.jar", "orbital-core.jar");

foreach (@files) {
	system("wget http://www.informatik.uni-oldenburg.de/~jdq/keymaera/$_") if not -f $_;
}
#system(qw(wget http://www.informatik.uni-oldenburg.de/~jdq/keymaera/keymaera.jar)) if not -f "keymaera.jar";

system(qw(wget http://www.antlr.org/download/antlr-3.0.1.jar)) if not -f "antlr-3.0.1.jar";

my $jlink;

if(not -f "JLink/Jink.jar") {
	do {
		print "Please enter the path to JLink: ";
		$jlink = <>;
		chomp($jlink);
		print "Path not found: $jlink\n" if not -d $jlink;
	} while(not -d $jlink);
	system("ln -s " . $jlink . " JLink"); 
}

chdir("..");

mkdir("bin") if not -d "bin";

chdir("bin");

@files = ("runProver", "runAbortProgram");

foreach (@files) {
	system("wget http://www.informatik.uni-oldenburg.de/~jdq/keymaera/bin/$_") if not -f $_;
	chmod 0755, $_;
}

chdir("..");

print "You can now start the prover using with: \n";
print "bin/runProver dL";

exit 0
