(ns hello
  (:require
   [io.pedestal.connector :as conn]
   [io.pedestal.http.http-kit :as hk]
   [nrepl.server :as nrepl.server])
  (:gen-class))

(defn greet [_request]
  "<h1>Hello, TosConf!</h1>\n")

(defn greet-handler [request]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body   (greet request)})


(def routes
  #{["/greet" :get #'greet-handler :route-name :greet]})

(defn http-port []
  (Integer/parseInt (or (System/getenv "PORT") "8890")))

(defn nrepl-port []
  (Integer/parseInt (or (System/getenv "NREPL_PORT") "8891")))

(defn host []
  (or (System/getenv "HOST") "127.0.0.1"))

(defn create-connector []
  (-> (conn/default-connector-map (host) (http-port))
      (conn/with-default-interceptors)
      (conn/with-routes routes)
      (hk/create-connector nil)))

(defonce server (atom nil))

(defn start-server []
  (reset! server (conn/start! (create-connector))))

(defn start []
  (nrepl.server/start-server :port (nrepl-port) :bind (host))
  (start-server))

(defn -main [& _args]
  (start)
  @(promise))


(comment
  (start)
  (slurp "http://localhost:8890/greet?name=TosConf")
  (class @server)
  (conn/stop! @server)

  (start-server)
  #_())
