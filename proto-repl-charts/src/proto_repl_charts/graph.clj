(ns proto-repl-charts.graph
  "TODO
   graph display data is a map of nodes and edges
   nodes are maps with :id and :label
   edges are maps of :from and :to "
  (:require [loom.graph :as lg]
            [clojure.string :as str]))

;; TODO add unit tests

(defn- error
  "Throws an exception containing a message joined from the msg-parts."
  [& msg-parts]
  (throw (Exception. (str/join " " msg-parts))))

(def ^:private expected-msg
  (str "Expecting loom graph or a map containing :nodes and :edges. Nodes can be "
       "strings or maps with any of the fields here: "
       "http://visjs.org/docs/network/nodes.html Edges can be 2 item sequences "
       "or maps containing any of the fields described here: "
       "http://visjs.org/docs/network/edges.html."))

(defn nodes->display-data
  "TODO"
  [nodes]
  (when-not (or (set? nodes) (sequential? nodes))
    (error "Expected sequence of nodes." expected-msg))

  (if (map? (first nodes))
    nodes
    (mapv #(hash-map :id % :label %) nodes)))

(defn edges->display-data
  "TODO"
  [edges]
  (when-not (or (set? edges) (sequential? edges))
    (error "Expected sequence of edges." expected-msg))
  (cond
    (map? (first edges))
    edges

    (sequential? (first edges))
    (mapv #(hash-map :from (first %) :to (second %)) edges)

    :else
    (error "Unexpected type for edges." (type (first edges)) expected-msg)))


(defn- loom-graph->display-graph
  "TODO"
  [g]
  (let [nodes (nodes->display-data (lg/nodes g))
        edges (if (lg/directed? g)
                ;; TODO use arrows for a directed graph
                (edges->display-data (lg/edges g))
                ;; Non-directed graph. We don't want duplicate edges between nodes
                ;; This will return edges twice for an undirected graph
                (->> g lg/edges (mapv sort) (into #{}) edges->display-data))]
   {:nodes nodes
    :edges edges}))

(defn- map-graph->display-graph
  [mg]
  (when-not (contains? mg :nodes)
    (error "Missing key :nodes." expected-msg))
  (when-not (contains? mg :edges)
    (error "Missing key :edges." expected-msg))
  (let [{:keys [nodes edges]} mg]
    {:nodes (nodes->display-data nodes)
     :edges (edges->display-data edges)}))

;; TODO convert to multimethod. Allows extension
(defn convert-graph-data-for-display
  "TODO"
  [graph-data options]
  (cond
    (lg/graph? graph-data)
    (assoc (loom-graph->display-graph graph-data) :options options)

    (map? graph-data)
    (assoc (map-graph->display-graph graph-data) :options options)

    :else
    (error "Unexpected graph data for display of type" (type graph-data) expected-msg)))