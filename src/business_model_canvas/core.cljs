(ns business-model-canvas.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [sablono.core :as html :refer [html] :include-macros true]
            [cljs.core.async :refer [put! <! chan]]))

(set! *print-fn* #(.log js/console %))

;;; Canvas model

(def blank-canvas
  {:designed-for {:value "A customer"}
   :designed-by  {:value "ME!!!!!"}
   :date         {:value "Today"}
   :version      {:value "7"}

   :sections
   {:key-partners []
    :key-activities []
    :key-resources []
    :value-propositions []
    :customer-relationships []
    :channels []
    :customer-segments []
    :cost-structure []
    :revenue-streams []}})

(defn add-item [canvas section title]
  (update-in canvas [:sections section]
             conj {:value title}))

;;; App model and events

(defn make-app []
  {:canvas blank-canvas
   :ui-events-chan (chan)})

(defn post-event [context event]
  (let [ui-events-chan (-> (meta context) :om.core/state deref :ui-events-chan)]
    (put! ui-events-chan event)))

(defn handle-event! [root-context [type & params]]
  (case type
    :add-item (let [[section title] params]
                (om/update! root-context [:canvas]
                            add-item section title))
    root-context))

;;; UI

(defn header-box [context opts]
  (om/component
    (html [:div.header-box
           [:div.row (:title opts)]
           [:div.row (-> context :value)]])))

(defn header [context opts]
  (om/component
    (html [:div.row
           [:div.col-md-4.title "The Business Model Canvas"]

           [:div.col-md-3 (om/build header-box context {:opts {:title "Designed For:"}
                                                        :path [:designed-for]})]

           [:div.col-md-3 (om/build header-box context {:opts {:title "Designed By:"}
                                                        :path [:designed-by]})]

           [:div.col-md-1 (om/build header-box context {:opts {:title "Date:"}
                                                        :path [:date]})]

           [:div.col-md-1 (om/build header-box context {:opts {:title "Version:"}
                                                        :path [:version]})]])))


(defn canvas-cell [context opts]
  (om/component
    (html [:div.table-cell
           [:div (:title opts)]
           [:ul
            (for [item context]
              [:li (:value item)])]])))


(defn canvas-table [context opts]
  (om/component
    (html [:table.table.table-bordered.canvas-table
           [:tr
            [:td.tall-cell {:rowSpan 2, :colSpan 2}
             (om/build canvas-cell context {:opts {:title "Key Partners"}
                                            :path [:sections :key-partners]})]

            [:td.short-cell {:rowSpan 1, :colSpan 2}
             (om/build canvas-cell context {:opts {:title "Key Activities"}
                                            :path [:sections :key-activities]})]

            [:td.tall-cell {:rowSpan 2, :colSpan 2}
             (om/build canvas-cell context {:opts {:title "Value Propositions"}
                                            :path [:sections :value-propositions]})]

            [:td.short-cell {:rowSpan 1, :colSpan 2}
             (om/build canvas-cell context {:opts {:title "Customer Relationships"}
                                            :path [:sections :customer-relationships]})]

            [:td.tall-cell {:rowSpan 2, :colSpan 2}
             (om/build canvas-cell context {:opts {:title "Customer Segments"}
                                            :path [:sections :customer-segments]})]]
           [:tr
            [:td.short-cell {:rowSpan 1, :colSpan 2}
             (om/build canvas-cell context {:opts {:title "Key Resources"}
                                            :path [:sections :key-resources]})]

            [:td.short-cell {:rowSpan 1, :colSpan 2}
             (om/build canvas-cell context {:opts {:title "Channels"}
                                            :path [:sections :channels]})]]

           [:tr
            [:td.short-cell {:rowSpan 1, :colSpan 5}
             (om/build canvas-cell context {:opts {:title "Cost Structure"}
                                            :path [:sections :cost-structure]})]

            [:td.short-cell {:rowSpan 1, :colSpan 5}
             (om/build canvas-cell context {:opts {:title "Revenue Streams"}
                                            :path [:sections :revenue-streams]})]]
           ])))

(defn root [context]
  (reify
    om/IWillMount
    (will-mount [_ owner]
      (let [{:keys [ui-events-chan]} context]
        (go (while true
              (handle-event! context (<! ui-events-chan))))))
    om/IRender
    (render [_ owner]
      (html
        [:div.container
         (om/build header context [:canvas])
         [:button {:onClick #(post-event context [:add-item :key-activities "~new item~"])}
          "Add an item"]
         [:div.row
          (om/build canvas-table context [:canvas])]]))
    ))

(om/root (make-app) root js/document.body)
