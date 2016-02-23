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
        formName: 'codeForm',
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
        }, {
            model: 'code.comments',
            type: 'form-selector'
        }, {
            model: 'code.access',
            type: 'form-selector'
        }, {
            model: 'code.reference',
            type: 'form-selector'
        }]
    }

};

