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
        formObj: 'note',
        fields: [{
            model: 'note.note',
            type: 'text-box'
        }]
    };

    this.subForms = {
        formName: 'noteForm',
        buttonId: 'notes',
        formObj: 'note',
        fields: [{
            model: 'note.access',
            type: 'form-selector'
        }, {
            model: 'note.reference',
            type: 'form-selector'
        }]
    }
};

describe ('note form tests', function() {

    var noteForm = new NoteForm();
    beforeEach(function() {
        noteForm.getPage();
    });

    it('should see if form is visible', function () {
        var vis = browser.findElement(By.id('addNoteForm')).isDisplayed();
        expect(vis).toBe(false)
    });

    it('should test form toggling', function () {
        var buttonId = noteForm.formElements.buttonId;
        var vis = browser.findElement(By.id('addNoteForm')).isDisplayed();
        var button = browser.findElement(By.id(buttonId + "-toggle"));
        expect(browser.findElement(By.id('addNoteForm')).isDisplayed()).toBe(false);
        button.click();
        expect(browser.findElement(By.id('addNoteForm')).isDisplayed()).toBe(true);
        button.click();
        expect(browser.findElement(By.id('addNoteForm')).isDisplayed()).toBe(false);

    });

    it('should test all basic form elements', function () {
        var commonElementTests = require('./TestWizardCommonElements.js');
        var elements = new commonElementTests;
        var breadcrumb = ['notes-toggle'];
        elements.testInputFields(noteForm.formElements, breadcrumb);
    });

    it('should test subforms', function () {
        var commonElementTests = require('./TestWizardCommonElements.js');
        var elements = new commonElementTests;
        var breadcrumb = ['notes-toggle'];
        elements.testInputFields(noteForm.subForms, breadcrumb);
    });
});


