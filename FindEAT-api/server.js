// Librerie
var express = require('express');
var app = express();
var request = require('request');

// crea la struttura del file json
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
        res.json(warning);



    } else {

        var google_key = [
            'AIzaSyCwedwG8Q__fE6c8TKaLhVFy5XsLraIKAs',
            'AIzaSyA8FVaaCLNkuPJr2Avl-jl_EEjpV90S0vQ',
            'AIzaSyCYb0i2xexV6qol3eftKYporkX1hCwOf5Y',
            'AIzaSyADXRrKmW3FaxwAZu0vvx46VznUfbGezh0',
            'AIzaSyB24JuXf8id9gSrJ1O2W9qBQbk4DPWopg0',
            'AIzaSyCpCIjJ19EiM2A1wIr-yphQyfQ2iTDl260',
            'AIzaSyB0F1R95dDgpWNaZdzYna5RLAQ8YwTnCP8'
        ];




        for (var i = 1; i < google_key.length; i++) {
            richiesta(i);
        }



    }


    function richiesta(i) {


        var options = {
            uri: 'https://maps.googleapis.com/maps/api/place/textsearch/json?query=ristoranti+a+urbino&key=' + google_key[i],
            json: true

        };
        request.get(options, function (error, response, body) {
            var data_store = {};
            data_store['lista'] = []



            if (body['STATUS'] == undefined) {

                if (req.query.tipo == 'luogo' && req.query.lista != undefined) {

                    var uri = 'https://maps.googleapis.com/maps/api/place/textsearch/json?query=ristoranti+a+' + req.query.lista + '&key=' + google_key[i]

                    var options = {
                        uri: uri,
                        json: true

                    };
                    request(options, function (error, response, body) {
                        for (var i = 0; i < body['results'].length; i++) {


                            var id = body['results'][i]['place_id'];
                            var nome = body['results'][i]['name'];
                            var indirizzo = body['results'][i]['formatted_address'];
                            var valutazione = body['results'][i]['rating'];
                            var posizione = body['results'][i]['geometry']['location']['lat'] + "," + body['results'][i]['geometry']['location']['lng'];

                            // crea un array all'i esimo elemento
                            data_store['lista'][i] = {}

                            // try/catch per alcuni ristoranti dove l'orario di apertura non è specificato
                            try {
                                var apertura = body['results'][i]['opening_hours']['open_now'].toString().replace("true", "Aperto").replace("false", "Chiuso");

                            } catch (Exception) {
                                var apertura = null;
                            }
                            data_store['lista'][i]['id'] = id
                            data_store['lista'][i]['nome'] = nome
                            data_store['lista'][i]['indirizzo'] = indirizzo
                            data_store['lista'][i]['posizione'] = posizione
                            data_store['lista'][i]['apertura'] = apertura
                            data_store['lista'][i]['valutazione'] = valutazione


                        }


                        res.send(data_store);
                        request.destroy()


                    });


                }


                if (req.query.tipo == 'id' && req.query.lista != undefined) {
                    var uri = 'https://maps.googleapis.com/maps/api/place/details/json?placeid=' + req.query.lista + '&key=' + google_key[i]


                    var options = {
                        uri: uri,
                        json: true

                    };
                    request(options, function (error, response, body) {
                        var nome = body['result']['name'];
                        var indirizzo = body['result']['formatted_address'];
                        var valutazione = body['result']['rating'];
                        var posizione = body['result']['geometry']['location']['lat'] + "," + body['result']['geometry']['location']['lng'];
                        var numero = body['result']['international_phone_number'];
                        var sitoweb = body['result']['website'];
                        var reviews = body['result']['reviews'];
                        var photo = body['result']['photos'][0]['photo_reference'];


                        // crea un array all'i esimo elemento
                        data_store['lista'][0] = {}

                        // try/catch per alcuni ristoranti dove l'orario di apertura non è specificato
                        try {
                            var apertura = body['result']['opening_hours']['open_now'].toString().replace("true", "Aperto").replace("false", "Chiuso");

                        } catch (Exception) {
                            var apertura = null;
                        }
                        data_store['lista'][0]['nome'] = nome
                        data_store['lista'][0]['indirizzo'] = indirizzo
                        data_store['lista'][0]['posizione'] = posizione
                        data_store['lista'][0]['apertura'] = apertura
                        data_store['lista'][0]['valutazione'] = valutazione
                        data_store['lista'][0]['photo'] = photo

                        if (numero == undefined)
                            data_store['lista'][0]['numtell'] = null
                        else
                            data_store['lista'][0]['numtell'] = numero


                        if (sitoweb == undefined)
                            data_store['lista'][0]['sitoweb'] = null
                        else
                            data_store['lista'][0]['sitoweb'] = sitoweb

                        try {
                            var weekday_text = body['result']['opening_hours']['weekday_text'];
                            if (apertura != 'Aperto')
                                data_store['orari'] = weekday_text
                            else
                                data_store['orari'] = null
                        } catch (Exception) {}


                        if (reviews == undefined)
                            data_store['feedback'] = null
                        else
                            data_store['feedback'] = reviews


                        res.send(data_store);
                        request.destroy()

                    });

                }





                if (req.query.tipo == 'diretto' && req.query.lista != undefined) {

                    var uri = 'https://maps.googleapis.com/maps/api/place/textsearch/json?query=' + req.query.lista + '&key=' + google_key[i]


                    var options = {
                        uri: uri,
                        json: true

                    };
                    request(options, function (error, response, body) {

                        var id = body['results'][0]['place_id'];
                        var nome = body['results'][0]['name'];
                        var indirizzo = body['results'][0]['formatted_address'];
                        var valutazione = body['results'][0]['rating'];
                        var posizione = body['results'][0]['geometry']['location']['lat'] + "," + body['results'][0]['geometry']['location']['lng'];

                        // crea un array all'i esimo elemento
                        data_store['lista'][0] = {}

                        // try/catch per alcuni ristoranti dove l'orario di apertura non è specificato
                        try {
                            var apertura = body['results'][0]['opening_hours']['open_now'].toString().replace("true", "Aperto").replace("false", "Chiuso");

                        } catch (Exception) {
                            var apertura = null;
                        }
                        data_store['lista'][0]['nome'] = nome
                        data_store['lista'][0]['indirizzo'] = indirizzo
                        data_store['lista'][0]['posizione'] = posizione
                        data_store['lista'][0]['apertura'] = apertura
                        data_store['lista'][0]['valutazione'] = valutazione

                        res.send(data_store);
                        request.destroy()
                    });
                }





            }

        });

    }



});


var listener = app.listen(process.env.PORT || 3000, function () {})