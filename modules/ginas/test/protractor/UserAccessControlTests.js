var UserAccessControlTests = function () {

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

describe('User Access Control Tests', function () {

    var userAccessTests = new UserAccessControlTests();

    it('should contain proper body', function() {
        browser.ignoreSynchronization = true; //to non angular sites
        browser.get("http://localhost:9000/ginas/app/register");
        var pageBody = element(by.tagName('body')).getText();
        pageBody.then(function(result) {
            expect(result).toContain('Not Authorized');
        });
    });

    it('should check login page', function() {
        browser.get("http://localhost:9000/ginas/app");
        element(by.id('login-button')).click();
        expect(browser.getCurrentUrl()).toMatch('/login');

        var uname = element(by.id('username'));
        var paswd = element(by.id('password'));
        var submitButton = element(by.tagName('button'));

        uname.sendKeys('test');
        paswd.sendKeys('abc');

        expect(uname.getAttribute('value')).toEqual('test');
        expect(paswd.getAttribute('value')).toEqual('abc');
        submitButton.click();
        expect(browser.getCurrentUrl()).toMatch('/login'); //login failure
    });
});



