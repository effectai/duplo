(ns duplo.state
  "Keep track of application state")

(defonce state (atom {:backend-url "http://localhost:10336"
                      :form nil                               ; current active form
                      :route :blocks                          ; current active route
                      :height 0                               ; blockchain height
                      :contracts []                           ; registered assets
                      :assets []                              ; registered assets
                      :block-ids []                           ; block ids in UI
                      :blocks {}                              ; map of blocks keyed by id
                      :keys []}))                             ; keys in the rpc wallet

(defn assoc-state! [key-path val]
  (swap! state #(assoc-in % key-path val)))

(defn update-state! [key-path update-fn]
  (swap! state #(update-in % key-path update-fn)))

