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
                ///this form operates a little strangely, because the fields can only be set once, they aren't added to an array...
    this.formElements = {
        formName: 'diverseDetailsForm',
        buttonID:'Details',
        fields: [{
            model: 'structurallyDiverse.infraSpecificType',
            type: 'dropdown-view-edit'
        }, {
            model: 'structurallyDiverse.infraSpecificName',
            type: 'dropdown-view-edit'
        }, {
            model: 'structurallyDiverse.developmentalStage',
            type: 'dropdown-view-edit'
        }]
    }
};

