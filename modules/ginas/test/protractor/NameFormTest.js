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
//future tests include:
    //validation on duplicates
    //testing of name resolver function
    //required fields
    

    this.formElements = {
        formName: 'nameForm',
        buttonID: 'names',
        fields: [{
            model: 'name.name',
            type: 'text-input'
        }, {
            model: 'name.type',
            type: 'dropdown-select'
        }, {
            model: 'name.languages',
            type: 'multi-select'
        }, {
            model: 'name.displayName',
            type: 'check-box'
        }, {
            model: 'name.preferred',
            type: 'check-box'
        }, {
            model: 'name.access',
            type: 'form-selector'
        }, {
            model: 'name.reference',
            type: 'form-selector'
        }, {
            model: 'name.domains',
            type: 'multi-select'
        }, {
            model: 'name.nameOrgs',
            type: 'form-selector'
        }, {
            model: 'name.nameJurisdiction',
            type: 'multi-select'
        }
        ]
    }
};

