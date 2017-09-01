package ix.utils;

import play.mvc.Http;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import ix.core.util.StreamUtil;
import play.mvc.Controller;

public class RequestHelper {
    private static class RequestInformation {
        Http.Request request = null;
        Map<String, String[]> params = null;

        public Http.Request getRequest() {
            return this.request;
        }

        public Map<String, String[]> getParams() {
            return this.params;
        }

        public RequestInformation setRequest(Http.Request req) {
            this.request = req;
            return this;
        }

        public RequestInformation setParams(Map<String, String[]> params) {
            this.params = params;
            return this;
        }

        public static RequestInformation of(Http.Request request) {
            RequestInformation ri = new RequestInformation();
            ri.request = request;
            return ri;
        }
    }

    private static class RLELinkedHashMap<K, V> extends LinkedHashMap<K, V> {
        /**         * 		 */
        private static final long serialVersionUID = 1L;
        private int max = 20;

        public RLELinkedHashMap(int max) {
            super(max, .75f, false);
            this.max = max;
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
            return size() > max;
        }
    }

    static RLELinkedHashMap<Http.Request, RequestInformation> lhm = new RLELinkedHashMap<Http.Request, RequestInformation>(20);

    public static Http.Request request() {
        Http.Request req = lhm.getOrDefault(Controller.request(), RequestInformation.of(Controller.request())).getRequest();
        if (req == null) return Controller.request();
        return req;
    }

    public static Map<String, String[]> queryParams() {
        System.out.println("Getting q params");
        Map<String, String[]> query = new HashMap<String, String[]>();
        query.putAll(request().queryString());
        Optional.ofNullable(lhm.get(Controller.request())).map(ri -> ri.getParams()).filter(mp -> mp != null).ifPresent(m -> {
            m.entrySet().stream().map(Tuple::of).forEach(t -> {
                String k = t.k();
                String[] add = query.getOrDefault(k, new String[]{});
                add = StreamUtil.with(Arrays.stream(add)).and(t.v()).stream().toArray(i -> new String[i]);
                query.put(k, add);
                System.out.println("Adding:" + k + Arrays.toString(add));
            });
        });
        return query;
    }

    public static void setLocalRequest(Http.Request req) {
        lhm.computeIfAbsent(Controller.request(), k -> new RequestInformation()).setRequest(req);
    }

    public static void setAdditionalParams(Map<String, String[]> add) {
        lhm.computeIfAbsent(Controller.request(), k -> new RequestInformation()).setParams(add);
    }

    public static void addAdditionalParam(String key, String val) {
        Map<String, String[]> qm = Optional.ofNullable(lhm.getOrDefault(Controller.request(), new RequestInformation()).getParams()).orElse(new HashMap<String, String[]>());
        String[] ok = qm.getOrDefault(key, new String[]{});
        String[] newarr = StreamUtil.with(Arrays.stream(ok)).and(val).stream().toArray(i -> {
            return new String[i];
        });
        qm.put(key, newarr);
        setAdditionalParams(qm);
    }

    public static String getViewSpace(String defaultView) {
        return queryParams().getOrDefault("viewName", new String[]{defaultView})[0];
    }

    public static void setViewSpace(String viewName) {
        addAdditionalParam("viewName", viewName);
    }
}

