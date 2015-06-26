
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
object menu extends BaseScalaTemplate[play.twirl.api.HtmlFormat.Appendable,Format[play.twirl.api.HtmlFormat.Appendable]](play.twirl.api.HtmlFormat) with play.twirl.api.Template0[play.twirl.api.HtmlFormat.Appendable] {

  /**/
  def apply():play.twirl.api.HtmlFormat.Appendable = {
      _display_ {

Seq[Any](format.raw/*1.1*/("""<!-- BANNER AND MENU -->
<nav class="navbar navbar-default">
    <div class="container-fluid" id="full">
        <div class="navbar-header">
            <button type="button" class="navbar-toggle collapsed"
            data-toggle="collapse" data-target="#menubar">
                <span class="sr-only">Toggle Navigation</span> <i class="fa fa-bars"
            id="menuicon"></i>
            </button>
        </div>

        <div class="collapse navbar-collapse navbar-default" id="menubar">
            <ul class="nav navbar-nav pull-left">
                <li><a href=""""),_display_(/*14.31*/ix/*14.33*/.ntd.controllers.routes.NTDFactory.index()),format.raw/*14.75*/(""""><span>Home</span></a></li>
                <li><a href="http://ginas.hres.ca/ginas/#view=register"><span>Register Substance</span></a></li>
                <li><a href="http://tripod.nih.gov/pub/ginasISO/"><span>Download</span></a></li>
                <li><a id = "bug-link" data-toggle="modal" data-target="#screenshot" role="button">Report a Bug</a></li>
                """),format.raw/*21.24*/("""
                """),format.raw/*22.17*/("""<li>
                    <form class="form-inline main-search" role="search" method="GET"
                    action="" id = "search-bar">
                        <div class="input-group">
                            <input type="text" class="form-control typeahead" id="search"
                            title="Search" placeholder="Search..." name="q">
                            <span class="input-group-btn">
                                <button class="btn btn-default" id="search" type="search"
                                value="submit">
                                    <span class="fa fa-search"></span>
                                </button>
                            </span>
                        </div>
                    </form>
                </li>
            </ul>
            <div class="nav navbar-nav pull-right" id ="menu-login">
                <ul class="nav navbar-nav pull-right">
                <li><a href= "" id ="login-button"><button class="btn btn-default">Login</button></a></li>
                </ul>
            </div>
        </div>
            <!-- close input group  -->
    </div>


</nav>
"""))}
  }

  def render(): play.twirl.api.HtmlFormat.Appendable = apply()

  def f:(() => play.twirl.api.HtmlFormat.Appendable) = () => apply()

  def ref: this.type = this

}
              /*
                  -- GENERATED --
                  DATE: Thu Jun 25 16:58:40 EDT 2015
                  SOURCE: /data/workspace/inxight/modules/ntd/app/ix/ntd/views/menu.scala.html
                  HASH: dd09ab8c3b09f1b6d948b6b9971d7aaa572ff9f1
                  MATRIX: 804->0|1406->575|1417->577|1480->619|1884->1192|1929->1209
                  LINES: 29->1|42->14|42->14|42->14|46->21|47->22
                  -- GENERATED --
              */
          