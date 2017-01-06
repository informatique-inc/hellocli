;; Copyright © 2017 Informatique, Inc.
;;
;; This software is licensed under the terms of the
;; Apache License, Version 2.0 which can be found in
;; the file LICENSE at the root of this distribution.

(ns hellocli.cli
  "hellocli command line interface."
  (:require [clojure.tools.cli :refer [parse-opts]]
            [clojure.string :as string]
            [clojure.pprint :as pp :refer [pprint]]
            [environ.core :refer [env]])
  (:gen-class)) ;; required for uberjar

(def #^{:added "0.2.0"}
  cli-options
  "Command line options"
  [["-h" "--help" "Print usage"]
   ["-V" "--version" "Print version"]
   ["-v" "--verbose" "Increase verbosity"
    :default 0
    :assoc-fn (fn [m k _] (update-in m [k] inc))]
   ["-e" "--print-env" "Print environment"]
   ["-f" "--foo FOO" "specify the foo"]
   ])

(defn usage
  "Print command line help."
  [options-summary]
  (->> [""
        "hellocli: boot hello cli script"
        ""
        "Usage: hellocli [options] [args..]"
        ""
        "Options:"
        options-summary]
    (string/join \newline)))

(defn exit
  "Exit with given status code (and optional messages)."
  [status & msgs]
  ;; (log/trace "EXIT(" status ")")
  (when msgs
    ;; (if (zero? status)
    (println (string/join \newline msgs))
    ;; (log/error \newline (string/join \newline msgs)))
    )
  (flush) ;; ensure all pending output has been flushed
  (shutdown-agents)
  (System/exit status)
  true)

(defn -main
  "boot hello cli script"
  {:version "0.2.0"}
  [& args]
  (let [hellocli-version (:version (meta #'-main))
        {:keys [options arguments errors summary]}
        (parse-opts args cli-options)
        {:keys [help version verbose foo print-env]} options
        verbose? (pos? (or verbose 0))
        exit?
        (cond
          help
          (exit 0 (usage summary))
          errors
          (exit 1 (string/join \newline errors) (usage summary))
          version
          (exit 0 hellocli-version)
          print-env
          (exit 0 (with-out-str (pprint (into (sorted-map) env))))
          :default
          false)]
    (when (not exit?)
      (when verbose?
        (when (> verbose 1)
          (println "version:" hellocli-version))
        (println "verbosity level:" verbose)
        (println "foo:" foo)
        (println "args:" arguments))
      (if (> verbose 1) ;; throw full exception with stack trace when -v -v
        (exit 0) ;; do work here
        (try
          (exit 0) ;; do work here
          (catch Throwable e
            (exit 1 "ERROR caught exception:" (.getMessage e))))))
    (exit 0)))
