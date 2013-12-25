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
    :add-item
    (let [[section title] params]
      (om/update! root-context [:canvas]
                  add-item section title))

    :set-document-property
    (let [[key value] params]
      (om/update! root-context [:canvas]
                  assoc key {:value value}))

    root-context))

;;; UI

(defn header-box [context {:keys [title key] :as opts}]
  (om/component
    (html [:div.header-box
           [:div.row title]
           ;[:div.row (-> context :value)]
           (dom/input #js {:value (-> context :value)
                           :onChange
                           #(post-event context
                                        [:set-document-property key (-> % .-target .-value)])})])))

(defn header [context opts]
  (om/component
    (html [:div.row
           [:div.col-md-4.title "The Business Model Canvas"]
           
           [:div.col-md-3 (om/build header-box context {:opts {:title "Designed For:", :key :designed-for}
                                                        :path [:designed-for]})]

           [:div.col-md-3 (om/build header-box context {:opts {:title "Designed By:", :key :designed-by}
                                                        :path [:designed-by]})]

           [:div.col-md-1 (om/build header-box context {:opts {:title "Date:", :key :date}
                                                        :path [:date]})]

           [:div.col-md-1 (om/build header-box context {:opts {:title "Version:", :key :version}
                                                        :path [:version]})]])))


(defn canvas-cell [context {:keys [title icon key]}]
  (om/component
    (html [:div.table-cell

           [:div
            [(keyword (str "span.glyphicon.canvas-cell-image" ".glyphicon-" icon))]
            title]

           [:ul
            (for [item context]
              [:li (:value item)])]

           [:button.btn.btn-default.btn-xs.add-item-button
            {:onClick #(post-event context [:add-item key "~new item~"])}
            [:span.glyphicon.glyphicon-plus]]])))


(defn canvas-table [context opts]
  (om/component
    (html [:table.table.table-bordered.canvas-table
           [:tr
            [:td.canvas-cell.tall-cell {:rowSpan 2, :colSpan 2}
             (om/build canvas-cell context {:opts {:title "Key Partners", :icon "link", :key :key-partners}
                                            :path [:sections :key-partners]})]

            [:td.canvas-cell.short-cell {:rowSpan 1, :colSpan 2}
             (om/build canvas-cell context {:opts {:title "Key Activities", :icon "check", :key :key-activities}
                                            :path [:sections :key-activities]})]

            [:td.canvas-cell.tall-cell {:rowSpan 2, :colSpan 2}
             (om/build canvas-cell context {:opts {:title "Value Propositions", :icon "gift", :key :value-propositions}
                                            :path [:sections :value-propositions]})]

            [:td.canvas-cell.short-cell {:rowSpan 1, :colSpan 2}
             (om/build canvas-cell context {:opts {:title "Customer Relationships", :icon "heart", :key :customer-relationships}
                                            :path [:sections :customer-relationships]})]

            [:td.canvas-cell.tall-cell {:rowSpan 2, :colSpan 2}
             (om/build canvas-cell context {:opts {:title "Customer Segments", :icon "user", :key :customer-segments}
                                            :path [:sections :customer-segments]})]]
           [:tr
            [:td.canvas-cell.short-cell {:rowSpan 1, :colSpan 2}
             (om/build canvas-cell context {:opts {:title "Key Resources", :icon "tree-deciduous", :key :key-resources}
                                            :path [:sections :key-resources]})]

            [:td.canvas-cell.short-cell {:rowSpan 1, :colSpan 2}
             (om/build canvas-cell context {:opts {:title "Channels", :icon "send", :key :channels}
                                            :path [:sections :channels]})]]

           [:tr
            [:td.canvas-cell.short-cell {:rowSpan 1, :colSpan 5}
             (om/build canvas-cell context {:opts {:title "Cost Structure", :icon "tags", :key :cost-structure}
                                            :path [:sections :cost-structure]})]

            [:td.canvas-cell.short-cell {:rowSpan 1, :colSpan 5}
             (om/build canvas-cell context {:opts {:title "Revenue Streams", :icon "usd", :key :revenue-streams}
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
