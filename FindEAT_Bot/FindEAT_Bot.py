from firebase import firebase
from settings import token, start_msg, client_file, url_api
from telepot.loop import MessageLoop
from telepot.namedtuple import ReplyKeyboardMarkup, ReplyKeyboardRemove, InlineKeyboardMarkup, InlineKeyboardButton
from time import sleep
import json
import os
import requests
import reverse_geocode
import sys
import telepot

firebase = firebase.FirebaseApplication(
    'https://findeatdb.firebaseio.com', None)

# State for user
user_state = {}
place = {}
restaurant = {}
url_salvato = {}


def on_chat_message(msg):
    content_type, chat_type, chat_id = telepot.glance(msg)

    # Check user state
    try:
        user_state[chat_id] = user_state[chat_id]
    except:
        user_state[chat_id] = 0

    # start command
    if 'text' in msg and msg['text'] == '/start':
        if register_user(chat_id):
            bot.sendMessage(chat_id, start_msg, parse_mode='Markdown')

    if 'text' in msg and msg['text'] == '/cerca':

        bot.sendMessage(
            chat_id, 'Invia la zona del ristorante o la tua posizione')
        user_state[chat_id] = 2

    elif user_state[chat_id] == 2:
        if content_type == 'text':
            try:
                r = requests.get(
                    url=url_api + '?tipo=luogo&lista=' + msg['text'].lower())
                json_data = r.json()

                place[chat_id] = msg['text'].lower()
                array = ''
                for i in range(5):
                    nome = json_data['lista'][i]['nome']
                    array += str(i + 1) + ': ' + nome + '\n'

                markup = ReplyKeyboardMarkup(keyboard=[["Si", "No"]
                                                       ])
                bot.sendMessage(
                    chat_id, array + "\nIl ristorante è nella lista?", reply_markup=markup)

                user_state[chat_id] = 3

            except:
                bot.sendMessage(
                    chat_id, "Qualcosa è andato storto, ci scusiamo...")
                print("Error API")

        if content_type == 'location':
            # get location
            coordinates = (str(msg['location']['latitude']),
                           str(msg['location']['longitude'])),
            a = reverse_geocode.search(coordinates)[0]['city']

            try:
                r = requests.get(
                    url=url_api + '?tipo=luogo&lista=' + a.lower())
                json_data = r.json()
                place[chat_id] = a.lower()

                array = ''
                for i in range(5):
                    nome = json_data['lista'][i]['nome']
                    array += str(i + 1) + ': ' + nome + '\n'

                markup = ReplyKeyboardMarkup(keyboard=[["Si", "No"]])
                bot.sendMessage(
                    chat_id, array + "\nIl ristorante è nella lista?", reply_markup=markup)

            except:
                print("Errore API")

            user_state[chat_id] = 3

    elif user_state[chat_id] == 3:

        if msg['text'] == 'Si':
            user_state[chat_id] = 4
            bot.sendMessage(chat_id, 'Scrivi il numero del ristorante', reply_markup=ReplyKeyboardRemove(
                remove_keyboard=True))
        if msg['text'] == 'No':
            bot.sendMessage(chat_id, 'Scrivi il nome del ristorante', reply_markup=ReplyKeyboardRemove(
                remove_keyboard=True))
            user_state[chat_id] = 5

    elif user_state[chat_id] == 4:
        try:
            r = requests.get(
                url=url_api + '?tipo=luogo&lista=' + place[chat_id])
            json_data = r.json()
            url_salvato[chat_id] = url_api + \
                '?tipo=luogo&lista=' + place[chat_id]

            if msg['text'].isdigit() and int(msg['text']) <= 5:
                nome = json_data['lista'][int(msg['text']) - 1]['nome']
                restaurant[chat_id] = nome

                orari = json_data['lista'][int(
                    msg['text']) - 1]['orari']
                posizione = json_data['lista'][int(
                    msg['text']) - 1]['posizione'].split(',')
                apertura = json_data['lista'][int(
                    msg['text']) - 1]['apertura']
                numtell = json_data['lista'][int(
                    msg['text']) - 1]['numtell']
                valutazione = json_data['lista'][int(
                    msg['text']) - 1]['valutazione']

                if orari == None:
                    orari = 'Orari non disponibili'
                else:
                    orari = '\n'.join(orari)

                keyboard = InlineKeyboardMarkup(inline_keyboard=[
                    [dict(text='Visualizza feedback di Google', callback_data=1),
                     dict(text='Visualizza feedback di FindEAT', callback_data=7)]
                ])

                if apertura == 'Aperto' or apertura == None:
                    bot.sendMessage(chat_id, '🍽 ' + nome + '\n🕐 ' + str(apertura).replace('None', 'Apertura non disponibile') +
                                    '\n📱 ' + str(numtell) + '\n⭐️ ' + str(valutazione))

                else:
                    bot.sendMessage(chat_id, '🍽 ' + nome + '\n🕐 ' + str(apertura).replace('None', 'Apertura non disponibile') +
                                    '\n📱 ' + str(numtell) + '\n⭐️ ' + str(valutazione) + '\n------\n' + orari)

                bot.sendLocation(chat_id, posizione[0], posizione[1])
                bot.sendMessage(
                    chat_id, "Hai bisogno di aiuto?", reply_markup=keyboard)
                user_state[chat_id] = 0

            else:
                user_state[chat_id] = 4
                bot.sendMessage(chat_id, 'Scrivi il numero del ristorante')

        except:
            print("Errore API")

    elif user_state[chat_id] == 5:
        try:
            r = requests.get(
                url=url_api + "/?tipo=diretto&lista=" + msg['text'] + ' ' + place[chat_id])
            json_data = r.json()
            url_salvato[chat_id] = url_api + "/?tipo=diretto&lista=" + \
                msg['text'] + ' ' + place[chat_id]

            array = ''
            nome = json_data['lista'][0]['nome']
            restaurant[chat_id] = nome
            apertura = json_data['lista'][0]['apertura']
            numtell = json_data['lista'][0]['numtell']
            valutazione = json_data['lista'][0]['valutazione']
            posizione = json_data['lista'][0]['posizione'].split(',')
            orari = json_data['lista'][0]['orari']

            if orari == None:
                orari = 'Orari non disponibili'
            else:
                orari = '\n'.join(orari)

            keyboard = InlineKeyboardMarkup(inline_keyboard=[
                [dict(text='Visualizza feedback di Google', callback_data=1),
                 dict(text='Visualizza feedback di FindEAT', callback_data=7)]
            ])

            if apertura == 'Aperto' or apertura == None:
                bot.sendMessage(chat_id, '🍽 ' + nome + '\n🕐 ' + str(apertura).replace('None', 'Apertura non disponibile') +
                                '\n📱 ' + str(numtell) + '\n⭐️ ' + str(valutazione))
            else:
                bot.sendMessage(chat_id, '🍽 ' + nome + '\n🕐 ' + str(apertura).replace('None', 'Apertura non disponibile') +
                                '\n📱 ' + str(numtell) + '\n⭐️ ' + str(valutazione) + '\n------\n' + orari)

            bot.sendLocation(chat_id, posizione[0], posizione[1])
            bot.sendMessage(
                chat_id, "Hai bisogno di aiuto?", reply_markup=keyboard)
            user_state[chat_id] = 0

        except:
            user_state[chat_id] = 5
            bot.sendMessage(chat_id, "Ristorante non trovato, riprova...")

    elif user_state[chat_id] == 6:
        try:
            author = msg['from']['first_name'] + ' ' + msg['from']['last_name']
        except:
            author = msg['from']['first_name']

        result = firebase.get(
            '/restaurants/'+place[chat_id] + '/'+restaurant[chat_id] + '/', None)
        try:
            i = len(result)
            result = firebase.patch('/restaurants/'+place[chat_id] + '/'+restaurant[chat_id] + '/'+str(i)+'/',
                                    {'author': author, 'text': msg['text']})
            bot.sendMessage(
                chat_id, 'Grazie per aver recensito ' + restaurant[chat_id] + ', questa è la ' +
                str(i+1) + ' recensione fatta 😄')
            user_state[chat_id] = 0
        except:
            i = 0
            # put element to firebase
            result = firebase.patch('/restaurants/'+place[chat_id] + '/'+restaurant[chat_id] + '/'+str(i)+'/',
                                    {'author': author, 'text': msg['text']})
            bot.sendMessage(
                chat_id, 'Complimenti! Hai creato la prima recensione per ' + restaurant[chat_id])
            user_state[chat_id] = 0


