var NoteForm = function () {

    this.getPage = function () {
        browser.get(browser.params.url);
    };

    this.clickById = function (name) {
        element(by.id(name)).click();
    };

    this.clickByModel = function (name) {
        element(by.model(name)).click();
    };

    this.formElements = {
        formName: 'noteForm',
        buttonId: 'notes',
        fields: [{
            model: 'note.note',
            type: 'text-box'
/*        }, {
            model: 'note.access',
            type: 'form-selector'
        }, {
            model: 'note.reference',
            type: 'form-selector'*/
        }]
    }
};

describe ('note form tests', function() {

    var noteForm = new NoteForm();
    beforeEach(function() {
        noteForm.getPage();
    });

    it('tests all form elements are loaded', function () {
        var commonElementTests = require('./TestWizardCommonElements.js');
        var elements = new commonElementTests;
        var buttonId = noteForm.formElements.buttonId;
        var formElements = noteForm.formElements.fields;
        var refElementTests = require('./ReferenceFormTest.js');
        var refPage = new refElementTests;
        for (var i = 0; i < formElements.length; i++) {
            var elementType = formElements[i].type;
            var model = formElements[i].model;
            noteForm.getPage();
            switch (elementType) {
                case "text-input":
                    elements.testTextInput(buttonId, model);
                    break;
                case "dropdown-select":
                    elements.testDropdownSelectInput(buttonId, model);
                    break;
                case "multi-select":
                    // elements.testMultiSelectInput(buttonId, model);
                    break;
                case "check-box":
                    // elements.testCheckBoxInput(buttonId, model);
                    break;
                case "form-selector":
                    // refPage.refPageTests(buttonId, model);
                    break;
            } //switch
        } //for i
    });
});


