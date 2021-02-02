(window.webpackJsonp=window.webpackJsonp||[]).push([[40],{"3oK8":function(l,n,u){"use strict";u.r(n);var t=u("CcnG"),e=function(){return function(){}}(),b=u("pMnS"),c=u("BHnd"),a=u("y4qS"),o=u("Ip0R"),i=u("ZYCi"),r=u("pIm3"),s=u("Fzqc"),m=u("dWZg"),d=u("mrSG"),f=u("EfhQ"),p=u("K9Ia"),M=function(l){function n(n){var u=l.call(this)||this;return u.utilsService=n,u.substanceUpdated=new p.a,u.structuralColumns=["Modification Type","Location Site","Location Type","Residue Modified","Extent","Modification Name","Modification ID"],u.physicalColumns=["Modification Role","Parameter Name","Amount"],u.agentColumns=["Modification Process","Modification Role","Modification Type","Amount","Modification Agent","Approved ID"],u}return d.a(n,l),n.prototype.ngOnInit=function(){var l=this;this.substanceUpdated.subscribe((function(n){l.substance=n,null!=l.substance&&(l.substance.modifications.structuralModifications.length>0&&(l.structural=l.substance.modifications.structuralModifications),l.substance.modifications.physicalModifications.length>0&&(l.physical=l.substance.modifications.physicalModifications),l.substance.modifications.agentModifications.length>0&&(l.agent=l.substance.modifications.agentModifications)),l.structural&&l.structural.forEach((function(n){n.extentAmount&&(n.$$amount=l.displayAmount(n.extentAmount))}))}))},n.prototype.displayAmount=function(l){var n=this.utilsService.displayAmount(l);return n&&0!==n.trim().length||(n="empty value"),n},n}(f.a),h=u("6E2U"),g=t.rb({encapsulation:0,styles:[[".param-container[_ngcontent-%COMP%]{width:100%;display:-webkit-box;display:-ms-flexbox;display:flex;-webkit-box-orient:vertical;-webkit-box-direction:normal;-ms-flex-direction:column;flex-direction:column}.param-container[_ngcontent-%COMP%]   .param-amount[_ngcontent-%COMP%], .param-container[_ngcontent-%COMP%]   .param-name[_ngcontent-%COMP%]{padding-bottom:5px;padding-top:6px}.empty[_ngcontent-%COMP%]{font-style:italic}.bottom-border[_ngcontent-%COMP%]{border-bottom:1px solid rgba(0,0,0,.05)}"]],data:{}});function K(l){return t.Pb(0,[(l()(),t.tb(0,0,null,null,2,"th",[["class","mat-header-cell"],["mat-header-cell",""],["role","columnheader"]],null,null,null,null,null)),t.sb(1,16384,null,0,c.e,[a.d,t.k],null,null),(l()(),t.Nb(-1,null,[" Modification Process "]))],null,null)}function _(l){return t.Pb(0,[(l()(),t.tb(0,0,null,null,2,"td",[["class","mat-cell"],["mat-cell",""],["role","gridcell"]],null,null,null,null,null)),t.sb(1,16384,null,0,c.a,[a.d,t.k],null,null),(l()(),t.Nb(2,null,[" "," "]))],null,(function(l,n){l(n,2,0,n.context.$implicit.agentModificationProcess)}))}function L(l){return t.Pb(0,[(l()(),t.tb(0,0,null,null,2,"th",[["class","mat-header-cell"],["mat-header-cell",""],["role","columnheader"]],null,null,null,null,null)),t.sb(1,16384,null,0,c.e,[a.d,t.k],null,null),(l()(),t.Nb(-1,null,[" Modification Role "]))],null,null)}function P(l){return t.Pb(0,[(l()(),t.tb(0,0,null,null,2,"td",[["class","mat-cell"],["mat-cell",""],["role","gridcell"]],null,null,null,null,null)),t.sb(1,16384,null,0,c.a,[a.d,t.k],null,null),(l()(),t.Nb(2,null,[" "," "]))],null,(function(l,n){l(n,2,0,n.context.$implicit.agentModificationRole)}))}function C(l){return t.Pb(0,[(l()(),t.tb(0,0,null,null,2,"th",[["class","mat-header-cell"],["mat-header-cell",""],["role","columnheader"]],null,null,null,null,null)),t.sb(1,16384,null,0,c.e,[a.d,t.k],null,null),(l()(),t.Nb(-1,null,[" Modification Type "]))],null,null)}function j(l){return t.Pb(0,[(l()(),t.tb(0,0,null,null,2,"td",[["class","mat-cell"],["mat-cell",""],["role","gridcell"]],null,null,null,null,null)),t.sb(1,16384,null,0,c.a,[a.d,t.k],null,null),(l()(),t.Nb(2,null,[" "," "]))],null,(function(l,n){l(n,2,0,n.context.$implicit.agentModificationType)}))}function k(l){return t.Pb(0,[(l()(),t.tb(0,0,null,null,2,"th",[["class","mat-header-cell"],["mat-header-cell",""],["role","columnheader"]],null,null,null,null,null)),t.sb(1,16384,null,0,c.e,[a.d,t.k],null,null),(l()(),t.Nb(-1,null,[" Amount "]))],null,null)}function D(l){return t.Pb(0,[(l()(),t.tb(0,0,null,null,6,"td",[["class","mat-cell"],["mat-cell",""],["role","gridcell"]],null,null,null,null,null)),t.sb(1,16384,null,0,c.a,[a.d,t.k],null,null),(l()(),t.tb(2,0,null,null,4,"span",[],null,null,null,null,null)),t.Kb(512,null,o.B,o.C,[t.s,t.t,t.k,t.E]),t.sb(4,278528,null,0,o.l,[o.B],{ngClass:[0,"ngClass"]},null),t.Ib(5,{empty:0}),(l()(),t.Nb(6,null,[" "," "]))],(function(l,n){var u=l(n,5,0,"empty value"===n.component.displayAmount(n.context.$implicit.amount));l(n,4,0,u)}),(function(l,n){l(n,6,0,n.component.displayAmount(n.context.$implicit.amount))}))}function N(l){return t.Pb(0,[(l()(),t.tb(0,0,null,null,2,"th",[["class","mat-header-cell"],["mat-header-cell",""],["role","columnheader"]],null,null,null,null,null)),t.sb(1,16384,null,0,c.e,[a.d,t.k],null,null),(l()(),t.Nb(-1,null,[" Modification Agent "]))],null,null)}function E(l){return t.Pb(0,[(l()(),t.tb(0,0,null,null,2,"td",[["class","mat-cell"],["mat-cell",""],["role","gridcell"]],null,null,null,null,null)),t.sb(1,16384,null,0,c.a,[a.d,t.k],null,null),(l()(),t.Nb(2,null,[" "," "]))],null,(function(l,n){l(n,2,0,null==n.context.$implicit.agentSubstance?null:n.context.$implicit.agentSubstance.refPname)}))}function x(l){return t.Pb(0,[(l()(),t.tb(0,0,null,null,2,"th",[["class","mat-header-cell"],["mat-header-cell",""],["role","columnheader"]],null,null,null,null,null)),t.sb(1,16384,null,0,c.e,[a.d,t.k],null,null),(l()(),t.Nb(-1,null,[" Approved ID "]))],null,null)}function A(l){return t.Pb(0,[(l()(),t.tb(0,0,null,null,3,"a",[["target","_blank"]],[[1,"target",0],[8,"href",4]],[[null,"click"]],(function(l,n,u){var e=!0;return"click"===n&&(e=!1!==t.Fb(l,1).onClick(u.button,u.ctrlKey,u.metaKey,u.shiftKey)&&e),e}),null,null)),t.sb(1,671744,null,0,i.o,[i.m,i.a,o.k],{target:[0,"target"],routerLink:[1,"routerLink"]},null),t.Gb(2,2),(l()(),t.Nb(3,null,["",""]))],(function(l,n){var u=l(n,2,0,"/substances",n.parent.context.$implicit.agentSubstance.refuuid);l(n,1,0,"_blank",u)}),(function(l,n){l(n,0,0,t.Fb(n,1).target,t.Fb(n,1).href),l(n,3,0,n.parent.context.$implicit.agentSubstance.approvalID||n.parent.context.$implicit.agentSubstance.refuuid)}))}function R(l){return t.Pb(0,[(l()(),t.tb(0,0,null,null,3,"td",[["class","mat-cell"],["mat-cell",""],["role","gridcell"]],null,null,null,null,null)),t.sb(1,16384,null,0,c.a,[a.d,t.k],null,null),(l()(),t.jb(16777216,null,null,1,null,A)),t.sb(3,16384,null,0,o.n,[t.P,t.M],{ngIf:[0,"ngIf"]},null)],(function(l,n){l(n,3,0,n.context.$implicit.agentSubstance&&n.context.$implicit.agentSubstance.refuuid)}),null)}function y(l){return t.Pb(0,[(l()(),t.tb(0,0,null,null,2,"tr",[["class","mat-header-row"],["mat-header-row",""],["role","row"]],null,null,null,r.d,r.a)),t.Kb(6144,null,a.k,null,[c.g]),t.sb(2,49152,null,0,c.g,[],null,null)],null,null)}function O(l){return t.Pb(0,[(l()(),t.tb(0,0,null,null,2,"tr",[["class","mat-row"],["mat-row",""],["role","row"]],null,null,null,r.e,r.b)),t.Kb(6144,null,a.m,null,[c.i]),t.sb(2,49152,null,0,c.i,[],null,null)],null,null)}function T(l){return t.Pb(0,[(l()(),t.tb(0,0,null,null,93,"div",[],null,null,null,null,null)),(l()(),t.tb(1,0,null,null,1,"h4",[],null,null,null,null,null)),(l()(),t.Nb(-1,null,["Agent Modifications:"])),(l()(),t.tb(3,0,null,null,90,"table",[["class","no-bottom-border mat-table"],["mat-table",""]],null,null,null,r.f,r.c)),t.Kb(6144,null,a.o,null,[c.k]),t.sb(5,2342912,null,4,c.k,[t.s,t.h,t.k,[8,null],[2,s.b],o.d,m.a],{dataSource:[0,"dataSource"]},null),t.Lb(603979776,1,{_contentColumnDefs:1}),t.Lb(603979776,2,{_contentRowDefs:1}),t.Lb(603979776,3,{_contentHeaderRowDefs:1}),t.Lb(603979776,4,{_contentFooterRowDefs:1}),(l()(),t.tb(10,0,null,null,12,null,null,null,null,null,null,null)),t.Kb(6144,null,"MAT_SORT_HEADER_COLUMN_DEF",null,[c.c]),t.sb(12,16384,null,3,c.c,[],{name:[0,"name"]},null),t.Lb(603979776,5,{cell:0}),t.Lb(603979776,6,{headerCell:0}),t.Lb(603979776,7,{footerCell:0}),t.Kb(2048,[[1,4]],a.d,null,[c.c]),(l()(),t.jb(0,null,null,2,null,K)),t.sb(18,16384,null,0,c.f,[t.M],null,null),t.Kb(2048,[[6,4]],a.j,null,[c.f]),(l()(),t.jb(0,null,null,2,null,_)),t.sb(21,16384,null,0,c.b,[t.M],null,null),t.Kb(2048,[[5,4]],a.b,null,[c.b]),(l()(),t.tb(23,0,null,null,12,null,null,null,null,null,null,null)),t.Kb(6144,null,"MAT_SORT_HEADER_COLUMN_DEF",null,[c.c]),t.sb(25,16384,null,3,c.c,[],{name:[0,"name"]},null),t.Lb(603979776,8,{cell:0}),t.Lb(603979776,9,{headerCell:0}),t.Lb(603979776,10,{footerCell:0}),t.Kb(2048,[[1,4]],a.d,null,[c.c]),(l()(),t.jb(0,null,null,2,null,L)),t.sb(31,16384,null,0,c.f,[t.M],null,null),t.Kb(2048,[[9,4]],a.j,null,[c.f]),(l()(),t.jb(0,null,null,2,null,P)),t.sb(34,16384,null,0,c.b,[t.M],null,null),t.Kb(2048,[[8,4]],a.b,null,[c.b]),(l()(),t.tb(36,0,null,null,12,null,null,null,null,null,null,null)),t.Kb(6144,null,"MAT_SORT_HEADER_COLUMN_DEF",null,[c.c]),t.sb(38,16384,null,3,c.c,[],{name:[0,"name"]},null),t.Lb(603979776,11,{cell:0}),t.Lb(603979776,12,{headerCell:0}),t.Lb(603979776,13,{footerCell:0}),t.Kb(2048,[[1,4]],a.d,null,[c.c]),(l()(),t.jb(0,null,null,2,null,C)),t.sb(44,16384,null,0,c.f,[t.M],null,null),t.Kb(2048,[[12,4]],a.j,null,[c.f]),(l()(),t.jb(0,null,null,2,null,j)),t.sb(47,16384,null,0,c.b,[t.M],null,null),t.Kb(2048,[[11,4]],a.b,null,[c.b]),(l()(),t.tb(49,0,null,null,12,null,null,null,null,null,null,null)),t.Kb(6144,null,"MAT_SORT_HEADER_COLUMN_DEF",null,[c.c]),t.sb(51,16384,null,3,c.c,[],{name:[0,"name"]},null),t.Lb(603979776,14,{cell:0}),t.Lb(603979776,15,{headerCell:0}),t.Lb(603979776,16,{footerCell:0}),t.Kb(2048,[[1,4]],a.d,null,[c.c]),(l()(),t.jb(0,null,null,2,null,k)),t.sb(57,16384,null,0,c.f,[t.M],null,null),t.Kb(2048,[[15,4]],a.j,null,[c.f]),(l()(),t.jb(0,null,null,2,null,D)),t.sb(60,16384,null,0,c.b,[t.M],null,null),t.Kb(2048,[[14,4]],a.b,null,[c.b]),(l()(),t.tb(62,0,null,null,12,null,null,null,null,null,null,null)),t.Kb(6144,null,"MAT_SORT_HEADER_COLUMN_DEF",null,[c.c]),t.sb(64,16384,null,3,c.c,[],{name:[0,"name"]},null),t.Lb(603979776,17,{cell:0}),t.Lb(603979776,18,{headerCell:0}),t.Lb(603979776,19,{footerCell:0}),t.Kb(2048,[[1,4]],a.d,null,[c.c]),(l()(),t.jb(0,null,null,2,null,N)),t.sb(70,16384,null,0,c.f,[t.M],null,null),t.Kb(2048,[[18,4]],a.j,null,[c.f]),(l()(),t.jb(0,null,null,2,null,E)),t.sb(73,16384,null,0,c.b,[t.M],null,null),t.Kb(2048,[[17,4]],a.b,null,[c.b]),(l()(),t.tb(75,0,null,null,12,null,null,null,null,null,null,null)),t.Kb(6144,null,"MAT_SORT_HEADER_COLUMN_DEF",null,[c.c]),t.sb(77,16384,null,3,c.c,[],{name:[0,"name"]},null),t.Lb(603979776,20,{cell:0}),t.Lb(603979776,21,{headerCell:0}),t.Lb(603979776,22,{footerCell:0}),t.Kb(2048,[[1,4]],a.d,null,[c.c]),(l()(),t.jb(0,null,null,2,null,x)),t.sb(83,16384,null,0,c.f,[t.M],null,null),t.Kb(2048,[[21,4]],a.j,null,[c.f]),(l()(),t.jb(0,null,null,2,null,R)),t.sb(86,16384,null,0,c.b,[t.M],null,null),t.Kb(2048,[[20,4]],a.b,null,[c.b]),(l()(),t.jb(0,null,null,2,null,y)),t.sb(89,540672,null,0,c.h,[t.M,t.s],{columns:[0,"columns"]},null),t.Kb(2048,[[3,4]],a.l,null,[c.h]),(l()(),t.jb(0,null,null,2,null,O)),t.sb(92,540672,null,0,c.j,[t.M,t.s],{columns:[0,"columns"]},null),t.Kb(2048,[[2,4]],a.n,null,[c.j])],(function(l,n){var u=n.component;l(n,5,0,u.agent),l(n,12,0,"Modification Process"),l(n,25,0,"Modification Role"),l(n,38,0,"Modification Type"),l(n,51,0,"Amount"),l(n,64,0,"Modification Agent"),l(n,77,0,"Approved ID"),l(n,89,0,u.agentColumns),l(n,92,0,u.agentColumns)}),null)}function $(l){return t.Pb(0,[(l()(),t.tb(0,0,null,null,2,"th",[["class","mat-header-cell"],["mat-header-cell",""],["role","columnheader"]],null,null,null,null,null)),t.sb(1,16384,null,0,c.e,[a.d,t.k],null,null),(l()(),t.Nb(-1,null,[" Modification Role "]))],null,null)}function F(l){return t.Pb(0,[(l()(),t.tb(0,0,null,null,2,"td",[["class","mat-cell"],["mat-cell",""],["role","gridcell"]],null,null,null,null,null)),t.sb(1,16384,null,0,c.a,[a.d,t.k],null,null),(l()(),t.Nb(2,null,[" "," "]))],null,(function(l,n){l(n,2,0,n.context.$implicit.physicalModificationRole)}))}function S(l){return t.Pb(0,[(l()(),t.tb(0,0,null,null,2,"th",[["class","mat-header-cell"],["mat-header-cell",""],["role","columnheader"]],null,null,null,null,null)),t.sb(1,16384,null,0,c.e,[a.d,t.k],null,null),(l()(),t.Nb(-1,null,[" Parameter Name "]))],null,null)}function w(l){return t.Pb(0,[(l()(),t.tb(0,0,null,null,1,"span",[["class","param-name"]],null,null,null,null,null)),(l()(),t.Nb(1,null,[" "," "]))],null,(function(l,n){l(n,1,0,n.context.$implicit.parameterName)}))}function v(l){return t.Pb(0,[(l()(),t.tb(0,0,null,null,2,"div",[["class","param-container"]],null,null,null,null,null)),(l()(),t.jb(16777216,null,null,1,null,w)),t.sb(2,278528,null,0,o.m,[t.P,t.M,t.s],{ngForOf:[0,"ngForOf"]},null)],(function(l,n){l(n,2,0,n.parent.context.$implicit.parameters)}),null)}function I(l){return t.Pb(0,[(l()(),t.tb(0,0,null,null,3,"td",[["class","mat-cell"],["mat-cell",""],["role","gridcell"]],null,null,null,null,null)),t.sb(1,16384,null,0,c.a,[a.d,t.k],null,null),(l()(),t.jb(16777216,null,null,1,null,v)),t.sb(3,16384,null,0,o.n,[t.P,t.M],{ngIf:[0,"ngIf"]},null)],(function(l,n){l(n,3,0,n.context.$implicit.parameters&&n.context.$implicit.parameters.length)}),null)}function U(l){return t.Pb(0,[(l()(),t.tb(0,0,null,null,2,"th",[["class","mat-header-cell"],["mat-header-cell",""],["role","columnheader"]],null,null,null,null,null)),t.sb(1,16384,null,0,c.e,[a.d,t.k],null,null),(l()(),t.Nb(-1,null,[" Amount "]))],null,null)}function H(l){return t.Pb(0,[(l()(),t.tb(0,0,null,null,5,"span",[["class","param-amount"]],null,null,null,null,null)),(l()(),t.tb(1,0,null,null,4,"span",[],null,null,null,null,null)),t.Kb(512,null,o.B,o.C,[t.s,t.t,t.k,t.E]),t.sb(3,278528,null,0,o.l,[o.B],{ngClass:[0,"ngClass"]},null),t.Ib(4,{empty:0}),(l()(),t.Nb(5,null,[" "," "]))],(function(l,n){var u=l(n,4,0,"empty value"===n.component.displayAmount(n.context.$implicit.amount));l(n,3,0,u)}),(function(l,n){l(n,5,0,n.component.displayAmount(n.context.$implicit.amount))}))}function B(l){return t.Pb(0,[(l()(),t.tb(0,0,null,null,2,"div",[["class","param-container"]],null,null,null,null,null)),(l()(),t.jb(16777216,null,null,1,null,H)),t.sb(2,278528,null,0,o.m,[t.P,t.M,t.s],{ngForOf:[0,"ngForOf"]},null)],(function(l,n){l(n,2,0,n.parent.context.$implicit.parameters)}),null)}function G(l){return t.Pb(0,[(l()(),t.tb(0,0,null,null,3,"td",[["class","mat-cell"],["mat-cell",""],["role","gridcell"]],null,null,null,null,null)),t.sb(1,16384,null,0,c.a,[a.d,t.k],null,null),(l()(),t.jb(16777216,null,null,1,null,B)),t.sb(3,16384,null,0,o.n,[t.P,t.M],{ngIf:[0,"ngIf"]},null)],(function(l,n){l(n,3,0,n.context.$implicit.parameters&&n.context.$implicit.parameters.length)}),null)}function q(l){return t.Pb(0,[(l()(),t.tb(0,0,null,null,2,"tr",[["class","mat-header-row"],["mat-header-row",""],["role","row"]],null,null,null,r.d,r.a)),t.Kb(6144,null,a.k,null,[c.g]),t.sb(2,49152,null,0,c.g,[],null,null)],null,null)}function Z(l){return t.Pb(0,[(l()(),t.tb(0,0,null,null,2,"tr",[["class","mat-row"],["mat-row",""],["role","row"]],null,null,null,r.e,r.b)),t.Kb(6144,null,a.m,null,[c.i]),t.sb(2,49152,null,0,c.i,[],null,null)],null,null)}function J(l){return t.Pb(0,[(l()(),t.tb(0,0,null,null,54,"div",[],null,null,null,null,null)),(l()(),t.tb(1,0,null,null,1,"h4",[],null,null,null,null,null)),(l()(),t.Nb(-1,null,["Physical Modifications:"])),(l()(),t.tb(3,0,null,null,51,"table",[["class","no-bottom-border mat-table"],["mat-table",""]],null,null,null,r.f,r.c)),t.Kb(6144,null,a.o,null,[c.k]),t.sb(5,2342912,null,4,c.k,[t.s,t.h,t.k,[8,null],[2,s.b],o.d,m.a],{dataSource:[0,"dataSource"]},null),t.Lb(603979776,23,{_contentColumnDefs:1}),t.Lb(603979776,24,{_contentRowDefs:1}),t.Lb(603979776,25,{_contentHeaderRowDefs:1}),t.Lb(603979776,26,{_contentFooterRowDefs:1}),(l()(),t.tb(10,0,null,null,12,null,null,null,null,null,null,null)),t.Kb(6144,null,"MAT_SORT_HEADER_COLUMN_DEF",null,[c.c]),t.sb(12,16384,null,3,c.c,[],{name:[0,"name"]},null),t.Lb(603979776,27,{cell:0}),t.Lb(603979776,28,{headerCell:0}),t.Lb(603979776,29,{footerCell:0}),t.Kb(2048,[[23,4]],a.d,null,[c.c]),(l()(),t.jb(0,null,null,2,null,$)),t.sb(18,16384,null,0,c.f,[t.M],null,null),t.Kb(2048,[[28,4]],a.j,null,[c.f]),(l()(),t.jb(0,null,null,2,null,F)),t.sb(21,16384,null,0,c.b,[t.M],null,null),t.Kb(2048,[[27,4]],a.b,null,[c.b]),(l()(),t.tb(23,0,null,null,12,null,null,null,null,null,null,null)),t.Kb(6144,null,"MAT_SORT_HEADER_COLUMN_DEF",null,[c.c]),t.sb(25,16384,null,3,c.c,[],{name:[0,"name"]},null),t.Lb(603979776,30,{cell:0}),t.Lb(603979776,31,{headerCell:0}),t.Lb(603979776,32,{footerCell:0}),t.Kb(2048,[[23,4]],a.d,null,[c.c]),(l()(),t.jb(0,null,null,2,null,S)),t.sb(31,16384,null,0,c.f,[t.M],null,null),t.Kb(2048,[[31,4]],a.j,null,[c.f]),(l()(),t.jb(0,null,null,2,null,I)),t.sb(34,16384,null,0,c.b,[t.M],null,null),t.Kb(2048,[[30,4]],a.b,null,[c.b]),(l()(),t.tb(36,0,null,null,12,null,null,null,null,null,null,null)),t.Kb(6144,null,"MAT_SORT_HEADER_COLUMN_DEF",null,[c.c]),t.sb(38,16384,null,3,c.c,[],{name:[0,"name"]},null),t.Lb(603979776,33,{cell:0}),t.Lb(603979776,34,{headerCell:0}),t.Lb(603979776,35,{footerCell:0}),t.Kb(2048,[[23,4]],a.d,null,[c.c]),(l()(),t.jb(0,null,null,2,null,U)),t.sb(44,16384,null,0,c.f,[t.M],null,null),t.Kb(2048,[[34,4]],a.j,null,[c.f]),(l()(),t.jb(0,null,null,2,null,G)),t.sb(47,16384,null,0,c.b,[t.M],null,null),t.Kb(2048,[[33,4]],a.b,null,[c.b]),(l()(),t.jb(0,null,null,2,null,q)),t.sb(50,540672,null,0,c.h,[t.M,t.s],{columns:[0,"columns"]},null),t.Kb(2048,[[25,4]],a.l,null,[c.h]),(l()(),t.jb(0,null,null,2,null,Z)),t.sb(53,540672,null,0,c.j,[t.M,t.s],{columns:[0,"columns"]},null),t.Kb(2048,[[24,4]],a.n,null,[c.j])],(function(l,n){var u=n.component;l(n,5,0,u.physical),l(n,12,0,"Modification Role"),l(n,25,0,"Parameter Name"),l(n,38,0,"Amount"),l(n,50,0,u.physicalColumns),l(n,53,0,u.physicalColumns)}),null)}function Q(l){return t.Pb(0,[(l()(),t.tb(0,0,null,null,2,"th",[["class","mat-header-cell"],["mat-header-cell",""],["role","columnheader"]],null,null,null,null,null)),t.sb(1,16384,null,0,c.e,[a.d,t.k],null,null),(l()(),t.Nb(-1,null,[" Modification Type "]))],null,null)}function W(l){return t.Pb(0,[(l()(),t.tb(0,0,null,null,2,"td",[["class","mat-cell"],["mat-cell",""],["role","gridcell"]],null,null,null,null,null)),t.sb(1,16384,null,0,c.a,[a.d,t.k],null,null),(l()(),t.Nb(2,null,[" "," "]))],null,(function(l,n){l(n,2,0,n.context.$implicit.structuralModificationType)}))}function Y(l){return t.Pb(0,[(l()(),t.tb(0,0,null,null,2,"th",[["class","mat-header-cell"],["mat-header-cell",""],["role","columnheader"]],null,null,null,null,null)),t.sb(1,16384,null,0,c.e,[a.d,t.k],null,null),(l()(),t.Nb(-1,null,[" Location Site "]))],null,null)}function z(l){return t.Pb(0,[(l()(),t.tb(0,0,null,null,2,"div",[],null,null,null,null,null)),(l()(),t.Nb(1,null,[" [","_","] "])),(l()(),t.tb(2,0,null,null,0,"br",[],null,null,null,null,null))],null,(function(l,n){l(n,1,0,n.context.$implicit.subunitIndex,n.context.$implicit.residueIndex)}))}function V(l){return t.Pb(0,[(l()(),t.tb(0,0,null,null,3,"td",[["class","mat-cell"],["mat-cell",""],["role","gridcell"]],null,null,null,null,null)),t.sb(1,16384,null,0,c.a,[a.d,t.k],null,null),(l()(),t.jb(16777216,null,null,1,null,z)),t.sb(3,278528,null,0,o.m,[t.P,t.M,t.s],{ngForOf:[0,"ngForOf"]},null)],(function(l,n){l(n,3,0,n.context.$implicit.sites)}),null)}function X(l){return t.Pb(0,[(l()(),t.tb(0,0,null,null,2,"th",[["class","mat-header-cell"],["mat-header-cell",""],["role","columnheader"]],null,null,null,null,null)),t.sb(1,16384,null,0,c.e,[a.d,t.k],null,null),(l()(),t.Nb(-1,null,[" Location Type "]))],null,null)}function ll(l){return t.Pb(0,[(l()(),t.tb(0,0,null,null,2,"td",[["class","mat-cell"],["mat-cell",""],["role","gridcell"]],null,null,null,null,null)),t.sb(1,16384,null,0,c.a,[a.d,t.k],null,null),(l()(),t.Nb(2,null,[" "," "]))],null,(function(l,n){l(n,2,0,n.context.$implicit.locationType)}))}function nl(l){return t.Pb(0,[(l()(),t.tb(0,0,null,null,2,"th",[["class","mat-header-cell"],["mat-header-cell",""],["role","columnheader"]],null,null,null,null,null)),t.sb(1,16384,null,0,c.e,[a.d,t.k],null,null),(l()(),t.Nb(-1,null,[" Residue Modified "]))],null,null)}function ul(l){return t.Pb(0,[(l()(),t.tb(0,0,null,null,2,"td",[["class","mat-cell"],["mat-cell",""],["role","gridcell"]],null,null,null,null,null)),t.sb(1,16384,null,0,c.a,[a.d,t.k],null,null),(l()(),t.Nb(2,null,[" "," "]))],null,(function(l,n){l(n,2,0,n.context.$implicit.residueModified)}))}function tl(l){return t.Pb(0,[(l()(),t.tb(0,0,null,null,2,"th",[["class","mat-header-cell"],["mat-header-cell",""],["role","columnheader"]],null,null,null,null,null)),t.sb(1,16384,null,0,c.e,[a.d,t.k],null,null),(l()(),t.Nb(-1,null,[" Extent "]))],null,null)}function el(l){return t.Pb(0,[(l()(),t.tb(0,0,null,null,6,"td",[["class","mat-cell"],["mat-cell",""],["role","gridcell"]],null,null,null,null,null)),t.sb(1,16384,null,0,c.a,[a.d,t.k],null,null),(l()(),t.Nb(2,null,[" ",""])),(l()(),t.tb(3,0,null,null,0,"br",[],null,null,null,null,null)),(l()(),t.tb(4,0,null,null,1,"b",[],null,null,null,null,null)),(l()(),t.Nb(-1,null,["Amount: "])),(l()(),t.Nb(6,null,[" "," "]))],null,(function(l,n){l(n,2,0,n.context.$implicit.extent),l(n,6,0,n.context.$implicit.$$amount)}))}function bl(l){return t.Pb(0,[(l()(),t.tb(0,0,null,null,2,"th",[["class","mat-header-cell"],["mat-header-cell",""],["role","columnheader"]],null,null,null,null,null)),t.sb(1,16384,null,0,c.e,[a.d,t.k],null,null),(l()(),t.Nb(-1,null,[" Modification Name "]))],null,null)}function cl(l){return t.Pb(0,[(l()(),t.tb(0,0,null,null,1,"span",[],null,null,null,null,null)),(l()(),t.Nb(1,null,[" "," "]))],null,(function(l,n){l(n,1,0,n.parent.context.$implicit.molecularFragment.refPname)}))}function al(l){return t.Pb(0,[(l()(),t.tb(0,0,null,null,3,"td",[["class","mat-cell"],["mat-cell",""],["role","gridcell"]],null,null,null,null,null)),t.sb(1,16384,null,0,c.a,[a.d,t.k],null,null),(l()(),t.jb(16777216,null,null,1,null,cl)),t.sb(3,16384,null,0,o.n,[t.P,t.M],{ngIf:[0,"ngIf"]},null)],(function(l,n){l(n,3,0,n.context.$implicit.molecularFragment)}),null)}function ol(l){return t.Pb(0,[(l()(),t.tb(0,0,null,null,2,"th",[["class","mat-header-cell"],["mat-header-cell",""],["role","columnheader"]],null,null,null,null,null)),t.sb(1,16384,null,0,c.e,[a.d,t.k],null,null),(l()(),t.Nb(-1,null,[" Modification ID "]))],null,null)}function il(l){return t.Pb(0,[(l()(),t.tb(0,0,null,null,4,"span",[],null,null,null,null,null)),(l()(),t.tb(1,0,null,null,3,"a",[["target","_blank"]],[[1,"target",0],[8,"href",4]],[[null,"click"]],(function(l,n,u){var e=!0;return"click"===n&&(e=!1!==t.Fb(l,2).onClick(u.button,u.ctrlKey,u.metaKey,u.shiftKey)&&e),e}),null,null)),t.sb(2,671744,null,0,i.o,[i.m,i.a,o.k],{target:[0,"target"],routerLink:[1,"routerLink"]},null),t.Gb(3,2),(l()(),t.Nb(4,null,["",""]))],(function(l,n){var u=l(n,3,0,"/substances",n.parent.context.$implicit.molecularFragment.refuuid);l(n,2,0,"_blank",u)}),(function(l,n){l(n,1,0,t.Fb(n,2).target,t.Fb(n,2).href),l(n,4,0,n.parent.context.$implicit.molecularFragment.approvalID||n.parent.context.$implicit.molecularFragment.refuuid)}))}function rl(l){return t.Pb(0,[(l()(),t.tb(0,0,null,null,3,"td",[["class","mat-cell"],["mat-cell",""],["role","gridcell"]],null,null,null,null,null)),t.sb(1,16384,null,0,c.a,[a.d,t.k],null,null),(l()(),t.jb(16777216,null,null,1,null,il)),t.sb(3,16384,null,0,o.n,[t.P,t.M],{ngIf:[0,"ngIf"]},null)],(function(l,n){l(n,3,0,n.context.$implicit.molecularFragment)}),null)}function sl(l){return t.Pb(0,[(l()(),t.tb(0,0,null,null,2,"tr",[["class","mat-header-row"],["mat-header-row",""],["role","row"]],null,null,null,r.d,r.a)),t.Kb(6144,null,a.k,null,[c.g]),t.sb(2,49152,null,0,c.g,[],null,null)],null,null)}function ml(l){return t.Pb(0,[(l()(),t.tb(0,0,null,null,2,"tr",[["class","mat-row"],["mat-row",""],["role","row"]],null,null,null,r.e,r.b)),t.Kb(6144,null,a.m,null,[c.i]),t.sb(2,49152,null,0,c.i,[],null,null)],null,null)}function dl(l){return t.Pb(0,[(l()(),t.tb(0,0,null,null,107,"div",[],null,null,null,null,null)),(l()(),t.tb(1,0,null,null,1,"h4",[],null,null,null,null,null)),(l()(),t.Nb(-1,null,["Structural Modifications:"])),(l()(),t.tb(3,0,null,null,104,"div",[["class","responsive"]],null,null,null,null,null)),(l()(),t.tb(4,0,null,null,103,"table",[["class","no-bottom-border mat-table"],["mat-table",""]],null,null,null,r.f,r.c)),t.Kb(6144,null,a.o,null,[c.k]),t.sb(6,2342912,null,4,c.k,[t.s,t.h,t.k,[8,null],[2,s.b],o.d,m.a],{dataSource:[0,"dataSource"]},null),t.Lb(603979776,36,{_contentColumnDefs:1}),t.Lb(603979776,37,{_contentRowDefs:1}),t.Lb(603979776,38,{_contentHeaderRowDefs:1}),t.Lb(603979776,39,{_contentFooterRowDefs:1}),(l()(),t.tb(11,0,null,null,12,null,null,null,null,null,null,null)),t.Kb(6144,null,"MAT_SORT_HEADER_COLUMN_DEF",null,[c.c]),t.sb(13,16384,null,3,c.c,[],{name:[0,"name"]},null),t.Lb(603979776,40,{cell:0}),t.Lb(603979776,41,{headerCell:0}),t.Lb(603979776,42,{footerCell:0}),t.Kb(2048,[[36,4]],a.d,null,[c.c]),(l()(),t.jb(0,null,null,2,null,Q)),t.sb(19,16384,null,0,c.f,[t.M],null,null),t.Kb(2048,[[41,4]],a.j,null,[c.f]),(l()(),t.jb(0,null,null,2,null,W)),t.sb(22,16384,null,0,c.b,[t.M],null,null),t.Kb(2048,[[40,4]],a.b,null,[c.b]),(l()(),t.tb(24,0,null,null,12,null,null,null,null,null,null,null)),t.Kb(6144,null,"MAT_SORT_HEADER_COLUMN_DEF",null,[c.c]),t.sb(26,16384,null,3,c.c,[],{name:[0,"name"]},null),t.Lb(603979776,43,{cell:0}),t.Lb(603979776,44,{headerCell:0}),t.Lb(603979776,45,{footerCell:0}),t.Kb(2048,[[36,4]],a.d,null,[c.c]),(l()(),t.jb(0,null,null,2,null,Y)),t.sb(32,16384,null,0,c.f,[t.M],null,null),t.Kb(2048,[[44,4]],a.j,null,[c.f]),(l()(),t.jb(0,null,null,2,null,V)),t.sb(35,16384,null,0,c.b,[t.M],null,null),t.Kb(2048,[[43,4]],a.b,null,[c.b]),(l()(),t.tb(37,0,null,null,12,null,null,null,null,null,null,null)),t.Kb(6144,null,"MAT_SORT_HEADER_COLUMN_DEF",null,[c.c]),t.sb(39,16384,null,3,c.c,[],{name:[0,"name"]},null),t.Lb(603979776,46,{cell:0}),t.Lb(603979776,47,{headerCell:0}),t.Lb(603979776,48,{footerCell:0}),t.Kb(2048,[[36,4]],a.d,null,[c.c]),(l()(),t.jb(0,null,null,2,null,X)),t.sb(45,16384,null,0,c.f,[t.M],null,null),t.Kb(2048,[[47,4]],a.j,null,[c.f]),(l()(),t.jb(0,null,null,2,null,ll)),t.sb(48,16384,null,0,c.b,[t.M],null,null),t.Kb(2048,[[46,4]],a.b,null,[c.b]),(l()(),t.tb(50,0,null,null,12,null,null,null,null,null,null,null)),t.Kb(6144,null,"MAT_SORT_HEADER_COLUMN_DEF",null,[c.c]),t.sb(52,16384,null,3,c.c,[],{name:[0,"name"]},null),t.Lb(603979776,49,{cell:0}),t.Lb(603979776,50,{headerCell:0}),t.Lb(603979776,51,{footerCell:0}),t.Kb(2048,[[36,4]],a.d,null,[c.c]),(l()(),t.jb(0,null,null,2,null,nl)),t.sb(58,16384,null,0,c.f,[t.M],null,null),t.Kb(2048,[[50,4]],a.j,null,[c.f]),(l()(),t.jb(0,null,null,2,null,ul)),t.sb(61,16384,null,0,c.b,[t.M],null,null),t.Kb(2048,[[49,4]],a.b,null,[c.b]),(l()(),t.tb(63,0,null,null,12,null,null,null,null,null,null,null)),t.Kb(6144,null,"MAT_SORT_HEADER_COLUMN_DEF",null,[c.c]),t.sb(65,16384,null,3,c.c,[],{name:[0,"name"]},null),t.Lb(603979776,52,{cell:0}),t.Lb(603979776,53,{headerCell:0}),t.Lb(603979776,54,{footerCell:0}),t.Kb(2048,[[36,4]],a.d,null,[c.c]),(l()(),t.jb(0,null,null,2,null,tl)),t.sb(71,16384,null,0,c.f,[t.M],null,null),t.Kb(2048,[[53,4]],a.j,null,[c.f]),(l()(),t.jb(0,null,null,2,null,el)),t.sb(74,16384,null,0,c.b,[t.M],null,null),t.Kb(2048,[[52,4]],a.b,null,[c.b]),(l()(),t.tb(76,0,null,null,12,null,null,null,null,null,null,null)),t.Kb(6144,null,"MAT_SORT_HEADER_COLUMN_DEF",null,[c.c]),t.sb(78,16384,null,3,c.c,[],{name:[0,"name"]},null),t.Lb(603979776,55,{cell:0}),t.Lb(603979776,56,{headerCell:0}),t.Lb(603979776,57,{footerCell:0}),t.Kb(2048,[[36,4]],a.d,null,[c.c]),(l()(),t.jb(0,null,null,2,null,bl)),t.sb(84,16384,null,0,c.f,[t.M],null,null),t.Kb(2048,[[56,4]],a.j,null,[c.f]),(l()(),t.jb(0,null,null,2,null,al)),t.sb(87,16384,null,0,c.b,[t.M],null,null),t.Kb(2048,[[55,4]],a.b,null,[c.b]),(l()(),t.tb(89,0,null,null,12,null,null,null,null,null,null,null)),t.Kb(6144,null,"MAT_SORT_HEADER_COLUMN_DEF",null,[c.c]),t.sb(91,16384,null,3,c.c,[],{name:[0,"name"]},null),t.Lb(603979776,58,{cell:0}),t.Lb(603979776,59,{headerCell:0}),t.Lb(603979776,60,{footerCell:0}),t.Kb(2048,[[36,4]],a.d,null,[c.c]),(l()(),t.jb(0,null,null,2,null,ol)),t.sb(97,16384,null,0,c.f,[t.M],null,null),t.Kb(2048,[[59,4]],a.j,null,[c.f]),(l()(),t.jb(0,null,null,2,null,rl)),t.sb(100,16384,null,0,c.b,[t.M],null,null),t.Kb(2048,[[58,4]],a.b,null,[c.b]),(l()(),t.jb(0,null,null,2,null,sl)),t.sb(103,540672,null,0,c.h,[t.M,t.s],{columns:[0,"columns"]},null),t.Kb(2048,[[38,4]],a.l,null,[c.h]),(l()(),t.jb(0,null,null,2,null,ml)),t.sb(106,540672,null,0,c.j,[t.M,t.s],{columns:[0,"columns"]},null),t.Kb(2048,[[37,4]],a.n,null,[c.j])],(function(l,n){var u=n.component;l(n,6,0,u.structural),l(n,13,0,"Modification Type"),l(n,26,0,"Location Site"),l(n,39,0,"Location Type"),l(n,52,0,"Residue Modified"),l(n,65,0,"Extent"),l(n,78,0,"Modification Name"),l(n,91,0,"Modification ID"),l(n,103,0,u.structuralColumns),l(n,106,0,u.structuralColumns)}),null)}function fl(l){return t.Pb(0,[(l()(),t.jb(16777216,null,null,1,null,T)),t.sb(1,16384,null,0,o.n,[t.P,t.M],{ngIf:[0,"ngIf"]},null),(l()(),t.tb(2,0,null,null,0,"br",[],null,null,null,null,null)),(l()(),t.jb(16777216,null,null,1,null,J)),t.sb(4,16384,null,0,o.n,[t.P,t.M],{ngIf:[0,"ngIf"]},null),(l()(),t.tb(5,0,null,null,0,"br",[],null,null,null,null,null)),(l()(),t.jb(16777216,null,null,1,null,dl)),t.sb(7,16384,null,0,o.n,[t.P,t.M],{ngIf:[0,"ngIf"]},null)],(function(l,n){var u=n.component;l(n,1,0,u.agent),l(n,4,0,u.physical),l(n,7,0,u.structural)}),null)}function pl(l){return t.Pb(0,[(l()(),t.tb(0,0,null,null,1,"app-substance-modifications",[],null,null,null,fl,g)),t.sb(1,114688,null,0,M,[h.a],null,null)],(function(l,n){l(n,1,0)}),null)}var Ml=t.pb("app-substance-modifications",M,pl,{},{countUpdate:"countUpdate"},[]),hl=u("EtvR"),gl=u("Wf4p"),Kl=u("ZYjt"),_l=u("6jyQ");u.d(n,"SubstanceModificationsModuleNgFactory",(function(){return Ll}));var Ll=t.qb(e,[],(function(l){return t.Cb([t.Db(512,t.j,t.bb,[[8,[b.a,Ml]],[3,t.j],t.x]),t.Db(4608,o.p,o.o,[t.u,[2,o.G]]),t.Db(1073742336,o.c,o.c,[]),t.Db(1073742336,hl.a,hl.a,[]),t.Db(1073742336,a.p,a.p,[]),t.Db(1073742336,s.a,s.a,[]),t.Db(1073742336,gl.n,gl.n,[[2,gl.f],[2,Kl.f]]),t.Db(1073742336,c.m,c.m,[]),t.Db(1073742336,i.p,i.p,[[2,i.u],[2,i.m]]),t.Db(1073742336,e,e,[]),t.Db(1024,i.j,(function(){return[[]]}),[]),t.Db(256,_l.a,M,[])])}))}}]);
//# sourceMappingURL=40.f579a8f1ed1ec526c0fe.js.map