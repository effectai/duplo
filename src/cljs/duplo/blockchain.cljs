(ns duplo.blockchain
  "Keep blockchain info synced"
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs-http.client :as http]
            [cljs.core.async :as async :refer [<!]]
            [duplo.state :refer [state assoc-state! update-state!]]))

(def rpc-server "http://localhost:10336")

(def num-blocks 10)

(def ms-per-block 3000)

(defonce update-timer (atom nil))

(defn make-request
  ([method callback-fn] (make-request method [] callback-fn))
  ([method params callback-fn]
   (let [response (go (<! (http/post
                           rpc-server
                           {:json-params {:jsonrpc 2.0 :id 1
                                          :method method
                                          :params params} 
                            :with-credentials? false})))]
     (async/take! response
                  (fn [{:keys [status success] {result :result} :body}]
                    (if (not success) (js/alert "RPC error"))
                    (prn "rpc request debug" result)
                    (callback-fn result))))))

(defn- fetch-block [n]
  (make-request
   "getblock" [n 1]
   (fn [block]
     (when block
       (assoc-state! [:blocks n] block)))))

(defn refresh-data! []
  (make-request
   "getblockcount"
   (fn [height]
     (let [height (dec height)]
       (assoc-state! [:height] height)
       (let [block-ids (range (- height num-blocks) height)]
         (assoc-state! [:block-ids] block-ids)
         (doall (map fetch-block block-ids))))))

  (make-request
   "getassets"
   (fn [res]
     (swap! state #(assoc-in % [:assets] res)))))

(defn- update-block! []
  (.log js/console "Updating block...")
  (let [cur-height (:height @state)]
    (when (contains? (:blocks @state) cur-height)
      (update-state! [:block-ids] #(concat % [cur-height]))
      (update-state! [:height] inc)))
  (fetch-block (:height @state)))

(defn start-sync []
  (reset! update-timer (js/setInterval update-block! ms-per-block)))
