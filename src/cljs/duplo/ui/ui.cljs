(ns duplo.ui.ui
  (:require
   [rum.core :as rum]
   [goog.string :as gstring]
   [duplo.ui.svg :as svg]
   [duplo.ui.form :as form]))

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
     [:div.item.grow [:div.label "Hash"] [:div.value.big (str 0 (gstring/unescapeEntities "&times;") hsh)] [:div.value.small (str 0 (gstring/unescapeEntities "&times;") (dots-in-middle hsh 30))]]
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
     [:div.item.fixed [:div.label "Name"] name-en]
     [:div.item.grow  [:div.label "Asset ID"] txid]
     [:div.item.fixed [:div.label "Type"]  type]
     [:div.item.fixed [:div.label "Amount"]  amount]
     [:div.item.fixed [:div.label "Admin"] admin]]))

(rum/defc contract-item
  [{:keys [author email name hash description]}]
  [:div.row
   [:div.item.fixed [:div.label "Name"] name]
   [:div.item.grow  [:div.label "Hash"] hash]
   [:div.item.fixed [:div.label "Email"] email]
   [:div.item.fixed [:div.label "Description"] description]])

(rum/defc asset-list < rum/reactive [items]
  (conj
   [:div]
    [:div.table
   (map asset-item (rum/react items))]))

(defn wallet-item
  [{:keys [public-key address wif]
    {:keys [neo gas]} :balance :as key}]
  [:div.row
   [:div.item.grow [:div.label "Address"] address]
   [:div.item.grow [:div.label "Public key"] public-key]
   [:div.item.fixed [:div.label "NEO"] neo]
   [:div.item.fixed [:div.label "GAS"] gas]])

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
