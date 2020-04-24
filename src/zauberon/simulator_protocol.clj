(ns zauberon.simulator-protocol)

(defprotocol Simulator
  "This protocol represents a simulator."
  (add-cli-options [this cli-options]
    "Adds initialize options, and returns them.")
  (collision-fn [this cli-map]
    "Returns a function that takes a map of simulator execution context 'ctx' and a sequence of sets of zauberons that
     have collided, and returns a map of simulator execution context and a sequence of zauberons with states resulting
     from the collisions.
     (fn [{:keys [ctx zauberon-collisions]}] ...) => {:ctx ctx, :zauberons [z1aftercollision z2aftercollision ...]}")
  (description [this ctx]
    "Given a simulator execution context, returns a description of the simulator.")
  (finalize [this ctx]
    "Finalizes simulator.")
  (initialize [this cli-map]
    "Initializes simulator, and return a simulator execution context.")
  (initialize-zauberons-fn [this cli-map]
    "Returns a function that takes a map of simulator execution context 'ctx' and a zauberon count, and returns a map of
     simulator execution context and a sequence of zauberon-count initialized zauberons.
     (fn [{:keys [ctx zauberon-count]}] ...) => {:ctx ctx, :zauberons [z1 z2 ...]}")
  (locate-collisions-fn [this cli-map]
    "Returns a function that takes a map of simulator execution context 'ctx' and a sequence of zauberons, and returns a
     map of simulator execution context and a sequence of sets of zauberons that have collided.  Any zauberon that has
     not collided is alone in a set.
     (fn [{:keys [ctx zauberons]}] ...) =>  {:ctx ctx, :zauberon-collisions [#{z1 z2 ...} #{z3 z4 ...} ...]}")
  (new-position-fn [this cli-map]
    "Returns a function that takes a map of simulator execution context 'ctx' and a sequence of zauberons, and returns a
     map of simulator execution context and sequence of zauberons with new positions.
     (fn [{:keys [ctx zauberons]}] ...) => {:ctx ctx, :zauberons [z1new z2new ...]}")
  (output-fn [this cli-map]
    "Returns a function that takes an iteration number, a map of simulator execution context 'ctx' and a sequence of
     zauberons, generates output, and returns a map of simulator execution context and a sequence of zauberons.
     (fn [iteration {:keys [ctx zauberons]}] ...) => {:ctx ctx, :zauberons [z1 z2 ...]}")
  (validate-cli-map [this cli-map]
    "Adds failure strings to the cli-map :errors vector, if needed, and returns cli-map"))
