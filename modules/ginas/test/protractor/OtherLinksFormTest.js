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
        formName: 'otherLinksForm',
        fields: [{
            model: 'otherLink.linkageType',
            type: 'dropdown-select'
        }, {
            model: 'otherLink',
            type: 'form-selector'
        }]
    }
};

