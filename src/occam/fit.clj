(ns occam.fit
  (:require [clojure.java.io :as io]
            [clojure.string :as string]))

(defn read-fit-table
  [filename]
  (let [raw (slurp filename)
        lines (string/split raw #"\n")
        portions (map #(string/split % #"\|") lines)
        entries (map #(map (fn [p] (string/split (string/trim p) #"\t")) %) portions)]
    entries))