var ProteinWizardPage = function () {

    this.getPage = function () {
        browser.get('/ginas/app/wizard?kind=structurallyDiverse');
    };

    this.clickById = function (name) {
        element(by.id(name)).click();
    };

    this.clickByModel = function (name) {
        element(by.model(name)).click();
    };

    this.formElements = {
        formName: 'noteForm',
        buttonID: 'notes',
        fields: [{
            model: 'note.note',
            type: 'text-box'
        }, {
            model: 'note.access',
            type: 'form-selector'
        }, {
            model: 'note.reference',
            type: 'form-selector'
        }]
    }
};

