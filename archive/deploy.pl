#!/usr/bin/perl

use strict;
use warnings;
use File::Path qw(make_path remove_tree);
use File::Copy qw(copy);
use File::Basename;
use Cwd 'abs_path';

use File::Find;
use Proc::Daemon;

use Getopt::Long;
use Pod::Usage;

sub getOldPid($);
sub getValue($$);
sub toString($);

my $load = 1;
my $loadFile = "modules/ginas/test/testdumps/jsonDumpINN_3000.txt.gz";

my @files = glob('modules/ginas/target/universal/*.zip');
#should only be one file
my $zipFile = $files[0];

my $outputPathRoot = undef;
my $port = undef;

my $applicationHost= undef;
my $applicationContext= "/dev/ginas/app";
my $conf = "ginas.conf";
my $start=1;
my $apply_evolutions=1;



my $kill9= 0;
my %overrides = ();



=head1 SYNOPSIS

Deploy a built ginas dist zip file to this machine.

A dist zip file will be copied to a folder under $deploy_root + "_" + $port (defined by required parameters) and
then unzipped and run.  If any ginas instances are running underthat deploy folder, this program will kill the process and delete
the old contents before doing anything else.

=head1 Options

  --deploy_root           (Required) root path to unzip the the dist.zip file to.  The actual unzip location will be $deploy_root + "_" + $port
  --port                  (Required) port number to use.
  --conf                  The ginas conf file to use. If not set will default to "ginas.conf" in the dist.
  --host                  The application host to use instead of the one in the conf file.
  --context               The application context, if not set default to /dev/ginas/app
  --zip_file              The dist zip file to use, if not specified will pick the first zip found in 'modules/ginas/target/universal/*.zip'
                          which is where the dist packages are generated.
  --load_file             The ginas JSON file of Substances to load at start up.  If --load is set.
  --load                  Load a ginas JSON file of Substances at start up.  This is on by default.
  --no-load, --noload     Do not load a ginas JSON file by default
  --no-start              Do not Start the ginas instance only kill and delete the old running on if needed.
  --no-evolution          Do not apply evolution
  --kill9                 When killing the old process perform a "kill -9" instead of a normal kill
  --override key=value    Override a $key in the conf file setting it's value to $value instead.
                          This is the same as -Dkey=value when running a ginas instance.

  --help                  Print this help

=head1 VERSION

1.0

=cut

GetOptions( 'load!' => \$load,
            'load_file=s' => \$loadFile,
            'zip_file=s' => \$zipFile,
            'port=i' => \$port,
            'deploy_root=s' => \$outputPathRoot,
            'host=s' => \$applicationHost,
            'context=s' => \$applicationContext,
            'start!' => \$start,
            'override=s%' => \%overrides,
            'conf=s' => \$conf,
            'evolution!' => \$apply_evolutions,
            'kill9' => \$kill9,
            'help'     =>   sub { pod2usage(0) },
) or pod2usage(1);




print "Running as: ", getpwuid($<), "\n";

my $zipName = basename($zipFile);


#set the loadFile to the value of the environment variable or our default value if not set (falsy)
#my $loadFile = $ENV{JENKINS_GINAS_LOAD_FILE} || "modules/ginas/test/testdumps/jsonDumpINN_3000.txt.gz";
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
		if($kill9){
		    system("kill -9 $pid");
		}else{
		    system("kill $pid");
		}
	}

	remove_tree($outputPath);
}

exit unless $start;

print "getting ready to start new instance\n";
make_path($outputPath);
#my $outputPath = "ginastmp/$zipName";

copy($zipFile, $outputPath)  or die "Copy failed: $!";


`unzip $zipFile -d $outputPath`;
my ($subDir, undef, undef) = fileparse($zipFile, ".zip");
my $abs_path = abs_path($outputPath. "/$subDir");

#chdir ($abs_path) or die  "cannot change to $abs_path: $!\n";
print("working dir is $abs_path\n");
#my $ginasFileDump = abs_path("modules/ginas/test/testdumps/rep90.ginas");
my $ginasFileDump = abs_path($loadFile);


my $command = "$abs_path/bin/ginas -mem 4096 -Djava.awt.headless=true -Dhttp.port=$port -Dconfig.resource=$conf -Dapplication.context=$applicationContext";

if($load){
    $command .= " -Dix.ginas.load.file=$ginasFileDump";

    print "$ginasFileDump\n";
    if(-e $ginasFileDump){
    	print "file exists\n";
    }else{
    	die "FILE DOES NOT EXIST!!!!";
    }
}

if($apply_evolutions){
$command .=" -DapplyEvolutions.default=true";
}
if(defined $applicationHost){
    $command .= " -Dapplication.host=$applicationHost";
}

 while (my ($key, $value) = each %overrides) {
 if( defined $key && defined $value){
    $command .= " -D$key=$value";
 }
 }

 print "command = $command\n";
#-Dix.admin=true -Dix.authentication.allownonauthenticated=false";

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

#just exit for now don't worry about the graph for now
exit;



sub getValue($$){
	my ($json, $key) = @_;

	print "json = \n$json\n";

	if($json =~/\"$key\"\:\"(\S+?)\"/){
		return $1;
	}
	if($json =~/\"$key\"\:(\d+)/){
		return $1;
	}
	return undef;

}

sub toString($){
    open my $fh, "<", shift;
    my $buf="";

    while(<$fh>){
        $buf .= $_;
    }
    close $fh;
    return $buf;
}

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
