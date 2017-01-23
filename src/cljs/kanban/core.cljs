
(ns kanban.core
  (:require [reagent.core :as reagent ]))

(enable-console-print!)

(def board 
  (reagent/atom {:columns
           [{:title "Todos"
             :cards [{:title "Learn about Reagent"}
                     {:title "Tell my friends about Lambda Island"
                      :editing true}]}]}))

(def cards-cursor
  (reagent/cursor board [:columns 0 :cards]))

(defn- update-title [card-cur title]
  (swap! card-cur assoc :title title))

(defn- stop-editing [card-cur]
  (swap! card-cur dissoc :editing))

(defn- start-editing [card-cur]
  (swap! card-cur assoc :editing true))

(defn- Card [card-cur]
  (let [{:keys [editing title]} @card-cur]
    (if editing
      [:div.card.editing [:input {:type "text"
                                  :value title
                                  :autoFocus true
                                  :on-change #(update-title card-cur (.. % -target -value))
                                  :on-blur #(stop-editing card-cur)
                                  :on-key-press #(if (= (.-charCode %) 13)
                                                   (stop-editing card-cur))}]]
      [:div.card {:on-click #(start-editing card-cur)} title])))

(defn NewCard []
  [:div.new-card
   "+ add new card"])

(defn Column [col-cur]
  (let [{:keys [title cards editing]} @col-cur]
    [:div.column
     (if editing
       [:input {:type "text" :value title}]
       [:h2 title])
     (for [i (range (count cards))]
       [Card (reagent/cursor col-cur [:cards i])])
     [NewCard]]))

(defn NewColumn []
  [:div.new-column
   "+ add new column"])

(defn set-title! [board col-idx card-idx title]
  (swap! board assoc-in [:columns col-idx :cards card-idx :title] title))

(defn Board [board]
  [:div.board
   (for [i (range (count (:columns @board)))]
     [Column (reagent/cursor board [:columns i])])
   [NewColumn]])

(reagent/render [Board board] (js/document.getElementById "app"))
