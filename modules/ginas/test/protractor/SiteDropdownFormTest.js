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
//not toggleable
    this.formElements = {
        formName: 'siteForm',
        buttonID: 'Sites',
        fields: [{
            model: 'referenceobj.sites[0].subunitIndex',
            type: 'dropdown-select'
        },{
            model: 'referenceobj.sites[0].residueIndex',
            type: 'dropdown-select'
        },{
            model: 'referenceobj.sites[1].subunitIndex',
            type: 'dropdown-select'
        },{
            model: 'referenceobj.sites[1].residueIndex',
            type: 'dropdown-select'
        }
        ]
    }
};

