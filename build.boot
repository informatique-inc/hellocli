;; Copyright © 2017 Informatique, Inc.
;;
;; This software is licensed under the terms of the
;; Apache License, Version 2.0 which can be found in
;; the file LICENSE at the root of this distribution.

(def project 'informatique-inc/hellocli)
(def version "0.2.0")
(def description "Simple Clojure CLI example")
(def project-url "https://github.com/informatique-inc/hellocli")
(def main 'hellocli.cli)

(set-env!
  :resource-paths #{"src"}
  :source-paths   #{"src" "test"}
  :dependencies   '[[org.clojure/clojure         "1.8.0"   :scope "provided"]
                    [environ                     "1.1.0"]
                    [org.clojure/tools.cli       "0.3.5"]
                    ;; testing/development
                    [adzerk/boot-test            "1.1.2"          :scope "test"]
                    [adzerk/bootlaces            "0.1.13"         :scope "test"]
                    ])

(require
  '[clojure.java.shell :as cjs]
  '[adzerk.boot-test :refer [test]]
  '[adzerk.bootlaces :refer :all])

(bootlaces! version)

(task-options!
  pom {:project     project
       :version     version
       :description description
       :url         project-url
       :scm         {:url project-url}
       :license     {"Apache-2.0" "http://opensource.org/licenses/Apache-2.0"}}
  aot {:namespace   #{main}}
  jar {:main        main}
  test {:namespaces #{'testing.hellocli.cli}})

(deftask uberjar
  "Build the project locally as an uberjar"
  [d dir PATH #{str} "the set of directories to write to (target)."]
  (let [dir (if (seq dir) dir #{"target"})]
    (comp
      (sift :include #{#"~$"} :invert true) ;; don't include emacs backups
      (aot)
      (uber)
      (jar :file (str (name project) ".jar"))
      (target :dir dir))))

(deftask cli-test
  "Run the command line tests."
  []
  (comp
    (uberjar)
    (with-post-wrap [_]
      (println "\nCLI Testing\n")
      (let [jar "target/hellocli.jar"
            cmd ["java" "-jar" jar "-v" "-v" "--foo" "bar" "aaa" "bbb" "ccc"]
            expected "version: 0.2.0\nverbosity level: 2\nfoo: bar\nargs: [aaa bbb ccc]\n"
            {:keys [exit out err]} (apply cjs/sh cmd)]
        (if (or (not (zero? exit)) (not= out expected))
          (do
          (println "command line test failed for:" cmd)
          (if (not (zero? exit))
            (println (str "non-zero exit status: " exit "\nstdout:\n" out))
            (println (str "--- Expected ---\n" expected "\n--- Actual ---\n" out)))
          (println (str "stderr:\n" err))
          (println "1 error.")
          (throw (Exception. "command line test failed")))
          (println "0 errors."))))))

(deftask all-tests
  "Run the Clojure and command line tests."
  []
  (comp
    (test)
    (cli-test)))

(deftask run
  "Run the project. boot run -a \"arg1 arg2 arg3...\""
  [a args ARG [str] "the arguments for the application."]
  (require [main :as 'app])
  (let [argv (if (pos? (count args))
               (clojure.string/split (first args) #" ")
               '())]
    (apply (resolve 'app/-main) argv)
    identity))
