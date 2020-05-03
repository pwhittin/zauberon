(ns zauberon.default-simulator
  (:require [clojure.java.io :as io]
            [clojure.string :as cstr]
            [zauberon.simulator-protocol :as simulator-protocol]))

(comment

  "
  Given
  
    e = energy
    h = Plank's constant
    c = speed of light
    m = mass
    f = Zauberon circular helical frequency
    r = Zauberon circular helical radius
    z = Zauberon mass

  then

    e = (* h f)
    e = (* m (* c c))
    z = (*' (*' pi r r) (/ c f)) ;; volume of one Zauberon wave length

  thus

    (*' (* (*' pi r r) (/ c f)) (* c c)) = (* h f)
    (/ (* pi r r c c c) f)             = (* h f)
    (* r r)                            = (/ (* h f f) (* pi c c c))
    r                                  = (* (Math/sqrt (/ h (* pi c c c))) f)
    schneider-constant                 = (Math/sqrt (/ h (* pi c c c)))
  
  hence 
  
    schneider-r = (* schneider-constant f)
  "

  )

(def h 6.62607004E-34)
(def c 299792458)
(def pi (Math/PI))
(def schneider-constant (Math/sqrt (/ h (*' pi c c c))))

(defn f->schneider-r [f]
  "Given a frequency 'f', return the Schneider circular helical radius"
  (*' f schneider-constant))

(defn f->wave-length [f]
  (/ c f))

(def box 100000.0)
(def space (/ box 2.0))

(def center-x (/ box 2))
(def center-y (/ box 2))
(def center-z (/ box 2))

(def back-boundry (+' center-x (/ space 2)))
(def front-boundry (-' center-x (/ space 2)))
(def right-boundry (+' center-y (/ space 2)))
(def left-boundry (-' center-y (/ space 2)))
(def up-boundry (-' center-z (/ space 2)))
(def down-boundry (+' center-z (/ space 2)))
(def negitive-space (*' -1 space))
(def positive-space space)

(def initial-xyz [(/ box 2) (/ box 2) (/ box 2)])

(def visible-light-frequencies (map *' (range 405 791) (repeat 1E12)))
(def visible-light-wave-lengths (map f->wave-length visible-light-frequencies))
(def visible-light-radii (map f->schneider-r visible-light-frequencies))

(def scale-factor (/ space (apply max visible-light-wave-lengths)))

(def wave-lengths (map * visible-light-wave-lengths (repeat scale-factor)))
(def radii (map * visible-light-radii (repeat scale-factor)))
(def wave-length->radius (zipmap wave-lengths radii))
(def x-step (/ (apply min wave-lengths) 10))

(def pi*2 (*' 2 pi))

(defn deg->rad [degrees]
  (*' pi*2 (/ degrees 360)))

(defn wave-length->angle-step [wave-length]
  (deg->rad (/ (*' 360 x-step) wave-length)))

(defn cos [r]
  (Math/cos r))

(defn sin [r]
  (Math/sin r))

(defn pitch-yaw-roll->xform-matrix [pitch-yaw-roll]
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

(defn rotate-3d [xform-matrix xyz]
  (->> (map #(map *' %1 %2) xform-matrix (repeat xyz))
       (map #(apply +' %))))

(defn random-angle []
  (deg->rad (rand-int 360)))

(defn random-rotation []
  (if (even? (rand-int 1000)) :right :left))

(defn random-in-sequence [value-sequence]
  (nth value-sequence (rand-int (count value-sequence))))

(defn random-pitch-yaw-roll []
  [(random-angle) (random-angle) (random-angle)])

(defn initialize-zauberon [initial-xyz]
  (let [pitch-yaw-roll (random-pitch-yaw-roll)
        wave-length (random-in-sequence wave-lengths)]
    {:angle          (random-angle)
     :angle-step     (wave-length->angle-step wave-length)
     :pitch-yaw-roll pitch-yaw-roll
     :radius         (wave-length->radius wave-length)
     :rotation       (random-rotation)
     :xform-matrix   (pitch-yaw-roll->xform-matrix pitch-yaw-roll)
     :xyz            initial-xyz})
  )

(defn space-adjust [x-y-or-z positive-boundry negative-boundry]
  (cond
    (< x-y-or-z positive-boundry) positive-space
    (> x-y-or-z negative-boundry) negitive-space
    :else x-y-or-z))

(defn xyz-space-adjustments [xyz]
  (map space-adjust xyz [front-boundry left-boundry up-boundry] [back-boundry right-boundry down-boundry]))

(defn new-zauberon-position
  [{:keys [angle angle-step radius rotation xform-matrix xyz] :as zauberon}]
  (let [new-angle ((if (= rotation :right) +' -) angle angle-step)
        xyz-origin-along-x-axis [x-step (*' radius (cos new-angle)) (*' radius (sin new-angle))]
        xyz-origin-along-helix-axis (rotate-3d xform-matrix xyz-origin-along-x-axis)
        new-xyz (map +' xyz xyz-origin-along-helix-axis)]
    (assoc zauberon
      :angle new-angle
      :xyz (xyz-space-adjustments new-xyz))))

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
   :zauberons (map (fn [_] (initialize-zauberon initial-xyz)) (range zauberon-count))})

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
    (let [[x y z] (zauberon :xyz)]
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
