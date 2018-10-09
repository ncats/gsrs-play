(function() {
    'use strict';
    var ginasApp = angular.module('ginas')
        .directive('topDock', topDock)

    function topDock() {

        return {
            restrict: 'A',
            scope: {
                dockedClass: '@'
            },
            link: link
        }

        function link(scope, element, attrs) {

            var body = document.getElementsByTagName("BODY")[0];
            var bodyPaddingTop = window.getComputedStyle(body, null).getPropertyValue("padding-top");
            var bodyPaddingTopNumber = Number(bodyPaddingTop.replace('px', ''));
            var elementToDock = element[0];
            var isDocked = false;
            var elementToDockTopDistance = elementToDock.offsetTop - elementToDock.scrollTop + elementToDock.clientTop;

            function loadDirective() {
                //preserve any old scroll listeners
                var oldScroll = window.onscroll;

                if (!oldScroll) {
                    oldScroll = function() {};
                }
                window.onscroll = function(e) {
                    checkVerticalPosition();
                    oldScroll(e);
                }
            }

            function checkVerticalPosition() {
                if (!isDocked && elementToDock.getBoundingClientRect().top < 1) {
                    dockElement();
                } else if (isDocked && (body.getBoundingClientRect().top * -1) < (elementToDockTopDistance + bodyPaddingTopNumber)) {
                    undockElement();
                }
            }

            function dockElement() {
                elementToDock.classList.add(scope.dockedClass);
                body.style.paddingTop = window.getComputedStyle(elementToDock, null).getPropertyValue("height");
                isDocked = true;
            }

            function undockElement() {
                elementToDock.classList.remove(scope.dockedClass);
                body.style.paddingTop = bodyPaddingTop;
                isDocked = false;
            }

            loadDirective();
        }
    }
})();