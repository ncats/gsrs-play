/* @preserve
 *
 * angular-bootstrap-file
 * https://github.com/itslenny/angular-bootstrap-file-field
 *
 * Version: 0.1.3 - 02/21/2015
 * License: MIT
 */

angular.module('bootstrap.fileField',[])
    .directive('fileField', function() {
        return {
            require:'ngModel',
            restrict: 'E',
            link: function (scope, element, attrs, ngModel) {
                //set default bootstrap class
                if(!attrs.class && !attrs.ngClass){
                    element.addClass('btn');
                }

                var fileField = element.find('input');

                fileField.bind('change', function(event){
                    scope.$evalAsync(function () {
                        ngModel.$setViewValue(event.target.files[0]);
                        if(attrs.preview){
                            var reader = new FileReader();
                            reader.onload = function (e) {
                                scope.$evalAsync(function(){
                                    scope[attrs.preview]=e.target.result;
                                });
                            };
                            reader.readAsDataURL(event.target.files[0]);
                        }
                    });
                });
                fileField.bind('click',function(e){
                    e.stopPropagation();
                });
                element.bind('click',function(e){
                    e.preventDefault();
                    fileField[0].click()
                });
            },
            template:'<button type="button" class = "btn btn-primary"><ng-transclude></ng-transclude><input type="file" style="display:none" name="file-name" id="file-name" aria-label="upload button input"></button>',
            replace:true,
            transclude:true
        };
    });