
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
object error extends BaseScalaTemplate[play.twirl.api.HtmlFormat.Appendable,Format[play.twirl.api.HtmlFormat.Appendable]](play.twirl.api.HtmlFormat) with play.twirl.api.Template2[Int,String,play.twirl.api.HtmlFormat.Appendable] {

  /**/
  def apply/*1.2*/(code: Int, message: String):play.twirl.api.HtmlFormat.Appendable = {
      _display_ {

Seq[Any](format.raw/*1.30*/("""
"""),_display_(/*2.2*/ix/*2.4*/.ncats.views.html.error(code, message)/*2.42*/ {_display_(Seq[Any](format.raw/*2.44*/("""
    """),_display_(/*3.6*/menu()),format.raw/*3.12*/("""
""")))}),format.raw/*4.2*/("""
"""))}
  }

  def render(code:Int,message:String): play.twirl.api.HtmlFormat.Appendable = apply(code,message)

  def f:((Int,String) => play.twirl.api.HtmlFormat.Appendable) = (code,message) => apply(code,message)

  def ref: this.type = this

}
              /*
                  -- GENERATED --
                  DATE: Thu Jun 25 16:58:40 EDT 2015
                  SOURCE: /data/workspace/inxight/modules/ntd/app/ix/ntd/views/error.scala.html
                  HASH: efa731e2843042e44bdad939206e5bf8ebb003f1
                  MATRIX: 734->1|850->29|877->31|886->33|932->71|971->73|1002->79|1028->85|1059->87
                  LINES: 26->1|29->1|30->2|30->2|30->2|30->2|31->3|31->3|32->4
                  -- GENERATED --
              */
          