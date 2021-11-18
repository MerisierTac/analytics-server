# Introduction

Ce projet gitlab.inria.fr est un des composants de la solution plus
globale [TousAntiCovid](https://gitlab.inria.fr/stopcovid19/accueil/-/blob/master/README.md).

Ce composant gère la fonctionnalité appelée "analytics", c'est une fonctionnalité qui permet de recevoir de la part des
applications mobiles leurs statistiques d'usage (quel model de téléphone, quelle version d'OS, à quelle date a été
effectué le dernier status, la liste des erreurs rencontrées...)

# Architecture générale

![](analytics.png)

Dans une première itération de cette application, la base de données mongo n'est pas utilisée.

L'application repose sur une application springboot en frontal. Celle-ci traite les requêtes provenant du terminal de
l'utilisateur final. Les principaux traitements effectués par cette application sont :

- validation du token JWT fourni par l'application robert, à noter qu'un token ne peut être utilisé qu'une unique fois
  pour soumettre des analytics. Une base de donnée mongodb est utilisée pour conserver la liste des tokens déjà
  utilisée.
- validation du contenu de la requête, on vérifie la structure de la requête vis à vis du contrat d'interface
- envoi des analytics dans un topic kafka.

Par la suite, le topic kafka est consommé par logstash qui repousse les analytics dans un elasticsearch. Les analytics
sont ensuite analysés grâce à un kibana.

2 principales fonctionnalités sont implémentées : 
- la création d'analytics
- la suppression sur demande de l'utilisateur de l'application mobile des analytiques afin de respecter la RGPD.

note : le fichier source du diagramme est fourni [ici](analytics.drawio)

## Contrat d'interface

Le contrat d'interface du service rest exposé est disponible au format
openapi [openapi-analytics.yaml](src/main/resources/openapi-analytics-v1.yml)

## Configuration Kafka SSL
Ce chapitre explique comment configurer une connexion sécurisée SSL entre le producteur de message (notre application) et le server kafka.

Il faut générer un certificat pour le server Kafka (avec CN = hostname). Ce certificat est stocké dans un truststore (au format jks) et protégé par un mot de passe.

Afin de générer un magasin au fomat `jks`, vous pouvez suivre les instructions suivantes:

```sh
openssl pkcs12 -inkey node-1.pem -in node-1.pem -name node-1 -export -out node-1.p12
keytool -importkeystore -deststorepass changeme \
    -destkeystore node-1-keystore.jks -srckeystore node-1.p12 -srcstoretype PKCS12
```
Le trustore doit être installé sur le system de fichier des serveurs hébergeant l'application analytics.

La configuration SSL est sauvegardée dans Vault et injectée à l'application via spring-cloud-vault-config-consul in apps.
Voici les propriétés devant être utilisées :
- `spring.kafka.bootstrap-servers` doit être initialisé avec la liste des hostname:port des serveurs kafka (il faut indiquer le même nom que le `CN` du certificat)
- `spring.kafka.properties.security.protocol` égal à `ssl` pour activer le SSL
- `spring.kafka.ssl.trust-store-location` suivant le nommage suivant `file:///path/to/kafka.client.truststore.jks` (ne pas oublier le préfixe `file://` pour une URL valide)
- `spring.kafka.ssl.trust-store-password`

# Environment de développement

Si ce n'est déjà fait, se logguer, sur la registry docker de l'inria.

    docker login registry.gitlab.inria.fr

Pour fonctionner, cette application à besoin à minima :

- De mongodb
- De kafka

La stack complète d'outil tel que décrit dans le schéma d'architecture générale afin de manipuler les analytics qui
auront été stockés dans l'elasticsearch

Ces différents services sont disponibles dans un environnement docker lançable par :

    docker-compose -f docker-compose.yaml up -d

Les services dockerisés ne sont utilisés que pour pouvoir lancer l'application par elle-même. Les tests unitaires
utilisent des services embarqués (embedded mongodb et kafka).

## Appel rest

Des appels rest peuvent être effectué sur l'application en utilisant la collection postman
fournie [postmail](src/main/doc/TAC-analytics.postman_collection.json)

## Spécificité de la configuration sous Windows 10

Dans le fichier etc/hosts (c:\windows\system32\drivers\etc\hosts) ajouter l'entrée suivante :

    127.0.0.1 docker-desktop

Dans l'application docker desktop settings, placer la quantité de mémoire à 10G.

