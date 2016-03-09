var DiverseDetailsPage = function () {

    this.getPage = function () {
        browser.get(browser.params.url);
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
        buttonId:'Details',
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

