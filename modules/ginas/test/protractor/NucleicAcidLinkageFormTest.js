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


    //test applyAll button

    this.formElements = {
        formName: 'linkageForm',
        fields: [{
            binding: 'noLinkages',
            type: 'binding'
        }, {
            model: 'linkage.linkage',
            type: 'dropdown-select'
        },  {
            model: 'linkage.sites',
            type: 'form-selector'
        }]
    }
};

