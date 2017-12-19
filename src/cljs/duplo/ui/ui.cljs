(ns duplo.ui.ui
  (:require
   [rum.core :as rum]
   [goog.string :as gstring]
   [duplo.ui.svg :as svg]
   [duplo.ui.form :as form]))

(defn split-hex [s parts step-size]
        (let [mid (/ (count s) 2)
              n step-size]
        (loop [i 0 
               cur nil
               l-idx mid 
               r-idx mid]
          (if (< i (count parts))
            (recur (inc i) 
                   (if (nil? cur)
                     [:span {:class (nth parts i)} 
                      (subs s (- l-idx n) (+ r-idx n))]
                     [:span {:class (nth parts i)} 
                      (subs s (- l-idx n) l-idx) cur (subs s r-idx (+ r-idx n))])
                   (- l-idx n)
                   (+ r-idx n))
            [:span.hex (subs s 0 l-idx) cur (subs s r-idx (count s))]))))

(defn dots-in-middle [s n]
  (let [middle (/ (count s) 2)
        offs (int (/ n 2))
        [first tail] (split-at offs s)
        [_ end] (split-at (* offs 2) tail)]
    (reduce str (concat first "..." end))))

(rum/defc block-item
  [{hsh :hash :keys [index confirmations size time tx] :as block}]
     [:div.row
     [:div.item.fixed [:div.label "Index"] [:div.value index]]
     [:div.item.grow  [:div.label "Hash"] [:div.value (split-hex hsh ["small" "medium" "big"] 8)]]
     [:div.item.fixed [:div.label "Transactions"] [:div.value(count tx)]]
     [:div.item.fixed [:div.label "Size"] [:div.value size]]
     [:div.item.fixed [:div.label "Confirmations"] [:div.value confirmations]]]
   )

(rum/defc block-list < rum/reactive
  [block-ids blocks]
  (conj
   [:div]
    [:div.table
   (map #(block-item (get (rum/react blocks) %))
        (reverse (rum/react block-ids)))]))

(rum/defc asset-item
  [{names :name :keys [type amount admin txid] :as asset}]
  (let [name-en (->> names (filter #(= (:lang %) "en")) first :name)]
    [:div.row
     [:div.item.fixed [:div.label "Name"] [:div.value name-en]]
     [:div.item.grow  [:div.label "Asset ID"] [:div.value (split-hex txid ["small" "medium" "big"] 8)]]
     [:div.item.fixed [:div.label "Type"]  [:div.value type]]
     [:div.item.fixed [:div.label "Amount"]  [:div.value amount]]
     [:div.item.fixed [:div.label "Admin"] [:div.value (split-hex admin ["small" "medium" "big"] 3)]]]))

(rum/defc contract-item
  [{:keys [author email name hash description]}]
  [:div.row
   [:div.item.fixed [:div.label "Name"] [:div.value name]]
   [:div.item.grow  [:div.label "Hash"] [:div.value hash]]
   [:div.item.fixed [:div.label "Email"] [:div.value email]]
   [:div.item.fixed [:div.label "Description"] [:div.value description]]])

(rum/defc asset-list < rum/reactive [items]
  (conj
   [:div]
    [:div.table
   (map asset-item (rum/react items))]))

(defn wallet-item
  [{:keys [public-key address wif]
    {:keys [neo gas]} :balance :as key}]
  [:div.row
   [:div.item.grow [:div.label "Address"] [:div.value address]]
   [:div.item.grow [:div.label "Public key"] [:div.value (split-hex public-key ["small" "medium" "big"] 8)]]
   [:div.item.fixed [:div.label "NEO"] [:div.value neo]]
   [:div.item.fixed [:div.label "GAS"] [:div.value gas]]])

(defn wallet-menu [callback-fn]
  [:ul
   [:li [:button {:on-click #(callback-fn [:generate-keys])}
         "Generate Keys"]]
   [:li [:button {:on-click #(callback-fn [:claim-initial-neo])}
         "Claim Initial NEO"]]
   [:li [:button {:on-click #(callback-fn [:open-form :make-tx])}
         "Make Transaction"]]
   [:li [:button {:on-click #(callback-fn [:claim-gas])}
         "Claim GAS"]]])

(defn contracts-menu [callback-fn]
  [:ul
   [:li [:button {:on-click #(callback-fn [:open-form :deploy-contract])}
    "Deploy Contract"]]])

(rum/defc wallet-list < rum/reactive
  [key-pairs callback-fn]
  (conj
   [:div.table
    (wallet-menu callback-fn)
    (form/active-form callback-fn)]
   (conj [:div]
         (->> key-pairs rum/react
              (sort-by #(get-in % [:balance :neo]))
              reverse
              (map wallet-item)))))

(rum/defc contract-list < rum/reactive
  [contracts callback-fn]
  (conj
   [:div.table
    (contracts-menu callback-fn)
    (form/active-form callback-fn)]
   (conj [:div]
         (map contract-item (rum/react contracts)))))

(rum/defc page-blocks [state]
  [:div.block-list
   [:h3 "Blocks"]
   (block-list (rum/cursor-in state [:block-ids])
               (rum/cursor-in state [:blocks]))])

(rum/defc page-assets [state]
  [:div.block-list
   [:h3 "Assets"]
   (asset-list (rum/cursor-in state [:assets]))])

(rum/defc page-contracts [state callback-fn]
  [:div.block-list
   [:h3 "Contracts"]
   (contract-list (rum/cursor-in state [:contracts]) callback-fn)])

(rum/defc page-wallet [state callback-fn]
  [:div.block-list
   [:h3 "Keys"]
   (wallet-list (rum/cursor-in state [:keys]) callback-fn)])

(rum/defc main-menu [route]
  [:menu
   [:ul
    [:li.logo [:a {:href "/"} svg/logo "DUPLO" ]]
    [:li [:a {:href "blocks"} [:img {:src "img/Blocks.svg"}] "Blocks" ]]
    [:li [:a {:href "assets"} [:img {:src "img/Assets.svg"}] "Assets" ]]
    [:li [:a {:href "contracts"} [:img {:src "img/Contract.svg"}] "Contracts" ]]
    [:li [:a {:href "wallet"} [:img {:src "img/Wallet.svg"}] "Wallet" ]]]])

(rum/defc app < rum/reactive [state callback-fn]
  [:div.container.flex
   [:header (main-menu)]
   [:main (case (rum/react (rum/cursor-in state [:route]))
     :blocks (page-blocks state)
     :assets (page-assets state)
     :contracts (page-contracts state callback-fn)
     :wallet (page-wallet state callback-fn))]
   [:footer]])
