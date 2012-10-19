(ns occam.core
  (:require [clojure.java.io :as io]
            [clojure.string :as string]))

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

(defn randomize-transform
  [occam]
  (update-in
   occam [:data]
   (fn [data]
     (sort-by (fn [_] (rand 3)) data))))

(defn parcel-test-data
  [occam portion]
  (let [random (:data (randomize-transform occam))
        total (count random)
        num (quot (* total portion) 1)
        test (take num random)
        data (drop num random)]
    (assoc occam
      :data data
      :test test)))
