(ns zauberon.default-simulator
  (:require [clojure.string :as cstr]
            [zauberon.simulator-protocol :as simulator-protocol]))

(defn add-cli-options [cli-options]
  (conj cli-options [nil "--output-file OUTPUTFILE" "File spec of output file"
                     :default ""
                     :validate [#(not (cstr/blank? %)) "Must be non blank"]]))

(defn collision [{:keys [ctx zauberon-collisions]}]
  )

(defn description [{:keys [output-file]}]
  (str "Default Simulator With Output To '" (.getPath output-file) "'"))

(defn finalize [{:keys [output-file]}]
  (try
    (.close output-file)
    (catch Exception e)))

(defn initialize [cli-map]
  )

(defn initialize-zauberons [{:keys [ctx zauberon-count]}]
  )

(defn locate-collisions [{:keys [ctx zauberons]}]
  )

(defn new-position [{:keys [ctx zauberons]}]
  )

(defn output [iteration {:keys [ctx zauberons]}]
  )

(defn validate-cli-map [cli-map]
  (letfn [(add-error [cli-map output-dir error-str]
            (update cli-map :errors conj (str "The specified output directory '" output-dir "' " error-str)))]
    (let [output-dir (-> (get-in cli-map [:options :output-file]) (.getParentFile) (java.io.File.))]
      (cond
        (not (.exists output-dir)) (add-error cli-map output-dir "does not exist")
        (not (.isDirectory output-dir)) (add-error cli-map output-dir "is not a directory")
        :else cli-map))))

(defn create []
  (reify simulator-protocol/Simulator
    (add-cli-options [_ cli-options] (add-cli-options cli-options))
    (collision-fn [_ _] collision)
    (description [_ ctx] (description ctx))
    (finalize [_ ctx] (finalize ctx))
    (initialize [_ cli-map] (initialize cli-map))
    (initialize-zauberons-fn [_ _] initialize-zauberons)
    (locate-collisions-fn [_ _] locate-collisions)
    (new-position-fn [_ _] new-position)
    (output-fn [_ cli-map] output)
    (validate-cli-map [_ cli-map] (validate-cli-map cli-map))))
