/*
 * To display the correct resources path, check the location.hostname.
 * If on our Dev or Staging servers, set to "../web_resources/OWH/",
 * else set to "../resources/".
 * KD - 20121018   
 *
*/
	var host = location.hostname;
	var rxsnavbase = "/resources/";	

	
/*
 * Superfish v1.4.8 - jQuery menu widget
 * Copyright (c) 2008 Joel Birch
 *
 * Dual licensed under the MIT and GPL licenses:
 * 	http://www.opensource.org/licenses/mit-license.php
 * 	http://www.gnu.org/licenses/gpl.html
 *
 * CHANGELOG: http://users.tpg.com.au/j_birch/plugins/superfish/changelog.txt
 */

/******************************************************** dev version, see minified below */
;(function($){
	$.fn.superfish = function(op){

		var sf = $.fn.superfish,
			c = sf.c,
			$arrow = $(['<span class="',c.arrowClass,'"> &#187;</span>'].join('')),
			over = function(){
				var $$ = $(this), menu = getMenu($$);
				clearTimeout(menu.sfTimer);
				$$.showSuperfishUl().siblings().hideSuperfishUl();
			},
			out = function(){
				var $$ = $(this), menu = getMenu($$), o = sf.op;
				clearTimeout(menu.sfTimer);
				menu.sfTimer=setTimeout(function(){
					o.retainPath=($.inArray($$[0],o.$path)>-1);
					$$.hideSuperfishUl();
					if (o.$path.length && $$.parents(['li.',o.hoverClass].join('')).length<1){over.call(o.$path);}
				},o.delay);	
			},
			getMenu = function($menu){
				var menu = $menu.parents(['ul.',c.menuClass,':first'].join(''))[0];
				sf.op = sf.o[menu.serial];
				return menu;
			},
			addArrow = function($a){ $a.addClass(c.anchorClass).append($arrow.clone()); };
			
		return this.each(function() {
			var s = this.serial = sf.o.length;
			var o = $.extend({},sf.defaults,op);
			o.$path = $('li.'+o.pathClass,this).slice(0,o.pathLevels).each(function(){
				$(this).addClass([o.hoverClass,c.bcClass].join(' '))
					.filter('li:has(ul)').removeClass(o.pathClass);
			});
			sf.o[s] = sf.op = o;
			
			$('li:has(ul)',this)[($.fn.hoverIntent && !o.disableHI) ? 'hoverIntent' : 'hover'](over,out).each(function() {
				if (o.autoArrows) addArrow( $('>a:first-child',this) );
			})
			.not('.'+c.bcClass)
				.hideSuperfishUl();
			
			var $a = $('a',this);
			$a.each(function(i){
				var $li = $a.eq(i).parents('li');
				$a.eq(i).focus(function(){over.call($li);}).blur(function(){out.call($li);});
			});
			o.onInit.call(this);
			
		}).each(function() {
			var menuClasses = [c.menuClass];
			if (sf.op.dropShadows  && !($.browser.msie && $.browser.version < 7)) menuClasses.push(c.shadowClass);
			$(this).addClass(menuClasses.join(' '));
		});
	};

	var sf = $.fn.superfish;
	sf.o = [];
	sf.op = {};
	sf.IE7fix = function(){
		var o = sf.op;
		if ($.browser.msie && $.browser.version > 6 && o.dropShadows && o.animation.opacity!=undefined)
			this.toggleClass(sf.c.shadowClass+'-off');
		};
	sf.c = {
		bcClass     : 'sf-breadcrumb',
		menuClass   : 'sf-js-enabled',
		anchorClass : 'sf-with-ul',
		arrowClass  : 'sf-sub-indicator',
		shadowClass : 'sf-shadow'
	};
	sf.defaults = {
		hoverClass	: 'sfHover',
		pathClass	: 'overideThisToUse',
		pathLevels	: 1,
		delay		: 800,
		animation	: {opacity:'show'},
		speed		: 'normal',
		autoArrows	: true,
		dropShadows : true,
		disableHI	: false,		// true disables hoverIntent detection
		onInit		: function(){}, // callback functions
		onBeforeShow: function(){},
		onShow		: function(){},
		onHide		: function(){}
	};
	$.fn.extend({
		hideSuperfishUl : function(){
			var o = sf.op,
				not = (o.retainPath===true) ? o.$path : '';
			o.retainPath = false;
			var $ul = $(['li.',o.hoverClass].join(''),this).add(this).not(not).removeClass(o.hoverClass)
					.find('>ul')
					/**** begin changed line */
					.css({'visibility':'hidden','left':'-9999em'});
					/**** end changed line */
			o.onHide.call($ul);
			return this;
		},
		showSuperfishUl : function(){
			var o = sf.op,
				sh = sf.c.shadowClass+'-off',
				$ul = this.addClass(o.hoverClass)
					/**** begin changed line */
					.find('>ul').css({'visibility':'visible','left':null});
					/**** end changed line */
			sf.IE7fix.call($ul);
			o.onBeforeShow.call($ul);
			$ul.animate(o.animation,o.speed,function(){ sf.IE7fix.call($ul); o.onShow.call($ul); });
			return this;
		}
	});

})(jQuery);

