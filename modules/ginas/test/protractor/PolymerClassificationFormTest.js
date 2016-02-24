var ProteinWizardPage = function () {

    this.getPage = function () {
        browser.get('/ginas/app/wizard?kind=nucleicAcid');
    };

    this.clickById = function (name) {
        element(by.id(name)).click();
    };

    this.clickByModel = function (name) {
        element(by.model(name)).click();
    };

    this.formElements = {
        formName: 'classificationForm',
        buttonID: 'polymerClassification',
        fields: [{
            model: 'parent.polymer.classification.polymerClass',
            type: 'dropdown-view-edit'
        }, {
            model: 'parent.polymer.classification.polymerSubclass',
            type: 'dropdown-view-edit'
        }, {
            model: 'parent.polymer.classification.polymerGeometry',
            type: 'dropdown-view-edit'
        }, {
            model: 'parent.polymer.classification.sourceType',
            type: 'dropdown-view-edit'
        }, {
            model: 'parent.polymer.classification.parentSubstance',
            type: 'substance-chooser'
        }, {
            model: 'parent.polymer.access',
            type: 'form-selector'
        }, {
            model: 'parent.polymer.reference',
            type: 'form-selector'
        }]
    }
};

