var GSRSAPI={
    builder: function(){
        var g_api={};
        g_api.GlobalSettings = {
       //   _url:"https://ginas.ncats.nih.gov/ginas/app/api/v1/",
        _url:"http://localhost:5000/ginas/app/api/v1/",
          getBaseURL: function() {
            return g_api.GlobalSettings._url;
          },
	        setBaseURL: function(url) {
            g_api.GlobalSettings._url=url;
            return g_api.GlobalSettings;
          },
          getHomeURL: function() {
            return g_api.GlobalSettings.getBaseURL().replace(/api.v1.*/g,"");
          },
          httpType: function() {
            //return "jsonp"; //get only
            return "json"; //CORS needed, updates possible
          },
          authenticate: function(req){
            req.headers={};
            //push token header for now
            //req.headers["auth-token"]="d012ef187d53138c8e515717ba4243830335a935";
          }
        };

        //TODO: should be its own service
        g_api.httpProcess=function (req) {
          return g_api.JPromise.of(function(cb) {
            var b = req._b;
            if (b) {
              b = JSON.stringify(b);
            } else {
              b = req._q;
            }
            if (req._url.match(/.*[?]/)) {
              req._url = req._url + "&cache=" + g_api.UUID.randomUUID();
            } else {
              req._url = req._url + "?cache=" + g_api.UUID.randomUUID();
            }
            g_api.GlobalSettings.authenticate(req);

            console.log("called:" + req._url);
            $.ajax({
              url: req._url,
              jsonp: "callback",
              dataType: GlobalSettings.httpType(),
              contentType: 'application/json',
              type: req._method,
              data: b,
              beforeSend: function(request) {
                if(req.headers){
                    _.forEach(_.keys(req.headers), function(k){
                                            request.setRequestHeader(k, req.headers[k]);
                                        });
                }
              },
              success: function(response) {
                cb(response);
              },
              error: function(rep, error, t) {
                console.log(error + "\t" + t);
                var err={isError:true, message:error, type:t };
                if(rep.responseJSON){
                	err.response=rep.responseJSON;
                }else if(rep.responseText){
                	err.response=rep.responseText;
                }
                cb(err);
                //cb(""); //for now
              }
            });
          });
        };
        //Returns an object which will call
        //the supplied callback after {{total}}
        //number of calls to {{decrement}}
        g_api.getListener = function(total, cb) {
          return {
            total: total,
            current: 0,
            callback: cb,
            decrement: function() {
              this.current++;
              if (this.current >= this.total) {
                this.callback();
              }
            }
          };
        }


        g_api.JPromise = {
              ofScalar: function(s) {
                return g_api.JPromise.of(function(cb) {
                  cb(s);
                });
              },
              of: function(calc) {
                var ret = {
                  get: function(cb) {
                    calc(cb);
                  },
                  andThen: function(lam) {
                    return g_api.JPromise.of(function(cb) {
                      ret.get(function(orig) {
                        var ret = lam(orig);
                        if (ret && ret._promise) {
                          ret.get(cb);
                        } else if(typeof ret === "undefined"){
                          cb(orig);
                        } else{
                          cb(ret);
                        }
                      });
                    });
                  },
                  _promise: true
                };
                return ret;
              },
              join: function(listo) {
                var list = [];
                if (arguments.length > 1) {
                  list = arguments;
                } else {
                  list = listo;
                }
                return g_api.JPromise.of(function(cb) {
                  var toRet = {};
                  var retFun = function() {
                    var retList = [];
                    for (var j = 0; j < list.length; j++) {
                      retList.push(toRet[j]);
                    }
                    return retList;
                  };
                  var listener = g_api.getListener(list.length, function() {
                    cb(retFun());
                  });
                  var proc = function(pFetch, key) {
                    pFetch.get(function(ret) {
                      toRet[key] = ret;
                      listener.decrement();
                    });
                  };
                  for (var i = 0; i < list.length; i++) {
                    var pFetch = list[i];
                    proc(pFetch, i);
                  }
                });
              }
            };

        g_api.gUtil = {
          null: {},
          deepIterate: function(o, path, cb) {
            if (_.isFunction(o)) {
              return g_api.gUtil.null;
            } else if (_.isObject(o)) {
              if (_.isArray(o)) {
                var ks = _.keys(o);
                _.forEach(ks, function(k) {
                  g_api.gUtil.deepIterate(o[k], path + "[" + k + "]", cb);
                });
              } else {
                var ks = _.keys(o);
                _.forEach(ks, function(k) {
                  g_api.gUtil.deepIterate(o[k], path + "/" + k, cb);
                });
              }
            } else {
              cb(path, o);
            }
          },
          forEachDeep: function(o, path, cb) {
            var node = function(path, key, value, parent) {
              return {
                path: path,
                key: key,
                value: value,
                parent: parent
              };
            };
            if (_.isFunction(o)) {
              return g_api.gUtil.null;
            } else if (_.isObject(o)) {
              if (_.isArray(o)) {
                var ks = _.keys(o);
                var mod = false;
                _.forEach(ks, function(k) {
                  var rep = cb(node(path, k, o[k], o));
                  if (rep == g_api.gUtil.null) {
                    o[k] = g_api.gUtil.null;
                    mod = true;
                  } else {
                    if (typeof rep !== "undefined") {
                      o[k] = rep;
                    }
                    g_api.gUtil.forEachDeep(o[k], path + "/" + k, cb);
                  };
                });
                if (mod) {
                  var newArray = _.filter(o, function(e) {
                    if (e == g_api.gUtil.null) return false;
                    return true
                  });
                  o.splice(0, o.length);
                  _.forEach(newArray, function(a) {
                    o.push(a);
                  });
                }
              } else {
                var ks = _.keys(o);
                _.forEach(ks, function(k) {
                  var rep = cb(node(path, k, o[k], o));
                  if (rep === gUtil.null) {
                    delete o[k];
                  } else {
                    if (typeof rep !== "undefined") {
                      o[k] = rep;
                    }
                    g_api.gUtil.forEachDeep(o[k], path + "/" + k, cb);
                  }
                });
              }
            }
          },
          removeDeep: function(o, test) {
            g_api.gUtil.forEachDeep(o, "", function(node) {
              if (test(node)) {
                return gUtil.null;
              }
            });
          },
          removeKeysLike: function(o, regex) {
            g_api.gUtil.removeDeep(o, function(node) {
              return node.key.match(regex);
            });
          },
          toDate: function(d){
            return new Date(d);
          }
        };

	//This is the basic structure for finding things from the API
        g_api.ResourceFinder = {
            builder: function(){
                var finder = {};
                finder.resource=function(resource){
                    finder.resource=resource;
                    return finder;
                };
                finder.searcher=function(){
                    return g_api.SearchRequest
                                 .builder()
                                 .resource(finder.resource);
                };
                finder.search = function(q){
                    return finder.searcher()
                                 .query(q)
                                 .execute();
                };



                finder.get = function(uuid){
                    var req = g_api.Request.builder()
                                     .url(g_api.GlobalSettings.getBaseURL() + finder.resource + "(" + uuid + ")");

                    return g_api.httpProcess(req).andThen(function(sim) {
                           //TODO: make generic
                           return g_api.SubstanceBuilder.fromSimple(sim);
                    });
                };

                finder.extend = function(f){
                  var nfinder= f(finder);
                  if(typeof nfinder !== "undefined"){
                    return nfinder;
                  }else{
                    return finder;
                  }
                };

                return finder;
            }
        };

        g_api.SubstanceFinder = g_api.ResourceFinder.builder()
                                      .resource("substances")
                                      .extend(function(sfinder){
                                        sfinder.searchByExactNameOrCode = function(q) {
                                            if(UUID.isUUID(q)){
                                                return sfinder.get(q).andThen(function(s){return {"content": [s]}});
                                            }
                                            return sfinder.search("root_names_name:\"^" + q + "$\" OR " +
                                              "root_approvalID:\"^" + q + "$\" OR " +
                                              "root_codes_code:\"^" + q + "$\"");
                                          };
                                        sfinder.getExactStructureMatches = function(smi) {
                                          //substances/structureSearch?q=CCOC(N)=O&type=exact
                                            var req = g_api.Request.builder()
                                                             .url(g_api.GlobalSettings.getBaseURL() + "substances/structureSearch")
                                                             .queryStringData({
                                                                    q:smi,
                                                                    type:"exact",
                                                                    sync:"true" //shouldn't be sync
                                                                });
                                            return g_api.httpProcess(req).andThen(function(tmp) {
                                              return tmp;
                                            });
                                          };
                                      });
        g_api.ReferenceFinder = g_api.ResourceFinder.builder()
              .resource("references")
              .extend(function(rfinder){
                  rfinder.searchByLastEdited = function(q) {
                      return rfinder.searcher()
                                    .query("root_lastEditedBy:\"^" + q + "$\" AND NOT root_docType:VALIDATION_MESSAGE AND NOT root_docType:BATCH_IMPORT AND NOT root_docType:SRS_LOCATOR AND NOT root_docType:SYSTEM")
                                    .order("$root_lastEdited")
                                    .execute();
                    };
              });

        g_api.CVFinder = g_api.ResourceFinder.builder()
              .resource("vocabularies")
              .extend(function(cvfinder){
                  cvfinder.searchByDomain = function(q) {
                       return cvfinder.search("root_domain:\"^" + q + "$\"");
                    };
              });

        g_api.SearchRequest = {
            builder: function(){
                var request = {
                       _limit:10,
                       _skip:0,
                       _resource:"resource",
                       _query:"",
                       _order:null
                    };
                request.limit = function(limit){
                    request._limit=limit;
                    return request;
                };
                request.skip = function(skip){
                    request._skip=skip;
                    return request;
                };
                request.top = function(top){
                    return request.limit(top);
                };
                request.resource = function(resource){
                    request._resource=resource;
                    return request;
                };
                request.query = function(q){
                    request._query=q;
                    return request;
                };

                request.order = function(order){
                    request._order=order;
                  return request;
                };

                request.asRequest = function(){
                    var qdat={
                      q: request._query,
                      top: request._limit,
                      skip: request._skip
                    };

                    if(request._order){
                      qdat.order=request._order;
                    }


                    return g_api.Request.builder()
                           .url(g_api.GlobalSettings.getBaseURL() + request._resource + "/search")
                           .queryStringData(qdat);
                };
                request.execute = function(){
                    return request.asRequest().execute();
                };
                return request;
            }
        };


        //TODO
        g_api.SearchResponse = {
            builder: function(){
                var resp ={};
                resp.mix= function(raw){
                    _.merge(resp, raw);
                    return resp;
                };
                return resp;
            }
        };

        g_api.SubstanceBuilder = {
          fromSimple: function(simple) {
            simple._cache = {};
            simple.getBestID = function() {
              if (simple._approvalIDDisplay) {
                return simple._approvalIDDisplay;
              } else {
                return simple.uuid;
              }
            };
            simple.full = function() {
              var req = Request.builder()
                .url(g_api.GlobalSettings.getBaseURL() + "substances(" + simple.uuid + ")")
                .queryStringData({
                  view: "full"
                });
              return req.execute();
            };
            simple.versions = function() {
                  var req = Request.builder()
                    .url(g_api.GlobalSettings.getBaseURL() + "substances(" + simple.uuid + ")/@edits");
                  return req.execute()
                  			.andThen(function(e){
                  				if(!e || e.isError){
                  					return [];
                  				}
                  				return e;
                  			})
                  			.andThen(function(r){
                  				return r.sort(function(a,b){
                  					return a.created-b.created;
                  				});
                  			});
            };
            simple.getVersion = function(v) {
                return simple.versions()
                             .andThen(function(vs){
                            	 try{
                            		 return _.filter(vs,{"version":v+""})[0];
                            	 }catch(e){
                            		 return {"isError":true,"type":"cannot find element"};
                            	 }
                             })
                             .andThen(function(b){
                            	 console.log(b.oldValue);
                            	 return Request.builder()
                                 				  .url(b.oldValue)
                                 				  .execute();
                             });
            };
            simple.restoreVersion= function(version){
            	 return simple.getVersion(version)
	                         .andThen(function(old){
	                            return simple.patch()
	                                    .mutateTo(old)
			                            .replace("/changeReason","reverted to version " + version)
			                            .replace("/version",simple.version)
	       					            .apply();
	                         });
            };
            simple.fetch = function(field, lambda) {
              var ret = simple._cache[field];
              var p = null;
              if (!ret) {
                var url = g_api.GlobalSettings.getBaseURL() + "substances(" + simple.uuid + ")/";
                if (field) {
                  url += field;
                }
                var req = g_api.Request.builder()
                  .url(url);
                p = g_api.httpProcess(req);
              } else {
                p = g_api.JPromise.ofScalar(ret);
              }
              if (lambda) {
                return p.andThen(lambda);
              }
              return p;
            };
            simple.patch = function() {
              var p = Patch.builder();
              p._oldApply = p.apply;
              p._oldCompute = p.compute;
              p._oldValidate = p.validate;
              p.apply = function() {
                return p._oldApply(simple);
              };
              p.compute = function() {
                return p._oldCompute(simple);
              };
              p.validate = function() {
                return p._oldValidate(simple);
              };
              return p;
            };
            simple.save = function(){
              var req = g_api.Request.builder()
                .url(g_api.GlobalSettings.getBaseURL() + "substances")
                .method("PUT")
                .body(simple);
              return g_api.httpProcess(req);
            };

            simple.validate = function(save){
              var req = g_api.Request.builder()
                .url(g_api.GlobalSettings.getBaseURL() + "substances/@validate" )
                .method("POST")
                .body(simple);
              return g_api.httpProcess(req);
             /*   .andThen(function(resp){
                  if(save){
                    return simple.save();
                  }else{
                    return resp;
                  }
                });*/
            };




            return simple;
          }
        };

        g_api.Patch = {
          builder: function() {
            var b = {
              ops: []
            };
            b.change = function(op) {
              b.ops.push(op);
              return b;
            };
            b.replace = function(path, n) {
              return b.change({
                op: "replace",
                path: path,
                value: n
              });
            };
            b.add = function(path, n) {
              return b.change({
                op: "add",
                path: path,
                value: n
              });
            };

            b.addData = function(data) {
              return data.addToPatch(b);
            };

            b.mutateTo = function(newjson){
            	return b.change({
                    op: "replace",
                    path: "",
                    value: newjson
                  });
            };

            //should return a promise
            b.apply = function(simpleSub) {
              return simpleSub.full()
                .andThen(function(ret) {
                  var rr = ret;
                  jsonpatch.apply(rr, b.ops);
                  var req = g_api.Request.builder()
                    .url(g_api.GlobalSettings.getBaseURL() + "substances")
                    .method("PUT")
                    .body(rr);
                  return g_api.httpProcess(req);
                });
            };

            //Calculates the new record, does not submit it
            b.compute = function(simpleSub) {
              return simpleSub.full()
                .andThen(function(ret) {
                  var rr = ret;
                  jsonpatch.apply(rr, b.ops);
                  return rr;
                });
            };

            //Calculates the new record, does not submit it
            b.validate = function(simpleSub) {
              return simpleSub.full()
                .andThen(function(ret) {
                  var rr = ret;
                  jsonpatch.apply(rr, b.ops);
                  var req = g_api.Request.builder()
                    .url(g_api.GlobalSettings.getBaseURL() + "substances/@validate")
                    .method("POST")
                    .body(rr);
                  return g_api.httpProcess(req);
                });
            };
            return b;
          }
        };

        g_api.ResolveWorker = {
          builder: function() {
            var worker = {
              _list: [],
              _fetchers: [],
              _consumer: function(r) {},
              _finisher: function() {},
              consumer: function(c) {
                worker._consumer = c;
                return worker;
              },
              list: function(l) {
                worker._list = l;
                return worker;
              },
              fetchers: function(f) {
                worker._fetchers = f;
                return worker;
              },
              finisher: function(f) {
                worker._finisher = f;
                return worker;
              },
              resolve: function() {
                var psubs = _.chain(worker._list)
                  .filter(function(r) {
                    return (r.length > 0);
                  })
                  .map(function(r) {
                    var pSub = g_api.SubstanceFinder.searchByExactNameOrCode(r);
                    pSub._q = r;
                    return pSub;
                  })
                  .value();

                var listener = getListener(psubs.length, function() {
                  worker._finisher();
                });

                _.forEach(psubs, function(pSub) {
                  worker.process(pSub, worker._fetchers).get(function(rows) {
                    _.forEach(rows, function(row) {
                      worker._consumer(row);
                    });
                    listener.decrement();
                  });
                });
              },
	      process: function(pSub, fetchNames) {
		  var row = pSub._q;
		  return pSub.andThen(function(ret) {
		      return ret["content"];
		    })
		    .andThen(function(content) {
		      if (content && content.length > 0) {
			var promises = _.map(content, function(c) {
			  return worker.outputAll(g_api.SubstanceBuilder.fromSimple(c), fetchNames);
			});
			return g_api.JPromise.join(promises).andThen(function(all) {
			  return _.map(all, function(q) {
			    return row + "\t" + q;
			  });
			});
		      } else {
			return g_api.JPromise.ofScalar([row]);
		      }
		    });
	      },
		outputAll: function(simpleSub, fetchNames) {
		  return g_api.JPromise.of(function(cb) {
		    g_api.FetcherRegistry.getFetchers(fetchNames)
		      .fetcher(simpleSub)
		      .get(function(g) {
			cb(g.join("\t"));
		      });
		  });
		}
            };
            return worker;
          }
        };


        //TODO: convert to builder pattern
        g_api.FetcherMaker = {
          make: function(name, maker) {
            var fetcher = {
              name: name,
              tags:[],
              fetcher: function(simp) {
		return g_api.JPromise.of(function(cb){
			maker(simp).get(function(ret){
				cb(ret,name);
			});
		});
              },
              andThen: function(after) {
                return g_api.FetcherMaker.make(name, function(simp){
                    return fetcher.fetcher(simp).andThen(after);
                });
              }
            };
            fetcher.addTag = function (tag){
                fetcher.tags.push(tag);
                return fetcher;
            };
            fetcher.setDescription = function (desc){
                fetcher.description = desc;
                return fetcher;
            };
            return fetcher;
          },
          makeAPIFetcher: function(property, name) {
            var nm = name;
            if (!nm) {
              nm = property;
            }
            return g_api.FetcherMaker.make(nm, function(simpleSub) {
              return simpleSub.fetch(property);
            });
          },
          makeScalarFetcher: function(property, name) {
            var nm = name;
            if (!nm) {
              nm = property;
            }
            return g_api.FetcherMaker.make(nm, function(simpleSub) {
              return g_api.JPromise.ofScalar(simpleSub[property]);
            });
          },
          makeCodeFetcher: function(codeSystem, name) {
            var nm = name;
            if (!nm) {
              nm = codeSystem + "[CODE]"
            }
            return g_api.FetcherMaker.make(nm, function(simpleSub) {
              return simpleSub.fetch("codes(codeSystem:" + codeSystem + ")")
                .andThen(function(cds) {
                  return _.chain(cds)
                          .sort(function(a,b){
                                if(a.type==="PRIMARY" && b.type!=="PRIMARY") {
                                        return -1;
                                }else if(a.type!=="PRIMARY" && b.type==="PRIMARY") {
                                        return 1
                                }else{
                                        return 0;
                                }
                          })
                          .map(function(cd) {
                                if (cd.type !== "PRIMARY") {
                                  return cd.code + " [" + cd.type + "]";
                                } else {
                                  return cd.code;
                                }
                          })
                          .value()
                          .join("; ");
                });
            });
          }
        };

        g_api.FetcherRegistry = {
          fetchMap: {},
          getFetcher: function(name) {
            var ret = g_api.FetcherRegistry.fetchMap[name];
            return ret;
          },
          addFetcher: function(fetcher) {
            g_api.FetcherRegistry.fetchMap[fetcher.name] = fetcher;
            g_api.FetcherRegistry.fetchers.push(fetcher);
            return g_api.FetcherRegistry;
          },
          fetchers: [],
          //Actually accumulates into a master fetcher
          getFetchers: function(list) {
            var retlist = _.map(list, function(f) {
              return g_api.FetcherRegistry.getFetcher(f);
            });
            return g_api.FetcherRegistry.joinFetchers(retlist);
          },
          joinFetchers: function(retlist) {
            return g_api.FetcherMaker.make("Custom", function(simpleSub) {
              var proms = _.map(retlist, function(r) {
                return r.fetcher(simpleSub);
              });
              var promNames = _.map(retlist, function(r) {
                return r.name;
              });


	      return g_api.JPromise.of(function (callback){
			g_api.JPromise.join(proms)
		          .get(function(array){
				callback(array,promNames);
			   });
	      });
            });
          },
          getFetcherTags: function(){
            var allTags=[];
            _.chain(g_api.FetcherRegistry.fetchers)
                 .map(function(f){return f.tags;})
                 .forEach(function(tgs){
                _.forEach(tgs,function(t){allTags.push(t);});
             }).value();
            return _.uniq(allTags);
          },
          getFetchersWithTag: function(tag){
            return _.chain(g_api.FetcherRegistry.fetchers)
                 .filter(function(f){return _.indexOf(f.tags,tag)>=0;})
                 .value();
          },
          getFetchersWithNoTag: function(){
            return _.chain(g_api.FetcherRegistry.fetchers)
                 .filter(function(f){return f.tags.length===0;})
                 .value();
          }
        };

        var UUID = {
          randomUUID: function() {
            return UUID.s4() + UUID.s4() + '-' + UUID.s4() + '-' + UUID.s4() + '-' +
              UUID.s4() + '-' + UUID.s4() + UUID.s4() + UUID.s4();
          },
          s4: function() {
            return Math.floor((1 + Math.random()) * 0x10000)
              .toString(16)
              .substring(1);
          },
          isUUID: function(uuid) {
            if ((uuid + "").match(/^[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}$/)) {
              return true;
            }
            return false;
          }
        };

        g_api.UUID=UUID;

        g_api.Request = {
              builder: function() {
                var rq = {
                  _method: "GET"
                };
                rq.url = function(url) {
                  rq._url = url;
                  return rq;
                };
                rq.method = function(method) {
                  rq._method = method;
                  return rq;
                };
                rq.queryStringData = function(q) {
                  rq._q = q;
                  return rq;
                };
                rq.body = function(b) {
                  rq._b = b;
                  return rq;
                };
		rq.execute = function(){
			return g_api.httpProcess(rq);
		}
                return rq;
              }
            };

        //********************************
        //Models
        //********************************

        var CommonData = {
          builder: function() {
            var data = {};

            //should be set
            data._path = "";
            data._type = "";

            //default values
            data.uuid = UUID.randomUUID();
            data.references = [];
            data.access = [];
            data._references = [];

            data.build = function() {
              var d = JSON.parse(JSON.stringify(data));
              g_api.gUtil.removeKeysLike(d, /^_/);
              return d;
            };

            data.setAccess = function(list) {
              data.access = list;
              return data;
            };

            data.setProtected = function() {
              data.access = ["protected"];
              return data;
            };

            data.setPublic = function(pub) {
              if (pub) {
                return data;
              }
              return data.setProtected();
            };

            data.setDeprecated = function(d) {
              if (d) {
                data.deprecated = true;
              } else {
                data.deprecated = false;
              }
              return data;
            };

            data.addReference = function(r) {
              if (UUID.isUUID(r)) {
                data.addReferenceUUID(r);
              } else {
                if (r._type === "reference") {
                  data._references.push(r);
                  data.addReferenceUUID(r.uuid);
                } else {
                  var ref = _.merge(Reference.builder(), r);
                  data._references.push(ref);
                  data.addReferenceUUID(ref.uuid);
                }
              }
              return data;
            };

            data.addReferenceUUID = function(ruuid) {
              data.references.push(ruuid);
              return data;
            };

            data.addToPatch = function(patch) {
              patch = patch.add(data._path, data.build());
              _.forEach(data._references, function(r) {
                patch = patch.add("/references/-", r.build());
              });
              return patch;
            };

            data.mix = function(source) {
              _.merge(data, source);
              return data;
            };

            return data;
          }
        };

        var Name = {
          builder: function() {
            var name = CommonData.builder();
            name._type = "name";
            name._path = "/names/-";

            name.type = "cn";
            name.setName = function(nm) {
              name.name = nm;
              return name;
            };
            name.setType = function(type) {
              name.type = type;
              return name;
            };
            name.setLanguages = function(lng) {
              name.languages = lng;
              return name;
            };
            name.setDomains = function(dmns) {
              name.domains = dmns;
              return name;
            };
            name.setNameOrgs = function(orgs) {
              name.nameOrgs = orgs;
              return name;
            };


            return name;
          }
        };

      var Relationship = {
        builder: function() {
          var relationship = CommonData.builder();
          relationship._type = "relationship";
          relationship._path = "/relationships/-";

          relationship.setAmount = function(am) {
            relationship.amount = am;
            return relationship;
          };
          relationship.setType = function(type) {
            relationship.type = type;
            return relationship;
          };
          relationship.setQualification = function(ql) {
            relationship.qualification = ql;
            return relationship;
          };
          relationship.setInteractionType = function(it) {
            relationship.interactionType = it;
            return relationship;
          };
          relationship.setRelatedSubstance = function(rs) {
            relationship.relatedSubstance = rs;
            return relationship;
          };

          return relationship;
        }
      };



        var Reference = {
          builder: function() {
            var ref = CommonData.builder();
            ref._type = "reference";
            ref._path = "references/-";

            ref.setCitation = function(cit) {
              ref.citation = cit;
              return ref;
            }
            ref.setUrl = function(url) {
              ref.url = url;
              return ref;
            }
            ref.setDocType = function(typ) {
              ref.docType = typ;
              return ref;
            }
            ref.setPublicDomain = function(pd) {
              ref.publicDomain = pd;
              return ref;
            }

            //@Override
            var oldBuild = ref.build;
            ref.build = function() {
              var d = oldBuild();
              delete d.references;
              return d;
            }
            return ref;
          }
        };

        g_api.CommonData=CommonData;
        g_api.Name=Name;
        g_api.Reference=Reference;


        var Scripts = {
          scriptMap: {},
          addScript: function(s) {
            Scripts.scriptMap[s.name] = s;
            return Scripts;
          },
          get: function(nm) {
            return Scripts.scriptMap[nm];
          },
          all: function() {
            return _.chain(_.keys(Scripts.scriptMap))
              .map(function(s) {
                return Scripts.scriptMap[s];
              })
              .value();
          }
        };

        var Script = {
          builder: function() {
            var scr = {};
            scr.argMap = {};
            scr.arguments = [];
            scr.addArgument = function(arg) {
              if (arg._type !== "argument") {
                arg = Argument.builder().mix(arg);
              }
              scr.arguments.push(arg);
              scr.argMap[arg.getKey()] = arg;
              return scr;
            };
            scr.setKey = function(key) {
              scr.key = key;
              return scr;
            };
            scr.setName = function(name) {
              scr.name = name;
              return scr;
            };
            scr.setDescription = function(desc) {
              scr.description = desc;
              return scr;
            };
            scr.mix = function(sc) {
              _.merge(scr, sc);
              _.forEach(scr.arguments, function(a) {
                scr.argMap[a.getKey()] = a;
              });
              return scr;
            };
            scr.getArgument = function(narg) {
              return scr.argMap[narg];
            };
            scr.getArgumentByName = function(narg) {
              var l = _.filter(scr.arguments,function(a){return a.name===narg});
              if(l.length==0)return undefined;
              return l[0];
            };
            scr.hasArgumentByName = function(narg) {
              return !(typeof scr.getArgumentByName(narg)==="undefined");
            };
            scr.hasArgument = function(narg) {
              return !(typeof scr.getArgument(narg)==="undefined");
            };

            scr.setExecutor = function(exec) {
              scr.executor = exec;
              return scr;
            };
            scr.useFor = function(cb){
            cb(scr);
            };

            //should return a promise
            //takes a
            scr.execute = function(vals) {
              return g_api.JPromise.of(function(cb) {
                var ret = scr.executor(vals);
                if (ret && ret._promise) {
                  ret.get(cb);
                } else {
                  cb(ret);
                }
              });

            };
            scr.runner = function() {
              var cargs = {
                args: {}
              };
              cargs.setValue = function(key, value) {
                var darg = scr.getArgument(key);
                if (!darg) {
                  throw "No such argument '" + key + "' in script '" + scr.name + "'";
                }
                cargs.args[key] = Argument.builder().mix(scr.getArgument(key)).setValue(value);
                return cargs;
              };
                    cargs.setValues = function(kvpairs){
                  _.forEach(_.keys(kvpairs), function(k){
                                cargs.setValue(k,kvpairs[k]);
                            });
                  return cargs;
              };
              cargs.getArguments = function(){
                      var ret=[];
                      _.forEach(_.keys(cargs.args), function(k){
                                ret.push(cargs.args[k]);
                            });
                  return ret;
              };
              _.forEach(scr.arguments, function(a) {
                cargs.args[a.getKey()] = a;
              });
              cargs.execute = function() {
                return scr.execute(cargs.args);
              };
              return cargs;
            };

            return scr;
          }
        };

        var Argument = {
          builder: function() {
            var arg = {};
            arg._type = "argument";
            //name of the argument
            arg.setName = function(nm) {
              arg.name = nm;
              return arg;
            };
            arg.mix = function(ar) {
              return _.merge(arg, ar);
            };
            arg.setRequired = function(r) {
              arg.required = r;
              return arg;
            };
            arg.getKey = function(){
              if(arg.key){
            return arg.key;
              }
              return arg.name;
            };

            arg.isRequired = function(r) {
              if (arg.required) return true;
              return (typeof arg.default) === "undefined";
            };

            arg.setDescription = function(des) {
              arg.description = des;
              return arg;
            };
            arg.setType = function(type) {
              arg.type = type;
              return arg;
            };
            arg.setValue = function(value) {
              arg.value = value;
              return arg;
            };
            arg.setDefault = function(def) {
              arg.default = def;
              return arg;
            };
            arg.getValue = function() {
              if (arg.value) {
                return arg.value;
              } else {
                return arg.default;
              }
            };
            return arg;
          }
        };
        g_api.Scripts=Scripts;
        g_api.Script=Script;
        g_api.Argument=Argument;

        GSRSAPI.initialize(g_api);

        return g_api;
    },
    initialize: function(g_api){
      _.chain(GSRSAPI.extensions)
       .forEach(function(ex){
            ex.init(g_api);
        });
    },
    addExtension:function(ext){
        GSRSAPI.extensions.push(ext);
    },
    extensions:[]
}





