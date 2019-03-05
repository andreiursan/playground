(ns playground.testing
  (:require [clojure.test :refer :all]
            [com.stuartsierra.component :as component]))


;;;; Data Model

;; Contract --
(defprotocol IDB
  (-add-user [this un pw])
  (-valid-user? [this un pw]))

(defn add-user [this un pw]
  {:pre [(string? un)
         (string? pw)
         (satisfies? IDB this)]}
  (-add-user this un pw))

(defn valid-user? [this un pw]
  {:pre [(string? un)
         (string? pw)
         (satisfies? IDB this)]}
  (-valid-user? this un pw))
;; --

(defrecord MockUserDB [state]
  IDB
  (-add-user [this un pw]
    (swap! state assoc un pw))
  (-valid-user? [this un pw]
    (= (get @state un) pw)))

(defn mock-user-db []
  (->MockUserDB (atom {})))

;;;

(defprotocol IRunOp
  (-run-op [this op args]))

(defn run-op [system op args]
  {:pre [(satisfies? IRunOp system)]}
  (-run-op system op args))

(defrecord RunOp [db]
  IRunOp
  (-run-op [this op args]
    (case op
      :add-user (apply add-user db args)
      :login (do (assert (apply valid-user? db args))
                 true))))

(defn run-op-components []
  (component/using (->RunOp nil)
                   [:db]))

(defn make-system []
  {:run-op (run-op-components)
   :db (mock-user-db)})

(deftest user-can-login
  (let [system (component/start-system (make-system))]
    (run-op (:run-op system) :add-user ["Joe" "Secret"])
    (assert (run-op (:run-op system) :login ["Joe" "Secret"]))))
