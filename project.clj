(defproject zauberon "0.1.0-SNAPSHOT"
  :description "A Clojure program to simulate the Quantum Physical characteristics of 'zauberon's ascribed to photons in
                the book \n'New Age Quantum Physics' by Al Schneider, ISBN-10: 1467938009."
  :url "https://github.com/pwhittin/zauberon"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url  "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojure/tools.cli "1.0.194"]]
  :repl-options {:init-ns zauberon.core}
  :main zauberon.core
  :target-path "target/%s"
  ;:omit-source true
  :profiles {:uberjar {:aot :all}})
