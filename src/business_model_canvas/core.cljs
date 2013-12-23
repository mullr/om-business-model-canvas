(ns business-model-canvas.core
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [sablono.core :as html :refer [html] :include-macros true]))

(set! *print-fn* #(.log js/console %))

(defn header-box [data opts]
  (om/component
    (html [:div.header-box
           [:div.row (:title opts)]
           [:div.row (-> data :value)]])))

(defn canvas-cell [data opts]
  (om/component
    (html [:div.table-cell
           [:div (:title opts)]
           [:ul
            (for [item data]
              [:li (:value item)])]])))

(defn root [data]
  (om/component
    (html
      [:div.container
       [:div.row
        [:div.col-md-4.title "The Business Model Canvas"]

        [:div.col-md-3 (om/build header-box data {:opts {:title "Designed For:"}
                                                  :path [:designed-for]})]

        [:div.col-md-3 (om/build header-box data {:opts {:title "Designed By:"}
                                                  :path [:designed-by]})]

        [:div.col-md-1 (om/build header-box data {:opts {:title "Date:"}
                                                  :path [:date]})]

        [:div.col-md-1 (om/build header-box data {:opts {:title "Version:"}
                                                  :path [:version]})]]

       [:div.row
        [:table.table.table-bordered.canvas-table
         [:tr
          [:td.tall-cell {:rowSpan 2, :colSpan 2}
           (om/build canvas-cell data {:opts {:title "Key Partners"}
                                       :path [:sections :key-partners]})]

          [:td.short-cell {:rowSpan 1, :colSpan 2}
           (om/build canvas-cell data {:opts {:title "Key Activities"}
                                       :path [:sections :key-activities]})]

          [:td.tall-cell {:rowSpan 2, :colSpan 2}
           (om/build canvas-cell data {:opts {:title "Value Propositions"}
                                       :path [:sections :value-propositions]})]

          [:td.short-cell {:rowSpan 1, :colSpan 2}
           (om/build canvas-cell data {:opts {:title "Customer Relationships"}
                                       :path [:sections :customer-relationships]})]

          [:td.tall-cell {:rowSpan 2, :colSpan 2}
          (om/build canvas-cell data {:opts {:title "Customer Segments"}
                                       :path [:sections :customer-segments]})]]
         [:tr
          [:td.short-cell {:rowSpan 1, :colSpan 2}
          (om/build canvas-cell data {:opts {:title "Key Resources"}
                                       :path [:sections :key-resources]})]

          [:td.short-cell {:rowSpan 1, :colSpan 2}
          (om/build canvas-cell data {:opts {:title "Channels"}
                                       :path [:sections :channels]})]]

         [:tr
          [:td.short-cell {:rowSpan 1, :colSpan 5}
           (om/build canvas-cell data {:opts {:title "Cost Structure"}
                                       :path [:sections :cost-structure]})]

          [:td.short-cell {:rowSpan 1, :colSpan 5}
           (om/build canvas-cell data {:opts {:title "Revenue Streams"}
                                       :path [:sections :revenue-streams]})]]

         ]]]
      )))

(def doc
  (atom
    {:designed-for {:value "A customer"}
     :designed-by  {:value "ME!!!!!"}
     :date         {:value "Today"}
     :version      {:value "7"}

     :sections
     {:key-partners
      [{:value "Item 1"}
       {:value "Item 2"}]

      :key-activities
      [{:value "Item 1"}
       {:value "Item 2"}]

      :key-resources
      [{:value "Item 1"}
       {:value "Item 2"}]

      :value-propositions
      [{:value "Item 1"}
       {:value "Item 2"}]

      :customer-relationships
      [{:value "Item 1"}
       {:value "Item 2"}]

      :channels
      [{:value "Item 1"}
       {:value "Item 2"}]

      :customer-segments
      [{:value "Item 1"}
       {:value "Item 2"}]

      :cost-structure
      [{:value "Item 1"}
       {:value "Item 2"}]

      :revenue-streams
      [{:value "Item 1"}
       {:value "Item 2"}]}}))

(om/root doc root js/document.body)
