(ns unified-log-processing-clj.core
  (:require [clj-kafka.consumer.zk :as zk]
            [clj-kafka.core]
            [clj-kafka.new.producer :as kp]
            [cheshire.core :refer :all])
  (:import
   [com.maxmind.geoip LookupService])
  (:gen-class))

(def maxmind (LookupService. "resources/GeoLiteCity.dat" LookupService/GEOIP_MEMORY_CACHE))

(def consumer-config {"zookeeper.connect" "localhost:2181"
                      "group.id" "ulp-03"
                      "auto.offset.reset" "smallest"
                      "auto.commit.interval.ms" "1000"
                      "session.timeout.ms" "30000"
                      "auto.commit.enable" "true"})

(def consumer-dev-config {"zookeeper.connect" "localhost:2181"
                          "group.id" "ulp-03"
                          "auto.offset.reset" "smallest"
                          "auto.commit.enable" "false"})

(defn read-one []
  (->> (clj-kafka.core/with-resource [c (zk/consumer consumer-dev-config)]
         zk/shutdown
         (doall
          (take 1 (zk/messages c "raw-events"))))
       first
       :value))

(defn write-event [topic event]
  (let [event-in-string (generate-string event)]
    (with-open [p (kp/producer {"bootstrap.servers" "127.0.0.1:9092"}
                               (kp/string-serializer)
                               (kp/string-serializer))]
      @(kp/send p (kp/record topic event-in-string)))))

(defn parse-event [raw]
  (parse-string (String. raw) true))

(defn valid-event? [event]
  (get-in event [:shopper :ipAddress]))

(defn add-location [event]
  (let [ip (get-in event [:shopper :ipAddress])
        location (.getLocation maxmind ip)
        country (.countryName location)
        city (.city location)
        shopper-info (into (:shopper event) {:country country
                                             :city city})]
    (assoc-in event [:shopper] shopper-info)))

(defn -main
  [& args]
  (println "starting...")
  (doseq [raw-event (zk/stream-seq (zk/create-message-stream (zk/consumer consumer-config) "raw-events"))]
    (try
      (let [event (parse-event (:value raw-event))]
        (if (valid-event? event)
            (write-event "enriched-events" (add-location event))
            (write-event "bad-events" {:error "shopper.ipAddress missing"})))
      (catch Exception e
        (write-event "bad-events" {:error (str (->> e .getClass .getSimpleName) ": " (.getMessage e))})))))
