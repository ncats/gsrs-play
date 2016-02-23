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
        formName: 'physicalModForm',
        fields: [{
            model: 'physicalModification.physicalModificationRole',
            type: 'dropdown-select'
        }, {
            model: 'physicalModification.physicalParameter',
            type: 'form-selector'
        }]
    }
};

