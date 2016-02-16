#!/usr/bin/perl

use strict;
use File::Copy qw(copy);
use File::Basename;
use IO::Uncompress::Unzip qw(unzip $UnzipError) ;

my $devCount=1;


my @files = glob('modules/ginas/target/universal/*.zip');
#should only be one file
my $zipFile = $files[0];

my $zipName = basename($zipFile);
#my $outputPath = "/ncats/users/ncatsweb/www/files/ginastmp/$zipName";
my $outputPath = "ginastmp/$zipName";

copy($zipFile, $outputPath)  or die "Copy failed: $!";

unzip $outputPath => $outputPath . "DEV-". $devCount
					or die "unzip failed: $UnzipError\n";
					
chdir ($outputPath) or die  "cannot change: $!\n";

my $command = "./bin/ginas -Djava.awt.headless=true -Dhttp.port=9004 -Dconfig.resource=conf/ginas.conf -DapplyEvolutions.default=true -Dapplication.context=/dev/ginas/app";

system($command. " &");