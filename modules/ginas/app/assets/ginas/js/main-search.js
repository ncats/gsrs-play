(function () {
    'use strict';

    angular
        .module('ginas')
        .directive('mainSearch', mainSearch);

    function mainSearch ($timeout) {
        var directive = {
            link: link,
            restrict: 'EA'
        };

        function link(scope, element, attrs) {

            scope.mainSearchGuideControl = {};

            scope.mainSearchVariables = {
                isShowHelp: false,
                isShowGuide: false,
                isFocus: false
            }

            var containerElement = element[0];
            var containerElementCurrentClassList;
            var searchInputElement = containerElement.querySelector('#search');
            var searchInputElementLeft = searchInputElement.getBoundingClientRect().left;
            var searchInputElementRight = window.screen.width - searchInputElement.getBoundingClientRect().right;
            var searchInputElementBottom = window.screen.height - (searchInputElement.getBoundingClientRect().top + Number(window.getComputedStyle(searchInputElement,null).getPropertyValue("height").replace('px', '')));

            var expandKeyframes = 'expand {' +
                'from {left: '+ searchInputElementLeft +'px; right: '+ searchInputElementRight +'px; bottom: '+ searchInputElementBottom +'px;}' +
                'to {left: 0; right: 0; bottom: 0}' +
            '}';
            var expandKeyFramesStyleElement = document.createElement('style');
            expandKeyFramesStyleElement.type = 'text/css';
            expandKeyFramesStyleElement.innerHTML = ('@-webkit-keyframes ' + expandKeyframes + '\n@keyframes ' + expandKeyframes);
            document.getElementsByTagName('head')[0].appendChild(expandKeyFramesStyleElement);

            var retractKeyframes = 'retract {' +
                'from {left: 0; right: 0; bottom: 0;}' +
                'to {left: '+ searchInputElementLeft +'px; right: '+ searchInputElementRight +'px; bottom: '+ searchInputElementBottom +'px;}' +
            '}';
            var retractKeyFramesStyleElement = document.createElement('style');
            retractKeyFramesStyleElement.type = 'text/css';
            retractKeyFramesStyleElement.innerHTML = ('@-webkit-keyframes ' + retractKeyframes + '\n@keyframes ' + retractKeyframes);
            document.getElementsByTagName('head')[0].appendChild(retractKeyFramesStyleElement);

            var closeSearchElement = containerElement.querySelector('#close-search');
            
            function loadDirective() {
                // searchInputElement.onfocus = focusSearch;
                closeSearchElement.addEventListener('click', closeSearch);
            }
            
            scope.closeLarge=closeSearch;

            scope.openQueryBuilder = function () {

                scope.mainSearchGuideControl.loadDirective();

                document.getElementsByTagName('body')[0].style.overflow = 'hidden';

            	scope.mainSearchVariables.isFocus=true;

                containerElementCurrentClassList = containerElement.className.split(' ');

                for(var i = 0; i < containerElementCurrentClassList.length; i++) {
                    var className = containerElementCurrentClassList[i];
                    console.log(className);
                    containerElement.classList.remove(className);
                }

                containerElement.classList.add('active-search');

                searchInputElement.focus();
            }

            scope.toggleHelp = function () {
                scope.mainSearchVariables.isShowGuide = false;
                scope.mainSearchVariables.isShowHelp = !scope.mainSearchVariables.isShowHelp;
            }

            scope.toogleGuide = function () {
                scope.mainSearchVariables.isShowHelp = false;
                scope.mainSearchVariables.isShowGuide = !scope.mainSearchVariables.isShowGuide;
            }

            function closeSearch () {
                scope.mainSearchVariables.isShowGuide = false;
            	if(scope.mainSearchVariables.isFocus){
	                containerElement.classList.add('deactivate-search');
	                containerElement.classList.remove('active-search');
	                $timeout(function(){
                        containerElement.classList.remove('deactivate-search');
                        if (containerElementCurrentClassList && containerElementCurrentClassList.length) {
                            containerElement.classList.add(containerElementCurrentClassList);
                        }
	                    document.getElementsByTagName('body')[0].style.overflow = null;
	                },350);
	                scope.mainSearchVariables.isFocus=false;
            	}
            }

            loadDirective();
        }

        return directive;
    }
})();