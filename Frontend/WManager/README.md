Prerequisiti
Node.js e npm: Angular richiede Node.js e npm (Node Package Manager). Puoi scaricare e installare Node.js dal sito ufficiale: Node.js. L'installazione di Node.js include anche npm.

Angular CLI: L'Angular Command Line Interface (CLI) semplifica la creazione e la gestione dei progetti Angular. Puoi installarlo globalmente usando npm:

        npm install -g @angular/cli
        cd Frontend/WManager
        npm install
        ng serve


Dopo aver avviato il server di sviluppo, Angular compilerà e servirà il progetto. Puoi accedere al progetto aprendo il browser e navigando all'indirizzo:

            http://localhost:4200
            
            

Ulteriori comandi utili

    ng generate component nome-component
    ng generate service nome-servizio
    ng generate module nome-modulo


Risorse:

    https://v17.angular.io/start
    https://material.angular.io/
    https://www.codemotion.com/magazine/it/frontend-it/angular-control-flow-la-guida-completa/


TODO:

- certificati HTTPS
  - visualizzazione degli shapefile
  - Sezione Admin
  - Quando l'utente invia i dati bisogna inserire i controlli del caso dentro sensorDataService

FATTO:
  - cambiare sensorID in userID all'interno dell'oggetto Sensor
  - associare la user password non al sensore ma direttamente all'utente
  - Aggiungere all'interno del Sensor un tipo Descrizione
  - aggiungere area d'interesse all'interno di sensor data  //verificare se va bene in sensorDataService - save (72)
  - Quando l'utente invia i dato deve inserire la user password e l'area d'interesse
  - Aggiungere nell'interfaccia grafica all'interno delle specifiche dell'utente lo userID per inviare i dati (manca il tasto per accedere ma sta in test/sensor)
    e l'id lo prende autonomamente (ma dovrebbe mandare il token di accesso piuttosto che l'id)



  - Il Sensor può essere registrato nel sistema tramite:
    - l'interfaccia grafica (manca solo il tasto. Il componente sta a /create-sensor)
    - una richiesta di registrazione tramite json


