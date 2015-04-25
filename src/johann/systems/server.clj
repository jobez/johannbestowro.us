(ns johann.systems.server
  (:require [johann.utils.config :refer [config]]
            [johann.utils.maker :refer [make]]
            [johann.utils.system :refer [new-system]]
            [johann.components.evernote :refer ( new-evernote-cache)]
            [bidi.bidi :refer (RouteProvider)]
            [modular.bidi]
            [modular.http-kit]
            [modular.ring :refer (WebRequestMiddleware)]
            [plumbing.core :refer :all]
            [schema.core :as s]
            [tangrammer.component.co-dependency :as co-dependency]))

(defn components [config]
  {:evernote-cache {:cmp (make new-evernote-cache config
                           {:notestore-url [:evernote :notestore-url]} ""
                           {:access-token [:evernote :access-token]} ""
                           {:notebook-uid [:evernote :notebook-uid]} ""
                           :context "/")}
   :public-resources
   {:cmp (make modular.bidi/new-web-resources config
               :uri-context "/public"
               :resource-prefix "public")}
   :webrouter
   {:cmp (modular.bidi/new-router)
    :using [:evernote-cache (s/protocol RouteProvider)
            :public-resources (s/protocol RouteProvider)]}
   :webhead
   {:cmp (modular.ring/new-web-request-handler-head)
    :using {:request-handler :webrouter
            (s/protocol WebRequestMiddleware) (s/protocol WebRequestMiddleware)}}
   :webserver
   {:cmp (make modular.http-kit/new-webserver config
               {:port [:web :port]} 3000)
    :using [:webhead]}})

(defn new-production-system
  []
  (new-system (components (config))))
