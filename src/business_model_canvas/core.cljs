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

;;; App model and events

(defn make-app []
  {:canvas blank-canvas
   :ui-events-chan (chan)})

(defn post-event [type context param]
  (let [{state-atom :om.core/state, path :om.core/path} (meta context)
        ui-events-chan (:ui-events-chan @state-atom)]
    (put! ui-events-chan [type path param])))

(defn handle-event [app [type path param :as e]]
  (case type
    :add-item
    (update-in app path conj param)

    :set-value
    (update-in app path assoc :value param)

    ;; default - do nothing
    app))

;;; UI
(defn start-editing-label! [owner]
  (om/set-state! owner [:editing] true))

(defn stop-editing-label! [owner]
  (om/set-state! owner [:editing] false))

;; keycodes
(def enter 13)
(def esc 27)

(defn editable-label [{:keys [value] :as context} opts]
  (reify
    om/IInitState
    (init-state [ _ owner] {:editing false})

    om/IRender
    (render [_ owner]
      (html
        [:div {:onDoubleClick #(start-editing-label! owner)}
         (if (om/get-state owner [:editing])
           (dom/input #js {:className "form-control"
                           :value value
                           :autoFocus true
                           :onChange #(post-event :set-value context (.. % -target -value))
                           :onBlur #(stop-editing-label! owner)
                           :onKeyUp #(if (#{enter esc} (.-keyCode %))
                                      (stop-editing-label! owner))})
           value)]))))

(defn header-box [context {:keys [title] :as opts}]
  (om/component
    (html [:div.header-box
           [:div.row title]
           (om/build editable-label context)])))

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
            (map-indexed (fn [n item]
                           [:li
                            (om/build editable-label context [n])])
                         context)]

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
      (let [{:keys [ui-events-chan]} context
            state-atom (:om.core/state (meta context))]
        (go (while true
              (swap! state-atom
                     handle-event (<! ui-events-chan))))))

    om/IRender
    (render [_ owner]
      (html
        [:div.container
         (om/build header context [:canvas])
         [:div.row
          (om/build canvas-table context [:canvas])]]))
    ))

(om/root (make-app) root js/document.body)
