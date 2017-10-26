(defproject fsdb "0.1.1-SNAPSHOT"
  :description "A reasonably convenient database on top of the file system."
  :url "http://gitlab.com/200ok/fsdb"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [io.forward/yaml "1.0.6"]
                 [org.clojure/data.json "0.2.6"]
                 [org.clojure/data.csv "0.1.4"]]
  :main ^:skip-aot fsdb.core)
