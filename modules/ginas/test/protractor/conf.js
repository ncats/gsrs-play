exports.config = {
    framework: 'jasmine2',
    seleniumAddress: 'http://localhost:4444/wd/hub',
    /* specs: [//'TestChemicalWizard.js',
     //'TestProteinWizard.js',
     //'TestStructurallyDiverseWizard.js',
     'TestWizardCommonElements.js',
     //'NameFormTest.js',
     'ReferenceFormTest.js'
     ],*/

   // baseUrl: 'http://localhost:9000/ginas/app/wizard?kind=chemical',

    "scripts": {
        "start": "http-server ./app -a localhost -p 9000"
    },

    capabilities: {
        'browserName': 'chrome',
        chromeOptions: {
            args: [
                '--start-maximized'
            ]
            // 'platform': 'ANY',
            // 'version': '11'
        }
    },

    /*  multiCapabilities: [
     {'browserName': 'chrome'},
     {'browserName': 'firefox'} ,
     {'browserName': 'internet explorer'}
     ],*/

    suites: {
        basic: [//'HeaderFormTest.js'
               //'NameFormTest.js' //passes
              //'CodeFormTest.js'//passes
            // ,'NoteFormTest.js' passes
            //'RelationshipFormTest.js'
            //,   'PropertyFormTest.js'
            //,  'ReferenceFormOnlyFormTest.js'
        ],
        modifications: [
         //   'AgentModificationFormTest.js'
          //, 'StructuralModificationFormTest.js'
          // , 'PhysicalModificationFormTest.js'
        ],
        chemical: [ //'StructureFormTest.js'
                    //,'MoietyFormTest.js'
        ],
        protein: [ 'ProteinDetailsFormTest.js'
                  //, 'SubunitFormTest.js'
                  //, 'GlycosylationFormTest.js'
        ],
        structurallyDiverse: [ //'DiverseDetailsFormTest.js'
                    //,'DiverseOrganismFormTest.js'
                   // 'DiverseSourceFormTest.js'
                    //,'DiverseTypeFormTest.js'
        ],
        polymer: [ 'PolymerClassificationFormTest.js'
                    ,'PolymerMonomerFormTest.js'
        ],
        mixture: [ 'TestMixtureForm.js'
        ],
        nucleicAcid: [ 'StructureFormTest.js'
                    ,'MoietyFormTest.js'
        ],
        concept: [ 'StructureFormTest.js'
                    ,'MoietyFormTest.js'
        ],
        g1ss: [ 'StructureFormTest.js'
                    ,'MoietyFormTest.js'
        ],
        accessControl:  [//'UserAccessControlTests.js',
                            'RegisterChemicalTest.js'
        ],
        publicDomain: [
            'SubstanceReferencePublicDomainUpdateTest.js'
        ]

    },

    jasmineNodeOpts: {
        showColors: true,
        isVerbose: true,
        realtimeFailure: true
    },
    params: {
        baseUrl: "",
        url:""
    },
    onPrepare: function(){
        global.EC = protractor.ExpectedConditions;

        switch(browser.params.baseUrl) {
            case 'chemical':
                browser.params.url = 'http://localhost:9000/ginas/app/wizard?kind=chemical';
                break;
            case 'protein':
                browser.params.url = 'http://localhost:9000/ginas/app/wizard?kind=protein';
                break;
            case 'structurallyDiverse':
                browser.params.url = 'http://localhost:9000/ginas/app/wizard?kind=structurallyDiverse';
                break;
            case 'polymer':
                browser.params.url = 'http://localhost:9000/ginas/app/wizard?kind=polymer';
                break;
            case 'mixture':
                browser.params.url = 'http://localhost:9000/ginas/app/wizard?kind=mixture';
                break;
            case 'nucleicAcid':
                browser.params.url = 'http://localhost:9000/ginas/app/wizard?kind=nucleicAcid';
                break;
            case 'concept':
                browser.params.url = 'http://localhost:9000/ginas/app/wizard?kind=concept';
                break;
            case 'g1ss':
                browser.params.url = 'http://localhost:9000/ginas/app/wizard?kind=g1ss';
                break;
            default:
                browser.get('http://localhost:9000/ginas/app/');
        }

        var jasmineReporters = require('jasmine-reporters');
        var Jasmine2HtmlReporter = require('protractor-jasmine2-html-reporter');

        return browser.getProcessedConfig().then(function(config) {
            jasmine.getEnv().addReporter(new jasmineReporters.JUnitXmlReporter('testtesttest', true, true));
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
        });
    }
};