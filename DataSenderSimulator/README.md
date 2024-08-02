Prerequisiti:

    Java 22, Intellij Ultimate

Installazione:
    
    Avviare il main
    Verificare che non ci siano errori nella console

    __________________


    Si possono cambiare il numero di sensori nell'apposita riga.
    Ogni sensore spamma i nuovi dati ogni 10 secondi
            

Risorse:

    https://chatgpt.com/

Errori:
    In caso di errore 429, basta aumentare il limite di richieste per ip accettate dal backend
    nel package: 
        config/interceptor/BucketService alla riga:
            Bandwidth bandwidth = Bandwidth.classic(600000, Refill.intervally(10, Duration.ofSeconds(60))); //RATE LIMITER LIMIT
    
    
    Il valore da modificare è il 600000