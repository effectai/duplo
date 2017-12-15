(ns duplo.app
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require 
   [clojure.core.async :as a :refer [<!]]
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
        "assets" :assets
        "wallet" :wallet}])

(defn event-handler [chan]
  (go-loop []
      (let [[event & params] (<! chan)]
        (prn " > received event " event params)
        (case event
          :generate-keys (blockchain/make-request
                          "makekeys" [3] #(blockchain/refresh-keys!))
          :claim-initial-neo (blockchain/make-request
                              "claiminitialneo" #())
          :open-form (assoc-state! [:form] (first params))
          :close-form (assoc-state! [:form] nil)
          :make-tx (blockchain/make-transaction (first params))
          nil))
      (recur)))

(defn init []
  (accountant/configure-navigation!
   {:nav-handler (fn [path]
                   (let [{:keys [handler]} (bidi/match-route app-routes path)]
                     (assoc-state! [:route] handler)))
    :path-exists? (fn [path]
                    (boolean (bidi/match-route app-routes path)))})

  (let [event-chan (a/chan)]
    (event-handler event-chan)
    (when-let [node (.getElementById js/document "container")]
      (rum/mount (ui/app state #(go (>! event-chan %))) node)
      (blockchain/refresh-data!)
      (blockchain/start-sync!))))
