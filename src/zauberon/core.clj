(ns zauberon.core
  (:require [clojure.pprint :refer [pprint]]
            [clojure.tools.cli :refer [parse-opts]]
            [zauberon.default-simulator :as sim]
            [zauberon.simulator-protocol :as sp])
  (:gen-class))

(defn simulate
  [iterations zauberon-count {:keys [initialize-fn new-position-fn locate-collisions-fn collision-fn output-fn]}]
  (loop [iteration iterations
         zauberons (initialize-fn zauberon-count)]
    (if (zero? iteration)
      (println "Executed" iterations "Iterations")
      (recur (dec iteration)
             (->> zauberons (new-position-fn) (locate-collisions-fn) (collision-fn) (output-fn iteration))))))

(def core-cli-options
  [["-i" "--iterations ITERATIONS" "Number of iterations"
    :parse-fn #(Integer/parseInt %)
    :validate [#(> % 0) "Must be a number greater than zero"]]
   ["-z" "--zauberons ZAUBERONS" "Number of zauberons"
    :parse-fn #(Integer/parseInt %)
    :validate [#(> % 0) "Must be a number greater than zero"]]
   ["-h" "--help"]])

(defn -main [& args]
  (try
    (let [zs (sim/create)
          cli-options (->> core-cli-options
                           (sp/initialize-cli-options zs)
                           (sp/new-position-cli-options zs)
                           (sp/locate-collisions-cli-options zs)
                           (sp/collision-cli-options zs)
                           (sp/output-cli-options zs))
          simulator {:initialize-fn        (sp/initialize-fn zs cli-options)
                     :new-position-fn      (sp/new-position-fn zs cli-options)
                     :locate-collisions-fn (sp/locate-collisions-fn zs cli-options)
                     :collision-fn         (sp/collision-fn zs cli-options)
                     :output-fn            (sp/output-fn zs cli-options)}]
      (simulate
        (get-in cli-options [:options :iterations])
        (get-in cli-options [:options :zauberons])
        simulator)
      (catch Exception e
        (println "Exception:")
        (pprint e)
        (System/exit 1))))
  (System/exit 0))
