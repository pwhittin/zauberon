(ns zauberon.core
  (:require [clojure.pprint :refer [pprint]]
            [clojure.tools.cli :as cli]
            [zauberon.default-simulator :as sim]
            [zauberon.simulator-protocol :as sp])
  (:gen-class))

(defn simulate
  [iterations zauberons ctx
   {:keys [initialize-zauberons-fn new-position-fn locate-collisions-fn collision-fn output-fn]}]
  (loop [iteration iterations
         ctx-zauberons-map (initialize-zauberons-fn {:ctx ctx, :zauberon-count zauberons})]
    (if (zero? iteration)
      iterations
      (recur (dec iteration)
             (->> ctx-zauberons-map
                  (new-position-fn)
                  (locate-collisions-fn)
                  (collision-fn)
                  (output-fn (inc (- iterations iteration))))))))

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
  (letfn [(try-add-error [cli-map predicate error-str]
            (if (predicate cli-map) cli-map (update cli-map :errors conj error-str)))]
    (-> cli-map
        (try-add-error #(> (get-in % [:options :iterations]) 0) "Iterations must be greater than zero")
        (try-add-error #(> (get-in % [:options :zauberons]) 0) "Zauberons must be greater than zero"))))

(defn valid-cli-map? [cli-map]
  (not (cli-map :errors)))

(defn print-cli-options [cli-map]
  (println "Command Line Options:")
  (println "  --------------------------------------------------------------------")
  (println "  short-form, long-form [long-form-value-name] [default] [description]")
  (println "  --------------------------------------------------------------------")
  (println (cli-map :summary)))

(defn print-cli-map-errors [cli-map]
  (print-cli-options cli-map)
  (println)
  (print "Error(s):")
  (doseq [error (cli-map :errors)]
    (println (str "\n" error))))

(defn run-simulation [zs cli-map [iterations zauberons]]
  (let [simulator {:collision-fn            (sp/collision-fn zs cli-map)
                   :initialize-zauberons-fn (sp/initialize-zauberons-fn zs cli-map)
                   :locate-collisions-fn    (sp/locate-collisions-fn zs cli-map)
                   :new-position-fn         (sp/new-position-fn zs cli-map)
                   :output-fn               (sp/output-fn zs cli-map)}
        ctx (sp/initialize zs cli-map)]
    (println)
    (println (sp/description zs ctx))
    (try
      (println (str "\nExecuted " (simulate iterations zauberons ctx simulator) " iteration(s) of "
                    iterations " on " zauberons " zauberon(s)"))
      (finally (sp/finalize zs ctx)))
    (println)))

(defn args->cli-map [args cli-options zs]
  (->> (sort-by first cli-options) (cli/parse-opts args) (sp/validate-cli-map zs) validate-cli-map))

(defn help? [cli-map]
  (get-in cli-map [:options :help]))

(defn print-exception [e]
  (println)
  (println "***** Exception:")
  (pprint e)
  (println))

(defn main [args exit-fn]
  (try
    (let [zs (sim/create)
          cli-options (->> core-cli-options (sp/add-cli-options zs))
          cli-map (args->cli-map args cli-options zs)]
      (if (valid-cli-map? cli-map)
        (if (help? cli-map)
          (do (println) (print-cli-options cli-map) (println) (exit-fn 0))
          (do (run-simulation zs cli-map ((juxt :iterations :zauberons) (cli-map :options))) (exit-fn 0)))
        (do (println) (print-cli-map-errors cli-map) (println) (exit-fn 1))))
    (catch Exception e (print-exception e) (exit-fn 1))))

(defn -main [& args]
  (main args #(System/exit %)))

(comment

  (time (main ["-i" "1000" "-z" "1000"] identity))

  ; comment
  )