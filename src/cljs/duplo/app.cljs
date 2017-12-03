(ns duplo.app
  (:require 
   [rum.core :as rum]
   [bidi.bidi :as bidi]
   [accountant.core :as accountant]
   [duplo.blockchain :as blockchain]
   [duplo.ui.ui :as ui]
   [duplo.state :refer [state assoc-state!]]))

(enable-console-print!)

(def app-routes
  ["/" {"" :blocks
        "blocks" :blocks
        "assets" :assets}])

(defn init []
  (accountant/configure-navigation!
   {:nav-handler (fn [path]
                   (let [{:keys [handler]} (bidi/match-route app-routes path)]
                     (assoc-state! [:route] handler)))
    :path-exists? (fn [path]
                    (boolean (bidi/match-route app-routes path)))})

  (when-let [node (.getElementById js/document "container")]
    (rum/mount (ui/app state) node)
    (blockchain/refresh-data!)
    (blockchain/start-sync)))
