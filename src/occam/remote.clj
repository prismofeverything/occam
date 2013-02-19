(ns occam.remote
  (:require [clojure.java.io :as io]
            [http.async.client :as http]))

;; The following curl call works:
;; curl -X POST -F 'data=@/Users/rspangler/Projects/fuga/occam/in/book-1-fugue-4-history-5-note.in' -F 'action=search' -F 'model=' -F 'evalmode=info' -F 'refmodel=default' -F 'searchtype=loopless' -F 'searchdir=default' -F 'sortby=information' -F 'searchsortdir=descending' -F 'searchlevels=7' -F 'sortreportby=information' -F 'sortdir=descending' -F 'show_h=yes' -F 'show_dlr=yes' -F 'show_alpha=yes' -F 'show_pct_dh=yes' -F 'show_aic=yes' -F 'show_bic=yes' -F 'show_bp=yes' -F 'show_incr_a=yes' -F 'show_pct=yes' -F 'show_pct_cover=yes' -F 'show_pct_miss=yes' -F 'printoptions=y' -F 'batchOutput=' -F 'emailSubject=' -F 'format=y' 'http://dmm.sysc.pdx.edu/weboccam.cgi?action=search'

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
   :printoptions "y"
   :batchOutput ""
   :emailSubject ""})

   ;; :format "y"
   ;; :inversenotation "y"
   ;; :skipnominal "y"
   ;; :functionvalues "y"
   ;; :batchOutput "ryan.spangler@gmail.com"
   ;; :emailSubject "yoyoyo"


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

