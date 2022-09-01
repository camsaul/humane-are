(ns humane-are.core-test
  (:require [clojure.test :as t]
            [humane-are.core :as humane-are]))

(t/deftest example-test
  (t/is (= 1000
           (humane-are/x)))
  (t/is (= 1 2)))