//Global Helpers
//For use in legacy code (should refactor)
var GGlob=GSRSAPI.builder();
var GlobalSettings = GGlob.GlobalSettings;
var getListener= GGlob.getListener;
var JPromise = GGlob.JPromise;
var gUtil = GGlob.gUtil;
var ResourceFinder =GGlob.ResourceFinder;
var SubstanceFinder=GGlob.SubstanceFinder;
var ReferenceFinder=GGlob.ReferenceFinder;
var SearchRequest  =GGlob.SearchRequest;
var SubstanceBuilder = GGlob.SubstanceBuilder;
var Patch = GGlob.Patch;
var ResolveWorker = GGlob.ResolveWorker;
var FetcherMaker = GGlob.FetcherMaker;
var FetcherRegistry = GGlob.FetcherRegistry;
var UUID = GGlob.UUID;
var Request = GGlob.Request;

//TODO: Finish this
var Validation = {
  builder: function() {
    var v = {};
  }
}


//********************************
//Models
//********************************

var CommonData = GGlob.CommonData;
var Name = GGlob.Name;
var Reference = GGlob.Reference;
var Debug = {}


//I don't like this yet
//it's here as a quick and dirty way to make
//VBA have a simple recipe for doing predefined things
var Scripts = GGlob.Scripts;
var Script = GGlob.Script;
var Argument = GGlob.Argument;




