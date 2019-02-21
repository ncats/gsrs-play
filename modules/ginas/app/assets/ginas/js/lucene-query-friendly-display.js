(function () {
    'use strict';

    angular
        .module('ginas')
        .directive('luceneQueryFriendlyDisplay', luceneQueryFriendlyDisplay);

    function luceneQueryFriendlyDisplay($location, $sce, $q, searchService) {

        var directive = {
            link: link,
            scope: {},
            restrict: 'A',
            template: '<span ng-bind-html="luceneQueryFriendlyDisplayVariables.longQueryDisplay" ng-show="luceneQueryFriendlyDisplayVariables.isShowLongDisplay"></span>' +
            '<span ng-bind-html="luceneQueryFriendlyDisplayVariables.shortQueryDisplay" ng-show="!luceneQueryFriendlyDisplayVariables.isShowLongDisplay"></span>' +
            '&nbsp;<a ng-click="luceneQueryFriendlyDisplayVariables.isShowLongDisplay = !luceneQueryFriendlyDisplayVariables.isShowLongDisplay">show {{luceneQueryFriendlyDisplayVariables.isShowLongDisplay ? "short" : "long"}} explanation</a>'
        };
        return directive;

        function link(scope) {

            scope.luceneQueryFriendlyDisplayVariables = {
                longQueryDisplay: null,
                shortQueryDisplay: null,
                isShowLongDisplay: false
            }

            var privateVariables = { 
                longQueryDisplay: '',
                shortQueryDisplay: '',
                searchFields: null
            }

            function loadDirective() {
                getSearchFieldsDictionary().then(function () {
                    if ($location.search()['q']) {
                        processLuceneQueryString($location.search()['q']);
                    }
                }, function (){});
            }

            function getSearchFieldsDictionary() {
                var deferred = $q.defer();

                searchService.getSearchFieldsDictionary().then(function success(response) {
                    privateVariables.searchFields = response.data;
                    deferred.resolve();
                }, function error() {
                    deferred.reject();
                });

                return deferred.promise;
            }

            function processLuceneQueryString(queryString) {
                queryString = queryString.replace(/-/g, "\\-");
                try {
                    processLuceneParserObject(luceneParser.parse(queryString));
                } catch (e) {
                    console.log(e);
                }
            }

            function processLuceneParserObject(luceParserResults) {
                Object.keys(luceParserResults).forEach(function (key) {
                    if (key === 'left' || key === 'right') {
                        if (luceParserResults[key].left || luceParserResults[key].right) {
                            processLuceneParserObject(luceParserResults[key]);
                        } else {
                            constructFriendlyString(luceParserResults[key]);
                        }
                    } else if (key === 'operator') {

                        privateVariables.longQueryDisplay += '. Also'

                        if (luceParserResults[key] === 'AND') {
                            privateVariables.longQueryDisplay += ' making sure to search '
                            privateVariables.shortQueryDisplay += ' AND ';
                        } else {
                            privateVariables.longQueryDisplay += ', if the previous condition(s) returns no results, search ';
                            privateVariables.shortQueryDisplay += ' OR ';
                        }
                        
                    }
                });

                loadQueryDisplay();
            }

            function constructFriendlyString(luceneParcerObject) {
                var friendlyQuery = ' in ';
                var shortFriendlyQuery = ' for '

                var fieldTitle = privateVariables.searchFields[luceneParcerObject.field] && privateVariables.searchFields[luceneParcerObject.field].title || luceneParcerObject.field;

                var fieldString = luceneParcerObject.field === '<implicit>' ? 'any ' : ('the <strong>' + fieldTitle + '</strong> ');

                friendlyQuery += fieldString;
                shortFriendlyQuery += fieldString;

                friendlyQuery += 'field, for a value or values that '
                shortFriendlyQuery += 'field '

                var fieldType = privateVariables.searchFields[luceneParcerObject.field] &&  privateVariables.searchFields[luceneParcerObject.field].type || 'text';

                if (fieldType === 'text') {
                    for (var i = 0; i < termPatterns[fieldType].length; i++) {
                        if (termPatterns[fieldType][i].pattern.test(luceneParcerObject.term)) {
                            friendlyQuery += termPatterns[fieldType][i].getFriendlyString(luceneParcerObject.term).friendlyString;
                            shortFriendlyQuery += termPatterns[fieldType][i].getFriendlyString(luceneParcerObject.term).shortFriendlyString;
                             break;
                        }
                    }
                } else {
                    if (luceneParcerObject.term || (Number(luceneParcerObject.term_max) - Number(luceneParcerObject.term_min)) === 86399999) {
                         var valueString = luceneParcerObject.term || moment(luceneParcerObject.term_max, 'x').format('M/D/YYYY');

                         friendlyQuery += 'must equal ' + valueString;
                         shortFriendlyQuery += ' = ' +  valueString;
                    } else {
                        var startsWithExists = false;

                        if (luceneParcerObject.term_min !== '\\-10E50') {
                            startsWithExists = true;
                            var valueString = '<strong>' + (fieldType === 'timestamp' && moment(luceneParcerObject.term_min, 'x').format('M/D/YYYY') || luceneParcerObject.term_min) + '</strong>';

                            friendlyQuery += 'must greater than ' + valueString;
                            shortFriendlyQuery += ' > ' + valueString;
                        } 

                        if (luceneParcerObject.term_max !== '10E50') {
                            var valueString = '<strong>' + (fieldType === 'timestamp' && moment(luceneParcerObject.term_max, 'x').format('M/D/YYYY') || luceneParcerObject.term_max) + '</strong>';

                            friendlyQuery += (startsWithExists? ' and ' : '') + 'must be less than ' + valueString;
                            shortFriendlyQuery += (startsWithExists? ' & ' : '') + '< ' + valueString;
                        }
                    }
                }

                privateVariables.longQueryDisplay += friendlyQuery;
                privateVariables.shortQueryDisplay += shortFriendlyQuery;
            }

            var termPatterns = {
                text: [
                    {
                        termType: 'exact',
                        pattern: /^\^.*\$$/,
                        getFriendlyString: function (termString) {
                            var value = termString.substring(
                                termString.lastIndexOf("^") + 1,
                                termString.lastIndexOf("$")
                            )
                            
                            var returnObject = {
                                friendlyString: 'must exactly match <strong>' + value + '</strong>',
                                shortFriendlyString: ' = <strong>' + value + '</strong>'
                            };

                            return returnObject;
                        }
                    },
                    {
                        termType: 'startsWith',
                        pattern: /^\^.*/,
                        getFriendlyString: function (termString) {
                            var value = termString.substring(
                                termString.lastIndexOf("^") + 1
                            )

                            var returnObject = {
                                friendlyString: 'must start with <strong>' + value + '</strong>',
                                shortFriendlyString: ' = <strong>' + value + '</strong> ...'
                            };

                            return returnObject;
                        }
                    },
                    {
                        termType: 'endsWith',
                        pattern: /.*\$$/,
                        getFriendlyString: function (termString) {
                            var value = termString.substring(
                                0,
                                termString.lastIndexOf("$")
                            )

                            var returnObject = {
                                friendlyString: 'must end with <strong>' + value + '</strong>',
                                shortFriendlyString: ' = ... <strong>' + value + '</strong>'
                            };

                            return returnObject;
                        }
                    },
                    {
                        termType: 'containsPartial',
                        pattern: /^\*.*\*$/,
                        getFriendlyString: function (termString) {
                            var value = termString.substring(
                                termString.indexOf("*") + 1,
                                termString.lastIndexOf("*")
                            )

                            var returnObject = {
                                friendlyString: 'has a WORD that contains <strong>' + value + '</strong>',
                                shortFriendlyString: ' = ...<strong>' + value + '</strong>...'
                            };

                            return returnObject;
                        }
                    },
                    {
                        termType: 'startsWithPartial',
                        pattern: /.*\*$/,
                        getFriendlyString: function (termString) {
                            var value = termString.substring(
                                0,
                                termString.lastIndexOf("*")
                            )

                            var returnObject = {
                                friendlyString: 'starts with with the word(s) <strong>' + value + '</strong>',
                                shortFriendlyString: ' = <strong>' + value + '</strong>...'
                            };

                            return returnObject;
                        }
                    },
                    {
                        termType: 'endsWithPartial',
                        pattern: /^\*.*/,
                        getFriendlyString: function (termString) {
                            var value = termString.substring(
                                termString.lastIndexOf("*") + 1
                            )

                            var returnObject = {
                                friendlyString: 'ends with with the word(s) <strong>' + value + '</strong>',
                                shortFriendlyString: ' = ...<strong>' + value + '</strong>'
                            };

                            return returnObject;
                        }
                    },
                    {
                        termType: 'contains',
                        pattern: /./,
                        getFriendlyString: function (termString) {
                            var value = termString

                            var returnObject = {
                                friendlyString: 'contains <strong>' + value + '</strong>',
                                shortFriendlyString: ' = ...<strong>' + value + '</strong>...'
                            };

                            return returnObject;
                        }
                    }
                ]
            }

            function loadQueryDisplay() {
                scope.luceneQueryFriendlyDisplayVariables.longQueryDisplay = $sce.trustAsHtml(privateVariables.longQueryDisplay);
                scope.luceneQueryFriendlyDisplayVariables.shortQueryDisplay = $sce.trustAsHtml(privateVariables.shortQueryDisplay);
            }

            loadDirective();
        }
    }
})()