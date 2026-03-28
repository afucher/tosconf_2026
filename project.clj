(defproject tosconf-2026 "0.1.0"
  :description "TosConf 2026 - Pedestal + http-kit demo"
  :dependencies [[org.clojure/clojure "1.12.0"]
                 [io.pedestal/pedestal.http-kit "0.8.2-beta-2"]
                 [org.slf4j/slf4j-simple "2.0.17"]
                 [nrepl/nrepl "1.5.1"]
                 [cider/cider-nrepl "0.55.7"]]
  :main hello
  :aot [hello]
  :uberjar-name "app.jar"
  :source-paths ["src"]
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
