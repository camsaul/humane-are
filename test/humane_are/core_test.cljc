(ns humane-are.core-test
  (:require [#?(:clj clojure.test :cljs cljs.test) :as t]
            [humane-are.core :as humane-are]
            [humane-are.install]))

#?(:clj
   (t/deftest macroexpansion-error-test
     ;; make sure we're in the correct namespace, the Cognitect runner runs all the tests out of `user`.
     (binding [*ns* (the-ns 'humane-are.core-test)]
       (t/is (thrown?
              clojure.lang.Compiler$CompilerException
              ;; spec should be smart enough to resolve the symbol `t/is` based on the ns-aliases for *ns*
              (macroexpand '(humane-are.core/are+ [x y] (t/is (= x y)) 1 2)))))))

;;; The above test actually does fail for ClojureScript, but it fails during compilation (not during test runs) so
;;; unfortunately we can't test it here... uncomment this if you want to verify locally.
(comment
  (macroexpand '(humane-are.core/are+ [x y] (t/is (= x y)) 1 2)))

;;; This is not a GREAT test since if the `are+` macro just expands to nothing it wouldn't fail... but you can at least
;;; check the number of tests ran.
(t/deftest are-test
  (humane-are/are+ [x y] (= x y)
    1 1))

;;; Uncomment this to verify that it works.
(comment
  (humane-are/are+ [x y] (= x y)
    1 (inc 2)
    1 (inc 3)))

(t/deftest test-4
  (t/are [expected s] (= expected s)
    1 (inc 2)))

(t/deftest test-5
  (humane-are/are+ [expected s] (= expected s)
                   1 (inc 2)))
