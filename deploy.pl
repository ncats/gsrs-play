#!/usr/bin/perl

use strict;
use warnings;
use File::Path qw(make_path remove_tree);
use File::Copy qw(copy);
use File::Basename;
use Cwd 'abs_path';

use File::Find;
use Proc::Daemon;


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
		print "\t$pid";
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
					
chdir ($abs_path) or die  "cannot change to $abs_path: $!\n";
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
