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
          :navigate (do (assoc-state! [:route] (first params))
                        (assoc-state! [:form] nil))
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
  (let [event-chan (a/chan)
        callback-fn #(go (>! event-chan %))]
    ;; Start listening for events
    (event-handler event-chan)

    ;; Setup router
    (accountant/configure-navigation!
     {:nav-handler (fn [path]
                     (let [{:keys [handler]} (bidi/match-route app-routes path)]
                       (callback-fn [:navigate handler])))
      :path-exists? (fn [path]
                      (boolean (bidi/match-route app-routes path)))})

    ;; Mount application and fetch initial data
    (when-let [node (.getElementById js/document "container")]
      (rum/mount (ui/app state callback-fn) node)
      (blockchain/refresh-data!)
      (blockchain/start-sync!))))
