// @SOURCE:/data/workspace/inxight/modules/ntd/conf/ix.ntd.routes
// @HASH:6c57ffef0f8aa24d53dfb1a3ea01fe2b7eb352da
// @DATE:Thu Jun 25 16:58:40 EDT 2015

import ix.ntd.Routes.{prefix => _prefix, defaultPrefix => _defaultPrefix}
import play.core._
import play.core.Router._
import play.core.Router.HandlerInvokerFactory._
import play.core.j._

import play.api.mvc._
import _root_.controllers.Assets.Asset
import _root_.play.libs.F

import Router.queryString


// @LINE:6
// @LINE:5
// @LINE:4
package ix.ntd.controllers {

// @LINE:5
class ReverseNTDApp {


// @LINE:5
def authenticate(): Call = {
   import ReverseRouteContext.empty
   Call("GET", _prefix + { _defaultPrefix } + "auth")
}
                        

}
                          

// @LINE:4
class ReverseNTDFactory {


// @LINE:4
def index(): Call = {
   import ReverseRouteContext.empty
   Call("GET", _prefix + { _defaultPrefix } + "index")
}
                        

}
                          

// @LINE:6
class ReverseNTDLoad {


// @LINE:6
def parse(): Call = {
   import ReverseRouteContext.empty
   Call("POST", _prefix + { _defaultPrefix } + "index")
}
                        

}
                          
}
                  


// @LINE:6
// @LINE:5
// @LINE:4
package ix.ntd.controllers.javascript {
import ReverseRouteContext.empty

// @LINE:5
class ReverseNTDApp {


// @LINE:5
def authenticate : JavascriptReverseRoute = JavascriptReverseRoute(
   "ix.ntd.controllers.NTDApp.authenticate",
   """
      function() {
      return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "auth"})
      }
   """
)
                        

}
              

// @LINE:4
class ReverseNTDFactory {


// @LINE:4
def index : JavascriptReverseRoute = JavascriptReverseRoute(
   "ix.ntd.controllers.NTDFactory.index",
   """
      function() {
      return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "index"})
      }
   """
)
                        

}
              

// @LINE:6
class ReverseNTDLoad {


// @LINE:6
def parse : JavascriptReverseRoute = JavascriptReverseRoute(
   "ix.ntd.controllers.NTDLoad.parse",
   """
      function() {
      return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "index"})
      }
   """
)
                        

}
              
}
        


// @LINE:6
// @LINE:5
// @LINE:4
package ix.ntd.controllers.ref {


// @LINE:5
class ReverseNTDApp {


// @LINE:5
def authenticate(): play.api.mvc.HandlerRef[_] = new play.api.mvc.HandlerRef(
   ix.ntd.controllers.NTDApp.authenticate(), HandlerDef(this.getClass.getClassLoader, "", "ix.ntd.controllers.NTDApp", "authenticate", Seq(), "GET", """""", _prefix + """auth""")
)
                      

}
                          

// @LINE:4
class ReverseNTDFactory {


// @LINE:4
def index(): play.api.mvc.HandlerRef[_] = new play.api.mvc.HandlerRef(
   ix.ntd.controllers.NTDFactory.index(), HandlerDef(this.getClass.getClassLoader, "", "ix.ntd.controllers.NTDFactory", "index", Seq(), "GET", """
 NTD
""", _prefix + """index""")
)
                      

}
                          

// @LINE:6
class ReverseNTDLoad {


// @LINE:6
def parse(): play.api.mvc.HandlerRef[_] = new play.api.mvc.HandlerRef(
   ix.ntd.controllers.NTDLoad.parse(), HandlerDef(this.getClass.getClassLoader, "", "ix.ntd.controllers.NTDLoad", "parse", Seq(), "POST", """""", _prefix + """index""")
)
                      

}
                          
}
        
    