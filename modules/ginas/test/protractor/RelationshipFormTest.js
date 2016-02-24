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
        formName: 'relationshipForm',
        buttonID: 'relationships',
        fields: [{
            model: 'relationship.relatedSubstance',
            type: 'substance-chooser'
        },{
            model: 'relationship.type',
            type: 'dropdown-select'
        },{
            model: 'relationship.interactionType',
            type: 'dropdown-select'
        },{
            model: 'relationship.agentSubstance',
            type: 'substance-chooser'
        },{
            model: 'relationship.access',
            type: 'form-selector'
        }, {
            model: 'relationship.reference',
            type: 'form-selector'
        }
        ]
    }
};