//********************************
//Fetchers
//********************************

FetcherRegistry.addFetcher(
  FetcherMaker.make("Active Moiety PT", function(simpleSub) {
    return simpleSub.fetch("relationships")
      .andThen(function(r) {
        return _.chain(r)
         .filter({type:"ACTIVE MOIETY"})
         .map(function(ro){return ro.relatedSubstance.refPname;})
         .value()
         .join("|");
      });
  }).addTag("Substance")
);

FetcherRegistry.addFetcher(
  FetcherMaker.make("Active Moiety ID", function(simpleSub) {
    return simpleSub.fetch("relationships")
      .andThen(function(r) {
        return _.chain(r)
         .filter({type:"ACTIVE MOIETY"})
         .map(function(ro){return ro.relatedSubstance.approvalID;})
         .value()
         .join("|");
      });
  }).addTag("Substance")
);

FetcherRegistry.addFetcher(
  FetcherMaker.make("Smiles", function(simpleSub) {
    return simpleSub.fetch("structure/smiles");
  }).addTag("Chemical")
);

FetcherRegistry.addFetcher(
  FetcherMaker.make("InChIKey", function(simpleSub) {
    return simpleSub.fetch("structure!$inchikey()")
                    .andThen(function(ik){
                        var iks=ik.split("=");
                        if(iks.length>1){
                            return iks[1];
                        }else{
                            return null;
                        }
                    });
  }).addTag("Chemical")
);


