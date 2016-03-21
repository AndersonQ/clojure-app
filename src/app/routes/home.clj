(ns app.routes.home
  (:require [compojure.core :refer :all]
            [clojure.core :refer :all]
            [app.views.layout :as layout]
            [ring.util.response :refer [response]]
            [clojure.java.jdbc :as jdbc]
            [java-jdbc.ddl :as ddl]))

(def db-spec {:classname "org.postgresql.Driver"
              :subprotocol "postgresql"
              :subname "//192.168.99.100:32772/clojure_app"
              :user "master"
              :password "secret"})

(defn loaddb []
  (try
    (jdbc/insert! db-spec :appuser {:email "a@a.com"
                                    :password "secret"
                                    :name "A"
                                    :admin true})
    (catch Exception e (str "loaddb caught exception: " (.getMessage e)))))


(defn create-db []
  (try
    (jdbc/db-do-commands db-spec
                         (ddl/create-table :appuser
                                           [:email "varchar(255)" "PRIMARY KEY"]
                                           [:name "varchar(255)"]
                                           [:password "varchar(255)"]
                                           [:admin "boolean"]))
    (catch Exception e (str "create-db caught exception: " (.getNextException e)))))

(defn drop-appuser []
  (try
    (jdbc/db-do-commands db-spec (ddl/drop-table :appuser))
    (catch Exception e (str "drop-appuser caught exception: " (.getMessage e)))))


(defn reset []
    (drop-appuser)
    (create-db)
    (loaddb))

(defn home []
  (layout/common [:h1 "Hello World!"]))

(defn admin [id]
  (if id
    (response {:id id :foo "foo" :bar "bar"})
    (response {:foo "foo" :bar "bar"})))

(defn post [r]
  (response (:body r)))

(defroutes home-routes
  (GET "/" [] (home))
  (POST "/" request (post request))
  (GET "/admin" [] (admin nil))
  (GET "/reset" [] (reset))
  (GET "/admin/:id" [id] (admin id)))
