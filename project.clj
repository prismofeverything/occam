(defproject occam "0.0.4"
  :description "Utilities for transforming and processing Occam files"
  :url "http://dmm.sysc.pdx.edu"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [org.clojure/data.codec "0.1.0"]
                 [org.clojure/data.csv "0.1.2"]
                 [http.async.client "0.5.0"]
                 [incanter "1.4.0"]]
  :jvm-opts ["-Xmx1g"])
