(ns occam.csv
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
            [clojure.data.csv :as csv]))

(def symbols "abcdefghijklmnopqrstuvwxyz")

(defn next-digit
  [base]
  (fn [d]
    (loop [index (dec (count d))
           digits d]
      (if (< index 0)
        (vec (cons 1 digits))
        (let [next (inc (nth d index))]
          (if (>= next base)
            (recur (dec index) (assoc digits index 0))
            (assoc digits index next)))))))

(defn digits-to-symbols
  [digits symbols]
  (string/join (map (partial nth symbols) digits)))

(defn name-sequence
  [symbols]
  (iterate (next-digit (count symbols)) []))

(defn read-csv
  [filename]
  (with-open [in (io/reader filename)]
    (doall
     (csv/read-csv in))))

(defn process-column
  [& column]
  (let [short-name (digits-to-symbols (first column) symbols)
        column (rest column)
        variable (first column)
        data (rest column)
        values (set data)
        value-map (into {} (map-indexed (fn [index value] [value index]) (sort values)))
        nominal {:long-name variable :degrees (count values) :role 0 :short-name short-name}]
    {:data (map value-map data) :nominal nominal :values value-map}))

(defn process-csv
  [rows]
  (let [header (first rows)
        data (rest rows)
        short-names (take (count header) (name-sequence symbols))
        columns (apply (partial map process-column) (cons short-names rows))
        after (apply (partial map vector) (map :data columns))
        nominals (map :nominal columns)]
    {:nominal nominals :data after}))