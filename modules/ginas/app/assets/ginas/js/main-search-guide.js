(function () {
    'use strict';

    angular
        .module('ginas')
        .directive('mainSearchGuide', mainSearchGuide);

    function mainSearchGuide(searchService, $location, $q, $timeout) {
        var directive = {
            link: link,
            scope: {
                q: '=',
                submitQuery: '&onQuerySubmit',
                control: '='
            },
            restrict: 'E',
            templateUrl: baseurl + 'assets/templates/elements/main-search-guide.html',
        };
        return directive;

        function link(scope, element, attrs) {

            scope.internalControl = scope.control || {};

            var everyWhereOption = {
                title: 'every field of the record',
                field: 'everywhere',
                type: 'text',
                description: ''
            };

            var otherOption = {
                title: 'Other',
                field: '',
                type: 'text',
                description: '',
                isOther:true,
                otherIndex: 0
            }

            var manuallyEnteredOption = function (fieldName) {
                return {
                    title: fieldName || 'Other',
                    field: fieldName || '',
                    type: 'text',
                    description: 'mannually entered field',
                    display: 'Other' + (fieldName ? (' - ' + fieldName) : ''),
                    isOther:true
                }
            }

            var Term = function (r) {
                var nt = {};

                nt.nextOptions = [];

            	/**
            	 * Validate should throw an exception if there's
            	 * a problem.
            	 * 
            	 */
                nt.validate = function (v) {
                    if (typeof v === "undefined" || v === null || (v.trim && v.trim() === "")) {
                        throw "Required";
                    }
                };

                nt.constructQuery = function (field, value) {
                    throw "Term type has no implementation for constructing a query!";
                };

                nt.setValidator = function (v) {
                    if (v) {
                        nt.validate = v;
                    }
                    return nt;
                };

                nt.addValidator = function (val) {
                    var oldV = nt.validate;
                    return nt.setValidator(function (v) {
                        oldV(v);
                        val(v);
                    });
                };
            	/**
            	 * This will form the query string from
            	 * the supplied prefix and value. If the prefix
            	 * does not end in ':', it will be added before
            	 * going forward.
            	 */
                nt.setConstructQuery = function (cq) {
                    if (cq) {
                        nt.constructQuery = function (f, v) {
                            if (f && !_.endsWith(f, ':')) {
                                f = f + ":";
                            }
                            return cq(f, v);
                        };
                    }
                    return nt;
                };

            	/**
            	 * This is a helper function to set a construct query
            	 * operation where the terms are parsed first as tokens
            	 */
                nt.setConstructQueryMultipleTerms = function (cq) {
                    if (cq) {
                        return nt.setConstructQuery(function (f, v) {
                            return cq(f, nt.parseWords(v));
                        });
                    }
                    return nt;
                };

            	/**
            	 * Utility method to parse words out of term
            	 */
                nt.parseWords = function (v) {
                    var list = v.trim().split(/\s+/);
                    return list;
                }

                nt.setValue = function (v) {
                    nt.value = v;
                    return nt;
                }
                nt.setDisplay = function (d) {
                    nt.display = d;
                    return nt;
                }
                nt.setDisplay = function (d) {
                    nt.display = d;
                    return nt;
                }
                nt.addNextOption = function (no) {
                    nt.nextOptions.push(Term(no));
                    return nt;
                };
                nt.setNextOptions = function (nos) {
                    nt.nextOptions = [];
                    if (nos) {
                        for (var i = 0; i < nos.length; i++) {
                            nt.addNextOption(nos[i]);
                        }
                    }
                    return nt;
                };

                nt.from = function (raw) {
                    return nt.setValue(raw.value)
                        .setDisplay(raw.display)
                        .setNextOptions(raw.nextOptions)
                        .setValidator(raw.validate)
                        .setConstructQuery(raw.constructQuery)
                        .setConstructQueryMultipleTerms(raw.constructQueryMultipleTerms);
                };

                if (r) {
                    return nt.from(r);
                }
                return nt;
            };

            var SingleWordTerm = function (r) {
                return Term(r).setValidator(function (v) {
                    var list = v.trim().split(/\s+/);
                    if (list.length > 1) throw "Does not support multiple words";
                });
            }
            var TimeStampTerm = function (r) {
                var t = Term().addValidator(function (v) {
                    if (!moment(v).isValid()) {
                        throw "Not a valid date";
                    }
                }).from(r);
                return t;
            }

            var FloatRangeTerm = function (r) {
                var t = Term().addValidator(function (v) {
                    if (isNaN(v)) {
                        throw "Value \"" + v + "\" is not a number";
                    }
                }).from(r);
                return t;
            }


            var queryTermOptions = {

                text: [
                    Term({
                        value: 'exact',
                        display: 'for the following exact phrase, which must match completely (no partial words)',
                        constructQuery: function (f, v) {
                            return f + ('"^' + v + '$"');
                        }
                    }),
                    Term({
                        value: 'contains',
                        display: 'for the following contained phrase, which must be found as written (no partial words)',
                        constructQuery: function (f, v) {
                            return f + ('"' + v + '"');
                        }
                    }),
                    Term({
                        value: 'any',
                        display: 'for ANY of the following words in any order or position',
                        constructQueryMultipleTerms: function (f, v) {
                            var sq = _.chain(v)
                                .map(function (t) {
                                    return f + t;
                                })
                                .value()
                                .join(" OR ");
                            if (v.length > 1) {
                                return "(" + sq + ")";
                            } else {
                                return sq;
                            }
                        }
                    }),
                    Term({
                        value: 'all',
                        display: 'for ALL of the following words in any order or position',
                        constructQueryMultipleTerms: function (f, v) {
                            var sq = _.chain(v)
                                .map(function (t) {
                                    return f + t;
                                })
                                .value()
                                .join(" AND ");
                            if (v.length > 1) {
                                return "(" + sq + ")";
                            } else {
                                return sq;
                            }
                        }
                    }),
                    SingleWordTerm({
                        value: 'startsWithPartial',
                        display: 'for a WORD that starts with',
                        constructQuery: function (f, v) {
                            return f + ('' + v + '*');
                        }
                    }),
                    SingleWordTerm({
                        value: 'endsWithPartial',
                        display: 'for a WORD that ends with',
                        constructQuery: function (f, v) {
                            return f + ('*' + v + '');
                        }
                    }),
                    SingleWordTerm({
                        value: 'containsPartial',
                        display: 'for a WORD that contains',
                        constructQuery: function (f, v) {
                            return f + ('*' + v + '*');
                        }
                    }),
                    Term({
                        value: 'startsWith',
                        display: 'for a value that starts with with the word(s)',
                        constructQuery: function (f, v) {
                            return f + ('"^' + v + '"');
                        }
                    }),
                    Term({
                        value: 'endsWith',
                        display: 'for a value that ends with the word(s)',
                        constructQuery: function (f, v) {
                            return f + ('"' + v + '$"');
                        }
                    })
                ],
                timestamp: [

                    TimeStampTerm({
                        value: 'startsWith',
                        display: 'for a date range that starts with',
                        constructQuery: function (f, v) {
                            var timestampStart = moment(v).set({ hour: 0, minute: 0, second: 0, millisecond: 0 }).utc().valueOf();
                            return f + ('[' + timestampStart + ' TO ');
                        },
                        nextOptions: [
                            TimeStampTerm({
                                value: 'end',
                                display: 'and doesn\'t end',
                                constructQuery: function (f, v) {
                                    return '10E50]';
                                }
                            }),
                            TimeStampTerm({
                                value: 'endsWith',
                                display: 'and ends with',
                                constructQuery: function (f, v) {
                                    var timestampEnd = moment(v).set({ hour: 23, minute: 59, second: 59, millisecond: 999 }).utc().valueOf();
                                    return timestampEnd + "]";
                                }
                            })
                        ]
                    }),
                    TimeStampTerm({
                        value: 'exact',
                        display: 'specifically for this date',
                        constructQuery: function (f, v) {
                            var timestampStart = moment(v).set({ hour: 0, minute: 0, second: 0, millisecond: 0 }).utc().valueOf();
                            var timestampEnd = moment(v).set({ hour: 23, minute: 59, second: 59, millisecond: 999 }).utc().valueOf();
                            return f + ('[' + timestampStart + ' TO ' + timestampEnd + ']');
                        }
                    }),
                    TimeStampTerm({
                        value: 'endsWith',
                        display: 'for a date before',
                        constructQuery: function (f, v) {
                            var timestampEnd = moment(v).set({ hour: 23, minute: 59, second: 59, millisecond: 999 }).utc().valueOf();
                            return f + ('[-10E50 TO ' + timestampEnd + ']');
                        }
                    })
                ],
                float: [
                    FloatRangeTerm({
                        value: 'startsWith',
                        display: 'for a number range that starts with',
                        constructQuery: function (f, v) {
                            return f + ('[' + v + ' TO ');
                        },
                        nextOptions: [
                            FloatRangeTerm({
                                value: 'end',
                                display: 'and doesn\'t end',
                                constructQuery: function (f, v) {
                                    return '10E50]';
                                }
                            }),
                            FloatRangeTerm({
                                value: 'endsWith',
                                display: 'and ends with',
                                constructQuery: function (f, v) {
                                    return v + ']';
                                }
                            })
                        ]
                    }),
                    FloatRangeTerm({
                        value: 'endsWith',
                        display: 'for a number lower than',
                        constructQuery: function (f, v) {
                            return f + '[-10E50 TO ' + v + ']';
                        }
                    }),
                    FloatRangeTerm({
                        value: 'exact',
                        display: 'specifically for this number',
                        constructQuery: function (f, v) {
                            return f + v;
                        }
                    })

                ]
            }

            var booleanOperatorOptions = [
                {
                    value: 'end',
                    display: 'and that\'s it'
                },
                {
                    value: 'AND',
                    display: 'also make sure to search'
                },
                {
                    value: 'OR',
                    display: 'if the above is not found, search'
                }
            ];

            var defaultQuery = function () {
                return {
                    field: everyWhereOption,
                    terms: [
                        {
                            type: 'exact',
                            value: '',
                            queryOption: _.filter(queryTermOptions.text, { value: 'exact' })[0],
                            options: queryTermOptions.text
                        },
                        {
                            type: 'end',
                            value: ''
                        }
                    ],
                    type: 'text',
                    index: 0,
                    selectedBool: booleanOperatorOptions[0]
                }
            };

            scope.mainSearchGuideVariables = {
                fieldOptions: [],
                queries: [],
                booleanOperatorOptions: booleanOperatorOptions,
                isSearchValid: false,
                otherFieldName: '',
            }

            var otherInputTimer;

            scope.internalControl.loadDirective = function () {
                scope.mainSearchGuideVariables.queries = [];

                getSearchFields().then(function success() {
                    if (scope.q || $location.search()['q']) {
                        var query = scope.q || $location.search()['q'];
                        processLuceneQueryString(query);
                        scope.validateQuery();
                    }

                    if (scope.mainSearchGuideVariables.queries.length === 0) {
                        scope.mainSearchGuideVariables.queries.push(defaultQuery());
                    }
                }, function error() { });
            }

            function processLuceneQueryString(queryString) {
                queryString = queryString.replace(/-/g, "\\-");

                try {
                    processLuceneParserObject(luceneParser.parse(queryString));
                } catch(e) {
                    console.log(e);
                }
            }


            function processLuceneParserObject(luceParserResults) {

                Object.keys(luceParserResults).forEach(function (key) {
                    if (key === 'left' || key === 'right') {
                        if (luceParserResults[key].left || luceParserResults[key].right) {
                            processLuceneParserObject(luceParserResults[key]);
                        } else {
                            addQueryFromParser(luceParserResults[key]);
                        }
                    } else if (key === 'operator') {

                        if (!scope.mainSearchGuideVariables.queries) {
                            scope.mainSearchGuideVariables.queries = [];
                        };

                        var operator;

                        if (luceParserResults[key] === '<implicit>') {
                            luceParserResults[key] = 'OR'
                        }

                        for (var i = 0; i < scope.mainSearchGuideVariables.booleanOperatorOptions.length; i++) {
                            if (scope.mainSearchGuideVariables.booleanOperatorOptions[i].value === luceParserResults[key]) {
                                operator = scope.mainSearchGuideVariables.booleanOperatorOptions[i];
                                break;
                            }
                        };

                        scope.mainSearchGuideVariables.queries.push({
                            booleanOperator: operator
                        });

                        if (scope.mainSearchGuideVariables.queries[scope.mainSearchGuideVariables.queries.length - 2]) {
                            scope.mainSearchGuideVariables.queries[scope.mainSearchGuideVariables.queries.length - 2].selectedBool = operator;
                        }
                    }
                });
            }

            function addQueryFromParser(luceneParserQuery) {

                var query;
                var lastQuery = scope.mainSearchGuideVariables.queries.length && scope.mainSearchGuideVariables.queries[scope.mainSearchGuideVariables.queries.length - 1] || {};

                if (lastQuery.booleanOperator && !lastQuery.terms) {
                    query = lastQuery;
                } else {
                    query = {};
                    if (!scope.mainSearchGuideVariables.queries) {
                        scope.mainSearchGuideVariables.queries = [];
                    }
                    scope.mainSearchGuideVariables.queries.push(query);
                }

                query.field = getFieldOption(luceneParserQuery.field);

                query.type = query.field.type;

                var terms = createQueryTermsFromParser(query.type, luceneParserQuery);
                var endTermArray = [
                    {
                        type: 'end',
                        value: ''
                    }
                ]

                query.terms = terms.concat(endTermArray);

                query.selectedBool = booleanOperatorOptions[0]

                if (query.index == null) {
                    query.index = scope.mainSearchGuideVariables.queries.length - 1;
                }
            }

            function createQueryTermsFromParser(fieldType, luceneParserQueryTerm) {

                var terms = [];
                
                if (fieldType === 'text') {
                    var term = {
                        options: queryTermOptions[fieldType]
                    }

                    for (var i = 0; i < termPatterns[fieldType].length; i++) {
                        if (termPatterns[fieldType][i].pattern.test(luceneParserQueryTerm.term)) {
                            term.type = termPatterns[fieldType][i].termType;
                            term.value = termPatterns[fieldType][i].getValue(luceneParserQueryTerm.term);
                            term.queryOption = termPatterns[fieldType][i].getQueryOption();
                            break;
                        }
                    }

                    terms.push(term);
                } else {
                    if (luceneParserQueryTerm.term || (Number(luceneParserQueryTerm.term_max) - Number(luceneParserQueryTerm.term_min)) === 86399999) {
                        var term = {
                            options: queryTermOptions[fieldType],
                            type: 'exact',
                            value: luceneParserQueryTerm.term || moment(luceneParserQueryTerm.term_max, 'x')._d,
                            queryOption: _.filter(queryTermOptions[fieldType], { value: 'exact' })[0]
                        }
                        terms.push(term);
                    } else {

                        if (luceneParserQueryTerm.term_min !== '\\-10E50') {
                            var term = {
                                options: queryTermOptions[fieldType],
                                type: 'startsWith',
                                value: fieldType === 'timestamp' && moment(luceneParserQueryTerm.term_min, 'x')._d || luceneParserQueryTerm.term_min,
                                queryOption: _.filter(queryTermOptions[fieldType], { value: 'startsWith' })[0]
                            }
                            terms.push(term);
                        } 

                        if (luceneParserQueryTerm.term_max !== '10E50') {
                            var term = {
                                options: terms.length > 0 ? terms[0].queryOption.nextOptions : queryTermOptions[fieldType],
                                type: 'endsWith',
                                value: fieldType === 'timestamp' && moment(luceneParserQueryTerm.term_max, 'x')._d || luceneParserQueryTerm.term_max,
                                queryOption: terms.length > 0 ? terms[0].queryOption.nextOptions[1] : _.filter(queryTermOptions[fieldType], { value: 'endsWith' })[0]
                            }
                            terms.push(term);
                        } else {
                            var term = {
                                options: terms[0].queryOption.nextOptions,
                                type: terms[0].queryOption.nextOptions[0].value,
                                value: '',
                                queryOption: terms[0].queryOption.nextOptions[0]
                            }
                            terms.push(term);
                        }
                    }
                }

                return terms;
            }

            var termPatterns = {
                text: [
                    {
                        termType: 'exact',
                        pattern: /^\^.*\$$/,
                        getValue: function (termString) {
                            return termString.substring(
                                termString.lastIndexOf("^") + 1,
                                termString.lastIndexOf("$")
                            )
                        },
                        getQueryOption: function () {
                            return _.filter(queryTermOptions.text, { value: 'exact' })[0];
                        }
                    },
                    {
                        termType: 'startsWith',
                        pattern: /^\^.*/,
                        getValue: function (termString) {
                            return termString.substring(
                                termString.lastIndexOf("^") + 1
                            )
                        },
                        getQueryOption: function () {
                            return _.filter(queryTermOptions.text, { value: 'startsWith' })[0];
                        }
                    },
                    {
                        termType: 'endsWith',
                        pattern: /.*\$$/,
                        getValue: function (termString) {
                            return termString.substring(
                                0,
                                termString.lastIndexOf("$")
                            )
                        },
                        getQueryOption: function () {
                            return _.filter(queryTermOptions.text, { value: 'endsWith' })[0];
                        }
                    },
                    {
                        termType: 'containsPartial',
                        pattern: /^\*.*\*$/,
                        getValue: function (termString) {
                            return termString.substring(
                                termString.indexOf("*") + 1,
                                termString.lastIndexOf("*")
                            )
                        },
                        getQueryOption: function () {
                            return _.filter(queryTermOptions.text, { value: 'containsPartial' })[0];
                        }
                    },
                    {
                        termType: 'startsWithPartial',
                        pattern: /.*\*$/,
                        getValue: function (termString) {
                            return termString.substring(
                                0,
                                termString.lastIndexOf("*")
                            )
                        },
                        getQueryOption: function () {
                            return _.filter(queryTermOptions.text, { value: 'startsWithPartial' })[0];
                        }
                    },
                    {
                        termType: 'endsWithPartial',
                        pattern: /^\*.*/,
                        getValue: function (termString) {
                            return termString.substring(
                                termString.lastIndexOf("*") + 1
                            )
                        },
                        getQueryOption: function () {
                            return _.filter(queryTermOptions.text, { value: 'endsWithPartial' })[0];
                        }
                    },
                    {
                        termType: 'contains',
                        pattern: /./,
                        getValue: function (termString) {
                            return termString
                        },
                        getQueryOption: function () {
                            return _.filter(queryTermOptions.text, { value: 'contains' })[0];
                        }
                    }
                ]
            }

            function getFieldOption(fieldName) {
                var fieldOption;

                if (fieldName === '<implicit>') {
                    fieldOption = everyWhereOption;
                } else {
                    for (var i = 0; i < scope.mainSearchGuideVariables.fieldOptions.length; i++) {
                        if (scope.mainSearchGuideVariables.fieldOptions[i].field === fieldName) {
                            fieldOption = scope.mainSearchGuideVariables.fieldOptions[i];
                            break;
                        }
                    }
                }

                if (!fieldOption) {

                    var unknownField = manuallyEnteredOption(fieldName);

                    scope.mainSearchGuideVariables.fieldOptions.push(unknownField);

                    fieldOption = unknownField;
                }

                return fieldOption;
            }

            function getSearchFields() {
                var deferred = $q.defer();

                searchService.getSearchFields().then(function success(response) {

                    scope.mainSearchGuideVariables.fieldOptions = response.data;
                    scope.mainSearchGuideVariables.fieldOptions.unshift(everyWhereOption);
                    scope.mainSearchGuideVariables.fieldOptions.push(otherOption);

                    _.chain(scope.mainSearchGuideVariables.fieldOptions)
                        .map(function (fieldOption) {
                            fieldOption.display = fieldOption.title + (fieldOption.description ? ' - ' + fieldOption.description : '');
                        })
                        .value();
                    deferred.resolve();
                }, function error() {
                    deferred.reject();
                });

                return deferred.promise;
            }

            scope.processOtherField = function(fieldOption){

                if (fieldOption.field) {
                    fieldOption.display = 'Other - '  + fieldOption.field;
                } else {
                    fieldOption.display = 'Other';
                }
               
                var containsOther=false;
                
                for(var i=0;i<scope.mainSearchGuideVariables.fieldOptions.length;i++){
                    var op = scope.mainSearchGuideVariables.fieldOptions[i];
                    if(op.display === "Other"){
                        containsOther=true;
                    }
                }
                if(!containsOther){
                    var otherField = manuallyEnteredOption();
                    scope.mainSearchGuideVariables.fieldOptions.push(otherField);
                } 

                scope.processValueChange();
            }

            scope.selectFieldOption = function (query, fieldOption) {
                query.type = fieldOption.type;
                query.terms = [
                    {
                        type: 'exact',
                        value: '',
                        queryOption: queryTermOptions[fieldOption.type][0],
                        options: queryTermOptions[fieldOption.type]
                    },
                    {
                        type: 'end',
                        value: ''
                    }
                ];
               
                scope.selectQueryOption(query, query.terms[0], 0);
                scope.processValueChange();
            }

            scope.selectQueryOption = function (query, term, termIndex) {

                var queryOption = term.queryOption;
                //var term = query.terms[termIndex];

                term.type = queryOption.value;

                if (queryOption.nextOptions.length > 0) {
                    query.terms[termIndex + 1] = {
                        type: queryOption.nextOptions[0].value,
                        value: '',
                        options: queryOption.nextOptions,
                        queryOption: queryOption.nextOptions[0],
                    };

                    query.terms[termIndex + 2] = {
                        type: 'end',
                        value: ''
                    };
                    query.terms = _.take(query.terms, termIndex + 3)
                } else {
                    query.terms[termIndex + 1] = {
                        type: 'end',
                        value: ''
                    };
                    query.terms = _.take(query.terms, termIndex + 2);
                }
                scope.processValueChange();
            }

            scope.selectBooleanOperatorOption = function (query) {
                var queryIndex = query.index;
                var booleanOperatorType = query.selectedBool;
                if (booleanOperatorType.value === 'end') {
                    scope.mainSearchGuideVariables.queries = _.take(scope.mainSearchGuideVariables.queries, queryIndex + 1);
                } else {
                    var q = defaultQuery();
                    q.booleanOperator = booleanOperatorType;
                    q.index = queryIndex + 1;
                    scope.mainSearchGuideVariables.queries[queryIndex + 1] = q;
                    
                    
                }
                scope.processValueChange();
            }


            // scope.updateDateTime = function (term) {
            //     term.value = moment(term.dateTime).format('YYYY-MM-DD');
            //     scope.validateQuery();
            // }

            function validateDate(testdate) {
                // var date_regex = /^(19|20)\d{2}\-(0[1-9]|1[0-2])\-(0[1-9]|1\d|2\d|3[01])$/;
                // return date_regex.test(testdate);
                return moment(testdate).isValid();
            }

            scope.validateQuery = function () {
                var isSearchValid = true;
                for (var queryIndex = 0; queryIndex < scope.mainSearchGuideVariables.queries.length; queryIndex++) {
                    var query = scope.mainSearchGuideVariables.queries[queryIndex];

                    query.isValid = true;

                    if (query.field.field == null || query.field.field == '') {
                        query.isValid = false;
                        isSearchValid = false;
                    } else {
                        for (var termIndex = 0; termIndex < query.terms.length; termIndex++) {
                            var term = query.terms[termIndex];

                            term.isValid = true;

                            if (term.type === 'end') {
                                term.value = null;
                                break;
                            } else if (term.queryOption) {
                                try {
                                    term.queryOption.validate(term.value);
                                } catch (e) {
                                    term.isValid = false;
                                    query.isValid = false;
                                    isSearchValid = false;
                                    term.errorMessage = e;
                                    break;
                                }
                            }

                        }
                    }

                    if (query.isValid) {
                        if (query.type === 'float' || query.type === 'timestamp') {
                            if (query.terms.length > 2 && query.terms[1].type !== 'end') {
                                var start = query.terms[0].value - 0;
                                var end = query.terms[1].value - 0;
                                if (end < start) {
                                    query.terms[1].isValid = false;
                                    query.isValid = false;
                                    isSearchValid = false;
                                    query.terms[1].errorMessage = "start value can not be greater than end value";
                                    break;
                                }
                            }
                        }
                    }
                }

                scope.mainSearchGuideVariables.isSearchValid = isSearchValid;
                
                return isSearchValid;
            }

            scope.dateOptions = {
                showWeeks: false
            };

            scope.openSearchDatePicker = function (term) {
                term.datePickerOpened = true;
            }

            scope.createQuery = function () {

                var constructedQuery = '';

                for (var queryIndex = 0; queryIndex < scope.mainSearchGuideVariables.queries.length; queryIndex++) {
                    var query = scope.mainSearchGuideVariables.queries[queryIndex];

                    if (queryIndex > 0) {
                        constructedQuery += (' ' + query.booleanOperator.value + ' ');
                    }

                    var fieldPrefix = "";

                    if (query.field.field !== 'everywhere') {
                        fieldPrefix = (query.field.field + ':');
                    }


                    for (var termIndex = 0; termIndex < query.terms.length; termIndex++) {
                        var term = query.terms[termIndex];
                        if (term.queryOption) {
                            constructedQuery += term.queryOption.constructQuery(fieldPrefix, term.value);
                        }
                    }
                }
                return constructedQuery;

            }

            scope.runQuery = function () {
                var isValid = scope.validateQuery();
                if (isValid) {
                    scope.q = scope.createQuery();
                    scope.submitQuery({isFromQueryBuilder: true});
                }
            }

            scope.processValueChange = function () {
                var isValid = scope.validateQuery();
                if (isValid) {
                    scope.q = scope.createQuery();
                }
            }
        }
    };
})()