(ns prc
  "Defines functions that allow the display of charts or tables.")

(defn- table-input->matrix
  "Converts table input into a sequence of sequences. Assumes table input is
  either already a sequence a sequences or a sequence of maps. Throws an exception
  otherwise."
  [table-input]
  (if (sequential? table-input)
    (if (map? (first table-input))
      (let [cols (keys (first table-input))]
        (cons cols (map #(map % cols) table-input)))
      table-input)
    (throw (Exception.
            "Table input must be a sequence of sequences or a sequence of maps."))))

;; TODO there's a limit on the amount of data that can be retrieved back from nRepl at a time
;; or the editor freezes. It appears to be a limitation of the underlying JavaScript library.
;; We could add a workaround for very large tables by writing the data to the file system
;; and then loading it in Atom.
(defn table
  "Displays the data in a table in a tab with the given name. rows can either be
  a sequence of sequences or a sequence of maps."
  [name rows]
  [:proto-repl-code-execution-extension
   "proto-repl-charts"
   {:type "table"
    :name name
    :data (table-input->matrix rows)}])

(defn custom-chart
  "Displays a custom chart in a tab with the given name. [C3](http://c3js.org/)
  is the charting library used. The chart config will be converted from Clojure
  to a JavaScript object and passed to C3. It can be any configuration data C3
  supports."
  [name chart-config]
  [:proto-repl-code-execution-extension
   "proto-repl-charts"
   {:type "chart"
    :name name
    :data chart-config}])

(defn- process-options
  "Processes the options modifying the chart as necessary."
  [chart options]
  (if (:labels options)
    (assoc-in chart [2 :data :axis :x]
              {:type "category" :categories (:labels options)})
    chart))

(defn line-chart
  "Displays a line chart in a tab with the given name.

  series-map is a map of series names to the values for the series. For example
  the following map would display two lines named 'alpha' and 'beta' on the
  same graph.

      {:alpha [1 2 3 4] :beta [10 20 30 40]}

  Options can be any of the following:
  * labels - a list of labels to give each value. The index of the label in the
  list corresponds to the index of the values in the series."
  ([name series-map]
   (line-chart name series-map nil))
  ([name series-map options]
   (-> (custom-chart name {:data {:json series-map}})
       (process-options options))))

(defn bar-chart
  "Displays a bar chart in a tab with the given name.

  series-map is a map of series names to the values for the series. For example
  the following map would display two sets of bars named 'alpha' and 'beta'.

  {:alpha [1 2 3 4] :beta [10 20 30 40]}

  Options can be any of the following:
  * labels - a list of labels to give each value. The index of the label in the
  list corresponds to the index of the values in the series."
  ([name series-map]
   (bar-chart name series-map nil))
  ([name series-map options]
   (-> (custom-chart name {:data {:json series-map
                                  :type "bar"}})
       (process-options options))))

(defn scatter-chart
  "Displays a scatter chart in a tab with the given name.

  series-map is a map of series names to the values for the series. For example
  the following map would display two sets of points named 'alpha' and 'beta'.

  {:alpha [1 2 3 4] :beta [10 20 30 40]}

  Options can be any of the following:
  * labels - a list of labels to give each value. The index of the label in the
  list corresponds to the index of the values in the series."
  ([name series]
   (scatter-chart name series nil))
  ([name series options]
   (-> (custom-chart name {:data {:json series
                                  :type "scatter"}})
       (process-options options))))