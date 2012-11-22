(ns occam.core
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
            [occam.cluster :as cluster]))

;; reading occam from strings

(defn split-nominal
  [line]
  (let [[long-name degrees role short-name] (map string/trim (string/split line #","))]
    {:long-name long-name :degrees degrees :role role :short-name short-name}))

(defn split-data
  [line]
  (let [trimmed (string/trim line)]
    (map #(Integer/parseInt %) (string/split trimmed #" +"))))

(defn process-raw
  [splitter raw]
  (if raw
    (let [trimmed (string/trim raw)
          lines (string/split trimmed #"\n")]
      (map splitter lines))))
  
(defn read-occam
  [filename]
  (let [raw (slurp filename)
        [raw-preamble other] (string/split raw #":nominal")
        [raw-nominal other] (string/split other #":data")
        [raw-data raw-test] (string/split other #":test")
        nominal (process-raw split-nominal raw-nominal)
        data (process-raw split-data raw-data)
        test (process-raw split-data raw-test)]
    {:preamble raw-preamble :nominal nominal :data data :test test}))

;; writing occam to strings

(defn histogram
  [s]
  (reduce
   (fn [hist item]
     (assoc hist
       item (if-let [total (get hist item)] (inc total) 1)))
   {} s))

(defn bind-frequencies
  [rows]
  (let [frequency-map (histogram rows)]
    (map
     (fn [[row frequency]]
       (concat row (list frequency)))
     frequency-map)))

(defn write-nominal
  [nm]
  (string/join "," [(:long-name nm) (:degrees nm) (:role nm) (:short-name nm)]))

(defn write-data
  [data]
  (string/join " " data))

(defn write-block
  [write block]
  (if block
    (string/join "\n" (map write block))))

(defn write-occam
  [filename occam]
  (let [nominal (write-block write-nominal (:nominal occam))
        data (write-block write-data (:data occam))
        test (write-block write-data (:test occam))
        preamble (:preamble occam)
        total (str preamble
                   ":nominal\n" nominal
                   "\n\n:data\n" data
                   (if test
                     (str "\n\n:test\n" test)))]
    (spit filename total)))

(defn transform-occam
  [transform from to]
  (let [occam (read-occam from)
        transformed (transform occam)]
    (write-occam to transformed)))

;; specific useful transforms

(defn randomize-transform
  [occam]
  (update-in
   occam [:data]
   (fn [data]
     (sort-by (fn [_] (rand 3)) data))))

(defn parcel-test-data
  [portion]
  (fn [occam]
    (let [random (:data (randomize-transform occam))
          total (count random)
          num (quot (* total portion) 1)
          test (take num random)
          data (drop num random)]
      (assoc occam
        :data data
        :test test))))

(defn find-in
  [s f v]
  (filter #(= v (f %)) s))

(defn occam-index
  [occam variable]
  (-> (find-in
       (map-indexed vector (:nominal occam))
       (fn [[index var]]
         (:short-name var))
       (name variable))
      first first))

(defn variable-metric
  [index]
  (fn [z]
    (nth z index)))

(defn bin-data-by-index
  [data index num-bins]
  (if data
    (let [metric (variable-metric index)
          cluster (cluster/clustering data metric)
          guesses (cluster/make-guesses data metric num-bins)
          groups (last (cluster guesses))]
      (apply
       concat
       (map-indexed
        (fn [state bin]
          (map #(assoc % index state) bin))
        groups)))))

(defn bin-variable-by
  [variable bins]
  (fn [occam]
    (let [index (occam-index occam variable)
          data (bin-data-by-index (:data occam) index bins)
          test (bin-data-by-index (:test occam) index bins)]
      (assoc occam
        :data data
        :test test))))

