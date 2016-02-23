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

    //sketcher will probably need its own set of tests
//not toggleable

    this.formElements = {
        formName: 'structureForm',
        buttonID: 'structure',
        fields: [{
            model: '"parent.structure.formula',
            type: 'text-input'
        }, {
            model: 'parent.stereochemistry',
            type: 'dropdown-view-edit'
        }, {
            model: 'parent.structure.opticalActivity',
            type: 'dropdown-view-edit'
        }, {
            model: 'parent.structure.atropisomerism',
            type: 'dropdown-view-edit'
        }, {
            model: 'parent.access',
            type: 'form-selector'
        }, {
            model: 'parent.references',
            type: 'form-selector'
        },{
            binding: 'parent.structure.mwt',
            type: 'binding'
        }, {
            binding: 'parent.structure.definedStereo',
            type: 'binding'
        }, {
            binding: 'parent.structure.stereoCenters',
            type: 'binding'
        }, {
            binding: 'parent.structure.ezCenters',
            type: 'binding'
        }, {
            binding: 'parent.structure.charge',
            type: 'binding'
        }, {
            model: 'parent.structure.stereoComments',
            type: 'form-selector'
        }]
    }
};

