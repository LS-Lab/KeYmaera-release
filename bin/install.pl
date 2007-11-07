#!/usr/bin/perl
use strict;
use warnings;

my @files = ("antlr.jar", "recoder.jar", "javacc.jar", "dresden-ocl-demo.jar", "jargs.jar", "log4j.jar", "xerces.jar");

mkdir("key-ext-jars") if not -d "key-ext-jars";

chdir "key-ext-jars";
foreach (@files) {
	system("wget http://i12www.ira.uka.de/~bubel/nightly/webstart/lib/$_") if not -f $_;
}

@files = ("keymaera.jar");

foreach (@files) {
	system("wget http://csd.informatik.uni-oldenburg.de/keymaera/$_") if not -f $_;
}

@files = ("orbital-ext.jar", "orbital-core.jar");

foreach (@files) {
	system("wget http://www.functologic.com/orbital/$_") if not -f $_;
}
 
system(qw(wget http://www.antlr.org/download/antlr-3.0.1.jar)) if not -f "antlr-3.0.1.jar";

my $jlink;

if(not -f "JLink/JLink.jar") {
	do {
		print "Please enter the path to JLink (e.g. /usr/local/Mathematica/SystemFiles/Links/JLink): ";
		$jlink = <>;
		chomp($jlink);
		print "Path not found: $jlink\n" if not -d $jlink;
	} while(not -d $jlink or not -f "$jlink/JLink.jar");
	system("ln -s " . $jlink . " JLink"); 
}

chdir("..");

mkdir("bin") if not -d "bin";

chdir("bin");

@files = ("runKeYmaera", "runAbortProgram");

foreach (@files) {
	system("wget http://csd.informatik.uni-oldenburg.de/keymaera/bin/$_") if not -f $_;
	chmod 0755, $_;
}

system("which math 2&>1 > /dev/null");

my $result=$?/256;

# We need user interaction to locate the mathkernel
unless($result == 0) {
    print "Mathematica was not found in the system path.\n";
    print "Please enter the correct location of the Mathematica executables e.g.\n";
    print "(/usr/local/Mathematica/Executables).\n";
    my $path = "";
    do {
        print "Location: ";
        $path = <>;
		chomp($path);
        print "Path not found!\n" unless -d $path;
        print "math binary not found in given path!\n" unless -f "$path/math";
    } while(not -d $path or not -f "$path/math");
    open(HANDLE, "<runKeYmaera");
    my @runKeYmaera = <HANDLE>;
    close(HANDLE);
    open(HANDLE, ">runKeYmaera");
    foreach(@runKeYmaera) {
        if($_ =~ m/^PATH=.*/) {
            my $p = $_;
            $p =~ s/PATH=(.*)/PATH=$path:$1/;
            print HANDLE $p;
        } else {
            print HANDLE;
        }
    }
    close HANDLE;
}

chdir("..");

print "\n";
print "You can now start the prover using with: \n";
print "bin/runKeYmaera\n";

exit 0
