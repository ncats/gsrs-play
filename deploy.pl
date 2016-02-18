#!/usr/bin/perl

use strict;
use warnings;
use File::Path qw(make_path remove_tree);

use File::Copy qw(copy);
use File::Basename;
use Cwd 'abs_path';


sub getOldPid($);

my @files = glob('modules/ginas/target/universal/*.zip');
#should only be one file
my $zipFile = $files[0];

my $zipName = basename($zipFile);
my $outputPath = "/ncats/users/ncatsweb/www/files/ginastmp/currentDevApp";
#my $outputPath = "ginastmp/currentDevApp";

if( -e $outputPath){
	#check if RUNNING_PID exists and kill job if it is
	my @pids = @{getOldPid($outputPath)};
	print "old pids to kill\n";

	foreach my $pid (@pids){
		#kill it
		system("kill -9 $pid");
	}
	
	remove_tree($outputPath);
}
#my $outputPath = "ginastmp/$zipName";

#copy($zipFile, $outputPath)  or die "Copy failed: $!";


`unzip $zipFile -d $outputPath`; 
my ($subDir, undef, undef) = fileparse($zipFile, ".zip");
my $abs_path = abs_path($outputPath. "/$subDir");
					
chdir ($abs_path) or die  "cannot change to $abs_path: $!\n";
print("working dir is $abs_path\n");

my $command = "./bin/ginas -Djava.awt.headless=true -Dhttp.port=9004 -Dconfig.resource=ginas.conf -DapplyEvolutions.default=true -Dapplication.context=/dev/ginas/app";

system($command. " &");

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