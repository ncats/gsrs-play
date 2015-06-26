// @SOURCE:/data/workspace/inxight/modules/ntd/conf/ix.ntd.routes
// @HASH:6c57ffef0f8aa24d53dfb1a3ea01fe2b7eb352da
// @DATE:Thu Jun 25 16:58:40 EDT 2015
package ix.ntd

import scala.language.reflectiveCalls
import play.core._
import play.core.Router._
import play.core.Router.HandlerInvokerFactory._
import play.core.j._

import play.api.mvc._
import _root_.controllers.Assets.Asset
import _root_.play.libs.F

import Router.queryString

object Routes extends Router.Routes {

import ReverseRouteContext.empty

private var _prefix = "/"

def setPrefix(prefix: String): Unit = {
  _prefix = prefix
  List[(String,Routes)](("",ix.ncats.Routes),("",ix.Routes)).foreach {
    case (p, router) => router.setPrefix(prefix + (if(prefix.endsWith("/")) "" else "/") + p)
  }
}

def prefix = _prefix

lazy val defaultPrefix = { if(Routes.prefix.endsWith("/")) "" else "/" }


// @LINE:4
private[this] lazy val ix_ntd_controllers_NTDFactory_index0_route = Route("GET", PathPattern(List(StaticPart(Routes.prefix),StaticPart(Routes.defaultPrefix),StaticPart("index"))))
private[this] lazy val ix_ntd_controllers_NTDFactory_index0_invoker = createInvoker(
ix.ntd.controllers.NTDFactory.index,
HandlerDef(this.getClass.getClassLoader, "ix.ntd", "ix.ntd.controllers.NTDFactory", "index", Nil,"GET", """
 NTD
""", Routes.prefix + """index"""))
        

// @LINE:5
private[this] lazy val ix_ntd_controllers_NTDApp_authenticate1_route = Route("GET", PathPattern(List(StaticPart(Routes.prefix),StaticPart(Routes.defaultPrefix),StaticPart("auth"))))
private[this] lazy val ix_ntd_controllers_NTDApp_authenticate1_invoker = createInvoker(
ix.ntd.controllers.NTDApp.authenticate,
HandlerDef(this.getClass.getClassLoader, "ix.ntd", "ix.ntd.controllers.NTDApp", "authenticate", Nil,"GET", """""", Routes.prefix + """auth"""))
        

// @LINE:6
private[this] lazy val ix_ntd_controllers_NTDLoad_parse2_route = Route("POST", PathPattern(List(StaticPart(Routes.prefix),StaticPart(Routes.defaultPrefix),StaticPart("index"))))
private[this] lazy val ix_ntd_controllers_NTDLoad_parse2_invoker = createInvoker(
ix.ntd.controllers.NTDLoad.parse,
HandlerDef(this.getClass.getClassLoader, "ix.ntd", "ix.ntd.controllers.NTDLoad", "parse", Nil,"POST", """""", Routes.prefix + """index"""))
        

// @LINE:8
lazy val ix_ncats_Routes3 = Include(ix.ncats.Routes)
        

// @LINE:9
lazy val ix_Routes4 = Include(ix.Routes)
        
def documentation = List(("""GET""", prefix + (if(prefix.endsWith("/")) "" else "/") + """index""","""ix.ntd.controllers.NTDFactory.index"""),("""GET""", prefix + (if(prefix.endsWith("/")) "" else "/") + """auth""","""ix.ntd.controllers.NTDApp.authenticate"""),("""POST""", prefix + (if(prefix.endsWith("/")) "" else "/") + """index""","""ix.ntd.controllers.NTDLoad.parse"""),ix.ncats.Routes.documentation,ix.Routes.documentation).foldLeft(List.empty[(String,String,String)]) { (s,e) => e.asInstanceOf[Any] match {
  case r @ (_,_,_) => s :+ r.asInstanceOf[(String,String,String)]
  case l => s ++ l.asInstanceOf[List[(String,String,String)]]
}}
      

def routes:PartialFunction[RequestHeader,Handler] = {

// @LINE:4
case ix_ntd_controllers_NTDFactory_index0_route(params) => {
   call { 
        ix_ntd_controllers_NTDFactory_index0_invoker.call(ix.ntd.controllers.NTDFactory.index)
   }
}
        

// @LINE:5
case ix_ntd_controllers_NTDApp_authenticate1_route(params) => {
   call { 
        ix_ntd_controllers_NTDApp_authenticate1_invoker.call(ix.ntd.controllers.NTDApp.authenticate)
   }
}
        

// @LINE:6
case ix_ntd_controllers_NTDLoad_parse2_route(params) => {
   call { 
        ix_ntd_controllers_NTDLoad_parse2_invoker.call(ix.ntd.controllers.NTDLoad.parse)
   }
}
        

// @LINE:8
case ix_ncats_Routes3(handler) => handler
        

// @LINE:9
case ix_Routes4(handler) => handler
        
}

}
     