def on_callback_query(msg):
    query_id, from_id, query_data = telepot.glance(
        msg, flavor='callback_query')
    edited = (from_id, msg['message']['message_id'])
    try:
        r = requests.get(url=url_salvato[from_id])
        json_data = r.json()

        if (query_data == str(1)):

            lista = json_data['lista'][0]['reviews'][int(query_data)-1]
            autore = lista['author_name']
            valurazione = str(lista['rating'])
            commento = lista['text']
            feedback = 'Autore: ' + autore + '\nValutazione: ' + \
                valurazione + '\nFeedback: ' + commento

            keyboard = InlineKeyboardMarkup(inline_keyboard=[
                [dict(text='Avanti', callback_data=2)]

            ])

            bot.editMessageText(edited, feedback,
                                reply_markup=keyboard)

        if (query_data == str(2)):
            keyboard = InlineKeyboardMarkup(inline_keyboard=[
                [dict(text='Indietro', callback_data=1),
                 dict(text='Avanti', callback_data=3)]
            ])

            lista = json_data['lista'][0]['reviews'][int(query_data)-1]
            autore = lista['author_name']
            valurazione = str(lista['rating'])
            commento = lista['text']
            feedback = 'Autore: ' + autore + '\nValutazione: ' + \
                valurazione + '\nFeedback: ' + commento

            bot.editMessageText(edited, feedback,
                                reply_markup=keyboard)

        if (query_data == str(3)):
            keyboard = InlineKeyboardMarkup(inline_keyboard=[
                [dict(text='Indietro', callback_data=2),
                 dict(text='Avanti', callback_data=4)]
            ])

            lista = json_data['lista'][0]['reviews'][int(query_data)-1]
            autore = lista['author_name']
            valurazione = str(lista['rating'])
            commento = lista['text']
            feedback = 'Autore: ' + autore + '\nValutazione: ' + \
                valurazione + '\nFeedback: ' + commento

            bot.editMessageText(edited, feedback,
                                reply_markup=keyboard)

        if (query_data == str(4)):
            keyboard = InlineKeyboardMarkup(inline_keyboard=[
                [dict(text='Indietro', callback_data=3),
                 dict(text='Avanti', callback_data=5)]
            ])

            lista = json_data['lista'][0]['reviews'][int(query_data)-1]
            autore = lista['author_name']
            valurazione = str(lista['rating'])
            commento = lista['text']
            feedback = 'Autore: ' + autore + '\nValutazione: ' + \
                valurazione + '\nFeedback: ' + commento

            bot.editMessageText(edited, feedback,
                                reply_markup=keyboard)

        if (query_data == str(5)):
            keyboard = InlineKeyboardMarkup(inline_keyboard=[
                [dict(text='Indietro', callback_data=4),
                 dict(text='Fine', callback_data=6)]
            ])

            lista = json_data['lista'][0]['reviews'][int(query_data)-1]
            autore = lista['author_name']
            valurazione = str(lista['rating'])
            commento = lista['text']
            feedback = 'Autore: ' + autore + '\nValutazione: ' + \
                valurazione + '\nFeedback: ' + commento

            bot.editMessageText(edited, feedback,
                                reply_markup=keyboard)

        if (query_data == str(6)):
            bot.editMessageText(edited, 'Fine')

            keyboard = InlineKeyboardMarkup(inline_keyboard=[
                [dict(text='Visualizza feedback di Google', callback_data=1)]
            ])
            bot.editMessageText(
                edited, "Hai bisogno di aiuto?", reply_markup=keyboard)

        if (query_data == str(7)):
            result = firebase.get(
                '/restaurants/'+place[from_id] + '/'+restaurant[from_id] + '/', None)
            if result == None:
                keyboard = InlineKeyboardMarkup(inline_keyboard=[
                    [dict(text='Scrivi una recensione', callback_data=100)]
                ])
                bot.editMessageText(
                    edited, 'Nessuna recensione!', reply_markup=keyboard)
            else:
                i = len(result)
                if i == 1:
                    keyboard = InlineKeyboardMarkup(inline_keyboard=[
                        [dict(text='Visualizza recensione', callback_data=8),
                         dict(text='Scrivi una recensione', callback_data=100)]
                    ])
                    bot.editMessageText(
                        edited, str(i) + ' recensione trovata', reply_markup=keyboard)

                else:
                    keyboard = InlineKeyboardMarkup(inline_keyboard=[
                        [dict(text='Visualizza recensioni', callback_data=8),
                            dict(text='Scrivi una recensione', callback_data=100)]
                    ])
                    bot.editMessageText(
                        edited, str(i) + ' recensioni trovate', reply_markup=keyboard)

        if (query_data == str(8)):
            result = firebase.get(
                '/restaurants/'+place[from_id] + '/'+restaurant[from_id] + '/', None)
            o = len(result)
            feedback = ''
            for i in range(int(o)):
                feedback = feedback + \
                    str(i+1)+') Autore: '+result[i]['author'] + \
                    ' \nFeedback: ' + result[i]['text'] + '\n\n'

            keyboard = InlineKeyboardMarkup(inline_keyboard=[
                [dict(text='Visualizza feedback di FindEAT', callback_data=7)]
            ])
            bot.editMessageText(
                edited, 'Feedback di FindEAT:\n' + feedback,  reply_markup=keyboard)

        # scrivi recensione
        if (query_data == str(100)):
            bot.editMessageText(edited, "Inserisci il tuo feedback")
            user_state[from_id] = 6

    except Exception as e:
        print(e)


def register_user(chat_id):
    """
    Register user
    """
    insert = 1

    try:
        f = open(client_file, "r+")

        for user in f.readlines():
            if user.replace('\n', '') == str(chat_id):
                insert = 0

    except IOError:
        f = open(client_file, "w")

    if insert:
        f.write(str(chat_id) + '\n')

    f.close()

    return insert


# Main
print("Avvio FindEAT_Bot")

# PID file
pid = str(os.getpid())
pidfile = "/tmp/FindEAT_Bot.pid"

# Check if PID exist
if os.path.isfile(pidfile):
    print("%s already exists, exiting!" % pidfile)
    sys.exit()

# Create PID file
f = open(pidfile, 'w')
f.write(pid)

# Start working
try:
    bot = telepot.Bot(token)
    MessageLoop(bot, {'chat': on_chat_message,
                      'callback_query': on_callback_query}).run_as_thread()
    while(1):
        sleep(10)
finally:
    os.unlink(pidfile)
