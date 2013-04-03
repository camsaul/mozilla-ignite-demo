(ns ignite-demo.views.graph
  "Helper functions to draw SVG charts and graphs.")

(declare display-label)

(defn display-vertical-bar-graph
  "Draws a vertical bar graph of width. col-pairs are vectors of the format [label value] for each column.
   Automatically scales values to fill graph appropriately."
  [title col-pairs graph-width col-height]
  (let [num-cols (count col-pairs)
        graph-height (* col-height num-cols)
        y-offsets (iterate (partial + col-height) 0)
        max-count (apply max (map last col-pairs))
        scale-x (quot (- graph-width 200) (+ 1 max-count))] ; leave a little padding at right for lables
    [:div.bar-graph
     [:h6 title]
     `[:svg {:xmlns "http://www.w3.org/2000/svg"
             :preserveAspectRatio "xMinYMin meet"
             :width ~graph-width :height ~graph-height :viewBox ~(format "0 0 %d %d" graph-width graph-height)}
       ~@(mapv (fn [[col-title count] y-offset]
                 (let [count (float count)
                       [r g b] (repeatedly #(int (rand 255)))
                       width (max 5.0 (* count scale-x))]
                   [:g
                    [:rect {:x 0 :y y-offset :width width :height col-height
                            :style (format "fill:rgb(%d,%d,%d);stroke-width:0;" r g b)} ""]
                    (display-label col-title (+ width 5) (+ y-offset col-height -5))]))
               col-pairs y-offsets)]]))

(defn display-bar-graph
  "Draws a graph of width and height. col-pairs are vector pairs of the format [label value] for each column.
   Automatically scales values to fill graph appropriately."
  [title col-pairs graph-width graph-height]
  (let [num-cols (count col-pairs)
        col-width (quot graph-width num-cols)
        x-offsets (iterate (partial + col-width) 0)
        max-count (apply max (map last col-pairs))
        scale-y (quot (- graph-height 40) (+ 1 max-count))] ; leave a little padding at top for lables
    [:div.bar-graph
     [:h6 title]
     `[:svg {:xmlns "http://www.w3.org/2000/svg"
             :preserveAspectRatio "xMinYMin meet"
             :width ~graph-width :height ~graph-height :viewBox ~(format "0 0 %d %d" graph-width graph-height)}
       ~@(mapv (fn [[col-title count] x-offset]
                 (let [count (float count)
                       [r g b] (repeatedly #(int (rand 255)))
                       height (max 5.0 (* count scale-y))
                       y-offset (- graph-height height)]
                   [:g
                    [:rect {:x x-offset :y y-offset :width col-width :height height
                            :style (format "fill:rgb(%d,%d,%d);stroke-width:0;" r g b)} ""]
                    (display-label col-title (format "%.1f" count) x-offset (- y-offset 20))]))
               col-pairs x-offsets)]]))

(defn display-label
  "Helper method to display the label for a column on a bar graph. Can optionally display a subtitle as well
   underneath the main title"
  ([title x-offset y-offset]
     (let []
       [:text {:x x-offset :y y-offset} title]))
  ([title subtitle x-offset y-offset]
     (let []
       [:text {:x x-offset :y y-offset} title
        [:tspan {:x x-offset :y (+ y-offset 15)} subtitle]])))
