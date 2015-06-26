
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
object load extends BaseScalaTemplate[play.twirl.api.HtmlFormat.Appendable,Format[play.twirl.api.HtmlFormat.Appendable]](play.twirl.api.HtmlFormat) with play.twirl.api.Template0[play.twirl.api.HtmlFormat.Appendable] {

  /**/
  def apply():play.twirl.api.HtmlFormat.Appendable = {
      _display_ {

Seq[Any](format.raw/*1.1*/("""{"""),format.raw/*1.2*/("""

    """),format.raw/*3.5*/("""<div class="container">
        <div class="row">
            <div class = "col-md-8 col-md-offset-2">


            </div>
        </div>
    </div>
"""),format.raw/*11.1*/("""}"""),format.raw/*11.2*/("""
"""))}
  }

  def render(): play.twirl.api.HtmlFormat.Appendable = apply()

  def f:(() => play.twirl.api.HtmlFormat.Appendable) = () => apply()

  def ref: this.type = this

}
              /*
                  -- GENERATED --
                  DATE: Thu Jun 25 16:58:40 EDT 2015
                  SOURCE: /data/workspace/inxight/modules/ntd/app/ix/ntd/views/load.scala.html
                  HASH: 593f4451d4718f04154e2ef4c16fadbf9acc7649
                  MATRIX: 804->0|831->1|863->7|1040->157|1068->158
                  LINES: 29->1|29->1|31->3|39->11|39->11
                  -- GENERATED --
              */
          