FetcherRegistry.addFetcher(
  FetcherMaker.make("Exact Test", function(simpleSub) {
    return simpleSub.fetch("structure/smiles")
                    .andThen(function(smi){
                        return SubstanceFinder.getExactStructureMatches(smi)
                                          .andThen(function(s){
                                            return _.chain(s.content)
                                                    .map(function(o){return o._name;})
                                                    .value().join("|");
                                          });
                    });

  }).addTag("Tests")
);

FetcherRegistry.addFetcher(
  FetcherMaker.make("Image URL", function(simpleSub) {
    var base= GlobalSettings.getBaseURL().replace(/api.*/g,"");
    var imgurl = base + "img/" + simpleSub.uuid + ".png?size=300";
    return JPromise.ofScalar(imgurl);
  })
);

FetcherRegistry.addFetcher(
  FetcherMaker.make("Protein Sequence", function(simpleSub) {
    return simpleSub.fetch("protein/subunits!(sequence)!join(;)");
  }).addTag("Protein")
);

FetcherRegistry.addFetcher(
  FetcherMaker.make("UUID", function(simpleSub) {
    return JPromise.ofScalar(simpleSub.uuid);
  }).addTag("Identifiers")
);

FetcherRegistry.addFetcher(
  FetcherMaker.make("Lychi", function(simpleSub) {
    return simpleSub.fetch("structure/properties(label:LyChI_L4)($0)/term");
  }).addTag("Chemical")
);

