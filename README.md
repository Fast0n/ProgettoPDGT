# 🍕 FindEAT 🍽 #
[![Build Status](https://travis-ci.org/Fast0n/ProgettoPDGT.svg?branch=master)](https://travis-ci.org/Fast0n/ProgettoPDGT)
[![Deploy](https://raw.githubusercontent.com/ashwanthkumar/gocd-build-badge-server/master/doc/passed.png)](https://findeatapi.herokuapp.com/)

## Progetto Piattaforme Digitali per la Gestione del Territorio ##

### Appello: ###
* Primo appello sessione estiva 2017/2018

### Alunni: ###
* [Giorgia Giuseppetti](http://github.com/gg97g)
* [Massimiliano Montaleone](http://github.com/Fast0n)

-----------------------------------------------------

## Descrizione ##

Il progetto _FindEAT_ si pone come obbiettivi primari:
* La ricerca dei ristoranti nelle vicinanze
* Uso dei filtri per delle ricerche in base alle esigenze dell'utente

-----------------------------------------------------

## Relazione ##

Il progetto è composto da 3 obbiettivi principali:
 * Realizzazione di un API (GET) in NodeJS
 * Implementazione di un BotTelegram (Python)
 * Implementazione di un'app Android (JAVA)



<div align="center"><a><img src='img/schema.png' height='400' alt='icon'/></a></div>


 ----------------------------------------------------
## FindEAT_Bot ##
 
FindEAT_Bot è il bot pensato per gli utenti che possono comodamente ricercare il loro ristorante preferito. Il bot permette di ricercare per zona o per posizione. Esso ristituisce all’utente i primi 5 ristoranti che riesce a trovare, se il ristorante desiderato non è nella lista è possibile fare una ricerca diretta scrivendo il nome del locale. Una volta trovato il locale, verranno inviate delle informazioni, come: nome, apertura/chiusura, numero di telefono, valutazione, eventuali orari nel caso il ristorante fosse chiuso e la posizione. Inoltre vengono visualizzati dei feedback utente come: l’uso del pos, se il costo è mediamente alto o basso e  la possibilità di un menù senza glutine o per bambini. Infine i feedback scritti dagli utenti di Google e quelli di FindEAT. 

Questo bot è stato realizzato in Python e si interfaccia alle API di Google ed al database di Firebase.

Abbiamo un solo semplice comando:

```
./cerca: permette di cercare il ristorante per zona o posizione    
```
 
 Ovviamente il bot è una versione ‘lite’, schematizzata e molto semplice da utilizzare e gestire.





