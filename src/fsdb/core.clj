(ns fsdb.core
  "fsdb - a file system data base"
  (:gen-class)
  (:require [clojure.string :refer [split]]
            [clojure.walk :refer [keywordize-keys]]
            [yaml.core :as yaml]
            [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [clojure.data.json :as json]
            [clojure.pprint :as pprint]))

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
      (json/read-str :key-fn keyword)))

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
  (let [files (list-files path)]
    (if (empty? files)
      {}
      (->> files
           (map read-file-with-prefixing)
           (apply deep-merge)))))

(defn- pathwalk
  "Walk a data structure e calling f for every node passing the path and
  the node, using the return of f in place."
  ([f e] (pathwalk f [] e))
  ([f path e]
   (let [e' (f path e)]
     (cond
       (map? e')
       (->> e'
            (map (fn [[k x]] [k (pathwalk f (conj path k) x)]))
            (into (empty e')))
       (coll? e')
       (->> e'
            (map-indexed (fn [i x] (pathwalk f (conj path i) x)))
            (into (empty e')))
       :else e'))))

(defn- obj?
  "Returns true if x implements clojure.lang.IObj."
  [x]
  (instance? clojure.lang.IObj x))

(defn- annotate [path data]
  (if (obj? data)
    (with-meta data {:path path :key (last path)})
    data))

(defn- pprint-with-meta
  "Pretty print with metadata."
  [obj]
  (let [orig-dispatch pprint/*print-pprint-dispatch*]
    (pprint/with-pprint-dispatch
      (fn [o]
        (when (meta o)
          (print "^")
          (orig-dispatch (meta o))
          (pprint/pprint-newline :fill))
        (orig-dispatch o))
      (pprint/pprint obj))))


(defn merge-down [args input]
  (reduce #(deep-merge %1 (get-in input (map keyword (split %2 #"/")))) {} args))


(defn -main
  "Takes multiple paths, reads the directory trees, merges them in
  order and pretty prints the result."
  [& args]
  (->> (map read-tree args)
       (apply deep-merge)
       (pathwalk annotate)
       (merge-down args)
       ;;pprint-with-meta
       json/write-str
       println))
