;; Copyright © 2017 Informatique, Inc.
;;
;; This software is licensed under the terms of the
;; Apache License, Version 2.0 which can be found in
;; the file LICENSE at the root of this distribution.

(ns testing.hellocli.cli
  (:require [clojure.test :refer :all]
            [hellocli.cli :refer :all]))

(deftest testing-hellocli-cli
  (testing "testing-hellocli-cli"
    (is (= (mapv second cli-options)
          ["--help" "--version" "--verbose" "--print-env" "--foo FOO"]))
    ))