/*************************************************************************** minified version */
;(function($){$.fn.superfish=function(op){var sf=$.fn.superfish,c=sf.c,$arrow=$(['<span class="',c.arrowClass,'"> ??</span>'].join('')),over=function(){var $$=$(this),menu=getMenu($$);clearTimeout(menu.sfTimer);$$.showSuperfishUl().siblings().hideSuperfishUl();},out=function(){var $$=$(this),menu=getMenu($$),o=sf.op;clearTimeout(menu.sfTimer);menu.sfTimer=setTimeout(function(){o.retainPath=($.inArray($$[0],o.$path)>-1);$$.hideSuperfishUl();if(o.$path.length&&$$.parents(['li.',o.hoverClass].join('')).length<1){over.call(o.$path);}},o.delay);},getMenu=function($menu){var menu=$menu.parents(['ul.',c.menuClass,':first'].join(''))[0];sf.op=sf.o[menu.serial];return menu;},addArrow=function($a){$a.addClass(c.anchorClass).append($arrow.clone());};return this.each(function(){var s=this.serial=sf.o.length;var o=$.extend({},sf.defaults,op);o.$path=$('li.'+o.pathClass,this).slice(0,o.pathLevels).each(function(){$(this).addClass([o.hoverClass,c.bcClass].join(' ')).filter('li:has(ul)').removeClass(o.pathClass);});sf.o[s]=sf.op=o;$('li:has(ul)',this)[($.fn.hoverIntent&&!o.disableHI)?'hoverIntent':'hover'](over,out).each(function(){if(o.autoArrows)addArrow($('>a:first-child',this));}).not('.'+c.bcClass).hideSuperfishUl();var $a=$('a',this);$a.each(function(i){var $li=$a.eq(i).parents('li');$a.eq(i).focus(function(){over.call($li);}).blur(function(){out.call($li);});});o.onInit.call(this);}).each(function(){var menuClasses=[c.menuClass];if(sf.op.dropShadows&&!($.browser.msie&&$.browser.version<7))menuClasses.push(c.shadowClass);$(this).addClass(menuClasses.join(' '));});};var sf=$.fn.superfish;sf.o=[];sf.op={};sf.IE7fix=function(){var o=sf.op;if($.browser.msie&&$.browser.version>6&&o.dropShadows&&o.animation.opacity!=undefined)
this.toggleClass(sf.c.shadowClass+'-off');};sf.c={bcClass:'sf-breadcrumb',menuClass:'sf-js-enabled',anchorClass:'sf-with-ul',arrowClass:'sf-sub-indicator',shadowClass:'sf-shadow'};sf.defaults={hoverClass:'sfHover',pathClass:'overideThisToUse',pathLevels:1,delay:800,animation:{opacity:'show'},speed:'normal',autoArrows:true,dropShadows:true,disableHI:false,onInit:function(){},onBeforeShow:function(){},onShow:function(){},onHide:function(){}};$.fn.extend({hideSuperfishUl:function(){var o=sf.op,not=(o.retainPath===true)?o.$path:'';o.retainPath=false;var $ul=$(['li.',o.hoverClass].join(''),this).add(this).not(not).removeClass(o.hoverClass).find('>ul').css({'visibility':'hidden','left':'-9999em'});o.onHide.call($ul);return this;},showSuperfishUl:function(){var o=sf.op,sh=sf.c.shadowClass+'-off',$ul=this.addClass(o.hoverClass).find('>ul').css({'visibility':'visible','left':''});sf.IE7fix.call($ul);o.onBeforeShow.call($ul);$ul.animate(o.animation,o.speed,function(){sf.IE7fix.call($ul);o.onShow.call($ul);});return this;}});})(jQuery);

