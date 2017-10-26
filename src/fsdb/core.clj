(ns fsdb.core
  "fsdb - a file system data base"
  (:gen-class)
  (:require [clojure.string :refer [split]]
            [clojure.walk :refer [keywordize-keys]]
            [yaml.core :as yaml]
            [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [clojure.data.json :as json]
            ))

(defn- file-extension
  "Extract the extension from filename and return it as keyword."
  [filename]
  (keyword (last (split filename #"\."))))

(defmulti read-file
  "Implementations for reading & parsing files into data structures,
  depending on file extension."
  file-extension)

(defmethod read-file :default [filename]
  (println (str "No method read-file for "
                (name (file-extension filename))
                ", skipping " filename))
  {})

(defmethod read-file :edn [filename]
  (-> filename
      slurp
      read-string))

(defmethod read-file :json [filename]
  (-> filename
      slurp
      json/read-str))

(defmethod read-file :csv [filename]
  (with-open [reader (io/reader filename)]
    (doall
     (csv/read-csv reader))))

(defmethod read-file :yml [filename]
  (-> filename
      yaml/from-file
      keywordize-keys))

(defn- prefixes [path]
  (-> path
      (split #"\.(?=[^\.]+$)")
      first
      (split #"/")))

(defn- with-nested-prefixes [prefixes value]
  (->> (map keyword prefixes)
       reverse
       (reduce #(hash-map %2 %1) value)))

(defn- read-file-with-prefixing [path]
  (with-nested-prefixes (prefixes path) (read-file path)))

;; TODO sort by specifity first, then alphabetical
(defn- list-files
  "Returns a list of Strings, representing paths to files (not
  directories)."
  [path]
  (->> (file-seq (clojure.java.io/file path))
       (filter #(.isFile %))
       (map #(.getPath %))))

(defn- deep-merge
  "Recursively merges maps. If vals are not maps, the last value wins."
  [& vals]
  (if (every? map? vals)
    (apply merge-with deep-merge vals)
    (last vals)))

(defn read-tree
  "Takes on path to read the directory tree from."
  [path]
  (->> (list-files path)
       (map read-file-with-prefixing)
       (apply deep-merge)))

(defn -main
  "Takes multiple paths, reads the directory trees, merges them in
  order and pretty prints the result."
  [& args]
  (clojure.pprint/pprint (apply deep-merge (map read-tree args))))