## Génération - Vérification d'un token

Pour cela il est possible d'utiliser https://jwt.io

Il faut sélectionner l'algorithm RS-256

La clef publique est récupérable au niveau de la clef `robert_jwt_analyticspublickey` dans le fichier application-dev.yml 
en le formattant de la façon suivante avant de la copier dans le champ prévu à cet effet sous jwt.io

      -----BEGIN PUBLIC KEY-----
      <1ERE LIGNE DE LA CLEF PUBLIQUE TRONQUEE à 65 caractères>
      <...>
      <...>
      -----END PUBLIC KEY-----

La clef privée est récupérable en commentaire sous la clef publique.
Il faut la formatter de la façon suivante avant de la copier dans le champ prévu à cet effet sous jwt.io

    -----BEGIN RSA PRIVATE KEY-----
      <1ERE LIGNE DE LA CLEF PRIVEE TRONQUEE à 65 caractères>
      <...>
      <...>
    -----END RSA PRIVATE KEY----

Il vous est alors possible de :

* modifier le payload pour générer un nouveau token
* lire le contenu d'un token

## Messages envoyés dans Kafka

Les messages sont envoyés dans les topics suivant :

* analytics.creation-topic
* analytics.deletion-topic

Format des objets :

    data class AnalyticsCreation(
      val installationUuid: String,
      val infos: Map<String, Any>,
      val events: List<AnalyticsEvent>,
      val errors: List<AnalyticsEvent>,
      val creationDate: OffsetDateTime = OffsetDateTime.now()
    )

    data class AnalyticsDeletion(
      val installationUuid: String,
      val deletionTimestamp: Instant
    )

Structure des événements lié à l'analytic :

    data class AnalyticsEvent(
      val name: String,
      val timestamp: OffsetDateTime,
      val desc: String?
    )

## Consommation du topic par Logstash

Il existe 4 pipelines logstash consommant les topics kafka :

