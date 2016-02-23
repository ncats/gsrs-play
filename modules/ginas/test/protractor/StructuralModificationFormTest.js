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
        formName: 'strucModForm',
        buttonID: 'structuralModifications',
        fields: [{
            model: 'mod.structuralModificationType',
            type: 'dropdown-select'
        },{
            model: 'mod.molecularFragment',
            type: 'substance-chooser'
        },{
            model: 'mod.locationType',
            type: 'dropdown-select'
        },{
            model: 'mod.sites',
            type: 'form-selector'
        }, {
            model: 'mod.extent',
            type: 'dropdown-select'
        },{
            model: 'mod.amount',
            type: 'form-selector'
        }
        ]
    }
};