function checkAtag(imgChild) {
	var isExit = 0;
	////console.debug("testing A tag: " + $(this));
			
	//if (imgChild.attr("alt"))   { //console.debug('has alt tag \n' + imgChild.attr("alt")); }
	//if (imgChild.attr("title")) { //console.debug('has title tag \n' + imgChild.attr("title")); }
	
	if  ( (imgChild.attr("alt")) 
			&& (   (imgChild.attr("alt").toLowerCase().indexOf("disclaimer") > -1) 
				|| (imgChild.attr("alt").toLowerCase().indexOf("external link") > -1  )
				)
		 ) { 
			////console.debug("[alt] " + imgChild.attr("alt").toLowerCase() + "(" + imgChild.attr("alt").toLowerCase().indexOf("disclaimer") + ")" + "(" + imgChild.attr("alt").toLowerCase().indexOf("external link") +")");
			isExit = 1; 
		 }
		 ////console.debug("-check alt- " + isExit + " | " + imgChild.attr("alt") );
		 
	if  ( (imgChild.attr("title")) 
			&& (   (imgChild.attr("title").toLowerCase().indexOf("disclaimer") > -1) 
				|| (imgChild.attr("title").toLowerCase().indexOf("external link") > -1  )
				)
		 ) { 
			////console.debug("[alt] " + imgChild.attr("title").toLowerCase() + "(" + imgChild.attr("title").toLowerCase().indexOf("disclaimer") + ")" + "(" + imgChild.attr("title").toLowerCase().indexOf("external link") +")");
			isExit = 1; 
		 }
		 ////console.debug("-check title- " + isExit + " | " + imgChild.attr("title") );

	return isExit;
}

	jQuery(document).ready(function(){ 

		// Fix some div heights //
		var continueResize = 0;
		var topicmain = "";
		if ( $("#module-topic-main").length ) {
			topicmain = $("#module-topic-main");
			continueResize = 1;
		} else if ( $("#mod-main-column").length) {
			topicmain = $("#mod-main-column");	
			continueResize = 1;
		}

		if ( continueResize == 1 ) {
				var len = topicmain.length;
				if (topicmain[0] != "" ) {
					var container = topicmain.parent();
					
					if (container.height() > topicmain.height() ) {
					container.css( { "overflow":"hidden", "height":"auto", "min-height":"100%" } );	
					topicmain.css({"height": container.height()} );	
					} 
				}
		}
		
	/*		External Link Checking, added 2012-11-09	*/	
					
			$("a").removeClass("exit");
			var isExit = 0;
			
			// So, do it differently
			$("a").each(function() {
				var imgChild = $(this).find("img");
				if (imgChild) { isExit = checkAtag(imgChild); }
				if (isExit == 1) { imgChild.remove(); }			
			});

			// So, do it differently
			/* $("img").each(function() {
				$(this).css("border","1px dashed #c00");
				var imgChild = $(this);
				if (imgChild) { isExit = checkAtag(imgChild); }
				if (isExit == 1) { 
					$(this).parent().css("border","1px dashed #c0c");
					imgChild.remove(); }				
			}); */

			var rtn = 0;
			var extLinkWin = '&nbsp;<a href="/about/disclaimer.html" style="padding:0; margin:0" ><img src="http://www.ncats.nih.gov/resources/images/exit-icon.png" alt="External Website Policy" title="External Website Policy" border="0" style="padding:0; margin:0;"></a>';

			$.expr[':'].external = function(obj){
				// Specify which links you do NOT want to have the externalLink class //
				var h = obj.href;
				//console.debug("h=" + h);
				
				if (h.length > 0){
					rtn = 0;
								
					// If any of these conditions exist, do not give the link the 'exit' class.
					if  (	
							   (h.indexOf(location.hostname)  > -1)
							|| (h.indexOf("/mailto:")         > -1)
							|| (h.indexOf("ncats.nih.gov") > -1)
							|| (h.indexOf("javascript:")      > -1)
							|| (h.indexOf(".gov")             > -1)
							|| (h.indexOf("www.addthis")      > -1)
						) 
						{ 
				       		rtn = 0;
					  	} else {
					  		rtn = 1;
					  	}
				}
				return rtn;
			};

			// Add 'external' CSS class to all external links
			// Change title to 'External Website'
			// Add "-External Website" to the alt tag 
			$('a:external').addClass('exit').after(extLinkWin);
			$('a.forceExtLinkGraphic').addClass('exit').after(extLinkWin);
			
			
			
	//-------------------------------------//
		

  });

