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
        formName: 'agentModForm',
        fields: [{
            model: 'agentMod.agentModificationType',
            type: 'dropdown-select'
        },{
            model: 'agentMod.agentModificationProcess',
            type: 'dropdown-select'
        },{
            model: 'agentMod.agentModificationRole',
            type: 'dropdown-select'
        },{
            model: 'agentMod.agentSubstance',
            type: 'substance-chooser'
        },{
            model: 'agentMod.amount',
            type: 'form-selector'
        }
        ]
    }
};

