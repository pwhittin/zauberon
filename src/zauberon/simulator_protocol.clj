(ns zauberon.simulator-protocol)

(defprotocol Simulator
  "This protocol represents a simulator."
  (description [this]
    "Returns a description of the simulator.")
  (initialize-fn [this cli-options]
    "Returns a function that returns a sequence of zauberon-count initialized zauberons.
     (fn [zauberon-count] ...) => [z1 z2 ...]")
  (initialize-cli-options [this cli-options]
    "Adds initialize options, and returns them.")
  (new-position-fn [this cli-options]
    "Returns a function that takes a sequence of zauberons and returns a sequence of zauberons with new positions.
     (fn [zauberons] ...) => [znew1 znew2 ...]")
  (new-position-cli-options [this cli-options]
    "Adds new-position options, and returns them.")
  (locate-collisions-fn [this cli-options]
    "Returns a function that takes a sequence of zauberons, and returns a sequence of sets of zauberons that have
     collided.
     (fn [zauberons] ...) => [#{z1 z2 ...} #{z3 z4 ...} ...]")
  (locate-collisions-cli-options [this cli-options]
    "Adds locate-collisions options, and returns them.")
  (collision-fn [this cli-options]
    "Returns a function that takes a sequence of sets of zauberons that have collided, and returns a sequence of
     zauberons with states resulting from the collisions.
     (fn [zauberon-collision-sets] ...) => [zaftercollision1 zaftercollision2 ...]")
  (collision-cli-options [this cli-options] 
    "Adds collision options, and returns them.")
  (output-fn [this cli-options]
    "Returns a function that takes an iteration number and a sequences of zauberons, generates output, and returns the
     sequence of zauberons.
     (fn [iteration zauberons] ...) => zauberons")
  (output-cli-options [this cli-options] 
    "Adds output options, and returns them."))
