(window.webpackJsonp=window.webpackJsonp||[]).push([[52],{Ur2V:function(l,n,u){"use strict";u.r(n);var e=u("CcnG"),t=function(){return function(){}}(),a=u("NcP4"),r=u("m46K"),b=u("BHnd"),o=u("y4qS"),i=u("OkvK"),c=u("Ip0R"),s=u("Mr+X"),d=u("SMsm"),m=u("pIm3"),f=u("dJrM"),h=u("seP3"),p=u("Wf4p"),g=u("Fzqc"),_=u("dWZg"),F=u("wFw1"),D=u("gIcY"),C=u("b716"),v=u("/VYK"),M=u("b1+6"),L=u("4epT"),S=u("mrSG"),P=u("dwHa"),k=u("K9Ia"),w=function(l){function n(n,u){var e=l.call(this,n)||this;return e.gaService=n,e.utilsService=u,e.references=[],e.displayedColumns=["citation","type","tags","files","dateAcessed"],e.substanceUpdated=new k.a,e.pageSize=10,e}return S.a(n,l),n.prototype.ngOnInit=function(){var l=this;this.substanceUpdated.subscribe((function(n){l.substance=n,null!=l.substance&&null!=l.substance.references&&(l.references=l.substance.references,l.filtered=l.substance.references,l.pageChange(),l.searchControl.valueChanges.subscribe((function(n){l.filterList(n,l.references,l.analyticsEventCategory)}),(function(l){console.log(l)}))),l.countUpdate.emit(l.references.length)}))},n.prototype.sortData=function(l){var n=this,u=this.references.slice();if(!l.active||""===l.direction)return this.filtered=u,void this.pageChange();this.filtered=u.sort((function(u,e){return n.utilsService.compare(u[l.active].toUpperCase,e[l.active].toUpperCase,"asc"===l.direction)})),this.pageChange()},n}(P.a),E=u("HECD"),O=u("6E2U"),A=e.rb({encapsulation:0,styles:[["table.mat-table[_ngcontent-%COMP%]{width:100%}td.mat-cell[_ngcontent-%COMP%]:not(:last-child), td.mat-footer-cell[_ngcontent-%COMP%]:not(:last-child), th.mat-header-cell[_ngcontent-%COMP%]:not(:last-child){padding-right:10px}td.mat-cell[_ngcontent-%COMP%], td.mat-footer-cell[_ngcontent-%COMP%], th.mat-header-cell[_ngcontent-%COMP%]{padding-top:10px;padding-bottom:10px}.no-bottom-border[_ngcontent-%COMP%]   tr[_ngcontent-%COMP%]:last-child   td.mat-cell[_ngcontent-%COMP%]{border-bottom:none}.search[_ngcontent-%COMP%]{width:400px;max-width:100%}"]],data:{}});function j(l){return e.Pb(0,[(l()(),e.tb(0,0,null,null,3,"th",[["class","mat-header-cell"],["mat-header-cell",""],["mat-sort-header",""],["role","columnheader"]],[[1,"aria-sort",0],[2,"mat-sort-header-disabled",null]],[[null,"click"],[null,"mouseenter"],[null,"mouseleave"]],(function(l,n,u){var t=!0;return"click"===n&&(t=!1!==e.Fb(l,2)._handleClick()&&t),"mouseenter"===n&&(t=!1!==e.Fb(l,2)._setIndicatorHintVisible(!0)&&t),"mouseleave"===n&&(t=!1!==e.Fb(l,2)._setIndicatorHintVisible(!1)&&t),t}),r.b,r.a)),e.sb(1,16384,null,0,b.e,[o.d,e.k],null,null),e.sb(2,245760,null,0,i.c,[i.d,e.h,[2,i.b],[2,"MAT_SORT_HEADER_COLUMN_DEF"]],{id:[0,"id"]},null),(l()(),e.Nb(-1,0,[" Citation "]))],(function(l,n){l(n,2,0,"")}),(function(l,n){l(n,0,0,e.Fb(n,2)._getAriaSortAttribute(),e.Fb(n,2)._isDisabled())}))}function K(l){return e.Pb(0,[(l()(),e.tb(0,0,null,null,1,"a",[["target","_blank"]],[[8,"href",4]],null,null,null,null)),(l()(),e.Nb(1,null,[""," "]))],null,(function(l,n){l(n,0,0,n.parent.context.$implicit.url),l(n,1,0,n.parent.context.$implicit.citation)}))}function y(l){return e.Pb(0,[(l()(),e.tb(0,0,null,null,1,"span",[],null,null,null,null,null)),(l()(),e.Nb(1,null,[" "," "]))],null,(function(l,n){l(n,1,0,n.parent.context.$implicit.citation)}))}function I(l){return e.Pb(0,[(l()(),e.tb(0,0,null,null,5,"td",[["class","mat-cell"],["mat-cell",""],["role","gridcell"]],null,null,null,null,null)),e.sb(1,16384,null,0,b.a,[o.d,e.k],null,null),(l()(),e.jb(16777216,null,null,1,null,K)),e.sb(3,16384,null,0,c.n,[e.P,e.M],{ngIf:[0,"ngIf"]},null),(l()(),e.jb(16777216,null,null,1,null,y)),e.sb(5,16384,null,0,c.n,[e.P,e.M],{ngIf:[0,"ngIf"]},null)],(function(l,n){l(n,3,0,n.context.$implicit.url),l(n,5,0,!n.context.$implicit.url)}),null)}function x(l){return e.Pb(0,[(l()(),e.tb(0,0,null,null,3,"th",[["class","mat-header-cell"],["mat-header-cell",""],["mat-sort-header",""],["role","columnheader"]],[[1,"aria-sort",0],[2,"mat-sort-header-disabled",null]],[[null,"click"],[null,"mouseenter"],[null,"mouseleave"]],(function(l,n,u){var t=!0;return"click"===n&&(t=!1!==e.Fb(l,2)._handleClick()&&t),"mouseenter"===n&&(t=!1!==e.Fb(l,2)._setIndicatorHintVisible(!0)&&t),"mouseleave"===n&&(t=!1!==e.Fb(l,2)._setIndicatorHintVisible(!1)&&t),t}),r.b,r.a)),e.sb(1,16384,null,0,b.e,[o.d,e.k],null,null),e.sb(2,245760,null,0,i.c,[i.d,e.h,[2,i.b],[2,"MAT_SORT_HEADER_COLUMN_DEF"]],{id:[0,"id"]},null),(l()(),e.Nb(-1,0,[" Type "]))],(function(l,n){l(n,2,0,"")}),(function(l,n){l(n,0,0,e.Fb(n,2)._getAriaSortAttribute(),e.Fb(n,2)._isDisabled())}))}function H(l){return e.Pb(0,[(l()(),e.tb(0,0,null,null,2,"td",[["class","mat-cell"],["mat-cell",""],["role","gridcell"]],null,null,null,null,null)),e.sb(1,16384,null,0,b.a,[o.d,e.k],null,null),(l()(),e.Nb(2,null,[" "," "]))],null,(function(l,n){l(n,2,0,n.context.$implicit.docType)}))}function N(l){return e.Pb(0,[(l()(),e.tb(0,0,null,null,3,"th",[["class","mat-header-cell"],["mat-header-cell",""],["mat-sort-header",""],["role","columnheader"]],[[1,"aria-sort",0],[2,"mat-sort-header-disabled",null]],[[null,"click"],[null,"mouseenter"],[null,"mouseleave"]],(function(l,n,u){var t=!0;return"click"===n&&(t=!1!==e.Fb(l,2)._handleClick()&&t),"mouseenter"===n&&(t=!1!==e.Fb(l,2)._setIndicatorHintVisible(!0)&&t),"mouseleave"===n&&(t=!1!==e.Fb(l,2)._setIndicatorHintVisible(!1)&&t),t}),r.b,r.a)),e.sb(1,16384,null,0,b.e,[o.d,e.k],null,null),e.sb(2,245760,null,0,i.c,[i.d,e.h,[2,i.b],[2,"MAT_SORT_HEADER_COLUMN_DEF"]],{id:[0,"id"]},null),(l()(),e.Nb(-1,0,[" Tags "]))],(function(l,n){l(n,2,0,"")}),(function(l,n){l(n,0,0,e.Fb(n,2)._getAriaSortAttribute(),e.Fb(n,2)._isDisabled())}))}function R(l){return e.Pb(0,[(l()(),e.tb(0,0,null,null,1,"span",[],null,null,null,null,null)),(l()(),e.Nb(-1,null,[", "]))],null,null)}function T(l){return e.Pb(0,[(l()(),e.tb(0,0,null,null,3,"span",[],null,null,null,null,null)),(l()(),e.Nb(1,null,["",""])),(l()(),e.jb(16777216,null,null,1,null,R)),e.sb(3,16384,null,0,c.n,[e.P,e.M],{ngIf:[0,"ngIf"]},null)],(function(l,n){l(n,3,0,!n.context.last)}),(function(l,n){l(n,1,0,n.context.$implicit)}))}function U(l){return e.Pb(0,[(l()(),e.tb(0,0,null,null,3,"td",[["class","mat-cell"],["mat-cell",""],["role","gridcell"]],null,null,null,null,null)),e.sb(1,16384,null,0,b.a,[o.d,e.k],null,null),(l()(),e.jb(16777216,null,null,1,null,T)),e.sb(3,278528,null,0,c.m,[e.P,e.M,e.s],{ngForOf:[0,"ngForOf"]},null)],(function(l,n){l(n,3,0,n.context.$implicit.tags)}),null)}function V(l){return e.Pb(0,[(l()(),e.tb(0,0,null,null,3,"th",[["class","mat-header-cell"],["mat-header-cell",""],["mat-sort-header",""],["role","columnheader"]],[[1,"aria-sort",0],[2,"mat-sort-header-disabled",null]],[[null,"click"],[null,"mouseenter"],[null,"mouseleave"]],(function(l,n,u){var t=!0;return"click"===n&&(t=!1!==e.Fb(l,2)._handleClick()&&t),"mouseenter"===n&&(t=!1!==e.Fb(l,2)._setIndicatorHintVisible(!0)&&t),"mouseleave"===n&&(t=!1!==e.Fb(l,2)._setIndicatorHintVisible(!1)&&t),t}),r.b,r.a)),e.sb(1,16384,null,0,b.e,[o.d,e.k],null,null),e.sb(2,245760,null,0,i.c,[i.d,e.h,[2,i.b],[2,"MAT_SORT_HEADER_COLUMN_DEF"]],{id:[0,"id"]},null),(l()(),e.Nb(-1,0,[" File "]))],(function(l,n){l(n,2,0,"")}),(function(l,n){l(n,0,0,e.Fb(n,2)._getAriaSortAttribute(),e.Fb(n,2)._isDisabled())}))}function $(l){return e.Pb(0,[(l()(),e.tb(0,0,null,null,2,"a",[],[[8,"href",4]],null,null,null,null)),(l()(),e.tb(1,0,null,null,1,"mat-icon",[["class","blue mat-icon notranslate"],["color","primary"],["role","img"],["svgIcon","cloud_download"]],[[2,"mat-icon-inline",null],[2,"mat-icon-no-color",null]],null,null,s.b,s.a)),e.sb(2,9158656,null,0,d.b,[e.k,d.d,[8,null],[2,d.a],[2,e.m]],{color:[0,"color"],svgIcon:[1,"svgIcon"]},null)],(function(l,n){l(n,2,0,"primary","cloud_download")}),(function(l,n){l(n,0,0,n.parent.context.$implicit.uploadedFile),l(n,1,0,e.Fb(n,2).inline,"primary"!==e.Fb(n,2).color&&"accent"!==e.Fb(n,2).color&&"warn"!==e.Fb(n,2).color)}))}function q(l){return e.Pb(0,[(l()(),e.tb(0,0,null,null,3,"td",[["class","mat-cell"],["mat-cell",""],["role","gridcell"]],null,null,null,null,null)),e.sb(1,16384,null,0,b.a,[o.d,e.k],null,null),(l()(),e.jb(16777216,null,null,1,null,$)),e.sb(3,16384,null,0,c.n,[e.P,e.M],{ngIf:[0,"ngIf"]},null)],(function(l,n){l(n,3,0,n.context.$implicit.uploadedFile)}),null)}function z(l){return e.Pb(0,[(l()(),e.tb(0,0,null,null,3,"th",[["class","mat-header-cell"],["mat-header-cell",""],["mat-sort-header",""],["role","columnheader"]],[[1,"aria-sort",0],[2,"mat-sort-header-disabled",null]],[[null,"click"],[null,"mouseenter"],[null,"mouseleave"]],(function(l,n,u){var t=!0;return"click"===n&&(t=!1!==e.Fb(l,2)._handleClick()&&t),"mouseenter"===n&&(t=!1!==e.Fb(l,2)._setIndicatorHintVisible(!0)&&t),"mouseleave"===n&&(t=!1!==e.Fb(l,2)._setIndicatorHintVisible(!1)&&t),t}),r.b,r.a)),e.sb(1,16384,null,0,b.e,[o.d,e.k],null,null),e.sb(2,245760,null,0,i.c,[i.d,e.h,[2,i.b],[2,"MAT_SORT_HEADER_COLUMN_DEF"]],{id:[0,"id"]},null),(l()(),e.Nb(-1,0,[" Date Accessed "]))],(function(l,n){l(n,2,0,"")}),(function(l,n){l(n,0,0,e.Fb(n,2)._getAriaSortAttribute(),e.Fb(n,2)._isDisabled())}))}function G(l){return e.Pb(0,[(l()(),e.tb(0,0,null,null,3,"td",[["class","mat-cell"],["mat-cell",""],["role","gridcell"]],null,null,null,null,null)),e.sb(1,16384,null,0,b.a,[o.d,e.k],null,null),(l()(),e.Nb(2,null,[" "," "])),e.Jb(3,2)],null,(function(l,n){var u=e.Ob(n,2,0,l(n,3,0,e.Fb(n.parent,0),n.context.$implicit.accessed,"short"));l(n,2,0,u)}))}function B(l){return e.Pb(0,[(l()(),e.tb(0,0,null,null,2,"tr",[["class","mat-header-row"],["mat-header-row",""],["role","row"]],null,null,null,m.d,m.a)),e.Kb(6144,null,o.k,null,[b.g]),e.sb(2,49152,null,0,b.g,[],null,null)],null,null)}function J(l){return e.Pb(0,[(l()(),e.tb(0,0,null,null,2,"tr",[["class","mat-row"],["mat-row",""],["role","row"]],null,null,null,m.e,m.b)),e.Kb(6144,null,o.m,null,[b.i]),e.sb(2,49152,null,0,b.i,[],null,null)],null,null)}function Y(l){return e.Pb(0,[e.Hb(0,c.e,[e.u]),(l()(),e.tb(1,0,null,null,20,"div",[["class","flex-row"]],null,null,null,null,null)),(l()(),e.tb(2,0,null,null,0,"span",[["class","middle-fill"]],null,null,null,null,null)),(l()(),e.tb(3,0,null,null,18,"mat-form-field",[["class","search mat-form-field"]],[[2,"mat-form-field-appearance-standard",null],[2,"mat-form-field-appearance-fill",null],[2,"mat-form-field-appearance-outline",null],[2,"mat-form-field-appearance-legacy",null],[2,"mat-form-field-invalid",null],[2,"mat-form-field-can-float",null],[2,"mat-form-field-should-float",null],[2,"mat-form-field-has-label",null],[2,"mat-form-field-hide-placeholder",null],[2,"mat-form-field-disabled",null],[2,"mat-form-field-autofilled",null],[2,"mat-focused",null],[2,"mat-accent",null],[2,"mat-warn",null],[2,"ng-untouched",null],[2,"ng-touched",null],[2,"ng-pristine",null],[2,"ng-dirty",null],[2,"ng-valid",null],[2,"ng-invalid",null],[2,"ng-pending",null],[2,"_mat-animation-noopable",null]],null,null,f.b,f.a)),e.sb(4,7520256,null,9,h.c,[e.k,e.h,[2,p.j],[2,g.b],[2,h.a],_.a,e.z,[2,F.a]],{floatLabel:[0,"floatLabel"]},null),e.Lb(603979776,1,{_controlNonStatic:0}),e.Lb(335544320,2,{_controlStatic:0}),e.Lb(603979776,3,{_labelChildNonStatic:0}),e.Lb(335544320,4,{_labelChildStatic:0}),e.Lb(603979776,5,{_placeholderChild:0}),e.Lb(603979776,6,{_errorChildren:1}),e.Lb(603979776,7,{_hintChildren:1}),e.Lb(603979776,8,{_prefixChildren:1}),e.Lb(603979776,9,{_suffixChildren:1}),(l()(),e.tb(14,0,null,1,7,"input",[["class","mat-input-element mat-form-field-autofill-control"],["matInput",""],["placeholder","Search"]],[[2,"mat-input-server",null],[1,"id",0],[1,"placeholder",0],[8,"disabled",0],[8,"required",0],[1,"readonly",0],[1,"aria-describedby",0],[1,"aria-invalid",0],[1,"aria-required",0],[2,"ng-untouched",null],[2,"ng-touched",null],[2,"ng-pristine",null],[2,"ng-dirty",null],[2,"ng-valid",null],[2,"ng-invalid",null],[2,"ng-pending",null]],[[null,"input"],[null,"blur"],[null,"compositionstart"],[null,"compositionend"],[null,"focus"]],(function(l,n,u){var t=!0;return"input"===n&&(t=!1!==e.Fb(l,15)._handleInput(u.target.value)&&t),"blur"===n&&(t=!1!==e.Fb(l,15).onTouched()&&t),"compositionstart"===n&&(t=!1!==e.Fb(l,15)._compositionStart()&&t),"compositionend"===n&&(t=!1!==e.Fb(l,15)._compositionEnd(u.target.value)&&t),"blur"===n&&(t=!1!==e.Fb(l,19)._focusChanged(!1)&&t),"focus"===n&&(t=!1!==e.Fb(l,19)._focusChanged(!0)&&t),"input"===n&&(t=!1!==e.Fb(l,19)._onInput()&&t),t}),null,null)),e.sb(15,16384,null,0,D.d,[e.E,e.k,[2,D.a]],null,null),e.Kb(1024,null,D.m,(function(l){return[l]}),[D.d]),e.sb(17,540672,null,0,D.g,[[8,null],[8,null],[6,D.m],[2,D.x]],{form:[0,"form"]},null),e.Kb(2048,null,D.n,null,[D.g]),e.sb(19,999424,null,0,C.b,[e.k,_.a,[6,D.n],[2,D.q],[2,D.j],p.d,[8,null],v.a,e.z],{placeholder:[0,"placeholder"]},null),e.sb(20,16384,null,0,D.o,[[4,D.n]],null,null),e.Kb(2048,[[1,4],[2,4]],h.d,null,[C.b]),(l()(),e.tb(22,0,null,null,79,"div",[["class","responsive"]],null,null,null,null,null)),(l()(),e.tb(23,0,null,null,78,"table",[["class","mat-table"],["mat-table",""],["matSort",""]],null,[[null,"matSortChange"]],(function(l,n,u){var e=!0;return"matSortChange"===n&&(e=!1!==l.component.sortData(u)&&e),e}),m.f,m.c)),e.Kb(6144,null,o.o,null,[b.k]),e.sb(25,2342912,null,4,b.k,[e.s,e.h,e.k,[8,null],[2,g.b],c.d,_.a],{dataSource:[0,"dataSource"]},null),e.Lb(603979776,10,{_contentColumnDefs:1}),e.Lb(603979776,11,{_contentRowDefs:1}),e.Lb(603979776,12,{_contentHeaderRowDefs:1}),e.Lb(603979776,13,{_contentFooterRowDefs:1}),e.sb(30,737280,null,0,i.b,[],null,{sortChange:"matSortChange"}),(l()(),e.tb(31,0,null,null,12,null,null,null,null,null,null,null)),e.Kb(6144,null,"MAT_SORT_HEADER_COLUMN_DEF",null,[b.c]),e.sb(33,16384,null,3,b.c,[],{name:[0,"name"]},null),e.Lb(603979776,14,{cell:0}),e.Lb(603979776,15,{headerCell:0}),e.Lb(603979776,16,{footerCell:0}),e.Kb(2048,[[10,4]],o.d,null,[b.c]),(l()(),e.jb(0,null,null,2,null,j)),e.sb(39,16384,null,0,b.f,[e.M],null,null),e.Kb(2048,[[15,4]],o.j,null,[b.f]),(l()(),e.jb(0,null,null,2,null,I)),e.sb(42,16384,null,0,b.b,[e.M],null,null),e.Kb(2048,[[14,4]],o.b,null,[b.b]),(l()(),e.tb(44,0,null,null,12,null,null,null,null,null,null,null)),e.Kb(6144,null,"MAT_SORT_HEADER_COLUMN_DEF",null,[b.c]),e.sb(46,16384,null,3,b.c,[],{name:[0,"name"]},null),e.Lb(603979776,17,{cell:0}),e.Lb(603979776,18,{headerCell:0}),e.Lb(603979776,19,{footerCell:0}),e.Kb(2048,[[10,4]],o.d,null,[b.c]),(l()(),e.jb(0,null,null,2,null,x)),e.sb(52,16384,null,0,b.f,[e.M],null,null),e.Kb(2048,[[18,4]],o.j,null,[b.f]),(l()(),e.jb(0,null,null,2,null,H)),e.sb(55,16384,null,0,b.b,[e.M],null,null),e.Kb(2048,[[17,4]],o.b,null,[b.b]),(l()(),e.tb(57,0,null,null,12,null,null,null,null,null,null,null)),e.Kb(6144,null,"MAT_SORT_HEADER_COLUMN_DEF",null,[b.c]),e.sb(59,16384,null,3,b.c,[],{name:[0,"name"]},null),e.Lb(603979776,20,{cell:0}),e.Lb(603979776,21,{headerCell:0}),e.Lb(603979776,22,{footerCell:0}),e.Kb(2048,[[10,4]],o.d,null,[b.c]),(l()(),e.jb(0,null,null,2,null,N)),e.sb(65,16384,null,0,b.f,[e.M],null,null),e.Kb(2048,[[21,4]],o.j,null,[b.f]),(l()(),e.jb(0,null,null,2,null,U)),e.sb(68,16384,null,0,b.b,[e.M],null,null),e.Kb(2048,[[20,4]],o.b,null,[b.b]),(l()(),e.tb(70,0,null,null,12,null,null,null,null,null,null,null)),e.Kb(6144,null,"MAT_SORT_HEADER_COLUMN_DEF",null,[b.c]),e.sb(72,16384,null,3,b.c,[],{name:[0,"name"]},null),e.Lb(603979776,23,{cell:0}),e.Lb(603979776,24,{headerCell:0}),e.Lb(603979776,25,{footerCell:0}),e.Kb(2048,[[10,4]],o.d,null,[b.c]),(l()(),e.jb(0,null,null,2,null,V)),e.sb(78,16384,null,0,b.f,[e.M],null,null),e.Kb(2048,[[24,4]],o.j,null,[b.f]),(l()(),e.jb(0,null,null,2,null,q)),e.sb(81,16384,null,0,b.b,[e.M],null,null),e.Kb(2048,[[23,4]],o.b,null,[b.b]),(l()(),e.tb(83,0,null,null,12,null,null,null,null,null,null,null)),e.Kb(6144,null,"MAT_SORT_HEADER_COLUMN_DEF",null,[b.c]),e.sb(85,16384,null,3,b.c,[],{name:[0,"name"]},null),e.Lb(603979776,26,{cell:0}),e.Lb(603979776,27,{headerCell:0}),e.Lb(603979776,28,{footerCell:0}),e.Kb(2048,[[10,4]],o.d,null,[b.c]),(l()(),e.jb(0,null,null,2,null,z)),e.sb(91,16384,null,0,b.f,[e.M],null,null),e.Kb(2048,[[27,4]],o.j,null,[b.f]),(l()(),e.jb(0,null,null,2,null,G)),e.sb(94,16384,null,0,b.b,[e.M],null,null),e.Kb(2048,[[26,4]],o.b,null,[b.b]),(l()(),e.jb(0,null,null,2,null,B)),e.sb(97,540672,null,0,b.h,[e.M,e.s],{columns:[0,"columns"]},null),e.Kb(2048,[[12,4]],o.l,null,[b.h]),(l()(),e.jb(0,null,null,2,null,J)),e.sb(100,540672,null,0,b.j,[e.M,e.s],{columns:[0,"columns"]},null),e.Kb(2048,[[11,4]],o.n,null,[b.j]),(l()(),e.tb(102,0,null,null,2,"mat-paginator",[["class","mat-paginator"],["showFirstLastButtons","true"]],null,[[null,"page"]],(function(l,n,u){var e=!0,t=l.component;return"page"===n&&(e=!1!==t.pageChange(u,t.analyticsEventCategory)&&e),e}),M.b,M.a)),e.sb(103,245760,null,0,L.b,[L.c,e.h],{length:[0,"length"],pageSize:[1,"pageSize"],pageSizeOptions:[2,"pageSizeOptions"],showFirstLastButtons:[3,"showFirstLastButtons"]},{page:"page"}),e.Gb(104,4)],(function(l,n){var u=n.component;l(n,4,0,"never"),l(n,17,0,u.searchControl),l(n,19,0,"Search"),l(n,25,0,u.paged),l(n,30,0),l(n,33,0,"citation"),l(n,46,0,"type"),l(n,59,0,"tags"),l(n,72,0,"files"),l(n,85,0,"dateAcessed"),l(n,97,0,u.displayedColumns),l(n,100,0,u.displayedColumns);var e=u.filtered&&u.filtered.length||0,t=l(n,104,0,5,10,25,100);l(n,103,0,e,10,t,"true")}),(function(l,n){l(n,3,1,["standard"==e.Fb(n,4).appearance,"fill"==e.Fb(n,4).appearance,"outline"==e.Fb(n,4).appearance,"legacy"==e.Fb(n,4).appearance,e.Fb(n,4)._control.errorState,e.Fb(n,4)._canLabelFloat,e.Fb(n,4)._shouldLabelFloat(),e.Fb(n,4)._hasFloatingLabel(),e.Fb(n,4)._hideControlPlaceholder(),e.Fb(n,4)._control.disabled,e.Fb(n,4)._control.autofilled,e.Fb(n,4)._control.focused,"accent"==e.Fb(n,4).color,"warn"==e.Fb(n,4).color,e.Fb(n,4)._shouldForward("untouched"),e.Fb(n,4)._shouldForward("touched"),e.Fb(n,4)._shouldForward("pristine"),e.Fb(n,4)._shouldForward("dirty"),e.Fb(n,4)._shouldForward("valid"),e.Fb(n,4)._shouldForward("invalid"),e.Fb(n,4)._shouldForward("pending"),!e.Fb(n,4)._animationsEnabled]),l(n,14,1,[e.Fb(n,19)._isServer,e.Fb(n,19).id,e.Fb(n,19).placeholder,e.Fb(n,19).disabled,e.Fb(n,19).required,e.Fb(n,19).readonly&&!e.Fb(n,19)._isNativeSelect||null,e.Fb(n,19)._ariaDescribedby||null,e.Fb(n,19).errorState,e.Fb(n,19).required.toString(),e.Fb(n,20).ngClassUntouched,e.Fb(n,20).ngClassTouched,e.Fb(n,20).ngClassPristine,e.Fb(n,20).ngClassDirty,e.Fb(n,20).ngClassValid,e.Fb(n,20).ngClassInvalid,e.Fb(n,20).ngClassPending])}))}function Z(l){return e.Pb(0,[(l()(),e.tb(0,0,null,null,1,"app-substance-references",[],null,null,null,Y,A)),e.sb(1,114688,null,0,w,[E.a,O.a],null,null)],(function(l,n){l(n,1,0)}),null)}var W=e.pb("app-substance-references",w,Z,{},{countUpdate:"countUpdate"},[]),Q=u("eDkP"),X=u("M2Lx"),ll=u("uGex"),nl=u("v9Dh"),ul=u("ZYjt"),el=u("EtvR"),tl=u("UodH"),al=u("4c35"),rl=u("qAlS"),bl=u("lLAP"),ol=u("ZYCi"),il=u("6jyQ");u.d(n,"SubstanceReferencesModuleNgFactory",(function(){return cl}));var cl=e.qb(t,[],(function(l){return e.Cb([e.Db(512,e.j,e.bb,[[8,[a.a,W]],[3,e.j],e.x]),e.Db(4608,c.p,c.o,[e.u,[2,c.G]]),e.Db(4608,Q.c,Q.c,[Q.i,Q.e,e.j,Q.h,Q.f,e.r,e.z,c.d,g.b,[2,c.j]]),e.Db(5120,Q.j,Q.k,[Q.c]),e.Db(4608,X.c,X.c,[]),e.Db(5120,ll.a,ll.b,[Q.c]),e.Db(5120,nl.b,nl.c,[Q.c]),e.Db(4608,ul.e,p.e,[[2,p.i],[2,p.n]]),e.Db(5120,L.c,L.a,[[3,L.c]]),e.Db(4608,p.d,p.d,[]),e.Db(4608,D.e,D.e,[]),e.Db(4608,D.w,D.w,[]),e.Db(5120,i.d,i.a,[[3,i.d]]),e.Db(1073742336,c.c,c.c,[]),e.Db(1073742336,el.a,el.a,[]),e.Db(1073742336,o.p,o.p,[]),e.Db(1073742336,g.a,g.a,[]),e.Db(1073742336,p.n,p.n,[[2,p.f],[2,ul.f]]),e.Db(1073742336,b.m,b.m,[]),e.Db(1073742336,_.b,_.b,[]),e.Db(1073742336,p.x,p.x,[]),e.Db(1073742336,tl.c,tl.c,[]),e.Db(1073742336,al.g,al.g,[]),e.Db(1073742336,rl.c,rl.c,[]),e.Db(1073742336,Q.g,Q.g,[]),e.Db(1073742336,p.v,p.v,[]),e.Db(1073742336,p.s,p.s,[]),e.Db(1073742336,X.d,X.d,[]),e.Db(1073742336,h.e,h.e,[]),e.Db(1073742336,ll.d,ll.d,[]),e.Db(1073742336,bl.a,bl.a,[]),e.Db(1073742336,nl.e,nl.e,[]),e.Db(1073742336,L.d,L.d,[]),e.Db(1073742336,v.c,v.c,[]),e.Db(1073742336,C.c,C.c,[]),e.Db(1073742336,D.v,D.v,[]),e.Db(1073742336,D.s,D.s,[]),e.Db(1073742336,D.k,D.k,[]),e.Db(1073742336,i.e,i.e,[]),e.Db(1073742336,d.c,d.c,[]),e.Db(1073742336,t,t,[]),e.Db(1024,ol.j,(function(){return[[]]}),[]),e.Db(256,il.a,w,[])])}))}}]);
//# sourceMappingURL=52.cb92a15ac09f071f9142.js.map