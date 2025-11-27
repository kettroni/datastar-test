(ns ui
  (:require [clojure.string :as s]
            [hiccup2.core :as h]))

(def datastar-cdn
  "https://cdn.jsdelivr.net/gh/starfederation/datastar@1.0.0-RC.6/bundles/datastar.js")

(defn ->kw [styles]
  (->> styles
       (map name)
       (s/join ".")
       keyword))

(def btn-grp-style
  [:button
   :bg-p-1 ; Use the new bg-p-1 class
   :text-p-1 ; Use the new text-p-1 class
   :font-bold
   :py-2
   :px-4
   :rounded
   :cursor-pointer
   :hover:bg-blue-300])

(defn hello-buttons []
  [:div.flex.flex-row.bg-gray-100.shadow-md.rounded.p-4.mx-auto.w-fit.space-x-4
   [(->kw btn-grp-style)
    {:data-on:click "@get('/say-hello')"}
    "Press to hello!"]
   [(->kw btn-grp-style)
    {:data-on:click "@get('/chunked-hello')"}
    "Press to chuncked hello!"]
   [(->kw btn-grp-style)
    {:data-on:click "@get('/subscribe')"}
    "Press to subscribe hello channel!"]])

(def hello-page
  (->> [:html
        {:data-datastar-root true}
        [:head
         [:script
          {:type "module"
           :src  ui/datastar-cdn}]
         [:link
          {:rel  "stylesheet"
           :href "/src/colors.css"}]
         [:link
          {:rel  "stylesheet"
           :href "/src/css/base.css"}]]
        [:body
         (ui/hello-buttons)
         [:p#hello-field.mt-4.text-xl]]]
       h/html
       str))
