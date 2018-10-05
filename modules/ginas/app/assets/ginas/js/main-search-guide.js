(function () {
    'use strict';

    angular
        .module('ginas')
        .directive('mainSearchGuide', mainSearchGuide);

    function mainSearchGuide(searchService) {
        var directive = {
            link: link,
            scope: {
                q: '='
            },
            restrict: 'E',
            templateUrl: baseurl + 'assets/templates/elements/main-search-guide.html',
        };
        return directive;

        function link(scope, element, attrs) {

            var everyWhereOption = {
                title: 'every field of the record',
                field: 'everywhere',
                type: 'text',
                description: ''
            };

            var otherField = {
                title: 'another field ...',
                field: 'other',
                isOther: true,
                type: 'text',
                description: ''
            };

            var Term = function(r){
                var nt={};

                nt.nextOptions=[];

                /**
                 * Validate should throw an exception if there's
                 * a problem.
                 *
                 */
                nt.validate=function(v){
                    if(typeof v === "undefined" || v === null || v.trim() === ""){
                        throw "Required";
                    }
                };

                nt.constructQuery=function(field, value){
                    throw "Term type has no implementation for constructing a query!";
                };

                nt.setValidator=function(v){
                    if(v){
                        nt.validate=v;
                    }
                    return nt;
                };

                nt.addValidator=function(val){
                    var oldV=nt.validate;
                    return nt.setValidator(function(v){
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
                nt.setConstructQuery=function(cq){
                    if(cq){
                        nt.constructQuery=function(f,v){
                            if(f && !_.endsWith(f, ':')){
                                f=f+":";
                            }
                            return cq(f,v);
                        };
                    }
                    return nt;
                };

                /**
                 * This is a helper function to set a construct query
                 * operation where the terms are parsed first as tokens
                 */
                nt.setConstructQueryMultipleTerms=function(cq){
                    if(cq){
                        return nt.setConstructQuery(function(f,v){
                            return cq(f,nt.parseWords(v));
                        });
                    }
                    return nt;
                };

                /**
                 * Utility method to parse words out of term
                 */
                nt.parseWords=function(v){
                    var list = v.trim().split(/\s+/);
                    return list;
                }

                nt.setValue=function(v){
                    nt.value=v;
                    return nt;
                }
                nt.setDisplay=function(d){
                    nt.display=d;
                    return nt;
                }
                nt.setDisplay=function(d){
                    nt.display=d;
                    return nt;
                }
                nt.addNextOption=function(no){
                    nt.nextOptions.push(Term(no));
                    return nt;
                };
                nt.setNextOptions=function(nos){
                    nt.nextOptions=[];
                    if(nos){
                        for(var i=0;i<nos.length;i++){
                            nt.addNextOption(nos[i]);
                        }
                    }
                    return nt;
                };

                nt.from=function(raw){
                    return nt.setValue(raw.value)
                        .setDisplay(raw.display)
                        .setNextOptions(raw.nextOptions)
                        .setValidator(raw.validate)
                        .setConstructQuery(raw.constructQuery)
                        .setConstructQueryMultipleTerms(raw.constructQueryMultipleTerms);
                };

                if(r){
                    return nt.from(r);
                }
                return nt;
            };

            var SingleWordTerm = function(r){
                return Term(r).setValidator(function(v){
                    var list = v.trim().split(/\s+/);
                    if(list.length>1)throw "Does not support multiple words";
                });
            }
            var TimeStampTerm = function(r){
                var t= Term().addValidator(function(v){
                    if(!moment(v).isValid()){
                        throw "Not a valid date";
                    }
                }).from(r);
                return t;
            }

            var FloatRangeTerm = function(r){
                var t= Term().addValidator(function(v){
                    if(isNaN(v)){
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
                        constructQuery: function(f,v){
                            return f + ('"^' + v + '$"');
                        }
                    }),
                    Term({
                        value: 'contains',
                        display: 'for the following contained phrase, which must be found as written (no partial words)',
                        constructQuery: function(f,v){
                            return f + ('"' + v + '"');
                        }
                    }),
                    Term({
                        value: 'any',
                        display: 'for ANY of the following words in any order or position',
                        constructQueryMultipleTerms: function(f,v){
                            var sq=_.chain(v)
                                .map(function(t){
                                    return f+t;
                                })
                                .value()
                                .join(" OR ");
                            if(v.length>1){
                                return "(" + sq + ")";
                            }else{
                                return sq;
                            }
                        }
                    }),
                    Term({
                        value: 'all',
                        display: 'for ALL of the following words in any order or position',
                        constructQueryMultipleTerms: function(f,v){
                            var sq=_.chain(v)
                                .map(function(t){
                                    return f+t;
                                })
                                .value()
                                .join(" AND ");
                            if(v.length>1){
                                return "(" + sq + ")";
                            }else{
                                return sq;
                            }
                        }
                    }),
                    SingleWordTerm({
                        value: 'startsWithPartial',
                        display: 'for a WORD that starts with',
                        constructQuery: function(f,v){
                            return f + ('' + v + '*');
                        }
                    }),
                    SingleWordTerm({
                        value: 'endsWithPartial',
                        display: 'for a WORD that ends with',
                        constructQuery: function(f,v){
                            return f + ('*' + v + '');
                        }
                    }),
                    SingleWordTerm({
                        value: 'containsPartial',
                        display: 'for a WORD that contains',
                        constructQuery: function(f,v){
                            return f + ('*' + v + '*');
                        }
                    }),
                    Term({
                        value: 'startsWith',
                        display: 'for a value that starts with with the word(s)',
                        constructQuery: function(f,v){
                            return f + ('"^' + v + '"');
                        }
                    }),
                    Term({
                        value: 'endsWith',
                        display: 'for a value that ends with the word(s)',
                        constructQuery: function(f,v){
                            return f + ('"' + v + '$"');
                        }
                    })
                ],
                timestamp: [

                    TimeStampTerm({
                        value: 'startsWith',
                        display: 'for a date range that starts with',
                        constructQuery: function(f,v){
                            var timestampStart = moment(v).set({hour: 0, minute: 0, second: 0, millisecond: 0}).utc().valueOf();
                            return f + ('[' + timestampStart + ' TO ');
                        },
                        nextOptions: [
                            TimeStampTerm({
                                value: 'end',
                                display: 'and doesn\'t end',
                                constructQuery: function(f,v){
                                    return '10E50]';
                                }
                            }),
                            TimeStampTerm({
                                value: 'endsWith',
                                display: 'and ends with',
                                constructQuery: function(f,v){
                                    var timestampEnd = moment(v).set({hour: 23, minute: 59, second: 59, millisecond: 999}).utc().valueOf();
                                    return timestampEnd + "]";
                                }
                            })
                        ]
                    }),
                    TimeStampTerm({
                        value: 'exact',
                        display: 'specifically for this date',
                        constructQuery: function(f,v){
                            var timestampStart = moment(v).set({hour: 0, minute: 0, second: 0, millisecond: 0}).utc().valueOf();
                            var timestampEnd = moment(v).set({hour: 23, minute: 59, second: 59, millisecond: 999}).utc().valueOf();
                            return f + ('[' + timestampStart + ' TO ' + timestampEnd + ']');
                        }
                    }),
                    TimeStampTerm({
                        value: 'endsWith',
                        display: 'for a date before',
                        constructQuery: function(f,v){
                            var timestampEnd = moment(v).set({hour: 23, minute: 59, second: 59, millisecond: 999}).utc().valueOf();
                            return f + ('[-10E50 TO ' + timestampEnd + ']');
                        }
                    })
                ],
                float: [
                    FloatRangeTerm({
                        value: 'startsWith',
                        display: 'for a number range that starts with',
                        constructQuery: function(f,v){
                            return f + ('[' + v + ' TO ');
                        },
                        nextOptions: [
                            FloatRangeTerm({
                                value: 'end',
                                display: 'and doesn\'t end',
                                constructQuery: function(f,v){
                                    return '10E50]';
                                }
                            }),
                            FloatRangeTerm({
                                value: 'endsWith',
                                display: 'and ends with',
                                constructQuery: function(f,v){
                                    return v + ']';
                                }
                            })
                        ]
                    }),
                    FloatRangeTerm({
                        value: 'endsWith',
                        display: 'for a number lower than',
                        constructQuery: function(f,v){
                            return f + '[-10E50 TO ' + v +']';
                        }
                    }),
                    FloatRangeTerm({
                        value: 'exact',
                        display: 'specifically for this number',
                        constructQuery: function(f,v){
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

            var defaultQuery=function(){
                return {
                    field: everyWhereOption,
                    terms: [
                        {
                            type: 'exact',
                            value: '',
                            queryOption:_.filter(queryTermOptions.text,{value:'exact'})[0],
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
                queries: [
                    defaultQuery()
                ],
                booleanOperatorOptions: booleanOperatorOptions,
                isSearchValid: false
            }

            function loadDirective() {
                getSearchFields();
            }

            function getSearchFields() {
                searchService.getSearchFields().then(function success(response) {


                    scope.mainSearchGuideVariables.fieldOptions = response.data;
                    scope.mainSearchGuideVariables.fieldOptions.unshift(everyWhereOption);
                    scope.mainSearchGuideVariables.fieldOptions.push(otherField);

                    _.chain(scope.mainSearchGuideVariables.fieldOptions)
                        .map(function(fieldOption){
                            fieldOption.display = fieldOption.title + (fieldOption.description ? ' - ' + fieldOption.description : '');
                        })
                        .value();
                })
            }

            scope.selectFieldOption = function (query, fieldOption) {
                query.type = fieldOption.type;
                query.terms = [
                    {
                        type: 'exact',
                        value: '',
                        queryOption:queryTermOptions[fieldOption.type][0],
                        options: queryTermOptions[fieldOption.type]
                    },
                    {
                        type: 'end',
                        value: ''
                    }
                ];
                scope.selectQueryOption(query, query.terms[0],0);
                scope.validateQuery();
            }

            scope.selectQueryOption = function (query, term, termIndex) {

                var queryOption = term.queryOption;
                //var term = query.terms[termIndex];

                term.type = queryOption.value;

                if (queryOption.nextOptions.length>0) {
                    query.terms[termIndex + 1] = {
                        type: queryOption.nextOptions[0].value,
                        value: '',
                        options: queryOption.nextOptions,
                        queryOption:queryOption.nextOptions[0],
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
                scope.validateQuery();
            }

            scope.selectBooleanOperatorOption = function (query) {
                var queryIndex = query.index;
                var booleanOperatorType = query.selectedBool;
                if (booleanOperatorType.value === 'end') {
                    scope.mainSearchGuideVariables.queries = _.take(scope.mainSearchGuideVariables.queries, queryIndex + 1);
                } else {
                    var q=defaultQuery();
                    q.booleanOperator=booleanOperatorType;
                    q.index=queryIndex+1;
                    scope.mainSearchGuideVariables.queries[queryIndex + 1] = q;
                }
                scope.validateQuery();
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

                    for (var termIndex = 0; termIndex < query.terms.length; termIndex++) {
                        var term = query.terms[termIndex];

                        term.isValid = true;

                        if (term.type === 'end') {
                            term.value = null;
                            break;
                        } else if(term.queryOption){
                            try{
                                term.queryOption.validate(term.value);
                            }catch(e){
                                term.isValid = false;
                                query.isValid = false;
                                isSearchValid = false;
                                term.errorMessage = e;
                                break;
                            }
                        }

                    }

                    if(query.isValid){
                        if(query.type==='float' || query.type==='timestamp'){
                            if(query.terms.length>2 && query.terms[1].type !== 'end'){
                                var start=query.terms[0].value-0;
                                var end=query.terms[1].value-0;
                                if(end<start){
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
                if(isSearchValid){
                    scope.createQuery();
                }
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

                    if (queryIndex > 0){
                        constructedQuery += (' ' + query.booleanOperator.value + ' ');
                    }

                    var fieldPrefix="";

                    if (query.field.field !== 'everywhere') {
                        fieldPrefix=(query.field.field + ':');
                    }


                    for (var termIndex = 0; termIndex < query.terms.length; termIndex++) {
                        var term = query.terms[termIndex];
                        if(term.queryOption){
                            constructedQuery +=term.queryOption.constructQuery(fieldPrefix, term.value);
                        }
                    }
                }
                scope.q = constructedQuery;
            }
            scope.runQuery = function () {
                if(scope.validateQuery()){
                    //TODO:Fix this, it's a hack
                    setTimeout(function(){
                        $("#searchtop #split-button").click();
                    },0);
                }
            }

            loadDirective();
        }
    };
})()