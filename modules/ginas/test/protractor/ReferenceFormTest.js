var WizardReferencePage = function () {

    this.clickById = function (name) {
        element(by.id(name)).click();
    };

    this.clickByModel = function (name) {
        element(by.model(name)).click();
    };
///reference apply needs its own functionality check
    //upload file needs separate testing
    //this form is not clooapsible, it is tolggled by the form selector directive

    this.formElements = {
        pageUrl:'/ginas/app/wizard?kind=chemical',
        formName: 'refForm',
        buttonId: 'references',
        fields: [/*{
            model: 'ref.citation',
            type: 'text-input'
        }, */{
            model: 'ref.docType',
            type: 'dropdown-select'
        }, {
            model: 'ref.tags',
            type: 'multi-select'
        }, {
            model: 'ref.url',
            type: 'text-input'
        }, {
            model: 'ref.access',
            type: 'form-selector'
        }, {
            binding: 'ref.uploadedFile',
            type: 'binding'
        }]
    }
};

describe ('name form', function() {

    it('name form tests', function () {
        var commonElementTests = require('./TestWizardCommonElements.js');
        var elements = new commonElementTests;
        var wizardRefPage = new WizardReferencePage();
        var pageUrl = wizardRefPage.formElements.pageUrl;
        var formName = wizardRefPage.formElements.formName;
        var buttonId = wizardRefPage.formElements.buttonId;
        var formElements = wizardRefPage.formElements.fields;


        for (var i = 0; i < formElements.length; i++) {
            var elementType = formElements[i].type;
            var model = formElements[i].model;
            switch (elementType) {
                case "text-input":
                    elements.testTextInput(buttonId, model, pageUrl);
                    break;
                case "dropdown-select":
                 //   elements.testDropdownSelectInput(buttonId, model, pageUrl);
                    break;
                case "multi-select":
                 //    elements.testMultiSelectInput(buttonId, model, pageUrl);
                    break;
                case "check-box":
                  //  elements.testCheckBoxInput(buttonId, model, pageUrl);
                    break;
            } //switch
        } //for i
    });
});