FetcherRegistry.addFetcher(
  FetcherMaker.make("Substance Class", function(simpleSub) {
    return JPromise.ofScalar(simpleSub.substanceClass);
  }).addTag("Substance")
);

FetcherRegistry.addFetcher(
  FetcherMaker.make("Record Access", function(simpleSub) {
    return JPromise.ofScalar(simpleSub.access.join(";"));
  }).addTag("Record")
);

FetcherRegistry.addFetcher(
  FetcherMaker.make("All Names", function(simpleSub) {

    return simpleSub.fetch("names!(name)!join(|)").andThen(function(n) {
      return n.replace(/%7C/g, "|");
    });
  }).addTag("Substance")
);

FetcherRegistry.addFetcher(FetcherMaker.makeCodeFetcher("BDNUM").addTag("Identifiers"))
  .addFetcher(FetcherMaker.makeCodeFetcher("WHO-ATC", "ATC Code").addTag("Substance"))
  .addFetcher(FetcherMaker.makeCodeFetcher("CAS", "CAS Numbers").addTag("Identifiers"))
  .addFetcher(FetcherMaker.makeCodeFetcher("EVMPD", "EVMPD Code").addTag("Identifiers"));



FetcherRegistry.addFetcher(FetcherMaker.makeScalarFetcher("_name", "Preferred Term").addTag("Substance"))
  .addFetcher(FetcherMaker.makeScalarFetcher("_approvalIDDisplay", "Approval ID").addTag("Identifiers"))
  .addFetcher(FetcherMaker.makeScalarFetcher("createdBy", "Created By").addTag("Record"))
  .addFetcher(FetcherMaker.makeScalarFetcher("created", "Created Date").andThen(gUtil.toDate).addTag("Record"))
  .addFetcher(FetcherMaker.makeScalarFetcher("lastEditedBy", "Last Edited By").addTag("Record"))
  .addFetcher(FetcherMaker.makeScalarFetcher("lastEdited", "Last Edited Date").andThen(gUtil.toDate).addTag("Record"))
  .addFetcher(FetcherMaker.makeScalarFetcher("version", "Version").addTag("Record"))
  .addFetcher(FetcherMaker.makeAPIFetcher("structure/formula", "Molecular Formula").addTag("Chemical"))
  .addFetcher(FetcherMaker.makeAPIFetcher("structure/mwt", "Molecular Weight").addTag("Chemical"));

