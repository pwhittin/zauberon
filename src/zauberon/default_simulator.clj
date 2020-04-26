(ns zauberon.default-simulator
  (:require [clojure.java.io :as io]
            [clojure.string :as cstr]
            [zauberon.simulator-protocol :as simulator-protocol]))

(def box 100000)
(def angle-steps 2)
(def helix-advance 10)
(def radius 1000)

(def pi*2 (*' 2 (Math/PI)))
(def angle-steps-adjustment (* pi*2 (/ angle-steps 360)))
(def xyz-helix [(/ box 2) (/ box 2) (/ box 2)])
(def xyz-zauberon [0 0 0])

(defn cos [r]
  (Math/cos r))

(defn sin [r]
  (Math/sin r))

(defn internal-pitch-yaw-roll->xform-matrix [pitch-yaw-roll]
  (let [[cos-pitch cos-yaw cos-roll] (map cos pitch-yaw-roll)
        [sin-pitch sin-yaw sin-roll] (map sin pitch-yaw-roll)]
    [[(*' cos-pitch cos-yaw)
      (-' (*' sin-roll sin-pitch cos-yaw) (*' cos-roll sin-yaw))
      (+' (*' sin-roll sin-yaw) (*' cos-roll sin-pitch cos-yaw))]
     [(*' cos-pitch sin-yaw)
      (+' (*' cos-roll cos-yaw) (*' sin-roll sin-pitch sin-yaw))
      (-' (*' cos-roll sin-pitch sin-yaw) (*' sin-roll cos-yaw))]
     [(-' sin-pitch)
      (*' sin-roll cos-pitch)
      (*' cos-roll cos-pitch)]]))
(def pitch-yaw-roll->xform-matrix (memoize internal-pitch-yaw-roll->xform-matrix))

(defn rotate-3d [pitch-yaw-roll xyz-0]
  (->> (map #(map *' %1 %2) (pitch-yaw-roll->xform-matrix pitch-yaw-roll) (repeat xyz-0))
       (map #(apply +' %))))

(defn random-angle []
  (*' pi*2 (/ (rand-int 360) 360)))

(defn initialize-zauberon [helix-advance xyz-helix radius]
  (let [pitch-yaw-roll [(random-angle) (random-angle) (random-angle)]
        rotation (if (even? (rand-int 2)) :right :left)]
    {:angle          (if (= rotation :right) 0 pi*2)
     :rotation       rotation
     :hv             (rotate-3d pitch-yaw-roll [helix-advance 0 0])
     :pitch-yaw-roll pitch-yaw-roll
     :radius         radius
     :xyz-helix      xyz-helix
     :xyz-zauberon   xyz-zauberon}))

(defn adjust-angle-right [angle]
  (let [possible-new-angle (+' angle angle-steps-adjustment)]
    (if (>= possible-new-angle pi*2) 0 possible-new-angle)))

(defn adjust-angle-left [angle]
  (let [possible-new-angle (-' angle angle-steps-adjustment)]
    (if (< possible-new-angle 0) pi*2 possible-new-angle)))

(defn new-zauberon-position [{:keys [angle rotation hv pitch-yaw-roll radius xyz-helix] :as zauberon}]
  (let [new-angle ((if (= rotation :right) adjust-angle-right adjust-angle-left) angle)
        xyz-0 [helix-advance (*' radius (cos new-angle)) (*' radius (sin new-angle))]
        xyz-1 (rotate-3d pitch-yaw-roll xyz-0)]
    (assoc zauberon
      :angle new-angle
      :xyz-helix (map +' xyz-helix hv)
      :xyz-zauberon (map +' xyz-helix xyz-1))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; protocol implementation
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn add-cli-options [cli-options]
  (conj cli-options ["-o" "--output-file OUTPUTFILE" "File spec of output file"
                     :default "zauberons.dat"
                     :validate [#(not (cstr/blank? %)) "Must be non blank"]]))

(defn collision [{:keys [zauberon-collisions] :as ctx-zauberons}]
  (assoc ctx-zauberons :zauberons (map first zauberon-collisions)))

(defn description [{:keys [output-file] :as ctx}]
  (str "Default Simulator: Output = '" output-file "'"))

(defn finalize [ctx]
  (.close (ctx :output-writer)))

(defn initialize [cli-map]
  (let [iterations (get-in cli-map [:options :iterations])
        output-file (get-in cli-map [:options :output-file])
        output-writer (io/writer output-file)]
    (.write output-writer "# x y z\n")
    {:output-file       output-file,
     :output-writer     output-writer
     :iteration-divisor (/ iterations 10)}))

(defn initialize-zauberons [{:keys [ctx zauberon-count]}]
  {:ctx       ctx
   :zauberons (for [_ (range zauberon-count)] (initialize-zauberon helix-advance xyz-helix radius))})

(defn locate-collisions [{:keys [ctx zauberons]}]
  {:ctx                 ctx
   :zauberon-collisions (map hash-set zauberons)})

(defn new-position [{:keys [ctx zauberons] :as ctx-zauberons}]
  (assoc ctx-zauberons :zauberons (map new-zauberon-position zauberons)))

(defn output [iteration {:keys [ctx zauberons] :as ctx-zauberons}]
  (when (zero? (mod iteration (ctx :iteration-divisor))) (println iteration))
  (doseq [zauberon zauberons]
    (let [[x y z] (zauberon :xyz-zauberon)]
      (.write (ctx :output-writer) (str x " " y " " z "\n"))))
  ctx-zauberons)

(defn validate-cli-map [cli-map]
  (letfn [(get-canonical-path [output-file]
            (if (cstr/blank? output-file) "" (.getCanonicalPath (java.io.File. output-file))))
          (add-error [cli-map output-dir error-str]
            (update cli-map :errors conj (str "The specified output directory '" output-dir "' " error-str)))]
    (let [output-spec (get-canonical-path (get-in cli-map [:options :output-file]))
          output-dir (-> output-spec (java.io.File.) (.getParentFile))]
      (cond
        (or (nil? output-dir) (not (.exists output-dir))) (add-error cli-map output-dir "does not exist")
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
