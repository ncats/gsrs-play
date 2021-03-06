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
        formName: 'orgForm',
        buttonID: 'nameOrgs',
        fields: [{
            model: 'org.nameOrg',
            type: 'multi-select'
        }, {
            model: 'name.deprecated',
            type: 'check-box'
        }, {
            model: 'name.deprecatedDate',
            type: 'date-picker'
        }]
    }
};

