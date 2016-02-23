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

//anything with '$$' in fron of the field name should get stripped out with the angular.toJson function
    //2 variables are passed in, so we probably want to check with binding
    //no button to toggle
    this.formElements = {
        formName: 'headerForm',
        fields: [{
            binding: 'formType',
            type: 'binding'
        },{
            binding: 'name',
            type: 'binding'
        },{
            model: 'parent.definitionType',
            type: 'dropdown-select'
        },{
            model: 'parent.$$relatedSubstance',
            type: 'form-selector'
        },{
            model: 'parent.access',
            type: 'form-selector'
        }]
    }
};