FetcherRegistry.addFetcher(
  FetcherMaker.make("Tags", function(simpleSub) {
    return simpleSub.fetch("tags!(term)!distinct()!sort()!join(|)").andThen(function(n) {
      return n.replace(/%7C/g, "|");
    });
  }).addTag("Record")
);


FetcherRegistry.addFetcher(
  FetcherMaker.make("Equivalance Factor", function(simpleSub) {

    return simpleSub.fetch("structure/mwt").andThen(function(mwt) {

      return simpleSub.fetch("relationships")
                      .andThen(function(r) {
                            var amuuid=_.chain(r)
                                 .filter({type:"ACTIVE MOIETY"})
                                 .map(function(ro){return ro.relatedSubstance.refuuid;})
                                 .value()[0];
                            return SubstanceFinder.get(amuuid)
                                           .andThen(function(amsub){
                                                return amsub.fetch("structure/mwt").andThen(function(mwt2){
                                                    return mwt2/mwt;
                                                });
                                           });
                      });
    });
  }).addTag("Chemical")
);

FetcherRegistry.addFetcher(
  FetcherMaker.make("Latin Binomial", function(simpleSub) {
    return simpleSub.fetch("structurallyDiverse!$select(organismGenus,organismSpecies)!join(%20)").andThen(function(n) {
      return n.replace(/%20/g, " ");
    });
  }).addTag("Structurally Diverse")
);

