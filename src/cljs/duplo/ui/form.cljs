(ns duplo.ui.form
  (:require [duplo.state :refer [state]]
            [duplo.dom :as dom]
            [rum.core :as rum]))

(rum/defc form-footer [callback-fn]
  [:div.footer
   [:button {:on-click (fn [_] (callback-fn [:close-form]) false)} "cancel"]

   [:button {:type "submit" :on-click (fn [_] false)} "submit"]])

(defn- gather-tx []
  {:to-addr (dom/value (dom/q ".to-addr"))
   :amount (dom/value (dom/q ".amount"))
   :asset (dom/value (dom/q ".asset"))})

(defn- asset-options []
  (map (fn [e] [:option {:value (:txid e)} (get-in e [:name 1 :name])]) (:assets @state)))

(rum/defc transaction-form [callback-fn]
  [:form.add-view {:on-submit (fn [e] (callback-fn [:make-tx (gather-tx)]) (.preventDefault e))}
   [:input.to-addr {:placeholder "To"}]
   [:input.amount {:placeholder "Amount"}]
   (vec (concat [:select.asset] (asset-options)))
   [:input.add-submit {:type "submit" :value "Submit"}]
   ])

(rum/defc active-form < rum/reactive
  [callback-fn]
  (case (rum/react (rum/cursor-in state [:form]))
    :make-tx (transaction-form callback-fn)
    nil))
