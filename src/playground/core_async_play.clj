(ns playground.core-async-play
  (:require [clojure.core.async :as async :refer [<!! >!!]]))

;; NOTE: By convention function ending with:
;;       '<' - we expect the last argument to be the input channel
;;       '>' - we expect the last argument to be the output channel

(comment
  (let [c (async/chan 10)]
    (>!! c 42)
    (<!! (pipeline< [4 inc
                     1 inc
                     2 dec
                     3 str]
                    c))))

(defn to-proc< [in]
  "This fn uses 'pipe' to put the processing
  on its own go routine"
  (let [out (async/chan 1)]
    (async/pipe in out)
    out))

(defn pipeline< [desc c]
  (let [p (partition 2 desc)]
    (reduce
      (fn [prev-c [n f]]
        (-> (for [_ (range n)]
              (-> (async/map< f prev-c)
                  to-proc<))
            async/merge))
      c
      p)))


