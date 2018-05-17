import os
import sys
import telepot
import json
import requests
from settings import token, start_msg, client_file
from telepot.namedtuple import ReplyKeyboardMarkup, ReplyKeyboardRemove, InlineKeyboardMarkup, InlineKeyboardButton
from time import sleep

# State for user
user_state = {}

testo_salvato = {}

url_api = 'https://progetto-pdgt.glitch.me/'


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

                testo_salvato[chat_id] = msg['text'].lower()
                array = ''
                for i in range(5):
                    nome = json_data['lista'][i]['nome']
                    array += str(i + 1) + ': ' + nome + '\n'

                bot.sendMessage(chat_id, array)

                msg = "Il ristorante è nella lista?"
                markup = ReplyKeyboardMarkup(keyboard=[["Si", "No"]
                                                       ])
                bot.sendMessage(chat_id, msg, reply_markup=markup)
                user_state[chat_id] = 3

            except:
                print("Error URL check README.md")

        if content_type == 'location':

            try:
                # send location
                response = requests.get('http://maps.googleapis.com/maps/api/geocode/json?latlng=' + str(
                    msg['location']['latitude']) + ',' + str(msg['location']['longitude']))
                resp_json_payload = response.json()
                a = str(resp_json_payload['results'][0]
                        ['address_components'][2]['long_name'])

                try:
                    r = requests.get(
                        url=url_api + a.lower() + ".json")
                    json_data = r.json()
                    testo_salvato[chat_id] = a.lower()

                    array = ''
                    for i in range(5):
                        nome = json_data['lista'][i]['nome']
                        array += str(i + 1) + ': ' + nome + '\n'

                    bot.sendMessage(chat_id, array)

                except:
                    print("Error URL check README.md")

                # bot.sendMessage(chat_id, 'Scrivi il nome/numero del ristorante')
                msg = "Il ristorante è nella lista?"
                markup = ReplyKeyboardMarkup(keyboard=[["Si", "No"]
                                                       ])
                bot.sendMessage(chat_id, msg, reply_markup=markup)
                user_state[chat_id] = 3
            except:
                bot.sendMessage(
                    chat_id, "Errore chiave Google, rimanda la posizione...")

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
                url=url_api + '?tipo=luogo&lista=' + testo_salvato[chat_id])
            json_data = r.json()

            if msg['text'].isdigit() and int(msg['text']) <= 5:
                nome = json_data['lista'][int(msg['text']) - 1]['nome']

                apertura = json_data['lista'][int(
                    msg['text']) - 1]['apertura']
                numtell = json_data['lista'][int(
                    msg['text']) - 1]['numtell']
                valutazione = json_data['lista'][int(
                    msg['text']) - 1]['valutazione']
                posizione = json_data['lista'][int(
                    msg['text']) - 1]['posizione'].split(',')

                bot.sendMessage(chat_id, '🍽 ' + nome + '\n🕐 ' + str(apertura).replace('None', 'Apertura non disponibile') + '\n📱 ' +
                                str(numtell) + '\n⭐️ ' + str(valutazione))
                bot.sendLocation(chat_id, posizione[0], posizione[1])
            else:
                user_state[chat_id] == 4
                bot.sendMessage(chat_id, 'Scrivi il numero del ristorante')

        except:
            print("Error URL check README.md")

    elif user_state[chat_id] == 5:
        try:
            r = requests.get(
                url=url_api + "/?tipo=diretto&lista=" + msg['text'] + ' ' + testo_salvato[chat_id])
            json_data = r.json()

            array = ''
            nome = json_data['lista'][0]['nome']
            apertura = json_data['lista'][0]['apertura']
            numtell = json_data['lista'][0]['numtell']
            valutazione = json_data['lista'][0]['valutazione']
            posizione = json_data['lista'][0]['posizione'].split(',')
            orari = json_data['lista'][0]['orari']

            if apertura == 'Aperto':
                bot.sendMessage(chat_id, '🍽 ' + nome + '\n🕐 ' + str(apertura).replace('None', 'Apertura non disponibile') + '\n📱 ' +
                                str(numtell) + '\n⭐️ ' + str(valutazione))
            else:

                if orari == None:
                    orari = 'Orari non disponibili'
                else:
                    orari = '\n'.join(orari)

                bot.sendMessage(chat_id, '🍽 ' + nome + '\n🕐 ' + str(apertura).replace('None', 'Apertura non disponibile') + '\n📱 ' +
                                str(numtell) + '\n⭐️ ' + str(valutazione) + '\n------\n' + orari)

            bot.sendLocation(chat_id, posizione[0], posizione[1])

        except:
            bot.sendMessage(chat_id, "Ristorante non trovato, riprova...")
            user_state[chat_id] = 5


def register_user(chat_id):
    """
    Register given user to receive news
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
    bot.message_loop(on_chat_message)
    while(1):
        sleep(10)
finally:
    os.unlink(pidfile)
