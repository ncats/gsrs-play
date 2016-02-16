#!/usr/bin/perl

use strict;
use File::Copy qw(copy);
use File::Basename;
use Cwd 'abs_path';

my $devCount=1;


my @files = glob('modules/ginas/target/universal/*.zip');
#should only be one file
my $zipFile = $files[0];

my $zipName = basename($zipFile);
#my $outputPath = "/ncats/users/ncatsweb/www/files/ginastmp/$zipName";
my $outputPath = "ginastmp/$zipName";

copy($zipFile, $outputPath)  or die "Copy failed: $!";

my $dir = $outputPath . "DEV-". $devCount;

`unzip $outputPath -d $dir`;
my ($subDir, undef, undef) = fileparse($zipFile, ".zip");
my $abs_path = abs_path($dir. "/$subDir");
					
chdir ($abs_path) or die  "cannot change to $abs_path: $!\n";
print("working dir is $abs_path\n");

my $command = "bin/ginas -Djava.awt.headless=true -Dhttp.port=9004 -Dconfig.resource=conf/ginas.conf -DapplyEvolutions.default=true -Dapplication.context=/dev/ginas/app";

system($command. " &");
