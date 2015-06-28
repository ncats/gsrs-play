requirejs.config({
    paths: {
        'ui-bootstrap': webjars.path('angular-ui-bootstrap', 'ui-bootstrap'),
        'ui-bootstrap-tpls': webjars.path('angular-ui-bootstrap', 'ui-bootstrap-tpls')
    },
    shim: {
        'ui-bootstrap': [ 'angular' ],
        'ui-bootstrap-tpls': [ 'angular' ]
    }
});
