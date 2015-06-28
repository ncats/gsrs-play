
package ix.ntd.views.html

import play.twirl.api._
import play.twirl.api.TemplateMagic._

import play.api.templates.PlayMagic._
import models._
import controllers._
import java.lang._
import java.util._
import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import play.api.i18n._
import play.core.j.PlayMagicForJava._
import play.mvc._
import play.data._
import play.api.data.Field
import play.mvc.Http.Context.Implicit._
import views.html._

/**/
object main extends BaseScalaTemplate[play.twirl.api.HtmlFormat.Appendable,Format[play.twirl.api.HtmlFormat.Appendable]](play.twirl.api.HtmlFormat) with play.twirl.api.Template2[String,Html,play.twirl.api.HtmlFormat.Appendable] {

  /**/
  def apply/*1.2*/(title: String)(content: Html):play.twirl.api.HtmlFormat.Appendable = {
      _display_ {

Seq[Any](format.raw/*1.32*/("""

"""),format.raw/*3.1*/("""<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en" ng-app ="ntd">
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
		<meta http-equiv="X-UA-Compatible" content="IE=edge">
		<title>"""),_display_(/*8.11*/title),format.raw/*8.16*/("""</title>
		<link rel="stylesheet"
		href='"""),_display_(/*10.10*/routes/*10.16*/.WebJarAssets.at(WebJarAssets.locate("bootstrap.min.css"))),format.raw/*10.74*/("""'>
		<link rel="stylesheet"
		href='"""),_display_(/*12.10*/routes/*12.16*/.WebJarAssets.at(WebJarAssets.locate("font-awesome.min.css"))),format.raw/*12.77*/("""'>
		<link rel="stylesheet"
		href='"""),_display_(/*14.10*/routes/*14.16*/.WebJarAssets.at(WebJarAssets.locate("smoothness/jquery-ui.css"))),format.raw/*14.81*/("""'>
		<link rel="stylesheet" href='"""),_display_(/*15.33*/routes/*15.39*/.Assets.at("ncats/css/footer.css")),format.raw/*15.73*/("""'>
		<link rel="stylesheet" href='"""),_display_(/*16.33*/routes/*16.39*/.Assets.at("ncats/css/idg.css")),format.raw/*16.70*/("""'>
		<link rel="stylesheet"
		href='"""),_display_(/*18.10*/routes/*18.16*/.Assets.at("publications/css/overwrite.css")),format.raw/*18.60*/("""'>
		<link rel="stylesheet"
		href='"""),_display_(/*20.10*/routes/*20.16*/.Assets.at("ginas/css/ginas.css")),format.raw/*20.49*/("""'>

		<script
		src='"""),_display_(/*23.9*/routes/*23.15*/.WebJarAssets.at(WebJarAssets.locate("jquery.min.js"))),format.raw/*23.69*/("""'
		type='text/javascript'></script>
		<script
		src='"""),_display_(/*26.9*/routes/*26.15*/.WebJarAssets.at(WebJarAssets.locate("bootstrap.min.js"))),format.raw/*26.72*/("""'></script>
		<script
		src='"""),_display_(/*28.9*/routes/*28.15*/.WebJarAssets.at(WebJarAssets.locate("jquery-ui.js"))),format.raw/*28.68*/("""'></script>
		<script
		src='"""),_display_(/*30.9*/routes/*30.15*/.WebJarAssets.at(WebJarAssets.locate("typeahead.bundle.min.js"))),format.raw/*30.79*/("""'></script>
		<script
		src='"""),_display_(/*32.9*/routes/*32.15*/.WebJarAssets.at(WebJarAssets.locate("handlebars.min.js"))),format.raw/*32.73*/("""'></script>
		<script
		src='"""),_display_(/*34.9*/routes/*34.15*/.WebJarAssets.at(WebJarAssets.locate("bloodhound.min.js"))),format.raw/*34.73*/("""'></script>
		<script
		src='"""),_display_(/*36.9*/routes/*36.15*/.WebJarAssets.at(WebJarAssets.locate("angular.js"))),format.raw/*36.66*/("""'></script>
        <script
        src='"""),_display_(/*38.15*/routes/*38.21*/.WebJarAssets.at(WebJarAssets.locate("angular-route.js"))),format.raw/*38.78*/("""'></script>
        <script src='"""),_display_(/*39.23*/routes/*39.29*/.Assets.at("ntd/js/app.js")),format.raw/*39.56*/("""' type='text/javascript'></script>

	</head>

	<body>
		"""),format.raw/*44.55*/("""
			"""),format.raw/*45.34*/("""
				"""),format.raw/*46.33*/("""
	"""),format.raw/*47.10*/("""
				"""),format.raw/*48.15*/("""
				"""),format.raw/*49.45*/("""
					"""),format.raw/*50.92*/("""
				"""),format.raw/*51.15*/("""
			"""),format.raw/*52.14*/("""
		"""),format.raw/*53.13*/("""

		"""),format.raw/*55.3*/("""<div class="container"  ng-controller="NtdController as ntd">"""),_display_(/*55.65*/content),format.raw/*55.72*/("""</div>
	</body>
</html>
"""))}
  }

  def render(title:String,content:Html): play.twirl.api.HtmlFormat.Appendable = apply(title)(content)

  def f:((String) => (Html) => play.twirl.api.HtmlFormat.Appendable) = (title) => (content) => apply(title)(content)

  def ref: this.type = this

}
              /*
                  -- GENERATED --
                  DATE: Thu Jun 25 16:58:40 EDT 2015
                  SOURCE: /data/workspace/inxight/modules/ntd/app/ix/ntd/views/main.scala.html
                  HASH: ca013127157618b0c313bcb25012d4b50621a14b
                  MATRIX: 734->1|852->31|880->33|1155->282|1180->287|1250->330|1265->336|1344->394|1408->431|1423->437|1505->498|1569->535|1584->541|1670->606|1732->641|1747->647|1802->681|1864->716|1879->722|1931->753|1995->790|2010->796|2075->840|2139->877|2154->883|2208->916|2256->938|2271->944|2346->998|2427->1053|2442->1059|2520->1116|2576->1146|2591->1152|2665->1205|2721->1235|2736->1241|2821->1305|2877->1335|2892->1341|2971->1399|3027->1429|3042->1435|3121->1493|3177->1523|3192->1529|3264->1580|3333->1622|3348->1628|3426->1685|3487->1719|3502->1725|3550->1752|3634->1860|3666->1894|3699->1927|3729->1937|3762->1952|3795->1997|3829->2089|3862->2104|3894->2118|3925->2131|3956->2135|4045->2197|4073->2204
                  LINES: 26->1|29->1|31->3|36->8|36->8|38->10|38->10|38->10|40->12|40->12|40->12|42->14|42->14|42->14|43->15|43->15|43->15|44->16|44->16|44->16|46->18|46->18|46->18|48->20|48->20|48->20|51->23|51->23|51->23|54->26|54->26|54->26|56->28|56->28|56->28|58->30|58->30|58->30|60->32|60->32|60->32|62->34|62->34|62->34|64->36|64->36|64->36|66->38|66->38|66->38|67->39|67->39|67->39|72->44|73->45|74->46|75->47|76->48|77->49|78->50|79->51|80->52|81->53|83->55|83->55|83->55
                  -- GENERATED --
              */
          