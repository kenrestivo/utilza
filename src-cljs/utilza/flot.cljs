(ns utilza.flot
  (:require [om.core :as om :include-macros true]))

;; Requires <script> tags for jquery, flot, and any plugins
;; Also requires you add the height and width in CSS

(defn flot
  "Wraps a flot chart.
   node-name is the id for the DOM node of the flot chart
   chart-options is a clojure nested map of options for flot (see flot docs)
   data is clojure vector of vectors or maps, with the data series (see flot docs)"
  [{:keys [node-name chart-options data]} owner]
  (reify
    om/IInitState
    (init-state [_]
      {:flot nil})
    om/IDidMount
    (did-mount [this]
      (let [g (.plot js/jQuery  (js/document.getElementById node-name)
                     (clj->js data)
                     (clj->js chart-options))]
        (om/set-state! owner :flot g)))
    om/IDidUpdate
    (did-update [this prev-props {:keys [flot] :as prev-state}]
      (when (not= (:data prev-props) data)
        (doto flot
          (.setData (clj->js data))
          .setupGrid
          .draw)))
    om/IRender
    (render [this]
      (dom/div #js {:react-key node-name
                    :ref node-name       
                    :id node-name}))))

