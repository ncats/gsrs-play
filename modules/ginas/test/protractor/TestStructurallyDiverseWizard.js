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

//}, 'diverseType', 'sourceMaterials', 'parents', 'parts', 'agentModifications', 'structuralModifications', 'physicalModifications', 'codes', 'relationships', 'notes', 'properties', 'references']

///    [{formName: 'nameForm',
    this.forms = {
        formElements: [
            {
                formName: 'nameForm',
                fields: [{
                    model: 'name.name',
                    type: 'text-input'
                }, {
                    model: 'name.type',
                    type: 'dropdown-select'
                }, {
                    model: 'name.languages',
                    type: 'multi-select'
                }, {
                    model: 'name.displayName',
                    type: 'check-box'
                }, {
                    model: 'name.preferred',
                    type: 'check-box'
                }, {
                    model: 'name.access',
                    type: 'form-selector'
                }, {
                    model: 'name.reference',
                    type: 'form-selector'
                }, {
                    model: 'name.domains',
                    type: 'multi-select'
                }, {
                    model: 'name.nameOrgs',
                    type: 'form-selector'
                }, {
                    model: 'name.nameJurisdiction',
                    type: 'multi-select'
                }
                ]
            },{
                formName: 'orgForm',
                fields: [{
                    model: 'org.nameOrg',
                    type: 'text-input'
                }, {
                    model: 'name.deprecated',
                    type: 'check-box'
                }, {
                    model: 'name.deprecatedDate',
                    type: 'date-picker'
                }]
            }]

    };

    this.testTextInput = function (model) {
        var userType = element(by.model(model));
        expect(userType.getText()).toEqual('');
    };

    this.testDropdownSelectInput = function (model) {
        var t = model.split(".")[1];
        console.log("ggggg");
        var userType = element(by.model(model));
        console.log(userType.all(by.id(t)));
        console.log(userType.all());
        expect(userType).$('option:checked').getText().toContain(t);

        /* element(by.model(model)).all(by.id(t)).each(function (element, index) {
         console.log(t);
         /!*element.getText().then(function (text) {
         var items = text.split('\n');
         console.log(items.length);
         expect(items.length).toBeGreaterThan(0);
         });*!/
         });*/
    };

    this.testMultiSelectInput = function (model) {
        // console.log("multi-select " +model);
    };

    this.testCheckBoxInput = function (model) {
        //   console.log("checkbox " +model);
    };

};

describe('Wizard Protein', function () {
    it('Check Wizard Load', function () {
        var proteinWizardPage = new ProteinWizardPage();
        proteinWizardPage.getPage();
        proteinWizardPage.clickById('names');
        /* proteinWizardPage.clickByModel('name.type');
         expect(element(by.model('name.type')).$('option:checked').getText()).toEqual('Type...');

         element(by.model('name.type')).all(by.id('type')).each(function (element, index) {
         element.getText().then(function (text) {
         var items = text.split('\n');
         console.log(items[5]);
         expect(items.length).toBe(7);
         expect(items[0]).toBe('Type...');
         expect(items[5]).toBe('Systematic Name');
         });
         });*!/*/
    }, 100000);

    it('should initialize to model', function () {
        var proteinWizardPage = new ProteinWizardPage();
        var page = proteinWizardPage.getPage();
        var forms = proteinWizardPage.forms;
        for (var i = 0; i < forms.formElements.length; i++) {
            var elementType = forms.formElements[i].type;
            var model = forms.formElements[i].model;
            switch (elementType) {
                case "text-input":
                    proteinWizardPage.testTextInput(model);
                    break;
                case "dropdown-select":
                    proteinWizardPage.clickByModel(model);
                    //  expect(element(by.model(model)).$('option:checked').getText()).toContain('type');
                    proteinWizardPage.testDropdownSelectInput(model);

                    break;
                case "multi-select":
                    proteinWizardPage.testMultiSelectInput(model);
                    break;
                case "check-box":
                    proteinWizardPage.testCheckBoxInput(model);
                    break;
            }
        }
    });
});
