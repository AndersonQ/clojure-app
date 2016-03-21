(ns app.routes.home
  (:require [compojure.core :refer :all]
            [clojure.core :refer :all]
            [app.views.layout :as layout]
            [ring.util.response :refer [response]]
            [clojure.java.jdbc :as jdbc]
            [java-jdbc.ddl :as ddl]
            [clojurewerkz.scrypt.core :as sc]
            [java-jdbc.sql :as sql]))

(def db-spec {:classname "org.postgresql.Driver"
              :subprotocol "postgresql"
              :subname "//192.168.99.100:32772/clojure_app"
              :user "master"
              :password "secret"})

(defn verify [email password]
  (println "verify")
  (let [q (jdbc/query db-spec
                      (sql/select "password" :appuser (sql/where {:email email})))]
    (let [h ((first q) :password)]
      (println password)
      (println h)
      (sc/verify password h))))

(defn save-user [email password name admin]
  (try
    (jdbc/insert! db-spec :appuser {:email email
                                    :password (sc/encrypt password 16384 8 1)
                                    :name name
                                    :admin admin})
    (catch Exception e (str "loaddb caught exception: " (.getMessage e)))))

(defn loaddb []
  (try
    (save-user "a@a.com" "secret" "A" true)
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

(defn login [body]
  (let [email ((body :body) "email")
        password ((body :body) "password")]
    (if (verify email password)
      (response {:r "ok"})
      (response {:r "nop"}))
    ))

(defn home []
  (layout/common [:h1 "Hello World!"]))

(defn admin [id]
  (if id
    (response {:id id :foo "foo" :bar "bar"})
    (response {:foo "foo" :bar "bar"})))

(defn post [r]
  (response (:body r)))

(defn lala []
  "lala")

(defroutes home-routes
  (GET "/admin" [] (admin nil))
  (GET "/reset" [] (reset))
  (GET "/admin/:id" [id] (admin id))
  (GET "/user" request (lala))
  (GET "/" [] (home))
  (POST "/login" request (login request))
  (POST "/" request (post request))
  )
