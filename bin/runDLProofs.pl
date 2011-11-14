#!/usr/bin/perl -w
use File::Find;
use File::Copy;
use File::Basename;
use Cwd;
use strict;
use Net::SMTP;
use Getopt::Std;
use POSIX ":sys_wait_h";
use Sys::Hostname;
    
my %option = ();
getopts("hcmtr:", \%option);

#system("limit memoryuse 4000"); #set memory limit to 4GB


my $bin_path = dirname($0);
my $path_to_pe = "../system/proofExamples/";
my $path_to_automated = "index/";
my $automaticdl_txt = "automaticDL.txt";
my $not_provable_txt = "notProvableDL.txt";
my $headerdl_txt = "headerDL.txt";
my $interactive_txt = "interactiveDL.txt";
my $quit_with_error_txt = "quitWithErrorDL.txt";
chdir $bin_path;
my $absolute_bin_path = &getcwd;
my $statfile = $absolute_bin_path . "/" . $path_to_pe . "statistics-" . hostname . ".csv";
print "$absolute_bin_path\n";
chdir $path_to_pe;

if ($option{r}) {
	$automaticdl_txt = "regressionProvableDL.txt";
	$not_provable_txt = "regressionNotProvableDL.txt";
	$headerdl_txt = "regressionHeaderDL.txt";
}

if ($option{h}) {
  print "runs all proofs listed in the files: $automaticdl_txt, $interactive_txt, $not_provable_txt and $quit_with_error_txt .\n";
  print "They can be found in " .  $bin_path . "/" . $path_to_pe .  "\n\n";
  print "Use '-t' to provide the global maximum timeout for each task (in seconds).\n";
  print "Use '-m email\@address.com' to send the report as an email to the specified address.\n";
  print "Use '-h' to get this text (very necessary this line).\n";
  print "Use '-c' to get the debug messages from the smtp part if there are email problems.\n";
  print "Use '-r' for enabling the regression test mode.\n";
  exit;
}



open (HEADER_DL, $path_to_automated . $headerdl_txt) or
  die $bin_path . "/" . $path_to_pe . $headerdl_txt . " couldn't be opened.";
my @headerDL = <HEADER_DL>;
close HEADER_DL;

open (AUTOMATIC, $path_to_automated . $automaticdl_txt) or
  die $bin_path . "/" . $path_to_pe . $automaticdl_txt . " couldn't be opened.";
my @automatic_DL = <AUTOMATIC>;
close AUTOMATIC;

open (NOT_PROVABLE, $path_to_automated . $not_provable_txt) or
  die  $bin_path . "/" . $path_to_pe . $not_provable_txt . " couldn't be opened.";
my @not_provable = <NOT_PROVABLE>;
close NOT_PROVABLE;

open (INTERACTIVE, $path_to_automated . $interactive_txt) or
  die  $bin_path . "/" . $path_to_pe . $interactive_txt . " couldn't be opened.";
my @interactive = <INTERACTIVE>;
close INTERACTIVE;



#open (QUIT_WITH_ERROR, $path_to_automated . $quit_with_error_txt) or die  $bin_path . "/" . $path_to_pe . $quit_with_error_txt . " couldn't be opened.";
#my @quit_with_error = <QUIT_WITH_ERROR>;
#close QUIT_WITH_ERROR;


my $counter = 0;
my $correct = 0;
my $failures = 0;
my $errors = 0;
my %successes;
my %failures;
my %erroneous;


 open (STS, ">>$statfile");
 print STS ", %% Computer: " . hostname . "\n";
 print STS ", %% Version: " . `git show |grep commit`;
 print STS ", %% Date: " . `date` . "\n";
 close(STS);

 foreach my $dotkey (@automatic_DL) {
   $dotkey = &fileline($dotkey);
	if ("$dotkey" ne "") {
		handlefile($dotkey, 0);
	}
  }

  foreach my $dotkey (@not_provable) {
    $dotkey = &fileline($dotkey);
	if ("$dotkey" ne "") {
		handlefile($dotkey, 1);
	}
  }

  open (STS, ">>$statfile");
  print STS ", %% END " . `git show |grep commit` . ", %%" .`date` . "\n";
  close(STS);

#  foreach my $dotkey (@interactive) {
#    $dotkey = &fileline($dotkey);
  #
  #  if ($dotkey) {
  #    my $success = runAuto ($dotkey, "interactive");
  #   if ( $success == 0) {
  #     &processReturn (0, "indeed provable with interaction", $dotkey);
  #   } elsif ($success == 1) {
  #     &processReturn (1, "proof failed", $dotkey);
  #   } else {
  #     &processReturn (2, "error in proof", $dotkey);
  #   }
  # }
  #}

