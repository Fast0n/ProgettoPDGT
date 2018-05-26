# FindEAT-api

## Methods
#### search for 'luogo'
List of restaurants from location

Parameters:
- tipo  [required] - luogo
- lista [required] - name of city/town


Example Usage:
```
findeat-api.herokuapp.com/?tipo=luogo&lista=urbino
```
Returns
The standard JSON array

- google id
- nome
- indirizzo
- posizione
- apertura
- valutazione


## Methods
#### search for 'diretto'
Search restaurants from location

Parameters:
- tipo  [required] - diretto
- lista [required] - name of restaurant and city/town


Example Usage:
```
findeat-api.herokuapp.com/?tipo=diretto&lista=il ghiottone urbino
```
Returns
The standard JSON array

- nome
- indirizzo
- posizione
- apertura
- valutazione
- photo
- numtell
- sitoweb
- orari
- feedback google