* [analytics](https://gitlab.inria.fr/stemcovid19/infrastructure/functional-zone/services/analytics/elk-ansible/-/blob/master/roles/logstash/files/mobapp.conf)
* [delete](https://gitlab.inria.fr/stemcovid19/infrastructure/functional-zone/services/analytics/elk-ansible/-/blob/master/roles/logstash/files/deletekafka.conf)
* [event](https://gitlab.inria.fr/stemcovid19/infrastructure/functional-zone/services/analytics/elk-ansible/-/blob/master/roles/logstash/files/event.conf)
* [error](https://gitlab.inria.fr/stemcovid19/infrastructure/functional-zone/services/analytics/elk-ansible/-/blob/master/roles/logstash/files/mobapp.conf)

Definis
ici : [pipelines.yml](https://gitlab.inria.fr/stemcovid19/infrastructure/functional-zone/services/analytics/elk-ansible/-/blob/master/roles/logstash/files/pipelines.yml)

Chaque pipeline définit les étapes suivantes :

* **input**: branchement au topic source pour la lecture des messages (kafka de l'instance analytics)

* **filter**: applique des transformations sur les entités, notamment un filtrage selon le type d'analytic :
  * statistiques application mobile => type 0
  * statistiques sanitaire => type 1

* **output** : branchement sur l'indexe cible du cluster ElasticSearch définit

Pour les analytics issues du topic de creation, les 3 pipelines analytics, event et error sont déclenchés :

* on consomme le message de kafka
* on filtre sur le type 1 ou 0
  * si type 1 (donnée sanitaire), on rejète le message si plus vieux de 7776000s (3 mois)
    et on envoie dans l'indexe **health-mobapp-%{+YYYY.MM.dd}**
  * sinon on envoie dans l'indexe **mobapp-%{+YYYY.MM.dd}**
  * dans les deux cas, on supprime la date de création du message pour la mapper sur le champ @timestamp géré par ES.

Si des données existent dans "events" ou "errors", les mêmes transformations s'appliquent et sont respectivements
envoyés vers **event-mobapp-%{+YYYY.MM.dd}** et **error-mobapp-%{+YYYY.MM.dd}**

Pour les analytics issues du topic de suppression :

* on consomme le message dans kafka
* on supprime tous les documents dans tous les indexes matchant l'installationUuid

    {
      "query": {
        "term": {
          "installationUuid":"%{[installationUuid]}"
        } 
      }
    }

* mobapp-*/_delete_by_query
* health-mobapp-*/_delete_by_query
* event-mobapp-*/_delete_by_query
* error-mobapp-*/_delete_by_query

## Statistiques remontées

### Applicatifs (analytics)

| Nom                                                   | Code | Catégorie | Type   | Exemple                                | Pour PROD? | Commentaire                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              |
| --------------------------------------------------------- | ---- | -------- | ------ | ------------------------------------ | ---------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| installationUuid \= champ spécial à part                  |      | spécial  | string | 123e4567-e89b-12d3-a456-426614174000 | yes        | ID unique par installation (reste identique à chaque appel pour une même "installation" de l'app), pour retrouver l'ensemble des infos/events/errors de cette "installation"                                                                                                                                                                                                                                                                                                                                                                                                                                         |
| type                                                      |      | infos    | number | 0                                    | yes        | toujours à 0 pour le différencier du type 1 (tab dans cette spreadsheet, le "HEALTH" type)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           |
| os                                                        |      | infos    | string | iOS ou Android                       | yes        | le système d'exploitation (enum)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     |
| deviceModel                                               |      | infos    | string | iPhone9,4                            | yes        | le modèle de smartphone                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              |
| osVersion                                                 |      | infos    | string | 13.4.1                               | yes        | la version du système d'exploitation                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 |
| appVersion                                                |      | infos    | string | 1.3.5                                | yes        | la version de l'app TAC installée                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    |
| appBuild                                                  |      | infos    | number | 92                                   | yes        | le numéro de build de l'app TAC installée                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            |
| receivedHelloMessagesCount                                |      | infos    | number | 3800032                              | yes        | le nombre total d'hello messages reçu sur les 14 derniers jours                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      |
| placesCount                                               |      | infos    | number |                                      | yes        | \[Cléa\] nombre de lieux total flashés par l'utilisateur                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             |
| formsCount                                                |      | infos    | number |                                      | yes        | nombre d'attestations dans l'app                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     |
| certificatesCount                                         |      | infos    | number |                                      | yes        | nombre de certificats de test ou vaccination dans le wallet                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| statusSuccessCount                                        |      | infos    | number | 24                                   | yes        | le nombre de status réalisées depuis le premier register                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             |
| userHasAZipcode                                           |      | infos    | bool   |                                      | yes        | l'utilisateur a ajouté ou pas un code postal dans l'app                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              |
| appReceivedBackgroundPush                                 | e1   | events   |        |                                      | yes        | iOS only: savoir si on a recu un push du serveur de push                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             |
| userReceivedAlertPush                                     | e2   | events   |        |                                      | yes        | l'app envoie à l'utilisateur un push suite à une alerte (RiskLevel > 0)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              |
| userEnteredInForeground                                   | e3   | events   |        |                                      | yes        | l'app est passée en foreground                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       |
| userTappedOnSharedApp                                     | e4   | events   |        |                                      | yes        | l'utilisateur a appuyé sur "Je partage StopCovid"                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    |
| userTappedOnHomeHealth                                    | e5   | events   |        |                                      | yes        | user appuie sur le bandeau "ma santé" avec son niveau de risque                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      |
| userTappedOnHomeIsolation                                 | e6   | events   |        |                                      | yes        | user appuie sur le module isolement                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  |
| userTappedOnHomeVaccination                               | e7   | events   |        |                                      | yes        | user appuie sur "Je me fais vacciner"                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                |
| userTappedOnHomeFigures                                   | e8   | events   |        |                                      | yes        | user appuie sur les chiffre (featured ou autres chiffres)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            |
| userTappedOnFigureDetails                                 | e9   | events   |        |                                      | yes        | user appuie sur les détails d'un chiffre                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             |
| userTappedOnHomeNews                                      | e10  | events   |        |                                      | yes        | user appuie sur dernier actu                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         |
| userTappedOnCertificate                                   | e11  | events   |        |                                      | yes        | user appuie sur attestation de sortie                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                |
| userTappedOnScanVenue                                     | e12  | events   |        |                                      | yes        | \[Cléa\] user appuie sur "scanner un lieu"                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           |
| userAddedACertificate                                     | e13  | events   |        |                                      | yes        | user a ajouté avec succes une preuve (test ou vaccination)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           |
| userAddedAPlace                                           | e14  | events   |        |                                      | yes        | \[Cléa\] user a ajouté avec succes un QR Code cléa dans l'app                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        |
| cleaProcessDuration                                       | e15  | events   |        |                                      | yes        | \[Cléa\] Si le user a un historique de QR Clea, juste avant de démarrer le nouveau process "Clea status" (fetch de multiples JSON), on lance un timer. Quand on finit, on log cet event avec cette durée en millisecondes, et on met en description (desc) si on est en foreground (juste la lettre "f") ou background ("b"), en plus de la durée en millisecondes, ex: "f 323233" ou "b 45223".<br>L'objectif est de monitorer le temps que prend tout le process de vérification “Cléa”, à savoir si l’utilisateur a été dans un lieu à risque. Et si on détecte des lenteurs, on pourra ainsi tenter d’optimiser. |
| appMadeStatus                                             | e16  | events   | NEW    |                                      | yes        | l'app a fait un status Robert en succès                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              |
| userDeletedAnalytics                                      | e17  | events   | NEW    |                                      | yes        | user a confirmé la suppression des analytics                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         |
| userTappedOnMagicScan                                     | e18  | events   |        |                                      |            | user appuie sur le petit bouton magic scan pour scanner un QR (en haut à droite)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     |
| userTappedOnLargeMagicScan                                | e19  | events   |        |                                      |            | user appuie sur le gros bouton magic scan pour scanner un QR                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         |
| ERR-<SERVICE\_NAME>-<WARNING\_API\_VERSION>-<ERROR\_CODE> |      | errors   |        |                                      | yes        | ERR-WREPORT-v2-500, ERR-STATUS-v6-404 : il ne faut plus que ce soit différencié iOS/Android (dans la mesure où c'est deja dans infos)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                |

### Sanitaires

| Nom                                                                           | Code | Categorie | Type         | Exemple                                | Pour PROD?   | Commentaire                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             |
| ------------------------------------------------------------------------------ | ---- | -------- | ------------ | ------------------------------------ | ------------ | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| installationUuid \= champ spécial à part, random à chaque appel du web-service |      | spécial  | string       | 123e4567-e89b-12d3-a456-426614174000 | yes          | UUID renouvelé à chaque appel pour anonymiser ces données de santé                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  |
| type                                                                           |      | infos    | number       | 1                                    | yes          | toujours à 1 pour le différencier du type 0 (tab dans cette spreadsheet, le "HEALTH" type)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| os                                                                             |      | infos    | string       | iOS ou Android                       | yes          | le système d'exploitation (enum)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    |
| secondsTracingActivated                                                        |      | infos    | number       | 32839                                | yes          | le nombre de secondes d'activation du tracing depuis le début<br>\- au startProximity: stocke la startDate<br>\- au stopProximity (manuel ou Bluetooth), on calcule l'elpased depuis le start, on supprime la startDate, et on l'ajoute au total des secondes ici.<br>Au moment d'envoyer les analytics, si le tracing est activé, on rajoute le temps courant et on update la startDate.<br>\=> bien gérer aussi l'upgrade de l'app, avec tracing activé, mais sans date : dans ce cas, au passage en foreground, on s'aperçoit qu'on a le tracing activé mais sans startDate, alors on ajoute la startDate (now). |
| riskLevel                                                                      |      | infos    | number/FLOAT |                                      | yes          | le riskLevel courant de l'utilisateur                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               |
| dateSample                                                                     |      | infos    | string-date  | 2021-04-07T19:42:39.344Z             | yes/optional | si l'utilisateur a rentré cette date (de prélévement positif test COVID)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            |
| dateFirstSymptoms                                                              |      | infos    | string-date  | 2021-04-07T19:42:39.344Z             | yes/optional | si l'utilisateur a rentré cette date (de premiers symptômes)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        |
| dateLastContactNotification                                                    |      | infos    | string-date  | 2021-04-07T19:42:39.344Z             | yes/optional | au changement de riskLevel, on écrit la lastContactDate à chaque changement de cette date, sauf si elle passe à nul => on le fait à la suite de l'algo du status pour déterminer le riskLevel de l'utilisateur.                                                                                                                                                                                                                                                                                                                                                                                                     |
| userReportedCovidPlus                                                          | eh1  | events   |              |                                      | yes          | l'utilisateur s'est déclaré Covid+ avec succès                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      |
| userReceivedAlertPush                                                          | eh2  | events   |              |                                      | yes          | l'app envoie à l'utilisateur un push suite à une alerte                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             |