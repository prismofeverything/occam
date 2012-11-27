(ns occam.remote
  (:require [clojure.java.io :as io]
            [http.async.client :as http]))

(def search-url
  "http://dmm.sysc.pdx.edu/weboccam.cgi?action=search")

(def search-defaults
  {:action "search"
	 :model ""
   :evalmode "info"
   :refmodel "default"
   :searchtype "loopless"
   :searchdir "default"
   :sortby "information"
   :searchsortdir "descending"
   :searchlevels "7"
   :sortreportby "information"
   :sortdir "descending"
   :show_h "yes"
   :show_dlr "yes"
   :show_alpha "yes"
   :show_pct_dh "yes"
   :show_aic "yes"
   :show_bic "yes"
   :show_bp "yes"
   :show_incr_a "yes"
   :show_pct "yes"
   :show_pct_cover "yes"
   :show_pct_miss "yes"
   ;; :format "y"
   :printoptions "y"
   ;; :inversenotation "y"
   ;; :skipnominal "y"
   ;; :functionvalues "y"
   ;; :batchOutput "ryan.spangler@gmail.com"
   ;; :emailSubject "yoyoyo"
   :batchOutput ""
   :emailSubject ""})

(def search-headers
  {"connection" "keep-alive"})

(defn multiate
  [m]
  (map
   (fn [[k v]]
     {:type :string
      :name (name k)
      :value v})
   m))

(defn http-post
  [url request]
  (with-open [client (http/create-client :follow-redirects true)]
    (let [response (;; http/stream-seq client :post url
                    http/POST
                    client url
                    :headers (:headers request)
                    :body (:body request))]
      (-> response http/await))))

(defn occam-search
  ([filename] (occam-search filename {}))
  ([filename params]
     (let [file (io/file filename)
           multifile {:type :file
                      :name "data"
                      :file file
                      :mime-type "multipart/form-data"}
           form (multiate (merge search-defaults params))
           multi (cons multifile form)]
       (http-post
        search-url
        {:body multi
         :headers search-headers}))))

