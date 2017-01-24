
(ns kanban.core
  (:require [reagent.core :as reagent ]))

(enable-console-print!)

(def board 
  (reagent/atom {:columns
           [{:title "Todos"
             :cards [{:title "Learn about Reagent"}
                     {:title "Tell my friends about Lambda Island"
                      :editing true}]}]}))

(def app-state (reagent/atom {:columns [{:id (random-uuid)
                                   :cards []}
                                  {:id (random-uuid)
                                   :cards [{:id (random-uuid)
                                            :title "Hello Island."}]}]}))

(def cards-cursor
  (reagent/cursor board [:columns 0 :cards]))

(defn- update-title [card-cur title]
  (swap! card-cur assoc :title title))

(defn- stop-editing [card-cur]
  (swap! card-cur dissoc :editing))

(defn- start-editing [card-cur]
  (swap! card-cur assoc :editing true))

(defn AutoFocusInput [props]
  (reagent/create-class
   {:display-name "AutoFocusInput"
    :component-did-mount (fn [component]
                           (.focus (reagent/dom-node component)))
    :reagent-render (fn [props]
                      [:input props])}))

(defn Editable [el cur]
  (let [{:keys [editing title]} @cur]
    (if editing
      [el
       {:className "editing"}
       [AutoFocusInput {:type "text"
                        :value title
                        :autoFocus true
                        :on-change #(update-title cur (.. % -target -value))
                        :on-blur #(stop-editing cur)
                        :on-key-press #(if (= (.-charCode %) 13)
                                         (stop-editing cur))}]]
      [el {:on-click #(start-editing cur)} title])))

(defn Card [card-cur]
  [Editable :div.card card-cur])

(defn add-new-card [col-cur]
  (swap! col-cur update :cards conj {:id (random-uuid)
                                     :title ""
                                     :editing true}))

(defn NewCard [col-cur]
  [:div.new-card
   {:on-click #(add-new-card col-cur)}
   "+ add new card"])

(defn Column [col-cur]
  (let [{:keys [title cards editing]} @col-cur]
    [:div.column
     ^{:key "title"} [Editable :h2 col-cur]
     (map-indexed (fn [idx {id :id}]
                    ^{:key id}
                    [Card (reagent/cursor col-cur [:cards idx])])
                  cards)
     ^{:key "new"} [NewCard col-cur]]))

(defn set-title! [board col-idx card-idx title]
  (swap! board assoc-in [:columns col-idx :cards card-idx :title] title))

(defn add-new-column [board]
  (swap! board update :columns conj {:id (random-uuid)
                                     :title ""
                                     :cards []
                                     :editing true}))

(defn NewColumn [board]
  [:div.new-column
   {:on-click #(add-new-column board)}
   "+ add new column"])

(defn Board [board]
  [:div.board
   (map-indexed (fn [i {id :id}]
                  ^{:key id}
                  [Column (reagent/cursor board [:columns i])])
                (:columns @board))
   ^{:key "new"} [NewColumn board]])

(reagent/render [Board board] (js/document.getElementById "app"))
