(defproject business-model-canvas "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-2127"]
                 [om "0.1.0-SNAPSHOT"]
                 [sablono "0.1.1"]
                 [org.clojure/core.async "0.1.267.0-0d7780-alpha"]]

  :plugins [[lein-cljsbuild "1.0.1"]]

  :cljsbuild
  { :builds [{:id "dev"
              :source-paths ["src"]
              :compiler
               { :output-to "main.js"
                 :output-dir "out"
                 :optimizations :none
                 :source-map true
                 :externs ["react/externs/react.js"]}}]}
  )
