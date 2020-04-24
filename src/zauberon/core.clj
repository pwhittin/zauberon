(ns zauberon.core
  (:require [clojure.pprint :refer [pprint]]
            [clojure.tools.cli :as cli]
            [zauberon.default-simulator :as sim]
            [zauberon.simulator-protocol :as sp])
  (:gen-class))

(defn simulate
  [iterations zauberon-count ctx
   {:keys [initialize-zauberons-fn new-position-fn locate-collisions-fn collision-fn output-fn]}]
  (loop [iteration iterations
         ctx-zauberons-map (initialize-zauberons-fn {:ctx ctx, :zauberon-count zauberon-count})]
    (if (zero? iteration)
      (str "Executed " iterations " Iterations")
      (recur (dec iteration)
             (->> ctx-zauberons-map (new-position-fn) (locate-collisions-fn) (collision-fn) (output-fn iteration))))))

(def core-cli-options
  [["-i" "--iterations ITERATIONS" "Number of iterations"
    :default 0
    :parse-fn #(Integer/parseInt %)
    :validate [#(> % 0) "Must be a number greater than zero"]]
   ["-z" "--zauberons ZAUBERONS" "Number of zauberons"
    :default 0
    :parse-fn #(Integer/parseInt %)
    :validate [#(> % 0) "Must be a number greater than zero"]]
   ["-h" "--help"]])

(defn validate-cli-map [cli-map]
  (when (cli-map :errors)
    (println)
    (println "Errors")
    (println "======")
    (doseq [error (cli-map :errors)]
      (println error))
    (println)
    (System/exit 1))
  cli-map)

(defn -main [& args]
  (try
    (let [zs (sim/create)
          cli-options (->> core-cli-options (sp/add-cli-options zs))
          cli-map (->> (cli/parse-opts args cli-options) (sp/validate-cli-map zs) validate-cli-map)
          simulator {:collision-fn            (sp/collision-fn zs cli-map)
                     :initialize-zauberons-fn (sp/initialize-zauberons-fn zs cli-map)
                     :locate-collisions-fn    (sp/locate-collisions-fn zs cli-map)
                     :new-position-fn         (sp/new-position-fn zs cli-map)
                     :output-fn               (sp/output-fn zs cli-map)}
          iterations (get-in cli-options [:options :iterations])
          zauberon-count (get-in cli-options [:options :zauberons])
          ctx (sp/initialize zs cli-map)]
      (println)
      (println (sp/description zs ctx))
      (try
        (println (simulate iterations zauberon-count ctx simulator))
        (finally (sp/initialize zs ctx)))
      (println))
    (catch Exception e
      (println "Exception:")
      (pprint e)
      (System/exit 1)))
  (System/exit 0))
