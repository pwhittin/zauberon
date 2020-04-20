(ns zauberon.core
  (:require [clojure.pprint :refer [pprint]])
  (:gen-class))

(defn simulate
  [{:keys [iterations zauberon-count initialize-fn new-position-fn locate-collisions-fn collision-fn output-fn]}]
  (loop [iteration iterations
         zauberons (initialize-fn zauberon-count)]
    (if (zero? iteration)
      (println "Executed" iterations "Iterations")
      (recur (dec iteration)
             (-> zauberons new-position-fn locate-collisions-fn collision-fn (output-fn iteration))))))

(defn -main [& args]
  (try
    (simulate {:iterations           number-of-iterations
               :zauberon-count       number-of-zauberons
               :initialize-fn        initialize-fn
               :new-position-fn      new-position-fn
               :locate-collisions-fn locate-collisions-fn
               :collision-fn         collision-fn
               :output-fn            output-fn})
    (catch Exception e
      (println "Exception:")
      (pprint e)
      (System/exit 1Abp2)))
  (System/exit 0))
