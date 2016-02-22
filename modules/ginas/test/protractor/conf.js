exports.config = {
    framework: 'jasmine',
    seleniumAddress: 'http://localhost:4444/wd/hub',
    specs: ['TestChemicalWizard.js',
            'TestProteinWizard.js'
            ],

    baseUrl: 'http://localhost:9000',

    "scripts": {
        "start": "http-server ./app -a localhost -p 9000"
    },

    capabilities: {
        'browserName': 'chrome'
       // 'platform': 'ANY',
       // 'version': '11'
    },

  /*  multiCapabilities: [
     {'browserName': 'chrome'},
    {'browserName': 'firefox'} ,
     {'browserName': 'internet explorer'}
     ],*/

   jasmineNodeOpts: {
        showColors: true,
        isVerbose: true
    },
   onPrepare: function() {
       var jasmineReporters = require('jasmine-reporters');
       var Jasmine2HtmlReporter = require('protractor-jasmine2-html-reporter');

        var capsPromise = browser.getCapabilities(); //to make the browser wait
        capsPromise.then(function (caps) {
            jasmine.getEnv().addReporter(
            new jasmineReporters.JUnitXmlReporter('protractor_output', true, true));
        });

       jasmine.getEnv().addReporter(
           new Jasmine2HtmlReporter({
               savePath: 'reports'
               , takeScreenshots: true
               , screenshotsFolder: '-images'
               , fixedScreenshotName: true
           }));
    }
}