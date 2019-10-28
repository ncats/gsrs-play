(window.webpackJsonp=window.webpackJsonp||[]).push([[0],{Pp4P:function(l,n,e){"use strict";e.d(n,"a",function(){return t}),e("CQqH");var u=e("Ip0R"),t=function(){function l(l){this.substanceService=l,this.matchedRef=[],this.displayedColumns=["index","citation","docType","tags","lastEdited"]}return l.prototype.ngOnInit=function(){var l=this;if(this.substance)this.subRef=this.substance.references;else if(this.subUUID)var n=this.substanceService.getSubstanceDetails(this.subUUID).subscribe(function(e){e&&(l.substance=e,l.subRef=l.substance.references),n.unsubscribe()},function(l){n.unsubscribe()});this.subRef&&this.compileReferences()},l.prototype.compileReferences=function(){var l=this;this.substance.references&&this.substance.references.forEach(function(n){l.references.indexOf(n.uuid)>-1&&l.matchedRef.push(n)})},l.prototype.convertTimestamp=function(l){return new u.e("en-US").transform(l,"MMM dd, yyyy")},l.prototype.getParentIndex=function(l){return this.subRef.indexOf(l)+1},l}()},TtEo:function(l,n,e){"use strict";e.d(n,"a",function(){return t}),e.d(n,"b",function(){return a});var u=e("CcnG"),t=(e("LC5p"),e("Ip0R"),e("Fzqc"),e("Wf4p"),u.Sa({encapsulation:2,styles:[".mat-divider{display:block;margin:0;border-top-width:1px;border-top-style:solid}.mat-divider.mat-divider-vertical{border-top:0;border-right-width:1px;border-right-style:solid}.mat-divider.mat-divider-inset{margin-left:80px}[dir=rtl] .mat-divider.mat-divider-inset{margin-left:auto;margin-right:80px}"],data:{}}));function a(l){return u.ob(2,[],null,null)}},Z16F:function(l,n,e){"use strict";e.d(n,"a",function(){return t});var u=e("CcnG"),t=(e("CQqH"),function(){function l(l){this.substanceService=l,this.selectionUpdated=new u.o,this.placeholder="Search",this.hintMessage="",this.header="Substance"}return l.prototype.ngOnInit=function(){},Object.defineProperty(l.prototype,"subuuid",{set:function(l){var n=this;l&&this.substanceService.getSubstanceSummary(l).subscribe(function(l){n.selectedSubstance=l})},enumerable:!0,configurable:!0}),l.prototype.processSubstanceSearch=function(l){var n=this;void 0===l&&(l="");var e=l.replace('"',"");this.substanceService.getSubstanceSummaries('root_names_name:"^'+e+'$" OR root_approvalID:"^'+e+'$" OR root_codes_BDNUM:"^'+e+'$"',!0).subscribe(function(l){l.content&&l.content.length?(n.selectedSubstance=l.content[0],n.selectionUpdated.emit(n.selectedSubstance),n.errorMessage=""):n.errorMessage="No substances found"})},l.prototype.editSelectedSubstance=function(){this.selectedSubstance=null,this.selectionUpdated.emit(this.selectedSubstance)},l}())},"a+Vh":function(l,n,e){"use strict";e("jTpw"),e("Pay0"),e("xwLN"),e("6E2U")},nahG:function(l,n,e){"use strict";var u=e("CcnG"),t=e("BHnd"),a=e("y4qS"),c=e("Ip0R"),i=e("pIm3"),o=e("Fzqc");e("Pp4P"),e("CQqH"),e.d(n,"a",function(){return r}),e.d(n,"b",function(){return x});var r=u.Sa({encapsulation:0,styles:[[".mat-row[_ngcontent-%COMP%]:nth-child(odd){background-color:#f9f9f9}"]],data:{}});function s(l){return u.ob(0,[(l()(),u.Ua(0,0,null,null,2,"th",[["class","mat-header-cell"],["mat-header-cell",""],["role","columnheader"]],null,null,null,null,null)),u.Ta(1,16384,null,0,t.e,[a.d,u.l],null,null),(l()(),u.mb(-1,null,[" Index "]))],null,null)}function d(l){return u.ob(0,[(l()(),u.Ua(0,0,null,null,2,"td",[["class","mat-cell"],["mat-cell",""],["role","gridcell"]],null,null,null,null,null)),u.Ta(1,16384,null,0,t.a,[a.d,u.l],null,null),(l()(),u.mb(2,null,[""," "]))],null,function(l,n){l(n,2,0,n.component.getParentIndex(n.context.$implicit))})}function b(l){return u.ob(0,[(l()(),u.Ua(0,0,null,null,2,"th",[["class","mat-header-cell"],["mat-header-cell",""],["role","columnheader"]],null,null,null,null,null)),u.Ta(1,16384,null,0,t.e,[a.d,u.l],null,null),(l()(),u.mb(-1,null,[" Source Text / Citation "]))],null,null)}function f(l){return u.ob(0,[(l()(),u.Ua(0,0,null,null,1,"a",[["appTrackLinkEvent",""],["evCategory","substanceOverview"],["target","_blank"]],[[8,"href",4]],null,null,null,null)),(l()(),u.mb(1,null,["",""]))],null,function(l,n){l(n,0,0,n.parent.context.$implicit.url),l(n,1,0,n.parent.context.$implicit.citation)})}function m(l){return u.ob(0,[(l()(),u.Ua(0,0,null,null,1,"span",[],null,null,null,null,null)),(l()(),u.mb(1,null,["",""]))],null,function(l,n){l(n,1,0,n.parent.context.$implicit.citation)})}function p(l){return u.ob(0,[(l()(),u.Ua(0,0,null,null,5,"td",[["class","mat-cell"],["mat-cell",""],["role","gridcell"]],null,null,null,null,null)),u.Ta(1,16384,null,0,t.a,[a.d,u.l],null,null),(l()(),u.Ma(16777216,null,null,1,null,f)),u.Ta(3,16384,null,0,c.l,[u.U,u.R],{ngIf:[0,"ngIf"]},null),(l()(),u.Ma(16777216,null,null,1,null,m)),u.Ta(5,16384,null,0,c.l,[u.U,u.R],{ngIf:[0,"ngIf"]},null)],function(l,n){l(n,3,0,n.context.$implicit.url),l(n,5,0,!n.context.$implicit.url)},null)}function h(l){return u.ob(0,[(l()(),u.Ua(0,0,null,null,2,"th",[["class","mat-header-cell"],["mat-header-cell",""],["role","columnheader"]],null,null,null,null,null)),u.Ta(1,16384,null,0,t.e,[a.d,u.l],null,null),(l()(),u.mb(-1,null,[" Source Type "]))],null,null)}function g(l){return u.ob(0,[(l()(),u.Ua(0,0,null,null,2,"td",[["class","mat-cell"],["mat-cell",""],["role","gridcell"]],null,null,null,null,null)),u.Ta(1,16384,null,0,t.a,[a.d,u.l],null,null),(l()(),u.mb(2,null,[" "," "]))],null,function(l,n){l(n,2,0,n.context.$implicit.docType)})}function v(l){return u.ob(0,[(l()(),u.Ua(0,0,null,null,2,"th",[["class","mat-header-cell"],["mat-header-cell",""],["role","columnheader"]],null,null,null,null,null)),u.Ta(1,16384,null,0,t.e,[a.d,u.l],null,null),(l()(),u.mb(-1,null,[" Tags "]))],null,null)}function T(l){return u.ob(0,[(l()(),u.Ua(0,0,null,null,1,"span",[["class","badge"]],null,null,null,null,null)),(l()(),u.mb(1,null,[" "," "]))],null,function(l,n){l(n,1,0,n.context.$implicit)})}function S(l){return u.ob(0,[(l()(),u.Ua(0,0,null,null,3,"td",[["class","mat-cell"],["mat-cell",""],["role","gridcell"]],null,null,null,null,null)),u.Ta(1,16384,null,0,t.a,[a.d,u.l],null,null),(l()(),u.Ma(16777216,null,null,1,null,T)),u.Ta(3,278528,null,0,c.k,[u.U,u.R,u.w],{ngForOf:[0,"ngForOf"]},null)],function(l,n){l(n,3,0,n.context.$implicit.tags)},null)}function U(l){return u.ob(0,[(l()(),u.Ua(0,0,null,null,2,"th",[["class","mat-header-cell"],["mat-header-cell",""],["role","columnheader"]],null,null,null,null,null)),u.Ta(1,16384,null,0,t.e,[a.d,u.l],null,null),(l()(),u.mb(-1,null,[" Date Accessed "]))],null,null)}function C(l){return u.ob(0,[(l()(),u.Ua(0,0,null,null,2,"td",[["class","mat-cell"],["mat-cell",""],["role","gridcell"]],null,null,null,null,null)),u.Ta(1,16384,null,0,t.a,[a.d,u.l],null,null),(l()(),u.mb(2,null,[" "," "]))],null,function(l,n){l(n,2,0,n.component.convertTimestamp(n.context.$implicit.lastEdited))})}function y(l){return u.ob(0,[(l()(),u.Ua(0,0,null,null,2,"tr",[["class","mat-header-row"],["mat-header-row",""],["role","row"]],null,null,null,i.d,i.a)),u.jb(6144,null,a.k,null,[t.g]),u.Ta(2,49152,null,0,t.g,[],null,null)],null,null)}function w(l){return u.ob(0,[(l()(),u.Ua(0,0,null,null,2,"tr",[["class","mat-row"],["mat-row",""],["role","row"]],null,null,null,i.e,i.b)),u.jb(6144,null,a.m,null,[t.i]),u.Ta(2,49152,null,0,t.i,[],null,null)],null,null)}function x(l){return u.ob(0,[(l()(),u.Ua(0,0,null,null,72,"table",[["class","mat-table"],["mat-table",""]],null,null,null,i.f,i.c)),u.Ta(1,2342912,null,4,t.k,[u.w,u.i,u.l,[8,null],[2,o.b]],{dataSource:[0,"dataSource"]},null),u.kb(603979776,1,{_contentColumnDefs:1}),u.kb(603979776,2,{_contentRowDefs:1}),u.kb(603979776,3,{_contentHeaderRowDefs:1}),u.kb(603979776,4,{_contentFooterRowDefs:1}),(l()(),u.Ua(6,0,null,null,11,null,null,null,null,null,null,null)),u.Ta(7,16384,null,3,t.c,[],{name:[0,"name"]},null),u.kb(335544320,5,{cell:0}),u.kb(335544320,6,{headerCell:0}),u.kb(335544320,7,{footerCell:0}),u.jb(2048,[[1,4]],a.d,null,[t.c]),(l()(),u.Ma(0,null,null,2,null,s)),u.Ta(13,16384,null,0,t.f,[u.R],null,null),u.jb(2048,[[6,4]],a.j,null,[t.f]),(l()(),u.Ma(0,null,null,2,null,d)),u.Ta(16,16384,null,0,t.b,[u.R],null,null),u.jb(2048,[[5,4]],a.b,null,[t.b]),(l()(),u.Ua(18,0,null,null,11,null,null,null,null,null,null,null)),u.Ta(19,16384,null,3,t.c,[],{name:[0,"name"]},null),u.kb(335544320,8,{cell:0}),u.kb(335544320,9,{headerCell:0}),u.kb(335544320,10,{footerCell:0}),u.jb(2048,[[1,4]],a.d,null,[t.c]),(l()(),u.Ma(0,null,null,2,null,b)),u.Ta(25,16384,null,0,t.f,[u.R],null,null),u.jb(2048,[[9,4]],a.j,null,[t.f]),(l()(),u.Ma(0,null,null,2,null,p)),u.Ta(28,16384,null,0,t.b,[u.R],null,null),u.jb(2048,[[8,4]],a.b,null,[t.b]),(l()(),u.Ua(30,0,null,null,11,null,null,null,null,null,null,null)),u.Ta(31,16384,null,3,t.c,[],{name:[0,"name"]},null),u.kb(335544320,11,{cell:0}),u.kb(335544320,12,{headerCell:0}),u.kb(335544320,13,{footerCell:0}),u.jb(2048,[[1,4]],a.d,null,[t.c]),(l()(),u.Ma(0,null,null,2,null,h)),u.Ta(37,16384,null,0,t.f,[u.R],null,null),u.jb(2048,[[12,4]],a.j,null,[t.f]),(l()(),u.Ma(0,null,null,2,null,g)),u.Ta(40,16384,null,0,t.b,[u.R],null,null),u.jb(2048,[[11,4]],a.b,null,[t.b]),(l()(),u.Ua(42,0,null,null,11,null,null,null,null,null,null,null)),u.Ta(43,16384,null,3,t.c,[],{name:[0,"name"]},null),u.kb(335544320,14,{cell:0}),u.kb(335544320,15,{headerCell:0}),u.kb(335544320,16,{footerCell:0}),u.jb(2048,[[1,4]],a.d,null,[t.c]),(l()(),u.Ma(0,null,null,2,null,v)),u.Ta(49,16384,null,0,t.f,[u.R],null,null),u.jb(2048,[[15,4]],a.j,null,[t.f]),(l()(),u.Ma(0,null,null,2,null,S)),u.Ta(52,16384,null,0,t.b,[u.R],null,null),u.jb(2048,[[14,4]],a.b,null,[t.b]),(l()(),u.Ua(54,0,null,null,11,null,null,null,null,null,null,null)),u.Ta(55,16384,null,3,t.c,[],{name:[0,"name"]},null),u.kb(335544320,17,{cell:0}),u.kb(335544320,18,{headerCell:0}),u.kb(335544320,19,{footerCell:0}),u.jb(2048,[[1,4]],a.d,null,[t.c]),(l()(),u.Ma(0,null,null,2,null,U)),u.Ta(61,16384,null,0,t.f,[u.R],null,null),u.jb(2048,[[18,4]],a.j,null,[t.f]),(l()(),u.Ma(0,null,null,2,null,C)),u.Ta(64,16384,null,0,t.b,[u.R],null,null),u.jb(2048,[[17,4]],a.b,null,[t.b]),(l()(),u.Ua(66,0,null,null,6,"tbody",[],null,null,null,null,null)),(l()(),u.Ma(0,null,null,2,null,y)),u.Ta(68,540672,null,0,t.h,[u.R,u.w],{columns:[0,"columns"]},null),u.jb(2048,[[3,4]],a.l,null,[t.h]),(l()(),u.Ma(0,null,null,2,null,w)),u.Ta(71,540672,null,0,t.j,[u.R,u.w],{columns:[0,"columns"]},null),u.jb(2048,[[2,4]],a.n,null,[t.j])],function(l,n){var e=n.component;l(n,1,0,e.matchedRef),l(n,7,0,"index"),l(n,19,0,"citation"),l(n,31,0,"docType"),l(n,43,0,"tags"),l(n,55,0,"lastEdited"),l(n,68,0,e.displayedColumns),l(n,71,0,e.displayedColumns)},null)}},v0ZX:function(l,n,e){"use strict";var u=e("CcnG"),t=e("4vFK"),a=e("9X7/"),c=e("6E2U"),i=e("HECD"),o=e("bujt"),r=e("UodH"),s=e("dWZg"),d=e("lLAP"),b=e("wFw1"),f=e("Mr+X"),m=e("SMsm"),p=e("ZYCi"),h=e("Ip0R");e("Z16F"),e("CQqH"),e.d(n,"a",function(){return g}),e.d(n,"b",function(){return U});var g=u.Sa({encapsulation:0,styles:[[".selected-substance-container[_ngcontent-%COMP%]{max-width:100%;width:100%}.selected-substance[_ngcontent-%COMP%]{display:flex;flex-direction:column;text-align:center;position:relative}.selected-substance[_ngcontent-%COMP%]   img[_ngcontent-%COMP%]{width:100%;height:auto;display:block;max-width:200px}.selected-substance-options[_ngcontent-%COMP%]{position:absolute;right:5px;top:15px;display:flex;flex-direction:column}.selected-substance-options[_ngcontent-%COMP%]   .mat-mini-fab[_ngcontent-%COMP%]{background-color:rgba(242,242,242,.85);color:#404040;width:35px;height:35px}.selected-substance-options[_ngcontent-%COMP%]   .mat-mini-fab[_ngcontent-%COMP%]:not(:first-child){margin-top:3px}.selected-substance-options[_ngcontent-%COMP%]   .mat-mini-fab[_ngcontent-%COMP%]     .mat-button-wrapper{padding:0}"]],data:{}});function v(l){return u.ob(0,[(l()(),u.Ua(0,0,null,null,1,"app-substance-text-search",[],null,[[null,"searchPerformed"]],function(l,n,e){var u=!0;return"searchPerformed"===n&&(u=!1!==l.component.processSubstanceSearch(e)&&u),u},t.b,t.a)),u.Ta(1,4440064,null,0,a.a,[c.a,u.l,i.a],{eventCategory:[0,"eventCategory"],placeholder:[1,"placeholder"]},{searchPerformed:"searchPerformed"})],function(l,n){var e=n.component;l(n,1,0,e.eventCategory,e.placeholder)},null)}function T(l){return u.ob(0,[(l()(),u.Ua(0,0,null,null,10,"div",[["class","selected-substance-options"]],null,null,null,null,null)),(l()(),u.Ua(1,0,null,null,3,"button",[["mat-mini-fab",""]],[[8,"disabled",0],[2,"_mat-animation-noopable",null]],[[null,"click"]],function(l,n,e){var u=!0;return"click"===n&&(u=!1!==l.component.editSelectedSubstance()&&u),u},o.d,o.b)),u.Ta(2,180224,null,0,r.b,[u.l,s.a,d.f,[2,b.a]],null,null),(l()(),u.Ua(3,0,null,0,1,"mat-icon",[["aria-label","Edit Selected Substance"],["class","mat-icon"],["role","img"],["svgIcon","edit"]],[[2,"mat-icon-inline",null]],null,null,f.b,f.a)),u.Ta(4,638976,null,0,m.a,[u.l,m.c,[8,null]],{svgIcon:[0,"svgIcon"]},null),(l()(),u.Ua(5,0,null,null,5,"a",[["mat-mini-fab",""],["target","_blank"]],[[1,"tabindex",0],[1,"disabled",0],[1,"aria-disabled",0],[2,"_mat-animation-noopable",null],[1,"target",0],[8,"href",4]],[[null,"click"]],function(l,n,e){var t=!0;return"click"===n&&(t=!1!==u.eb(l,6)._haltDisabledEvents(e)&&t),"click"===n&&(t=!1!==u.eb(l,7).onClick(e.button,e.ctrlKey,e.metaKey,e.shiftKey)&&t),t},o.c,o.a)),u.Ta(6,180224,null,0,r.a,[s.a,d.f,u.l,[2,b.a]],null,null),u.Ta(7,671744,null,0,p.o,[p.m,p.a,h.i],{target:[0,"target"],routerLink:[1,"routerLink"]},null),u.fb(8,2),(l()(),u.Ua(9,0,null,0,1,"mat-icon",[["aria-label","Open in new tab"],["class","mat-icon"],["role","img"],["svgIcon","open_in_new"]],[[2,"mat-icon-inline",null]],null,null,f.b,f.a)),u.Ta(10,638976,null,0,m.a,[u.l,m.c,[8,null]],{svgIcon:[0,"svgIcon"]},null)],function(l,n){var e=n.component;l(n,4,0,"edit"),l(n,7,0,"_blank",l(n,8,0,"/substances",e.selectedSubstance.uuid)),l(n,10,0,"open_in_new")},function(l,n){l(n,1,0,u.eb(n,2).disabled||null,"NoopAnimations"===u.eb(n,2)._animationMode),l(n,3,0,u.eb(n,4).inline),l(n,5,0,u.eb(n,6).disabled?-1:u.eb(n,6).tabIndex||0,u.eb(n,6).disabled||null,u.eb(n,6).disabled.toString(),"NoopAnimations"===u.eb(n,6)._animationMode,u.eb(n,7).target,u.eb(n,7).href),l(n,9,0,u.eb(n,10).inline)})}function S(l){return u.ob(0,[(l()(),u.Ua(0,0,null,null,7,"div",[["class","selected-substance"]],null,[[null,"mouseenter"],[null,"mouseleave"]],function(l,n,e){var u=!0,t=l.component;return"mouseenter"===n&&(u=0!=(t.showOptions=!0)&&u),"mouseleave"===n&&(u=0!=(t.showOptions=!1)&&u),u},null,null)),(l()(),u.Ma(16777216,null,null,1,null,T)),u.Ta(2,16384,null,0,h.l,[u.U,u.R],{ngIf:[0,"ngIf"]},null),(l()(),u.Ua(3,0,null,null,1,"div",[["class","section-header"]],null,null,null,null,null)),(l()(),u.mb(4,null,["",""])),(l()(),u.Ua(5,0,null,null,0,"img",[],[[8,"src",4]],null,null,null,null)),(l()(),u.Ua(6,0,null,null,1,"div",[],null,null,null,null,null)),(l()(),u.mb(7,null,[" "," "]))],function(l,n){l(n,2,0,n.component.showOptions)},function(l,n){var e=n.component;l(n,4,0,e.header),l(n,5,0,e.substanceService.getSafeIconImgUrl(e.selectedSubstance)),l(n,7,0,e.selectedSubstance._name)})}function U(l){return u.ob(0,[(l()(),u.Ua(0,0,null,null,4,"div",[["class","substance-selector-container"]],null,null,null,null,null)),(l()(),u.Ma(16777216,null,null,1,null,v)),u.Ta(2,16384,null,0,h.l,[u.U,u.R],{ngIf:[0,"ngIf"]},null),(l()(),u.Ma(16777216,null,null,1,null,S)),u.Ta(4,16384,null,0,h.l,[u.U,u.R],{ngIf:[0,"ngIf"]},null)],function(l,n){var e=n.component;l(n,2,0,!e.selectedSubstance),l(n,4,0,e.selectedSubstance)},null)}},xhaW:function(l,n,e){"use strict";e.d(n,"a",function(){return i});var u=e("mrSG"),t=e("bBkp"),a=e("gIcY"),c=e("AytR"),i=function(l){function n(n){var e=l.call(this)||this;return e.gaService=n,e.page=0,e.pageSize=5,e.searchControl=new a.f,e}return Object(u.a)(n,l),n.prototype.pageChange=function(l,n){if(null!=l){var e=void 0,u=void 0;this.pageSize!==l.pageSize?(e="select:page-size",u=l.pageSize):this.page!==l.pageIndex&&(e="icon-button:page-number",u=l.pageIndex+1),this.gaService.sendEvent(n,e,"pager",u),this.page=l.pageIndex,this.pageSize=l.pageSize}this.paged=[];for(var t=this.page*this.pageSize,a=t;a<t+this.pageSize&&null!=this.filtered[a];a++)this.paged.push(this.filtered[a])},n.prototype.filterList=function(l,n,e){var u=this;void 0===e&&(e="substance card"),null!=this.searchTimer&&clearTimeout(this.searchTimer),this.searchTimer=setTimeout(function(){u.gaService.sendEvent(e,"search",!c.a.isAnalyticsPrivate&&l||"input value"),u.filtered=[],n.forEach(function(n){JSON.stringify(n).toLowerCase().indexOf(l.toLowerCase())>-1&&u.filtered.push(n)}),clearTimeout(u.searchTimer),u.searchTimer=null,u.page=0,u.pageChange()},700)},n}(t.a)}}]);