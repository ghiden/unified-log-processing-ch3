(defproject unified-log-processing-clj "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [clj-kafka "0.3.4"]
                 [cheshire "5.5.0"]
                 [com.maxmind.geoip/geoip-api "1.2.15"]]
  :main ^:skip-aot unified-log-processing-clj.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