FetcherRegistry.addFetcher(
  FetcherMaker.make("Author", function(simpleSub) {
    return simpleSub.fetch("structurallyDiverse/organismAuthor");
  }).addTag("Structurally Diverse")
);

FetcherRegistry.addFetcher(
  FetcherMaker.make("Part", function(simpleSub) {
    return simpleSub.fetch("structurallyDiverse/part!(term)!join(|)").andThen(function(n) {
      return n.replace(/%7C/g, "|");
    });
  }).addTag("Structurally Diverse")
);



FetcherRegistry.addFetcher(
  FetcherMaker.make("Stereo Type", function(simpleSub) {
    return simpleSub.fetch("structure/stereoChemistry");
  }).addTag("Chemical")
);

FetcherRegistry.addFetcher(
  FetcherMaker.make("Record URL", function(simpleSub) {
    return JPromise.ofScalar(GlobalSettings.getHomeURL() + "substance/" + simpleSub.uuid);
  }).addTag("Record")
);


//If these names are directly registered
FetcherRegistry.addFetcher(
  FetcherMaker.make("Bracket Terms", function(simpleSub) {
    return simpleSub.fetch("names!(name)").andThen(function(n) {
      return _.chain(n)
              .filter(function(n1) {
                return n1.match(/\[.*\]/g);
              })
              .value().join("|");
    });
  }).addTag("Substance")
);

