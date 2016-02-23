#!/usr/bin/perl

use strict;
use warnings;
use File::Path qw(make_path remove_tree);
use File::Copy qw(copy);
use File::Basename;
use Cwd 'abs_path';

use File::Find;
use Proc::Daemon;
use LWP::UserAgent;
  

sub getOldPid($);

my @files = glob('modules/ginas/target/universal/*.zip');
#should only be one file
my $zipFile = $files[0];

print "Running as: ", getpwuid($<), "\n";

my $zipName = basename($zipFile);

my $outputPathRoot = $ENV{JENKINS_GINAS_DEPLOY_ROOT};
my $port = $ENV{JENKINS_GINAS_DEPLOY_PORT};
print "output root = ". $outputPathRoot ."\n";
die "no deploy root specified" unless(defined $outputPathRoot);
die "no deploy port specified" unless(defined $port);


#my $outputPath = "/ncats/users/ncatsweb/www/files/ginastmp/currentDevApp2";
#my $outputPath = "ginastmp/currentDevApp";

my $outputPath = $outputPathRoot . "_" . $port;
if( -e $outputPath){
	#check if RUNNING_PID exists and kill job if it is
	my @pids = @{getOldPid($outputPath)};
	print "old pids to kill\n";

	foreach my $pid (@pids){
		#kill it
		print "\t$pid\n";
		system("kill -9 $pid");
	}
	
	remove_tree($outputPath);
}

make_path($outputPath);
#my $outputPath = "ginastmp/$zipName";

copy($zipFile, $outputPath)  or die "Copy failed: $!";


`unzip $zipFile -d $outputPath`; 
my ($subDir, undef, undef) = fileparse($zipFile, ".zip");
my $abs_path = abs_path($outputPath. "/$subDir");
					
#chdir ($abs_path) or die  "cannot change to $abs_path: $!\n";
print("working dir is $abs_path\n");

my $command = "$abs_path/bin/ginas -Djava.awt.headless=true -Dhttp.port=$port -Dconfig.resource=ginas.conf -DapplyEvolutions.default=true -Dapplication.context=/dev/ginas/app";

my $daemon = Proc::Daemon->new(
        work_dir => $abs_path,
       exec_command => $command,
       pid_file => $abs_path. "/daemon.pid.txt",
        child_STDOUT => $abs_path . "/daemon.out",
        child_STDERR => $abs_path . "/daemon.err",
        file_umask => 022
    );
    
my $Kid_1_PID = $daemon->Init;
print "daemon process is $Kid_1_PID\n";


my $ua = LWP::UserAgent->new;
my $startUpURL = "http://localhost:$port/dev/ginas/app/api/v1";
my $startReq = HTTP::Request->new(GET => $startUpURL);
my $startResponse;
my $tries=0;
do{
	#wait 10 seconds for ginas to start up...
  sleep(10);
  $startResponse = $ua->request($startReq);
  $tries++;
  print "$tries\n";
}while($tries < 10 && $startResponse->is_error());

if($startResponse->is_error()){
	die "could not connect to ginas ", $startReq->status_line, "\n";
}

my $ginasFileDump = "modules/ginas/test/testdumps/rep90.ginas";

if(-e $ginasFileDump){
	print "file exists\n";
}else{
	print "FILE DOES NOT EXIST!!!!";
}

system "curl -v -F file-type=JSON -F file-name=@" ."$ginasFileDump http://localhost:$port/dev/ginas/app/load";

#we have to pretend to be a filled in form
#content_type multi-part/form-data

#my $loadResp = $ua->post("http://localhost:$port/dev/ginas/app/load", 
#					,
#					#leading colon to force spelling perl will auto uppercase first letters of words
#					Content_Type => 'form-data',
#				  Content=>[
#				      #to upload the file, the path has to be the
#								#first element in an array...
#								':file-name' =>[$ginasFileDump, "rep90.ginas",
#												     Header => [':file-type' => "JSON"],
#													]
#								#':file-type' => "JSON",
#								]
#					
#					);
#
#
#if($loadResp->is_error()){
#	print "error loading JSON file ", $loadResp->status_line, "\n";
#	exit;
#}
print "=======================\n";
print "querying status JSON\n";
print "=======================\n";
my $statusURL = "http://localhost:$port/dev/ginas/app/api/v1/jobs/1";

my $done=undef;
while (!$done){
	sleep(5);
	
	# Create a request
	my $req = HTTP::Request->new(GET => $statusURL);
	# Pass request to the user agent and get a response back
	my $res = $ua->request($req);
	
	#res is a JSON response we want to look for value of "status":"PENDING" or RUNNING or COMPLETE
	print $res->content , "\n";
	if($res->content =~/\"status\"\:\"([A-Z]+)?\"/){
	
		print "STATUS = ",  $1, "\n";
		$done = $1 eq "COMPLETE";
	}

}




#exit;
#system($command. " &");

sub getOldPid($){
	my $dir = shift;
	my @pids = ();

	find( sub{
		my $file = $_;
		if($file eq "RUNNING_PID"){
			# Work with the file
			open my $fh, "<", $file;
			#should only be 1 line
			my $line = <$fh>;
			
			chomp $line;
			push @pids, $line;
			close $fh;
			return $line;
		}
	},
	$dir);
	
	return \@pids;
	}
	
END;
