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
        formName: 'diverseSourceForm',
        buttonID: 'sourceMaterials',
        fields: [{
            model: 'structurallyDiverse.sourceMaterialClass',
            type: 'dropdown-view-edit'
        }, {
            model: 'structurallyDiverse.sourceMaterialType',
            type: 'dropdown-view-edit'
        }, {
            model: 'structurallyDiverse.sourceMaterialState',
            type: 'dropdown-view-edit'
        }, {
            model: 'structurallyDiverse.references',
            type: 'form-selector'
        }]
    }
};

