var WizardReferencePage = function () {

    this.getPage = function(){
        browser.get(browser.params.url);
    };

    this.clickById = function (name) {
        element(by.id(name)).click();
    };

    this.clickByModel = function (name) {
        element(by.model(name)).click();
    };
///reference apply needs its own functionality check
    //upload file needs separate testing
    //this form is not collapsible, it is toggled by the form selector directive

    this.formElements = {
        formName: 'refForm',
        buttonId: 'references',
        formObj: 'reference',
        fields: [{
            model: 'reference.citation',
            type: 'text-input'
        }, {
            model: 'reference.docType',
            type: 'dropdown-select'
        }, {
            model: 'reference.tags',
            type: 'multi-select'
/*        }, {
            model: 'reference.url',
            type: 'text-input'*/
/*        },{
            model: 'access',
            type: 'form-selector'*/
/*        }, {
            binding: 'reference.uploadedFile',
            type: 'binding'*/
        }]
    };
};

module.exports = WizardReferencePage;