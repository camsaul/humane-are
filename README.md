[![GitHub Workflow Status (branch)](https://img.shields.io/github/workflow/status/camsaul/humane-are/Tests/master?style=for-the-badge)](https://github.com/camsaul/humane-are/actions/workflows/config.yml)
[![License](https://img.shields.io/badge/license-Eclipse%20Public%20License-blue.svg?style=for-the-badge)](https://raw.githubusercontent.com/camsaul/humane-are/master/LICENSE)
[![GitHub last commit](https://img.shields.io/github/last-commit/camsaul/humane-are?style=for-the-badge)](https://github.com/camsaul/humane-are/commits/)
[![Codecov](https://img.shields.io/codecov/c/github/camsaul/humane-are?style=for-the-badge)](https://codecov.io/gh/camsaul/humane-are)
[![GitHub Sponsors](https://img.shields.io/github/sponsors/camsaul?style=for-the-badge)](https://github.com/sponsors/camsaul)
[![cljdoc badge](https://img.shields.io/badge/dynamic/json?color=informational&label=cljdoc&query=results%5B%3F%28%40%5B%22artifact-id%22%5D%20%3D%3D%20%22humane-are%22%29%5D.version&url=https%3A%2F%2Fcljdoc.org%2Fapi%2Fsearch%3Fq%3Dio.github.camsaul%2Fhumane-are&style=for-the-badge)](https://cljdoc.org/d/io.github.camsaul/humane-are/CURRENT)
<!-- [![Get help on Slack](http://img.shields.io/badge/slack-clojurians%20%23toucan-4A154B?logo=slack&style=for-the-badge)](https://clojurians.slack.com/channels/toucan) -->
<!-- [![Downloads](https://versions.deps.co/camsaul/humane-are/downloads.svg)](https://versions.deps.co/camsaul/humane-are) -->
<!-- [![Dependencies Status](https://versions.deps.co/camsaul/humane-are/status.svg)](https://versions.deps.co/camsaul/humane-are) -->

[![Clojars Project](https://clojars.org/io.github.camsaul/humane-are/latest-version.svg)](https://clojars.org/io.github.camsaul/humane-are)

# Humane Are

```clj
(require 'humane-are.core)

(humane-are.core/install!)
```

`clojure.test/are` is great for writing lots of assertions quickly, but it has two big problems that prevent me from
using it everywhere:

1. Failing assertions give no indication as to which set of arguments failed if you're using anything that pretty
prints test output, such as [Humane Test Output](https://github.com/pjstadig/humane-test-output),
[CIDER](https://github.com/clojure-emacs/cider), or [eftest](https://github.com/weavejester/eftest)
2. `are` lets you shoot yourself in the foot by writing expressions that include `is` or `testing`, and wraps them in
   another `is` without complaining

Humane Are solves both of these problems.

## Meaningful Error Messages in Failing Tests

Here's a [real-world test using `are` that I wrote for
Metabase](https://github.com/metabase/metabase/blob/bc4acbd2d1984e40ab91e87f61a40878939fb560/test/metabase/util_test.clj#L255-L274):

```clj
(deftest parse-currency-test
  (are [s expected] (= expected
                       (u/parse-currency s))
    nil             nil
    ""              nil
    "   "           nil
    "$1,000"        1000.0M
    "$1,000,000"    1000000.0M
    "$1,000.00"     1000.0M
    "€1.000"        1000.0M
    "€1.000,00"     1000.0M
    "€1.000.000,00" 1000000.0M
    "-£127.54"      -127.54M
    "-127,54 €"     -127.54M
    "kr-127,54"     -127.54M
    "€ 127,54-"     -127.54M
    "¥200"          200.0M
    "¥200."         200.0M
    "$.05"          0.05M
    "0.05"          0.05M))
```

What happens if there's a test failure? Here's the output with standard `are` with [Humane Test
Output](https://github.com/pjstadig/humane-test-output):

```
Fail in parse-currency-test

expected: 1000.0M

  actual: 1000.0
    diff: - 1000.0M
          + 1000.0
```

There's no easy way to tell *which* specific assertion caused the test to fail.

*Note: this isn't a problem if you're using normal "inhumane" test output, since you'd get something like*

```
FAIL in (parse-currency-test)
expected: (= 1000.0M (u/parse-currency "$1,000.00"))
  actual: (not (= 1000.0M 1000.0))
```

*If that describes you, you can skip to the next section.*

Let's try installing Humane Are, and running the test again:

```clj
(require 'humane-are.core)

(humane-are.core/install!)
```

```
Fail in parse-currency-test
(= 1000.0M (u/parse-currency "$1,000.00"))

expected: 1000.0M

  actual: 1000.0
    diff: - 1000.0M
          + 1000.0
```

Humane Are adds `testing` context so you know which specific arguments caused the test to fail.

## Anti-Foot-Shooting Protection

Here's [another real-world
example](https://github.com/metabase/metabase/blob/bc4acbd2d1984e40ab91e87f61a40878939fb560/test/metabase/util_test.clj#L339-L346)
that I discovered just now while I was in the process of writing this README. What's wrong with this test?

```clj
(deftest email->domain-test
  (are [domain email] (is (= domain
                             (u/email->domain email))
                          (format "Domain of email address '%s'" email))
    nil              nil
    "metabase.com"   "cam@metabase.com"
    "metabase.co.uk" "cam@metabase.co.uk"
    "metabase.com"   "cam.saul+1@metabase.com"))
```

Let's try changing one of the assertions, to try to make it fail. Here I've swapped out one of the domains from
`metabase.com` to `metabase.commm`:

```clj
(deftest email->domain-test
  (are [domain email] (is (= domain
                             (u/email->domain email))
                          (format "Domain of email address '%s'" email))
    nil              nil
    "metabase.com"   "cam@metabase.commm"
    "metabase.co.uk" "cam@metabase.co.uk"
    "metabase.com"   "cam.saul+1@metabase.com"))
```

```
2 non-passing tests:

Fail in email->domain-test
Domain of email address 'cam@metabase.commm'
expected: "metabase.com"

  actual: "metabase.commm"
    diff: - "metabase.com"
          + "metabase.commm"


Fail in email->domain-test

expected: (is
           (= "metabase.com" (u/email->domain "cam@metabase.commm"))
           (format "Domain of email address '%s'" "cam@metabase.commm"))

  actual: false
```

Why are we getting *two* failures instead of *one*?!

We've unwittingly written a test that does two assertions instead of the one we thought we were getting: we're testing
not just

```clj
(is (= "metabase.com" (u/email->domain "cam@metabase.commm")))
```

but

```clj
(is (is (= "metabase.com" (u/email->domain "cam@metabase.commm"))))
```

as well. `are` automatically wraps the assertions in its macroexpansion in `is`, so by including an `is` ourselves
we're actually getting `(is (is ...))`. This is generally the wrong thing to do. At best you're just doing an extra
assertion everywhere, where one has borderline meaningless output when it fails; at worst you can wind up with
tests that previously passed suddenly no longer passing in ways that are prone to make you pull your hair out.

Suppose you've defined a `is` custom assertion method. If you tweak it so it stops returning a logically truthy value
when the test passes, you can wind up with mystery test failures in places that use it:

```clj
(defmethod assert-expr 'broken=
  [message [_ expected actual]]
  `(let [expected# ~expected
         actual#   ~actual]
     (when-not (= expected# actual#)
       (do-report {:type :fail, :message ~message, :expected expected#, :actual actual#}))))

(deftest x-test
  (are [x] (is (broken= x 100))
    100))
```

```
Fail in x-test

expected: (is (broken= 100 100))

  actual: nil
```

We're accidentally testing both `(is (broken= 100 100))` and `(is (is (broken= 100 100))`, and while the former is
fine, the latter fails because the macroexpansion for `broken=` returns `nil`.

It's better just to disallow `is` or `testing` forms inside `are` to prevent you from shooting yourself in the foot.

Humane Are adds an `fdef` [spec](https://github.com/clojure/spec.alpha/) to `are` to validate the expression form
during macroexpansion; if the expression is a list starting with a symbol that would resolve to `clojure.test/is` or
`clojure.test/testing` (or `cljs.test/` for ClojureScript) it will fail spec validation, triggering an error during
macroexpansion. Here's an example of the useful errors Humane Are gives you:

```
Call to clojure.test/are did not conform to spec.
#:clojure.spec.alpha{:problems
                     [{:path [:expr],
                       :pred (clojure.core/complement humane-are.core/is-or-testing-form?),
                       :val (is (= domain (u/email->domain email)) (format "Domain of email address '%s'" email)),
                       :via [],
                       :in [1]}]}
```

## How Does it Work?

`(humane-are.core/install!)` simply swaps out the `clojure.core/are` macro with a replacement macro,
`humane-are.core/are+`, and defines a spec for `are` with `clojure.spec/fdef`. Any time Clojure macroexpands an `are`
form after installing it, it will use the new macro with extra `testing` context, and Clojure will check the args
using the spec. The replacement macro uses the same underlying namespace, `clojure.template`, that `clojure.test/are`
uses, so the behavior is otherwise exactly the same.

If you don't want to *replace* `clojure.core/are`, you can use `humane-are.core/are+` directly without installing it.

Don't be afraid to install it tho. If you change your mind or hate fun you can use `humane-are.core/uninstall!` to
uninstall Humane Are and go back to a sad world of imhumane `are`.

I've tried [living in a world of having a separate custom version of
`are`](https://github.com/metabase/metabase/blob/bc4acbd2d1984e40ab91e87f61a40878939fb560/test/metabase/test.clj#L359-L378)
for a few years now and I think having tried it both ways replacing `clojure.core/are` is absolutely the way to go.

## ClojureScript Support

`humane-are.core/are+` works with ClojureScript, including both the extra testing context
(`cljs.test/testing` in this case) and spec-based validation, without jumping thru any hoops.

`cljs.test/are` and `humane-are/are+` are macros, which means they normally get macroexpanded in a JVM Clojure context
before ClojureScript ever sees them. This means you can only `install!` Humane Are in a Clojure context. To `install!`
Humane Are so it's used when compiling macros for ClojureScript, you can create a `.cljc` file like this:

```clj
(ns some-cljc-namespace
  (:require [humane-are.core]))

#?(:clj
   (humane-are.core/install!))
```

As a convenience this library provides the namespace `humane-are.install` which does exactly the same thing. Simply
requiring this namespace in a `.cljs` or `.cljc` file will install Humane Are for you.

## License

Code and documentation copyright © 2022 [Cam Saul](https://camsaul.com).

Distributed under the [Eclipse Public License](https://raw.githubusercontent.com/camsaul/humane-are/master/LICENSE),
same as Clojure.
