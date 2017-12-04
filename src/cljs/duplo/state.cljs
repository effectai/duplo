(ns duplo.state
  "Keep track of application state")

(defonce state (atom {:route :blocks
                      :height 0
                      :assets []
                      :block-ids []
                      :blocks {}
                      :keys []}))

(defn assoc-state! [key-path val]
  (swap! state #(assoc-in % key-path val)))

(defn update-state! [key-path update-fn]
  (swap! state #(update-in % key-path update-fn)))