# foreach my $dotkey (@quit_with_error) {
#   $dotkey = &fileline($dotkey);
#   if ($dotkey) {
#     my $success = runAuto ($dotkey);
#     if ( $success == 0) {
#       &processReturn (1, "should not be provable", $dotkey);
#     } elsif ($success == 1) {
#       &processReturn (1, "should not be not provable", $dotkey);
#     } else {
#       &processReturn (0, "indeed error in proof", $dotkey);
#     }
#   }
# }

print "\n$correct/$counter prover runs according to spec. $errors errors occured.\n";
my $text = &produceResultText;
if ($text) {
  print $text;
  &mailResults ( $text ) if $option{m};
}



# ------------------------------------------------------------


sub fileline {
  $_[0] =~ s/\n$//;
  if ($_[0] =~ /\w*#/) {
    '';
  } else {
    $_[0];
  }
}

sub handlefile {
   my ($dotkey, $resultscheme) = @_;
   if ($dotkey =~ /#$/) {return 2;}
   my @split = split(/ /, $dotkey);
   $dotkey = $split[0];
   my $timeout = -1;
   $timeout = $option{t} if $option{t};
   if($#split > 0) {
	   if($timeout == -1 || $split[1] < $timeout) {
	   	$timeout = $split[1];
	   }
   } 
   open (HANDLE, $dotkey) or die  $dotkey. " couldn't be opened.";
   my $cnt=grep /\\settings/, <HANDLE>;
   close HANDLE;

   open (HANDLE, $dotkey) or die  $dotkey. " couldn't be opened.";
   my @old = <HANDLE>;
   close HANDLE;

   foreach my $headerfile (@headerDL) {
       if ($headerfile =~ /#$/) {
       } else {
	   $headerfile =~ s/\n$//;
	   my $stripped = $headerfile;
       $stripped =~ s/^\s+//;
	   $stripped =~ s/\s+$//;
	   if("$stripped" ne "") {
	   open (CURHEAD, $path_to_automated . $headerfile) or
		  die $path_to_automated . $headerfile . " couldn't be opened.";
	   binmode(CURHEAD);
	   my @curhead = <CURHEAD>;
	   close CURHEAD;
	   my $tmpfile = $dotkey;
	   $tmpfile =~ s#^.*/.*/(.*).key#$1#;
	   my $headertmp = $headerfile;
	   $headertmp =~ s#^.*/.*/(.*).txt#$1#;
   	   $tmpfile = "/tmp/$tmpfile-autoRun-$headertmp.$$.key";
	   print $tmpfile;

	   open (HANDLE, ">$tmpfile");
	   if (!$cnt) {
		   foreach (@curhead) {
			   print HANDLE;
		   }
	   }
	   foreach (@old) {
		 print HANDLE;
	   }
	   close HANDLE;

	   if ($tmpfile) {
		 my $success = runAuto ($tmpfile, $dotkey, $headerfile, $timeout, $resultscheme);
   		 unlink($tmpfile);
		 if ( $success == 0) {
			 if($resultscheme == 0) {
			   &processReturn (0, "indeed provable with $headerfile", $dotkey);
		     } else {
        		&processReturn (1, "should not be provable with $headerfile", $dotkey);
			 }
		 } elsif ($success == 1) {
			 if($resultscheme == 1) {
        		&processReturn (0, "indeed not provable with $headerfile", $dotkey);
		     } else {
			   &processReturn (1, "proof failed with $headerfile", $dotkey);
			 }
		 } else {
		   &processReturn (2, "error in proof with $headerfile", $dotkey);
		 }
	   }
       }
   }
   }

}

sub produceResultText {
  my $result;
  if (%failures) {
    $result .= "++The following files did not behave as expected:\n";
    foreach (keys %failures) {
      $result .= "$_ \t :  $failures{$_}\n"
    }
  }
  if (%erroneous) {
    $result .= "++The following files produced unexpected errors:\n";
    foreach (keys %erroneous) {
      $result .= "$_ \t :  $erroneous{$_}\n"
    }
  }
  $result;
}

sub killtree
{
    my ($pid,$sig) = @_;
    my @blub=qx/ps -o pid --no-headers --ppid $pid/;
    foreach(@blub) {
        print "Killing $_ with signal $sig\n";
        killtree($_, $sig);
    }  
	kill $sig, ($pid);
}


sub runAuto {
  my ($dk, $realfilename, $headerfile, $timeout, $expectedresult) = @_;
  my $tmp = "/tmp/statistics.tmp.$$";
  my $result;
  my $pid;
  eval {
	  $pid = open(STATUS, "$absolute_bin_path/runKeYmaera $dk auto print_statistics $tmp 2>&1 |");
	  #local $SIG{ALRM} = sub { my @pids = ($pid); print "killing $pid\n"; kill 15, @pids; sleep 5; kill 9, @pids; die "alarm\n"; };
	  #local $SIG{ALRM} = sub { print "killing $pid\n"; system("killtree.sh $pid"); sleep 5; system("killtree.sh $pid 9"); die "alarm\n"; };
	  local $SIG{ALRM} = sub { print "killing $pid\n"; killtree($pid, 15); sleep 5; killtree($pid, 9); die "alarm\n"; };
	  if ($timeout > 0) {
		  print "timeout is $timeout\n"; 
		  alarm $timeout;
	  }
	  #system("killall reduce");
	  print "now trying to prove $realfilename\n";
	  print "output follows\n";
	  while(<STATUS>) {
		  print;
	  }
	  close(STATUS);
	  print "\n";
	  $result = $? / 256;#exit code from system is multiplied by 256
	  print "Result is $result\n";
	  alarm 0;
  };
  if($@) {
	die "unexpected error" unless $@ eq "alarm\n";
	alarm 0;
    open (STS, ">>$statfile");
	my $printtimeout = $timeout*1000;
	print STS "T, $realfilename, NA, $printtimeout, NA, NA, $headerfile, $expectedresult, TIMEOUT\n";
	close(STS);
	return 2;
  } else {
	  open (TMPF, $tmp);
	  my @test = <TMPF>;
	  my $line = $test[-1];
	  if($line) {
		  $line =~ s/^.*?, (.*)\n$/$realfilename, $1, $headerfile, $expectedresult, $result\n/;
                  if ($expectedresult == $result) {
                      $line = "_, " . $line;
                  } else {
                      $line = "E, " . $line;
                  }
		  #$line =~ s/\n$/, $headerfile, $expectedresult, $result\n/;
		  #$line =~ s/^(.*?),/$realfilename,/;
		  open (STS, ">>$statfile");
		  print STS $line;
		  close(STS);
	  }
	  close(TMPF);
	  unlink($tmp);
  }
  $result; 
}

sub processReturn { 
  $counter++;
  if ($_[0] == 0) {
    print "$_[1] : $_[2]\n";
    $successes{"$_[2]"} = $_[1];
    $correct++;
  } elsif ($_[0] == 1) {
    print "$_[1] : $_[2]\n";
    $failures{"$_[2]"} = $_[1];
    $failures++;
  } elsif ($_[0] == 2) {
    print "$_[1] : $_[2]\n";
    $erroneous{"$_[2]"} = $_[1];
    $errors++;
  }
}

sub mailResults {
  # This debug flag will print debugging code to your browser, 
  # depending on its value  
  # Set this to 1 to send debug code to your browser.  
  # Set it to 0 to turn it off.  

  my $DEBUG = $option{h};

  if ($DEBUG) {  
    $| = 1;  
    open(STDERR, ">&STDOUT");  
  }

  my $ServerName = 'orwell.informatik.uni-oldenburg.de';


  # Connect to the server
  my $smtp = Net::SMTP->new($ServerName, Debug => $DEBUG);
  #die "Couldn't connect to server" unless $smtp;
  print "Couldn't connect to server" unless $smtp;


  my $MailFrom = "quesel\@informatik.uni-oldenburg.de";
  my $MailFromText = "Reporting cronjob for HyKeY";
#  my $MailTo = "schlager\@ira.uka.de";
  my $MailTo = $option{m};


  $smtp->mail( $MailFrom );
  $smtp->to( $MailTo );

  # Start the mail
  $smtp->data();

  # Send the header.
  $smtp->datasend("To: $MailTo\n");
  $smtp->datasend("From: $MailFromText <$MailFrom>\n");
  $smtp->datasend("Subject: Problems with automated proof runs\n");
  $smtp->datasend("\n");

  # Send the message
  $smtp->datasend("$_[0]\n\n");

  # Send the termination string
  $smtp->dataend();

  $smtp->quit();

}
