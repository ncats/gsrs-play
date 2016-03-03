var SiteStringPage = function () {

    this.getPage = function () {
        browser.get(browser.params.url);
    };

    this.clickById = function (name) {
        element(by.id(name)).click();
    };

    this.clickByModel = function (name) {
        element(by.model(name)).click();
    };

    //this form has a lot of parsing involved

//not toggleable
    this.formElements = {
        formName: 'siteStringForm',
        formObj:'site',
        buttonId: 'sites',
        fields: [{
            model: 'referenceobj.$$displayString',
            type: 'text-box'
        }
        ]
    }
};
module.exports=SiteStringPage;

