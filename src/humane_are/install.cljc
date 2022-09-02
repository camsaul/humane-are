(ns humane-are.install
  "This is a convenience for installing Humane Are in ClojureScript -- see README for more info."
  (:require [humane-are.core]))

#?(:clj
   (humane-are.core/install!))
