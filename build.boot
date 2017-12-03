(set-env!
 :source-paths   #{"src/cljs" "less"}
 :resource-paths #{"resources"}
 :dependencies   '[[adzerk/boot-cljs          "2.1.4"  :scope "test"]
                   [adzerk/boot-cljs-repl     "0.3.3"  :scope "test"]
                   [adzerk/boot-reload        "0.5.2"  :scope "test"]
                   [pandeiro/boot-http        "0.8.3"  :scope "test"]
                   [com.cemerick/piggieback   "0.2.1"  :scope "test"]
                   [org.clojure/tools.nrepl   "0.2.13" :scope "test"]
                   [deraen/boot-less          "0.2.1"  :scope "test"]
                   [weasel                    "0.7.0"  :scope "test"]
                   [org.clojure/clojurescript "1.9.946"]
                   [rum                       "0.10.8"]     ; react
                   [cljs-http                 "0.1.44"]     ; http requests
                   [venantius/accountant      "0.2.3"]      ; navigation
                   [bidi                      "2.1.2"]      ; routing
                   ])

(require
 '[adzerk.boot-cljs      :refer [cljs]]
 '[adzerk.boot-cljs-repl :refer [cljs-repl start-repl]]
 '[adzerk.boot-reload    :refer [reload]]
 '[pandeiro.boot-http    :refer [serve]]
 '[deraen.boot-less      :refer [less]])

(deftask build []
  (comp (speak)
        (cljs)
        (less)
        (sift :move {#"less.css" "css/less.css" #"less.main.css.map" "css/less.main.css.map"})))

(deftask run []
  (comp (serve)
        (watch)
        (cljs-repl)
        (reload)
        (build)))

(deftask production []
  (task-options! cljs {:optimizations :advanced}
                 less {:compression true})
  identity)

(deftask development []
  (task-options! cljs {:optimizations :none
                       :source-map true}
                 reload {:on-jsload 'duplo.app/init}
                 less   {:source-map true})
  identity)

(deftask dev
  "Simple alias to run application in development mode"
  []
  (comp (development)
        (run)))
