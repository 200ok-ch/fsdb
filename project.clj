(defproject fsdb "1.1.3"
  :description "A reasonably convenient database on top of the file system."
  :url "http://gitlab.com/200ok/fsdb"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.12.0"]
                 [io.forward/yaml "1.0.11"]
                 [org.clojure/data.json "2.5.1"]
                 [org.clojure/data.csv "1.1.0"]
                 ;; [shell-smith/shell-smith "0.1.0-SNAPSHOT" :git/url "https://github.com/200ok-ch/shell-smith.git"]
                 ]
  :main ^:skip-aot fsdb.core
  :bin {:name "fsdb"
        :bin-path "~/bin"}
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}
             :dev {:plugins [[lein-binplus "0.6.8"]]}}
  :plugins [[com.github.liquidz/antq "RELEASE"]])
