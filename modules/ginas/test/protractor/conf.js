exports.config = {
    framework: 'jasmine',
    seleniumAddress: 'http://localhost:4444/wd/hub',
    //seleniumServerJar: './node_modules/protractor/selenium/latest.jar',
    specs: ['TestChemicalWizard.js'],

    baseUrl: 'http://localhost:9000',

    "scripts": {
        "start": "http-server ./app -a localhost -p 9000"
    }



   /*
    capabilities: {
    browserName: 'firefox' //default 'chrome'
    }
   , jasmineNodeOpts: {
        showColors: true,
        isVerbose: true
    }
   , onPrepare: function() {
        var jasmineReporters = require('jasmine-node-reporter-fix');
        //to make the browser wait
        var capsPromise = browser.getCapabilities();
        capsPromise.then(function (caps) {
            jasmine.getEnv().addReporter(
                new jasmine.JUnitXmlReporter('protractor_output', true, true, 'testresults.e2e.'))
        });

    var HtmlReporter = require('protractor-html-screenshot-reporter');
    jasmine.getEnv().addReporter(new HtmlReporter({
    baseDirectory: 'e2e-reports/html-report'
    }));
    }*/
}