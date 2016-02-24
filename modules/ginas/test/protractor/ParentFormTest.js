var ProteinWizardPage = function () {

    this.getPage = function () {
        browser.get('/ginas/app/wizard?kind=mixture');
    };

    this.clickById = function (name) {
        element(by.id(name)).click();
    };

    this.clickByModel = function (name) {
        element(by.model(name)).click();
    };

    this.formElements = {
        formName: 'parentForm',
        buttonID: 'parents',
        fields: [{
            model: 'parent.structurallyDiverse.parentSubstance',
            type: 'substance-chooser'
        },{
            model: 'parent.structurallyDiverse.hybridSpeciesPaternalOrganism',
            type: 'substance-chooser'
        },{
            model: 'parent.structurallyDiverse.hybridSpeciesMaternalOrganism',
            type: 'substance-chooser'
        }]
    }
};

