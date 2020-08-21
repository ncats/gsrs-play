#/usr/bin/perl
use strict;
use warnings;

#<testsuite hostname="ip-172-31-11-143" name="util.json.JsonUtilTest" tests="14" errors="0" failures="0" time="0.078">

sub handleDir($$){
    my $results=shift;
    my $dir = shift;
    opendir(DIR, $dir);
    my @files = grep(/\.xml/,readdir(DIR));
    closedir(DIR);
    foreach my $file (@files) {
        handleFile($results, $dir."/". $file);
    }
}
sub handleFile($$){
    my $results=shift;
    my $file = shift;
    open(FH, $file) or die("File $file not found");


    while(my $String = <FH>)
    {

        if($String =~ /<testsuite hostname=\S+ name=\"(.+)\" tests=\"(\d+)\" errors=\"(\d+)\" failures=\"(\d+)\"/)
        {
            print "found $1  $2\n";

            $results->{$1}= {
                "tests"    => $2,
                "errors"   => $3,
                "failures" => $4
            };
            my @ids = keys %$results;
            print "now hash = " . @ids ."\n";

        }
    }
    close(FH);
}

my %results=();
foreach my $fileOrDir (@ARGV){

    if( -d $fileOrDir){
        #isDir
        handleDir(\%results, $fileOrDir)
    }else{
        handleFile(\%results, $fileOrDir);
    }
}
my $numTests = keys %results;
print "number of test result files = $numTests\n";

my $totalTestCount=0;
my $totalPassCount=0;
my $totalFailCount=0;
my $totalErrorCount=0;

foreach my $test (keys %results) {
    # do whatever you want with $key and $value here ...

    my $value = $results{$test};

    my $testsInThisFile = $value->{"tests"};
    $totalTestCount += $testsInThisFile;
    $totalFailCount += $value->{"failures"};
    $totalErrorCount += $value->{"errors"};

    my $numPass = $testsInThisFile - $value->{"failures"} - $value->{"errors"};
    $totalPassCount += $numPass;
    if($value->{"failures"} > 0 || $value->{"errors"} > 0 ){
        print "TEST FAILURES = $test : pass = $numPass error = " . $value->{"errors"}. "  failure = " . $value->{"failures"} . "\n";
    }
}
print "\n\n[TOTALS]\n==========\n";
print "num of tests : $totalTestCount\nnum of pass : $totalPassCount\nnum of fail : $totalFailCount\nnum of error : $totalErrorCount\n";

END;
