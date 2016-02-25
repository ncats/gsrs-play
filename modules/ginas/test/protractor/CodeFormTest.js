var WizardCodePage = function () {
    this.getPage = function () {
        browser.get('/ginas/app/wizard?kind=chemical');
    };

    this.clickById = function (name) {
        element(by.id(name)).click();
    };

    this.clickByModel = function (name) {
        element(by.model(name)).click();
    };

    this.formElements = {
        formName: 'codeForm',
        buttonId:'codes',
        fields: [{
            model: 'code.codeSystem',
            type: 'dropdown-select'
        }, {
            model: 'code.type',
            type: 'dropdown-select'
        }, {
            model: 'code.code',
            type: 'text-input'
        }, {
            model: 'code.url',
            type: 'text-input'
        },/* {
            model: 'textbox',
            type: 'form-selector'
        }, {
            model: 'code-access',
            type: 'form-selector'
        },*/ {
            model: 'code-reference',
            type: 'form-selector'
        }]
    }
};

describe ('Code form', function() {

    var wizardCodePage = new WizardCodePage();
    /*beforeEach(function() {
        wizardCodePage.getPage();
    });*/

    it('Code form tests', function () {
        var commonElementTests = require('./TestWizardCommonElements.js');
        var elements = new commonElementTests;
        var buttonId = wizardCodePage.formElements.buttonId;
        var formElements = wizardCodePage.formElements.fields;

        var refElementTests = require('./ReferenceFormTest.js');
        var refPage = new refElementTests;
        var accessElementTests = require('./AccessFormTest.js');
        var accessPage = new accessElementTests;
        var commentElementTests = require('./CommentFormTest.js');
        var commentPage = new commentElementTests;

        for (var i = 0; i < formElements.length; i++) {
            var elementType = formElements[i].type;
            var model = formElements[i].model;
            wizardCodePage.getPage();
            switch (elementType) {
                case "text-input":
                    elements.testTextInput(buttonId, model);
                    break;
                case "dropdown-select":
                    elements.testDropdownSelectInput(buttonId, model);
                    break;
                case "multi-select":
                    elements.testMultiSelectInput(buttonId, model);
                    break;
                case "check-box":
                    elements.testCheckBoxInput(buttonId, model);
                    break;
                case "form-selector":
                    if(model == 'code-reference') {
                        refPage.refPageTests(buttonId, model);
                    } else if(model == 'code-access'){
                        accessPage.accessPageTests(buttonId, model);
                    }else if(model == 'textbox'){
                        commentPage.commentPageTests(buttonId, model);
                    }

                    break;
            } //switch
        } //for i
    });
});


