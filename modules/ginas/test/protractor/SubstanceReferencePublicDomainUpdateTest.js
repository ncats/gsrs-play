var SubstanceRefPublicDomain = function () {

    this.getCurrentPage = function () {
        browser.get(browser.getCurrentURL());
    };

    this.clickById = function (name) {
        element(by.id(name)).click();
    };

    this.clickByModel = function (name) {
        element(by.model(name)).click();
    };
};




describe('Public Domian Update test', function () {

     it('submit substance', function() {
        browser.ignoreSynchronization = true;
        browser.get("http://localhost:9000/ginas/app");
        element(by.id('login-button')).click();
        expect(browser.getCurrentUrl()).toMatch('/login');
        var uname = element(by.id('username'));
        var paswd = element(by.id('password'));
        var submitButton = element(by.tagName('button'));
        uname.sendKeys('admin');
        paswd.sendKeys('admin');
        expect(uname.getAttribute('value')).toEqual('admin');
        expect(paswd.getAttribute('value')).toEqual('admin');
        submitButton.click();
        expect(browser.getCurrentUrl()).toMatch('/ginas/app');

        element(by.cssContainingText("a", "Browse Substances")).click();
        // element.all(by.cssContainingText("a", "Edit Protein")).first().click();
        browser.get("http://localhost:9000/ginas/app/substance/75cb409e/edit");
        var urlString = browser.getCurrentUrl();
        element(by.buttonText("Submit")).click();
        var submitConfirm = element(by.id('submitConfirm'));
        browser.wait(EC.visibilityOf(submitConfirm), 5000);
        submitConfirm.click();
        var suModal = element(by.cssContainingText("a", "Return to Browse"));
        browser.wait(EC.visibilityOf(suModal), 1000);
        suModal.isDisplayed().then(function (result) {
            if (result) {
                suModal.click();
                expect(browser.getCurrentUrl()).toMatch('/ginas/app/substances')
                //browser.waitForAngular();
            } else {
                element(by.buttonText("Go Back")).click();
                expect(browser.getCurrentUrl()).toMatch(urlString.toString());
            }
        });


      //  browser.get(urlString.toString());
    /*    browser.get("http://localhost:9000/ginas/app/substance/75cb409e/edit");
        browser.switchTo().alert().then(
            function (alert) {
                console.log("there is an alert");
                alert.accept(); },
            function (err) { }
        );

       element(by.id('showJSONBtn')).click();
       element(by.buttonText("Show Raw")).click();
       var json = element(by.id('rawJson')).getText();
        //(not in CV)

        var vis = element(by.id('addRefOnlyForm'));
        vis.click();
        var button = element(by.id("references-toggle"));
        button.click();

        var firstChekBox = element(by.id('publicDomian'));
        firstChekBox.click();
        firstChekBox.isDisplayed().then(function (result) {
            if (result) {
                firstChekBox.click();
               // expect(browser.getCurrentUrl()).toMatch('/ginas/app/substances')
                //browser.waitForAngular();
            } else {
                var notChecked = element.all(by.id('fieldNotSet')).first();
                notChecked.click();
            }
        });*/
    });
});
