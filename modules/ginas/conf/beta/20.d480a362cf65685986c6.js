(window.webpackJsonp=window.webpackJsonp||[]).push([[20],{EfhQ:function(l,n,u){"use strict";u.d(n,"a",function(){return e});var t=u("CcnG"),e=function(){return function(){this.countUpdate=new t.o}}()},gCRT:function(l,n,u){"use strict";u.r(n);var t=u("CcnG"),e=function(){},a=u("NcP4"),c=u("BHnd"),o=u("y4qS"),b=u("pIm3"),s=u("Fzqc"),r=u("mrSG"),i=u("EfhQ"),d=u("K9Ia"),m=function(l){function n(){var n=l.call(this)||this;return n.displayedColumns=["Sugar","Site Range","Site Count"],n.substanceUpdated=new d.a,n}return Object(r.a)(n,l),n.prototype.ngOnInit=function(){var l=this;this.substanceUpdated.subscribe(function(n){l.substance=n,null!=l.substance&&null!=l.substance.nucleicAcid&&null!=l.substance.nucleicAcid.sugars&&l.substance.nucleicAcid.sugars.length&&(l.sugars=l.substance.nucleicAcid.sugars,l.countUpdate.emit(l.sugars.length),l.getTotalSites())})},n.prototype.getTotalSites=function(){this.siteCount=0;for(var l=0,n=this.sugars;l<n.length;l++)this.siteCount=this.siteCount+n[l].sites.length},n.prototype.getSiteCount=function(l){return l.length+"/"+this.siteCount},n}(i.a),f=t.Sa({encapsulation:0,styles:[[""]],data:{}});function h(l){return t.ob(0,[(l()(),t.Ua(0,0,null,null,2,"th",[["class","mat-header-cell"],["mat-header-cell",""],["role","columnheader"]],null,null,null,null,null)),t.Ta(1,16384,null,0,c.e,[o.d,t.l],null,null),(l()(),t.mb(-1,null,[" Sugar "]))],null,null)}function g(l){return t.ob(0,[(l()(),t.Ua(0,0,null,null,2,"td",[["class","mat-cell"],["mat-cell",""],["role","gridcell"]],null,null,null,null,null)),t.Ta(1,16384,null,0,c.a,[o.d,t.l],null,null),(l()(),t.mb(2,null,[" "," "]))],null,function(l,n){l(n,2,0,n.context.$implicit.sugar)})}function p(l){return t.ob(0,[(l()(),t.Ua(0,0,null,null,2,"th",[["class","mat-header-cell"],["mat-header-cell",""],["role","columnheader"]],null,null,null,null,null)),t.Ta(1,16384,null,0,c.e,[o.d,t.l],null,null),(l()(),t.mb(-1,null,[" Site Range "]))],null,null)}function T(l){return t.ob(0,[(l()(),t.Ua(0,0,null,null,2,"td",[["class","mat-cell"],["mat-cell",""],["role","gridcell"]],null,null,null,null,null)),t.Ta(1,16384,null,0,c.a,[o.d,t.l],null,null),(l()(),t.mb(2,null,[" "," "]))],null,function(l,n){l(n,2,0,n.context.$implicit.sitesShorthand)})}function k(l){return t.ob(0,[(l()(),t.Ua(0,0,null,null,2,"th",[["class","mat-header-cell"],["mat-header-cell",""],["role","columnheader"]],null,null,null,null,null)),t.Ta(1,16384,null,0,c.e,[o.d,t.l],null,null),(l()(),t.mb(-1,null,[" Site Count "]))],null,null)}function C(l){return t.ob(0,[(l()(),t.Ua(0,0,null,null,2,"td",[["class","mat-cell"],["mat-cell",""],["role","gridcell"]],null,null,null,null,null)),t.Ta(1,16384,null,0,c.a,[o.d,t.l],null,null),(l()(),t.mb(2,null,[" "," "]))],null,function(l,n){l(n,2,0,n.component.getSiteCount(n.context.$implicit.sites))})}function w(l){return t.ob(0,[(l()(),t.Ua(0,0,null,null,2,"tr",[["class","mat-header-row"],["mat-header-row",""],["role","row"]],null,null,null,b.d,b.a)),t.jb(6144,null,o.k,null,[c.g]),t.Ta(2,49152,null,0,c.g,[],null,null)],null,null)}function S(l){return t.ob(0,[(l()(),t.Ua(0,0,null,null,2,"tr",[["class","mat-row"],["mat-row",""],["role","row"]],null,null,null,b.e,b.b)),t.jb(6144,null,o.m,null,[c.i]),t.Ta(2,49152,null,0,c.i,[],null,null)],null,null)}function U(l){return t.ob(0,[(l()(),t.Ua(0,0,null,null,49,"div",[["class","responsive"]],null,null,null,null,null)),(l()(),t.Ua(1,0,null,null,48,"table",[["class","mat-table"],["mat-table",""]],null,null,null,b.f,b.c)),t.Ta(2,2342912,null,4,c.k,[t.w,t.i,t.l,[8,null],[2,s.b]],{dataSource:[0,"dataSource"]},null),t.kb(603979776,1,{_contentColumnDefs:1}),t.kb(603979776,2,{_contentRowDefs:1}),t.kb(603979776,3,{_contentHeaderRowDefs:1}),t.kb(603979776,4,{_contentFooterRowDefs:1}),(l()(),t.Ua(7,0,null,null,11,null,null,null,null,null,null,null)),t.Ta(8,16384,null,3,c.c,[],{name:[0,"name"]},null),t.kb(335544320,5,{cell:0}),t.kb(335544320,6,{headerCell:0}),t.kb(335544320,7,{footerCell:0}),t.jb(2048,[[1,4]],o.d,null,[c.c]),(l()(),t.Ma(0,null,null,2,null,h)),t.Ta(14,16384,null,0,c.f,[t.R],null,null),t.jb(2048,[[6,4]],o.j,null,[c.f]),(l()(),t.Ma(0,null,null,2,null,g)),t.Ta(17,16384,null,0,c.b,[t.R],null,null),t.jb(2048,[[5,4]],o.b,null,[c.b]),(l()(),t.Ua(19,0,null,null,11,null,null,null,null,null,null,null)),t.Ta(20,16384,null,3,c.c,[],{name:[0,"name"]},null),t.kb(335544320,8,{cell:0}),t.kb(335544320,9,{headerCell:0}),t.kb(335544320,10,{footerCell:0}),t.jb(2048,[[1,4]],o.d,null,[c.c]),(l()(),t.Ma(0,null,null,2,null,p)),t.Ta(26,16384,null,0,c.f,[t.R],null,null),t.jb(2048,[[9,4]],o.j,null,[c.f]),(l()(),t.Ma(0,null,null,2,null,T)),t.Ta(29,16384,null,0,c.b,[t.R],null,null),t.jb(2048,[[8,4]],o.b,null,[c.b]),(l()(),t.Ua(31,0,null,null,11,null,null,null,null,null,null,null)),t.Ta(32,16384,null,3,c.c,[],{name:[0,"name"]},null),t.kb(335544320,11,{cell:0}),t.kb(335544320,12,{headerCell:0}),t.kb(335544320,13,{footerCell:0}),t.jb(2048,[[1,4]],o.d,null,[c.c]),(l()(),t.Ma(0,null,null,2,null,k)),t.Ta(38,16384,null,0,c.f,[t.R],null,null),t.jb(2048,[[12,4]],o.j,null,[c.f]),(l()(),t.Ma(0,null,null,2,null,C)),t.Ta(41,16384,null,0,c.b,[t.R],null,null),t.jb(2048,[[11,4]],o.b,null,[c.b]),(l()(),t.Ua(43,0,null,null,6,"tbody",[],null,null,null,null,null)),(l()(),t.Ma(0,null,null,2,null,w)),t.Ta(45,540672,null,0,c.h,[t.R,t.w],{columns:[0,"columns"]},null),t.jb(2048,[[3,4]],o.l,null,[c.h]),(l()(),t.Ma(0,null,null,2,null,S)),t.Ta(48,540672,null,0,c.j,[t.R,t.w],{columns:[0,"columns"]},null),t.jb(2048,[[2,4]],o.n,null,[c.j])],function(l,n){var u=n.component;l(n,2,0,u.sugars),l(n,8,0,"Sugar"),l(n,20,0,"Site Range"),l(n,32,0,"Site Count"),l(n,45,0,u.displayedColumns),l(n,48,0,u.displayedColumns)},null)}var j=t.Qa("app-substance-na-sugars",m,function(l){return t.ob(0,[(l()(),t.Ua(0,0,null,null,1,"app-substance-na-sugars",[],null,null,null,U,f)),t.Ta(1,114688,null,0,m,[],null,null)],function(l,n){l(n,1,0)},null)},{},{countUpdate:"countUpdate"},[]),R=u("Ip0R"),y=u("eDkP"),v=u("M2Lx"),M=u("uGex"),D=u("v9Dh"),A=u("4epT"),x=u("Wf4p"),G=u("EtvR"),H=u("dWZg"),I=u("UodH"),P=u("4c35"),Q=u("qAlS"),_=u("seP3"),q=u("lLAP"),E=u("/VYK"),F=u("b716"),N=u("ZYCi"),$=u("6jyQ");u.d(n,"SubstanceNaSugarsModuleNgFactory",function(){return B});var B=t.Ra(e,[],function(l){return t.bb([t.cb(512,t.k,t.Ha,[[8,[a.a,j]],[3,t.k],t.B]),t.cb(4608,R.n,R.m,[t.y,[2,R.y]]),t.cb(4608,y.c,y.c,[y.i,y.e,t.k,y.h,y.f,t.u,t.D,R.d,s.b]),t.cb(5120,y.j,y.k,[y.c]),t.cb(4608,v.c,v.c,[]),t.cb(5120,M.a,M.b,[y.c]),t.cb(5120,D.b,D.c,[y.c]),t.cb(5120,A.c,A.a,[[3,A.c]]),t.cb(4608,x.d,x.d,[]),t.cb(1073742336,R.c,R.c,[]),t.cb(1073742336,G.a,G.a,[]),t.cb(1073742336,o.p,o.p,[]),t.cb(1073742336,s.a,s.a,[]),t.cb(1073742336,x.n,x.n,[[2,x.f]]),t.cb(1073742336,c.m,c.m,[]),t.cb(1073742336,H.b,H.b,[]),t.cb(1073742336,x.y,x.y,[]),t.cb(1073742336,I.c,I.c,[]),t.cb(1073742336,P.g,P.g,[]),t.cb(1073742336,Q.b,Q.b,[]),t.cb(1073742336,y.g,y.g,[]),t.cb(1073742336,x.w,x.w,[]),t.cb(1073742336,x.t,x.t,[]),t.cb(1073742336,v.d,v.d,[]),t.cb(1073742336,_.e,_.e,[]),t.cb(1073742336,M.d,M.d,[]),t.cb(1073742336,q.a,q.a,[]),t.cb(1073742336,D.e,D.e,[]),t.cb(1073742336,A.d,A.d,[]),t.cb(1073742336,E.c,E.c,[]),t.cb(1073742336,F.b,F.b,[]),t.cb(1073742336,e,e,[]),t.cb(1024,N.k,function(){return[[]]},[]),t.cb(256,$.a,m,[])])})}}]);