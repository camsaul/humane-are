((nil . ((indent-tabs-mode . nil)       ; always use spaces for tabs
         (require-final-newline . t)))  ; add final newline on save
 (clojure-mode . ((cljr-favor-prefix-notation . nil)
                  (fill-column . 120)
                  (clojure-docstring-fill-column . 120)
                  (cider-default-cljs-repl . shadow-select)
                  (cider-shadow-default-options . "node-repl"))))
