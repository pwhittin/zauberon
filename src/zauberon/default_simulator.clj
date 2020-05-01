(ns zauberon.default-simulator
  (:require [clojure.java.io :as io]
            [clojure.string :as cstr]
            [zauberon.simulator-protocol :as simulator-protocol]))

(def pi*2 (* 2 (Math/PI)))

(def box 100000.0)
(def angle-steps-config [1000 1 15])
(def helix-advance-config [1000 1 50])
(def radius-config [1000 500 5000])
(def space (/ box 2.0))

(def center-x (/ box 2))
(def center-y (/ box 2))
(def center-z (/ box 2))

(def back-boundry (+ center-x (/ space 2)))
(def front-boundry (- center-x (/ space 2)))
(def right-boundry (+ center-y (/ space 2)))
(def left-boundry (- center-y (/ space 2)))
(def up-boundry (- center-z (/ space 2)))
(def down-boundry (+ center-z (/ space 2)))
(def negitive-space (* -1 space))
(def positive-space space)

(def xyz-helix [(/ box 2) (/ box 2) (/ box 2)])
(def xyz-zauberon [0 0 0])

(defn d2r [degrees]
  (* pi*2 (/ degrees 360)))

(defn cos [r]
  (Math/cos r))

(defn sin [r]
  (Math/sin r))

(defn pitch-yaw-roll->xform-matrix [pitch-yaw-roll]
  (let [[cos-pitch cos-yaw cos-roll] (map cos pitch-yaw-roll)
        [sin-pitch sin-yaw sin-roll] (map sin pitch-yaw-roll)]
    [[(* cos-pitch cos-yaw)
      (- (* sin-roll sin-pitch cos-yaw) (* cos-roll sin-yaw))
      (+ (* sin-roll sin-yaw) (* cos-roll sin-pitch cos-yaw))]
     [(* cos-pitch sin-yaw)
      (+ (* cos-roll cos-yaw) (* sin-roll sin-pitch sin-yaw))
      (- (* cos-roll sin-pitch sin-yaw) (* sin-roll cos-yaw))]
     [(- sin-pitch)
      (* sin-roll cos-pitch)
      (* cos-roll cos-pitch)]]))

(defn rotate-3d [xform-matrix xyz-0]
  (->> (map #(map * %1 %2) xform-matrix (repeat xyz-0))
       (map #(apply + %))))

(defn random-angle []
  (* pi*2 (/ (rand-int 360) 360)))

(defn random-in-range [[divisions min max]]
  (let [r (inc (rand-int divisions))
        r-over-d (/ r divisions)
        delta (Math/abs (- max min))]
    (* delta r-over-d)))

(defn initialize-zauberon [xyz-helix]
  (let [pitch-yaw-roll [(random-angle) (random-angle) (random-angle)]
        xform-matrix (pitch-yaw-roll->xform-matrix pitch-yaw-roll)
        rotation (if (even? (rand-int 2)) :right :left)
        helix-advance (random-in-range helix-advance-config)]
    {:angle                  (random-angle)
     :angle-steps-adjustment (d2r (random-in-range angle-steps-config))
     :rotation               rotation
     :helix-advance          helix-advance
     :hv                     (rotate-3d xform-matrix [helix-advance 0 0])
     :pitch-yaw-roll         pitch-yaw-roll
     :radius                 (random-in-range radius-config)
     :xform-matrix           xform-matrix
     :xyz-helix              xyz-helix
     :xyz-zauberon           xyz-zauberon}))

(defn space-adjust [x-y-or-z-zauberon positive-boundry negative-boundry]
  (cond
    (< x-y-or-z-zauberon positive-boundry) positive-space
    (> x-y-or-z-zauberon negative-boundry) negitive-space
    :else 0))

(defn xyz-space-adjustments [xyz-zauberon]
  (map space-adjust
       xyz-zauberon
       [front-boundry left-boundry up-boundry]
       [back-boundry right-boundry down-boundry]))

(defn xyz-adjust [xyz-zauberon xyz-helix]
  (let [space-adjustments (xyz-space-adjustments xyz-zauberon)]
    [(map + space-adjustments xyz-zauberon) (map + space-adjustments xyz-helix)]))

(defn new-zauberon-position
  [{:keys [angle angle-steps-adjustment helix-advance hv rotation radius xform-matrix xyz-helix] :as zauberon}]
  (let [new-angle ((if (= rotation :right) + -) angle angle-steps-adjustment)
        xyz-0 [helix-advance (* radius (cos new-angle)) (* radius (sin new-angle))]
        xyz-1 (rotate-3d xform-matrix xyz-0)
        new-xyz-zauberon (map + xyz-helix xyz-1)
        new-xyz-helix (map + xyz-helix hv)
        [adjusted-xyz-zauberon adjusted-xyz-helix] (xyz-adjust new-xyz-zauberon new-xyz-helix)]
    (assoc zauberon
           :angle new-angle
           :xyz-helix adjusted-xyz-helix
           :xyz-zauberon adjusted-xyz-zauberon)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; protocol implementation
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn add-cli-options [cli-options]
  (conj cli-options ["-o" "--output-file OUTPUTFILE" "File spec of output file"
                     :default "zauberons.dat"
                     :validate [#(not (cstr/blank? %)) "Must be non blank"]]))

(defn collision [{:keys [ctx zauberon-collisions]}]
  ;; TODO: Implement a real collision resolution function
  {:ctx       ctx
   :zauberons zauberon-collisions})

(defn description [{:keys [output-file]}]
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
   :zauberons (map (fn [_] (initialize-zauberon xyz-helix)) (range zauberon-count))})

(defn locate-collisions [{:keys [ctx zauberons]}]
  ;; TODO: Implement a real collision locate function
  {:ctx                 ctx
   :zauberon-collisions zauberons})

(defn new-position [{:keys [zauberons] :as ctx-zauberons}]
  (assoc ctx-zauberons :zauberons (map new-zauberon-position zauberons)))

(defn output [iteration {:keys [ctx zauberons] :as ctx-zauberons}]
  (when (zero? (mod iteration (ctx :iteration-divisor))) (println iteration))
  (.write (ctx :output-writer) (str "# Iteration: " iteration "\n"))
  (doseq [zauberon zauberons]
    (let [[x y z] (zauberon :xyz-zauberon)]
      (.write (ctx :output-writer) (str x " " y " " z "\n"))))
  ctx-zauberons)

(defn validate-cli-map [cli-map]
  (letfn [(get-canonical-path [output-file]
            (if (cstr/blank? output-file) "" (.getCanonicalPath (io/file output-file))))
          (add-error [cli-map output-dir error-str]
            (update cli-map :errors conj (str "The specified output directory '" output-dir "' " error-str)))]
    (let [output-spec (get-canonical-path (get-in cli-map [:options :output-file]))
          output-dir (-> output-spec (io/file) (.getParentFile))]
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
