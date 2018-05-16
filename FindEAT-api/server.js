// Librerie
var express = require('express');
var app = express();
var request = require('request');

// variabilli
var google_key = 'AIzaSyDzBgxO4MX2Sha0qZ7aM3P91pQlWB-fdFc';

app.get('/', function (req, res) {

    var json = [];
    if (req.query.tipo == undefined) {

        var warning = {
            STATUS: '1',
            CODE: res.statusCode,
            MESSAGE: 'For help, go here -> https://github.com/Fast0n/Progetto-PDGT',
            DATA: [],
            TOTALS: [],
        };
        [0]['name']
        res.json(warning);



    } else {
        var nElementi;
        if (req.query.tipo == 'gps' && req.query.lista != undefined)
            richiesta('https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=' + req.query.lista + '&radius=3000&type=restaurant&key=' + google_key, 'vicinity', nElementi = 5);

        if (req.query.tipo == 'luogo' && req.query.lista != undefined)
            richiesta('https://maps.googleapis.com/maps/api/place/textsearch/json?query=ristoranti+a+' + req.query.lista + '&key=' + google_key, 'formatted_address', nElementi = 5)

        if (req.query.tipo == 'diretto' && req.query.lista != undefined)
            richiesta('https://maps.googleapis.com/maps/api/place/textsearch/json?query=' + req.query.lista + '&key=' + google_key, 'vicinity', nElementi = 1)

    }

    function richiesta(uri, indirizzo, nElementi) {
        // richiesta alle api da Google chiedendo un file JSON
        var options = {
            uri: uri,
            json: true

        };
        // crea la struttura del file json
        var data_store = {};
        data_store['lista'] = []

        request(options, function (error, response, body) {
            if (!error && response.statusCode == 200) {

                for (var i = 0; i < nElementi; i++) {
                    var id = body['results'][i]['place_id'];
                    var nome = body['results'][i]['name'];
                    var indirizzo = body['results'][i][indirizzo];
                    var valutazione = body['results'][i]['rating'];
                    var posizione = body['results'][i]['geometry']['location']['lat'] + "," + body['results'][i]['geometry']['location']['lng'];

                    // crea un array all'i esimo elemento
                    data_store['lista'][i] = {}

                    // try/catch per alcuni ristoranti dove l'orario di apertura non è specificato
                    try {
                        var apertura = body['results'][i]['opening_hours']['open_now'].toString().replace("true", "Aperto").replace("false", "Chiuso");
                        info(id, data_store['lista'][i], apertura);

                    } catch (Exception) {
                        var apertura = null;
                        info(id, data_store['lista'][i], apertura);
                    }
                    data_store['lista'][i]['nome'] = nome
                    data_store['lista'][i]['indirizzo'] = indirizzo
                    data_store['lista'][i]['posizione'] = posizione
                    data_store['lista'][i]['apertura'] = apertura
                    data_store['lista'][i]['valutazione'] = valutazione
                }

                function info(id, data_store1, apertura) {
                    // creata una nuova richiesta per i numeri di telefono
                    var options = {
                        uri: 'https://maps.googleapis.com/maps/api/place/details/json?placeid=' + id + '&language=it&key=' + google_key,
                        json: true
                    };

                    request(options, function (error, response, body) {
                        if (!error && response.statusCode == 200) {

                            var numero = body['result']['international_phone_number'];
                            var sitoweb = body['result']['website'];
                            var reviews = body['result']['reviews'];

                            try {
                                var weekday_text = body['result']['opening_hours']['weekday_text'];
                            } catch (Exception) {}

                            if (sitoweb != undefined && weekday_text != undefined && apertura != 'Aperto') {
                                data_store1['numtell'] = numero
                                data_store1['sitoweb'] = sitoweb
                                data_store1['reviews'] = reviews
                                data_store1['orari'] = weekday_text

                            } else {
                                data_store1['numtell'] = numero
                                data_store1['sitoweb'] = sitoweb
                                data_store1['reviews'] = reviews
                                data_store1['orari'] = null
                            }
                        }
                    });

                    // crea un timeout di 1sec per attendere le richieste 
                    setTimeout(function () {
                        res.send(data_store);
                    }, 1000);

                }

            }

        });

    }

});

var listener = app.listen(process.env.PORT || 3000, function () {});