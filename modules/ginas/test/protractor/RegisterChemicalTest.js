var RegisterChemicalPage = function () {

    this.getPage = function () {
        browser.get(browser.params.url);
    };

    this.clickById = function (name) {
        element(by.id(name)).click();
    };

    this.clickByModel = function (name) {
        element(by.model(name)).click();
    };

};

describe('Register chemical test', function () {

    var regChemPage = new RegisterChemicalPage();

    var jsonFile = require('../testJson/pass/aspirin1.json');

 /*   exports.config = {
        params: require('./testJson/pass/aspirin1.json')
    };*/

    //browser.params = require('./testJson/pass/aspirin1.json');

    /*it('should see the login page', function () {
        browser.ignoreSynchronization = true; //to non angular sites
        browser.get("http://localhost:9000/ginas/app");
        var loginButton = element(by.id('login-button'));
        loginButton.click();
            expect(browser.getCurrentUrl()).toMatch('/login');
    });*/

    it('verify login page', function() {
        browser.ignoreSynchronization = true; //to non angular sites
        browser.get("http://localhost:9000/ginas/app");
        element(by.id('login-button')).click();
        expect(browser.getCurrentUrl()).toMatch('/login');

        var uname = element(by.id('username'));
        var paswd = element(by.id('password'));
        var submitButton = element(by.tagName('button'));

        uname.sendKeys('admin');
        paswd.sendKeys('admin');
        submitButton.click();
        expect(browser.getCurrentUrl()).toMatch('/app');
        element(by.cssContainingText("a", "Register Substance")).click();
        expect(browser.getCurrentUrl()).toMatch('/register');
        element(by.cssContainingText("a", "Register a Chemical")).click();
        expect(browser.getCurrentUrl()).toEqual('http://localhost:9000/ginas/app/wizard?kind=chemical');

        var txtArea = element(by.model('paste'));
       // txtArea.sendKeys(JSON.stringify(jsonFile));
        txtArea.sendKeys(jsonFile.toString());
        var pasteButton = regChemPage.clickById('pasteSubstanceBtn');
        console.log("Json file names:" + jsonFile.names.length);


        element.all(by.repeater('obj in parent.names')).count().then(function(len) {
            console.log("name count:" + len);
        });

       // expect(names.count()).toBe(jsonFile.names.length);
        var submitButton = element(by.cssContainingText("button", "Submit"));
        submitButton.click();


        var ErorModal = element(by.id('byModalLabelExport'));
        var errorWarn = element.all(by.repeater('err in errorsArray'))

         });
     });




