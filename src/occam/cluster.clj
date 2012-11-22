(ns occam.cluster)

(defn closest
  [point means distance]
  (first (sort-by #(distance % point) means)))

(defn point-groups
  [means data distance]
  (group-by #(closest % means distance) data))

(defn new-means
  [average point-groups means]
  (for [old means]
    (if (contains? point-groups old)
      (average (get point-groups old))
      old)))

(defn iterate-means
  [data distance average]
  (fn [means]
    (let [next-means (new-means average (point-groups means data distance) means)]
      (println next-means)
      next-means)))

(defn groups
  [data distance means]
  (vals (point-groups means data distance)))

(defn take-while-unstable 
  ([sq]
     (lazy-seq
      (if-let [sq (seq sq)]
        (cons (first sq) (take-while-unstable (rest sq) (first sq))))))

  ([sq last]
     (lazy-seq
      (if-let [sq (seq sq)]
        (if (= (first sq) last) '() (take-while-unstable sq))))))

(defn k-groups
  [data distance average]
  (fn [guesses]
    (take-while-unstable
     (map
      (partial groups data distance)
      (iterate (iterate-means data distance average) guesses)))))

(defn distance-for
  [metric]
  (fn [a b]
    (let [between (- a (metric b))]
      (if (< 0 between) between (- between) ))))

(defn average-for
  [metric]
  (fn [z]
    (/ (reduce #(+ %1 (metric %2)) 0 z) (count z))))

(defn greatest
  [c f z]
  (let [head (first z)
        value (f head)]
    (first
     (reduce
      (fn [[great value] untested]
        (let [possibly (f untested)]
          (if (c possibly value)
            [untested possibly]
            [great value])))
      [head value]
      z))))

(defn make-guesses
  [data metric num-bins]
  (let [top (greatest > metric data)
        bins (take num-bins (iterate inc 1))
        guesses (reverse (map #(/ (metric top) %) bins))]
    guesses))

(defn clustering
  [data metric]
  (k-groups data (distance-for metric) (average-for metric)))

(defn cluster-by
  [data metric num-bins new-key]
  (let [cluster (clustering data metric)
        guesses (make-guesses data metric num-bins)
        groups (sort-by (comp metric first) (last (cluster guesses)))
        average (average-for metric)
        averages (map average groups)
        restored (apply
                  concat
                  (map-indexed
                   (fn [index group]
                     (map
                      (fn [datum]
                        (assoc datum
                          new-key index))
                      group))
                   groups))]
    {:restored restored :averages averages}))