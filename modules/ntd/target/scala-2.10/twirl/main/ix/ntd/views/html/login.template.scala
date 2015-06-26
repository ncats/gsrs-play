
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
object login extends BaseScalaTemplate[play.twirl.api.HtmlFormat.Appendable,Format[play.twirl.api.HtmlFormat.Appendable]](play.twirl.api.HtmlFormat) with play.twirl.api.Template0[play.twirl.api.HtmlFormat.Appendable] {

  /**/
  def apply():play.twirl.api.HtmlFormat.Appendable = {
      _display_ {

Seq[Any](_display_(/*1.2*/ix/*1.4*/.ncats.views.html.main("Login", "null")/*1.43*/ {_display_(Seq[Any](format.raw/*1.45*/("""
    """),_display_(/*2.6*/menu()),format.raw/*2.12*/("""
""")))}/*3.2*/ {_display_(Seq[Any](format.raw/*3.4*/("""
    """),format.raw/*4.5*/("""<h3></h3>
""")))}/*5.2*/ {_display_(Seq[Any](format.raw/*5.4*/("""

    """),format.raw/*7.5*/("""<div class="container">

        <div class="row" id="pub-history">
            <div class="col-md-8 col-md-offset-2">
                <h1>Login/Register</h1>
                <form class="form-horizontal" role="form" method="POST"
                action=""""),_display_(/*13.26*/ix/*13.28*/.ntd.controllers.routes.NTDApp.authenticate()),format.raw/*13.73*/(""""
                id="filereader">
                    <div class="form-group">
                        <div class="col-md-3">
                            <select class="form-control" name="region" id="region">
                                <option>USA</option>
                                <option>not USA</option>
                            </select>
                        </div>
                    </div>
                    <div class="form-group">
                        <div class="col-md-6">
                            <input type="text" class="form-control" name="username"
                            id="username" placeholder="username">
                        </div>
                    </div>
                    <div class="form-group">
                        <div class="col-md-6">
                            <input type="password" class="form-control" name="password"
                            id="pword" placeholder="password">
                        </div>
                    </div>
                    <div class="form-group">
                        <div class="col-md-2">
                            <button type="submit" class="btn btn-default">Login</button>
                        </div>
                    </div>
                </form>
            </div>
        </div>
    </div>
""")))}),format.raw/*44.2*/("""
"""))}
  }

  def render(): play.twirl.api.HtmlFormat.Appendable = apply()

  def f:(() => play.twirl.api.HtmlFormat.Appendable) = () => apply()

  def ref: this.type = this

}
              /*
                  -- GENERATED --
                  DATE: Thu Jun 25 16:58:40 EDT 2015
                  SOURCE: /data/workspace/inxight/modules/ntd/app/ix/ntd/views/login.scala.html
                  HASH: 5bb764e5f32770568f63ac528a224290d37927e9
                  MATRIX: 805->1|814->3|861->42|900->44|931->50|957->56|976->58|1014->60|1045->65|1073->76|1111->78|1143->84|1426->340|1437->342|1503->387|2860->1714
                  LINES: 29->1|29->1|29->1|29->1|30->2|30->2|31->3|31->3|32->4|33->5|33->5|35->7|41->13|41->13|41->13|72->44
                  -- GENERATED --
              */
          