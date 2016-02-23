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

    //this form may need to be examined--- might not be fully enterable
    //does not have toggle directive -- needs to be upgraded
    this.formElements = {
        formName: 'partForm',
        buttonID: 'organismDetails',

        fields: [{
            model: 'parent.structurallyDiverse.partLocation',
            type: 'dropdown-view-edit'
        }, {
            model: 'parent.structurallyDiverse.part',
            type: 'multi-select'
        }, {
            model: 'parent.structurallyDiverse.fractionName',
            type: 'text-input'
        },{
            model: '"parent.structurallyDiverse.fractionMaterialType',
            type: 'dropdown-view-edit'
        }]
    }
};

