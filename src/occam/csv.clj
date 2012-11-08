(ns occam.csv
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
            [clojure.data.csv :as csv]
            [occam.core :as occam]))

(def symbols "abcdefghijklmnopqrstuvwxyz")

(defn next-digit
  [base]
  (fn [d]
    (loop [index (dec (count d))
           digits d]
      (if (< index 0)
        (vec (cons 0 digits))
        (let [next (inc (nth d index))]
          (if (>= next base)
            (recur (dec index) (assoc digits index 0))
            (assoc digits index next)))))))

(defn digits-to-symbols
  [digits symbols]
  (string/join (map (partial nth symbols) digits)))

(defn name-sequence
  [symbols]
  (iterate (next-digit (count symbols)) [0]))

(defn read-csv
  [filename]
  (with-open [in (io/reader filename)]
    (doall
     (csv/read-csv in))))

(defn find-unique
  [s]
  (sort
   (keys
    (reduce
     #(assoc %1 %2 true)
     {} s))))

(defn process-column
  [& column]
  (let [short-name (digits-to-symbols (first column) symbols)
        column (rest column)
        variable (first column)
        data (rest column)
        values (sort (set data))
        value-map (into {} (map-indexed (fn [index value] [value index]) values))
        nominal {:long-name variable :degrees (count values) :role 0 :short-name short-name}]
    {:data (map value-map data) :nominal nominal :values value-map}))

(defn process-csv
  [rows]
  (let [header (first rows)
        data (rest rows)
        short-names (take (count header) (name-sequence symbols))
        columns (apply (partial map process-column) (cons short-names rows))
        after (apply (partial map vector) (map :data columns))
        nominals (map :nominal columns)
        maps (map :values columns)]
    {:nominal nominals :data after :values maps}))

(defn csv-to-occam
  [from to]
  (let [csv (read-csv from)
        occam (process-csv csv)]
    (occam/write-occam to occam)))