(ns duplo.ui.form
  (:require [duplo.state :refer [state]]
            [duplo.dom :as dom]
            [rum.core :as rum]))


;;;
;;; UI components
;;;

(rum/defc form-footer [callback-fn]
  [:div.footer
   [:button {:on-click (fn [_] (callback-fn [:close-form]) false)} "cancel"]

   [:button {:type "submit" :on-click (fn [_] false)} "submit"]])

;;;
;;; These functions extract entities from forms in the DOM 
;;;

(defn- extract-tx []
  {:to-addr (dom/value (dom/q ".to-addr"))
   :amount (dom/value (dom/q ".amount"))
   :asset (dom/value (dom/q ".asset"))})

(defn- extract-contract []
  {:name (dom/value (dom/q ".name"))
   :version (dom/value (dom/q ".version"))
   :author (dom/value (dom/q ".author"))
   :email (dom/value (dom/q ".email"))
   :description (dom/value (dom/q ".description"))
   :needs-storage (not (nil? (dom/q ".needs-storage:checked")))
   :params (dom/value (dom/q ".params"))
   :return (dom/value (dom/q ".return"))
   :script (dom/value (dom/q ".script"))})

;;;
;;; Helpers
;;;

(defn- asset-options []
  (map (fn [e] [:option {:value (:txid e)} (get-in e [:name 1 :name])]) (:assets @state)))

;;;
;;; The forms
;;;

(rum/defc transaction-form [callback-fn]
  [:form {:on-submit (fn [e] (callback-fn [:make-tx (extract-tx)]) (.preventDefault e))}
   [:input.to-addr {:placeholder "To"}]
   [:input.amount {:placeholder "Amount"}]
   (vec (concat [:select.asset] (asset-options)))
   [:input {:type "submit" :value "Submit"}]])

(rum/defc deploy-contract-form [callback-fn]
  [:form {:on-submit (fn [e] (callback-fn [:deploy-contract (extract-contract)]) (.preventDefault e))}
   [:input.name {:placeholder "name"}]
   [:input.version {:placeholder "version"}]
   [:input.author {:placeholder "author"}]
   [:input.email {:placeholder "email"}]
   [:textarea.description {:placeholder "description"}]
   [:label [:input.needs-storage {:type "checkbox"}] "needs-storage"]
   [:input.params {:placeholder "params"}]
   [:input.return {:placeholder "return"}]
   [:textarea.script {:placeholder "script"}]
   [:input {:type "submit" :value "Deploy"}]])

(rum/defc active-form < rum/reactive
  [callback-fn]
  (case (rum/react (rum/cursor-in state [:form]))
    :make-tx (transaction-form callback-fn)
    :deploy-contract (deploy-contract-form callback-fn)
    nil))
