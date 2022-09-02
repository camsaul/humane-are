(ns humane-are.core
  (:require [clojure.spec.alpha :as s]
            [clojure.template :as clojure.template]
            [clojure.test :as t]
            [net.cgrand.macrovich :as macros]))

(defmacro are+ [bindings assertion & args]
  {:pre [(every? symbol? bindings)]}
  (let [tuples (partition-all (count bindings) args)]
    `(do
       ~@(for [tuple tuples]
           (let [spliced (clojure.template/apply-template bindings assertion tuple)]
             `(~(macros/case
                  :clj  'clojure.test/testing
                  :cljs 'cljs.test/testing) '~spliced
               (~(macros/case
                   :clj  'clojure.test/is
                   :cljs 'cljs.test/is) ~spliced)))))))

(defn- referred-is-or-testing? [symb]
  (and ('#{is testing} symb)
       (when-let [varr (get (ns-refers *ns*) symb)]
         (#{"clojure.test" "cljs.test"} (namespace (symbol varr))))))

(defn- aliased-is-or-testing? [symb]
  (when-let [namespace-symb (some-> (namespace symb) symbol)]
    (when-let [aliased-namespace (get (ns-aliases *ns*) namespace-symb)]
      ('#{clojure.test cljs.test} (ns-name aliased-namespace)))))

(defn is-or-testing-form? [form]
  (and (seq? form)
       (symbol? (first form))
       (let [symb (first form)]
         (or ('#{clojure.test/is clojure.test/testing cljs.test/is cljs.test/testing} symb)
             (referred-is-or-testing? symb)
             (aliased-is-or-testing? symb)))))

(s/def ::are-args
  (s/cat :bindings vector?
         :expr     (complement is-or-testing-form?)
         :args     (s/+ any?)))

(s/fdef are+
  :args ::are-args
  :ret  any?)

(macros/case :cljs (require 'cljs.test))

(defonce ^:private original-are (var-get (macros/case :clj #'clojure.test/are :cljs #_{:clj-kondo/ignore [:unresolved-namespace]} #'cljs.test/are)))

(defn install!
  "Replace [[clojure.test/are]] with a version that includes the actual form being tested as [[clojure.test/testing]]
  context. It's a lot easier to debug that way. It expands things exactly the same way
  tho (using [[clojure.template]]) so there is zero difference for anything but the test output."
  []
  (doto (macros/case :clj #'clojure.test/are :cljs #'cljs.test/are)
    (alter-var-root (constantly @#'are+))
    (alter-meta! merge (select-keys (meta #'are+) [:ns :name :file :column :line])))

  (println "INSTALLED OVER" (macros/case :clj #'clojure.test/are :cljs #'cljs.test/are))

  (macros/case
    :clj
    (s/fdef clojure.test/are
      :args ::are-args
      :ret  any?)

    :cljs
    (s/fdef cljs.test/are
      :args ::are-args
      :ret  any?)))

(defn uninstall! []
  (doto (macros/case :clj #'clojure.test/are :cljs #'cljs.test/are)
    (alter-var-root (constantly @#'original-are))
    (alter-meta! merge (select-keys (meta #'original-are) [:ns :name :file :column :line])))

  (macros/case
    :clj
    (s/fdef clojure.test/are
      :args any?
      :ret  any?)

    :cljs
    (s/fdef cljs.test/are
      :args any?
      :ret  any?)))

(comment
  (macros/case :cljs (install!)))

(println "<INSTALL>")
(install!)