//********************************
//Scripts
//********************************

Script.builder().mix({name: "Add Name", description: "Adds a name to a substance record"})
  .addArgument({
    "key": "uuid", name: "UUID", description:"UUID of the substance record", required:true
  })
  .addArgument({
    "key": "name", name: "NAME", description:"Name text of the new name", required:true
  })
  .addArgument({
    "key": "nameType", name: "NAME TYPE", description:"Name text of the new name", default:"cn", required: false
  })
  .addArgument({
    "key": "public", name: "PD", description:"Public Domain status of the name (sets access for reference as well)", default:false, required: false
  })
  .addArgument({
    "key": "referenceType", name: "REFERENCE TYPE", description:"Type of reference", default:"SYSTEM", required: false
  })
  .addArgument({
    "key": "referenceCitation", name: "REFERENCE CITATION", description:"Citation text for reference", required:true
  })
  .addArgument({
    "key": "referenceUrl", name: "REFERENCE URL", description:"URL for the reference", required:false
  })
 .addArgument({
    "key": "changeReason", name: "CHANGE REASON", default: "Added Name", description:"Text for the record change", required: false
  })
  .setExecutor(function(args) {
    var uuid = args.uuid.getValue();
    var name = args.name.getValue();
    var nameType = args.nameType.getValue();
    var public = args.public.getValue();
    var referenceType = args.referenceType.getValue();
    var referenceCitation = args.referenceCitation.getValue();
    var referenceUrl = args.referenceUrl.getValue();

    var reference = Reference.builder().mix({citation:referenceCitation, docType:referenceType});
    if (referenceUrl && referenceUrl.length > 0) {
      reference = reference.setUrl(referenceUrl);
    }
    if (public && public === "true" || public === true || public === "Y") {
      reference.setPublic(true);
      reference.setPublicDomain(true);
    } else {
      reference.setPublic(false);
      reference.setPublicDomain(false);
    }

    var name = Name.builder().setName(name)
      .setType(nameType)
      .setPublic(public)
      .addReference(reference);

    return SubstanceFinder.get(uuid)
      .andThen(function(s) {
        return s.patch().addData(name)
            .add("/changeReason",args.changeReason.getValue())
            .apply()
            .andThen(_.identity);
      });
  })
  .useFor(function(s){Scripts.addScript(s);});





Script.builder().mix({name: "Add Code", description: "Adds a code to a substance record"})
  .addArgument({
    "key": "uuid", name: "UUID", description:"UUID of the substance record", required:true
  })
  .addArgument({
    "key": "code", name: "CODE", description:"Code text of the new code", required:true
  })
  .addArgument({
    "key": "codeSystem", name: "CODE SYSTEM", description:"Code system of the new code", required:true
  })
  .addArgument({
    "key": "codeType", name: "CODE TYPE", description:"Code type of code. For instance, whether it's a primary code", default:"PRIMARY", required: false
  })
  .addArgument({
    "key": "url", name: "CODE URL", description:"URL to evaluate this code (this is distinct from the reference URL)", required:false
  })
  .addArgument({
    "key": "public", name: "PD", description:"Public Domain status of the code (sets access for reference as well)", default:false, required: false
  })
  .addArgument({
    "key": "referenceType", name: "REFERENCE TYPE", description:"Type of reference", default:"SYSTEM", required: false
  })
  .addArgument({
    "key": "referenceCitation", name: "REFERENCE CITATION", description:"Citation text for reference", required:true
  })
  .addArgument({
    "key": "referenceUrl", name: "REFERENCE URL", description:"URL for the reference", required:false
  })
  .addArgument({
    "key": "changeReason", name: "CHANGE REASON", default: "Added Code", description:"Text for the record change", required: false
  })
  .setExecutor(function(args) {
    var uuid = args.uuid.getValue();
    var code = args.code.getValue();
    var codeType = args.codeType.getValue();
    var codeSystem = args.codeSystem.getValue();
    var url = args.url.getValue();
    var public = args.public.getValue();
    var referenceType = args.referenceType.getValue();
    var referenceCitation = args.referenceCitation.getValue();
    var referenceUrl = args.referenceUrl.getValue();

    var reference = Reference.builder().mix({citation:referenceCitation, docType:referenceType});
    if (referenceUrl && referenceUrl.length > 0) {
      reference = reference.setUrl(referenceUrl);
    }
    if (public && public === "true" || public === true || public === "Y") {
      reference.setPublic(true);
      reference.setPublicDomain(true);
    } else {
      reference.setPublic(false);
      reference.setPublicDomain(false);
    }

    var code = Code.builder().setCode(code)
      .setType(codeType)
      .setCodeSystem(codeSystem)
      .setPublic(public)
      .addReference(reference);

    if(url){
      code.setUrl(url)
    }

    return SubstanceFinder.get(uuid)
      .andThen(function(s) {
        return s.patch().addData(code)
            .add("/changeReason",args.changeReason.getValue())
            .apply()
            .andThen(_.identity);
      });
  })
  .useFor(function(s){Scripts.addScript(s);});
