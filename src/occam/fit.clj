(ns occam.fit
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
            [incanter.core :as m]))

(defn read-fit-table
  [filename]
  (let [raw (-> (slurp filename)
                (string/trim)
                (string/replace #"\*" ""))
        lines (string/split raw #"\n")
        portions (map #(string/split % #"\|") lines)
        entries (map #(map (fn [p] (string/split (string/trim p) #"\t")) %) portions)
        [variables observed calculated] (first entries)
        maps (rest entries)]
    entries))

(defn iterate-tables
  [tables f]
  (loop [tables tables
         transformed []]
    (if (or (empty? tables) (empty? (first tables)))
      transformed
      (let [table (map first tables)
            others (map rest tables)
            product (f table)]
        (recur others (conj transformed product))))))

(defn parse-vector
  [v]
  (map #(Double/parseDouble %) v))

(defn parse-matrix
  [table]
  (let [headings (first table)
        data (rest table)
        parsed (map parse-vector data)
        matrix (m/matrix parsed)]
    {:headings headings :matrix matrix}))

(defn parse-tables
  [tables]
  (iterate-tables tables parse-matrix))

(defn subcols
  [table cols]
  (m/sel (:matrix table) :cols (range cols)))

(defn split-state
  [equal]
  (let [[variable state] (string/split equal #"=")]
    (Integer/parseInt state)))

(defn branch-into
  [tree [path probabilities]]
  (assoc-in tree path probabilities))

(defn intify
  [matrix]
  (map
   (fn [x]
     (if (number? x)
       [(int x)]
       (map int x)))
   matrix))

(defn fit-mapping
  [[conditions observed calculated]]
  (let [cols (-> observed :headings count (- 3))
        m-observed (subcols observed cols)
        m-calculated (subcols calculated cols)
        variables (:headings conditions)
        states (map split-state (take cols (:headings calculated)))
        int-conditions (intify (:matrix conditions))
        pair (map vector int-conditions m-calculated)
        mapping (reduce branch-into {} pair)]
    {:mapping mapping
     :conditions int-conditions
     :states states}))

(defn produce-state
  [mapping states]
  (if-let [row (get-in (:mapping mapping) states)]
    (let [delve (rand 100)
          index
          (loop [levels row
                 depth 0
                 index 0]
            (if-let [level (first levels)]
              (let [reach (+ depth level)]
                (if (>= reach delve)
                  index
                  (recur (rest levels) reach (inc index))))
              (do
                (println "INDEX OUT?" index)
                (dec index))))]
      (nth (:states mapping) index))))

(defn read-fit-mapping
  [filename]
  (let [tables (-> (read-fit-table filename)
                   (parse-tables))]
    (fit-mapping tables)))
