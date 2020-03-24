from settings import token, start_msg, client_file, url_api, config
from telepot.loop import MessageLoop
from telepot.namedtuple import ReplyKeyboardMarkup, ReplyKeyboardRemove, InlineKeyboardMarkup, InlineKeyboardButton
from time import sleep
import time
import json
import os
import requests
from geopy.geocoders import Nominatim
import sys
import telepot
import pyrebase

geo = Nominatim(user_agent="AppDaemon")
firebase = pyrebase.initialize_app(config)
db = firebase.database()

# State for user
user_state = {}
place = {}
restaurant = {}
place_id = {}


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
            cerca(msg['text'], msg)

        if content_type == 'location':
            lat_long = "{}, {}".format(
                str(msg['location']['latitude']), str(msg['location']['longitude']))
            coordinates = geo.reverse(lat_long)
            try:
                a = coordinates.raw["address"]['town']
            except:
                a = coordinates.raw["address"]['city']

            cerca(a, msg)

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
        richiesta(url_api + '?tipo=id&lista=' +
                  place_id[chat_id][int(msg['text'])-1], msg)

    elif user_state[chat_id] == 5:
        try:
            richiesta(url_api + "?tipo=diretto&lista=" +
                      msg['text'] + ' ' + place[chat_id], msg)
        except:
            bot.sendMessage(chat_id, 'Ristorante non trovato... Riprova')
            richiesta(url_api + "?tipo=diretto&lista=" +
                      msg['text'] + ' ' + place[chat_id], msg)

    elif user_state[chat_id] == 6:

        try:
            author = msg['from']['first_name'] + ' ' + msg['from']['last_name']
        except:
            author = msg['from']['username']

        result = db.child("restaurants").child(
            place[chat_id]).child(restaurant[chat_id]).get()

        try:
            i = len(result.val())
        except:
            i = 0

        datetime = int(time.time())

        data = {'author_name': author, 'text': msg['text'], 'time': datetime}
        db.child("restaurants").child(place[chat_id]).child(
            restaurant[chat_id]).child(i).set(data)

        markup = ReplyKeyboardMarkup(
            keyboard=[["Si", "No"]], resize_keyboard=True)

        bot.sendMessage(
            chat_id, 'Grazie per aver recensito ' + restaurant[chat_id] + ', questa è la ' +
            str(i+1) + ' recensione fatta 😄\n\nSi può pagare con carte di credito?', reply_markup=markup)

        user_state[chat_id] = 7

    elif user_state[chat_id] == 7:
        if msg['text'] == 'Si':
            filtri(('/filters/{}/{}/cartadicredito/').format(place[chat_id], restaurant[chat_id]), 'votisi/', 'votino/',
                   ('/filters/{}/{}/cartadicredito/').format(place[chat_id], restaurant[chat_id]), 'votisi')

        if msg['text'] == 'No':

            filtri(('/filters/{}/{}/cartadicredito/').format(place[chat_id], restaurant[chat_id]), 'votisi/', 'votino/',
                   ('/filters/{}/{}/cartadicredito/').format(place[chat_id], restaurant[chat_id]), 'votino')

        markup = ReplyKeyboardMarkup(
            keyboard=[["Si", "No"]], resize_keyboard=True)
        bot.sendMessage(
            chat_id, 'Il ristorante è costoso?', reply_markup=markup)

        user_state[chat_id] = 8

    elif user_state[chat_id] == 8:
        if msg['text'] == 'Si':
            filtri(('/filters/{}/{}/costo/').format(place[chat_id], restaurant[chat_id]), 'alto/', 'basso/',
                   ('/filters/{}/{}/costo/').format(place[chat_id], restaurant[chat_id]), 'alto')

        if msg['text'] == 'No':

            filtri(('/filters/{}/{}/costo/').format(place[chat_id], restaurant[chat_id]), 'alto/', 'basso/',
                   ('/filters/{}/{}/costo/').format(place[chat_id], restaurant[chat_id]), 'basso')

        markup = ReplyKeyboardMarkup(
            keyboard=[["Si", "No"]], resize_keyboard=True)
        bot.sendMessage(
            chat_id, 'Il ristorante prevede un menu senza glutine?', reply_markup=markup)
        user_state[chat_id] = 9

    elif user_state[chat_id] == 9:
        if msg['text'] == 'Si':
            filtri(('/filters/{}/{}/menu/0/celiaci/').format(place[chat_id], restaurant[chat_id]), 'votisi/', 'votino/',
                   ('/filters/{}/{}/menu/0/celiaci').format(place[chat_id], restaurant[chat_id]), 'votisi')

        if msg['text'] == 'No':
            filtri('/filters/{}/{}/menu/0/celiaci/', 'votisi/', 'votino/',
                   '/filters/{}/{}/menu/0/celiaci', 'votino')
        markup = ReplyKeyboardMarkup(
            keyboard=[["Si", "No"]], resize_keyboard=True)
        bot.sendMessage(
            chat_id, 'Il ristorante prevede un menu per bambini?', reply_markup=markup)
        user_state[chat_id] = 10

    elif user_state[chat_id] == 10:
        if msg['text'] == 'Si':
            filtri(('/filters/{}/{}/menu/1/bambino/').format(place[chat_id], restaurant[chat_id]), 'votisi/', 'votino/',
                   ('/filters/{}/{}/menu/1/bambino').format(place[chat_id], restaurant[chat_id]), 'votisi')

        if msg['text'] == 'No':
            filtri(('/filters/{}/{}/menu/1/bambino/').format(place[chat_id], restaurant[chat_id]), 'votisi/', 'votino/',
                   ('/filters/{}/{}/menu/1/bambino').format(place[chat_id], restaurant[chat_id]), 'votino')
        markup = ReplyKeyboardMarkup(
            keyboard=[["Si", "No"]], resize_keyboard=True)
        bot.sendMessage(
            chat_id, 'Grazie per aver risposto alle domande!!!🤞🏻🤝😄 ', reply_markup=ReplyKeyboardRemove(
                remove_keyboard=True))
        user_state[chat_id] = 0


def richiesta(url, msg):
    content_type, chat_type, chat_id = telepot.glance(msg)
    r = requests.get(
        url=url)
    json_data = r.json()
    file = open("__pycache__/"+str(chat_id)+".json", "w")
    file.write(r.text)
    file.close()

    nome = json_data['lista'][0]['nome'].replace('.', '')
    restaurant[chat_id] = nome
    try:
        orari = json_data['orari'][0]
        orari = '\n'.join(orari)
    except:
        orari = 'Orari non disponibili'
    posizione = json_data['lista'][0]['posizione'].split(',')
    apertura = json_data['lista'][0]['apertura']
    numtell = json_data['lista'][0]['numtell']
    valutazione = json_data['lista'][0]['valutazione']

    counter(('/filters/{}/{}/contatore/').format(place[chat_id], nome), '0/',
            ('/filters/{}/{}/contatore/').format(place[chat_id], nome), '0')
    keyboard = InlineKeyboardMarkup(inline_keyboard=[
        [dict(text='Feedback di Google', callback_data=1),
         dict(text='Feedback di FindEAT', callback_data=7)]
    ])

    contatore = db.child(
        ('/filters/{}/{}/contatore/0').format(place[chat_id], nome)).get()
    cartadicredito = db.child(
        ('/filters/{}/{}/cartadicredito/votisi').format(place[chat_id], nome)).get()
    cartadicredito1 = db.child(
        ('/filters/{}/{}/cartadicredito/votino').format(place[chat_id], nome)).get()
    costo = db.child((
        '/filters/{}/{}/costo/alto').format(place[chat_id], nome)).get()
    costo1 = db.child((
        '/filters/{}/{}/costo/basso').format(place[chat_id], nome)).get()
    celiaci = db.child((
        '/filters/{}/{}/menu/0/celiaci/votisi').format(place[chat_id], nome)).get()
    celiaci1 = db.child((
        '/filters/{}/{}/menu/0/celiaci/votino').format(place[chat_id], nome)).get()
    bambino = db.child((
        '/filters/{}/{}/menu/1/bambino/votisi').format(place[chat_id], nome)).get()
    bambino1 = db.child((
        '/filters/{}/{}/menu/1/bambino/votino').format(place[chat_id], nome)).get()
    if apertura == 'Aperto' or apertura == None:
        text = ("🍽{}\n🕐{}\n📱{}\n⭐️{}\n\nFeedback by FindEAT\nCercato {} volte\n💳 POS: {} Si, {} No\n").format(
            json_data['lista'][0]['nome'], str(apertura).replace('None', 'Apertura non disponibile'), str(numtell), str(valutazione), str(contatore.val()).replace("None", "0"), str(cartadicredito.val()).replace("None", "0"), str(cartadicredito1.val()).replace("None", "0"))

        text1 = ("💸 Locale Costoso: {} Si, {} No\n📒 Menu senza glutine: {} Si, {} No\n👶🏼 Menu per bambini: {} Si, {} No").format(str(costo.val()).replace("None", "0"), str(costo1.val()).replace("None", "0"),
                                                                                                                                 str(celiaci.val()).replace("None", "0"), str(celiaci1.val()).replace("None", "0"), str(bambino.val()).replace("None", "0"), str(bambino1.val()).replace("None", "0"))
        bot.sendMessage(chat_id, text+text1)
    else:

        text = ("🍽{}\n🕐{}\n📱{}\n⭐️{}\n------\n{}\n\nFeedback by FindEAT\nCercato {} volte\n💳 POS: {} Si, {} No\n").format(
            json_data['lista'][0]['nome'], str(apertura).replace('None', 'Apertura non disponibile'), str(numtell), str(valutazione), orari, str(contatore.val()).replace("None", "0"), str(cartadicredito.val()).replace("None", "0"), str(cartadicredito1.val()).replace("None", "0"))

        text1 = ("💸 Locale Costoso: {} Si, {} No\n📒 Menu senza glutine: {} Si, {} No\n👶🏼 Menu per bambini: {} Si, {} No").format(str(costo.val()).replace("None", "0"), str(costo1.val()).replace("None", "0"),
                                                                                                                                 str(celiaci.val()).replace("None", "0"), str(celiaci1.val()).replace("None", "0"), str(bambino.val()).replace("None", "0"), str(bambino1.val()).replace("None", "0"))

        bot.sendMessage(chat_id, text+text1)

    bot.sendLocation(chat_id, posizione[0], posizione[1])
    bot.sendMessage(
        chat_id, "Hai bisogno di aiuto?", reply_markup=keyboard)
    user_state[chat_id] = 0


def counter(initial, opa, setDB, increase):
    a = db.child(initial + opa).get()

    if increase == opa[:-1]:
        try:
            y = int(a.val())+1
        except:
            y = 1

    data = {opa[:-1]: y}
    db.child(setDB).set(data)


def filtri(initial, opa, opb, setDB, increase):

    a = db.child(initial + opa).get()
    b = db.child(initial + opb).get()

    if increase == opa[:-1]:
        try:
            y = int(a.val())+1
            n = b.val()
        except:
            y = 1
            n = 0
    else:
        try:
            n = int(b.val())+1
            y = a.val()
        except:
            n = 1
            y = 0

    data = {opa[:-1]: y, opb[:-1]: n}
    db.child(setDB).set(data)


def cerca(luogo, msg):
    content_type, chat_type, chat_id = telepot.glance(msg)
    r = requests.get(
        url=url_api + '?tipo=luogo&lista=' + luogo.lower())
    try:
        json_data = r.json()
        place[chat_id] = luogo.lower()

        array = ''
        array1 = ''
        for i in range(5):
            nome = json_data['lista'][i]['nome']
            placeid = json_data['lista'][i]['id']
            array += str(i + 1) + ': ' + nome + '\n'
            array1 += placeid + ','

        place_id[chat_id] = array1.split(',')

        markup = ReplyKeyboardMarkup(
            keyboard=[["Si", "No"]], resize_keyboard=True)
        bot.sendMessage(
            chat_id, array + "\nIl ristorante è nella lista?", reply_markup=markup)

        user_state[chat_id] = 3
    except:
        bot.sendMessage(
            chat_id, "Locali non trovati/ Errore interno API, riscrivi il nome del luogo")


def feedback_def(json_data, query_data):
    lista = json_data['feedback'][int(query_data)-1]
    autore = lista['author_name']
    valutazione = str(lista['rating'])
    commento = lista['text']
    feedback = ('Autore: {}\nValutazione: {}⭐\nFeedback: {}').format(
        autore, valutazione, commento)

    return feedback


def on_callback_query(msg):
    query_id, from_id, query_data = telepot.glance(
        msg, flavor='callback_query')
    edited = (from_id, msg['message']['message_id'])
    try:

        file = open("__pycache__/"+str(from_id)+".json", "r")
        json_data = json.load(file)

        if (query_data == str(1)):

            keyboard = InlineKeyboardMarkup(inline_keyboard=[
                [dict(text='Avanti', callback_data=2)]

            ])

            bot.editMessageText(edited, feedback_def(json_data, query_data),
                                reply_markup=keyboard)

        if (query_data == str(2)):
            keyboard = InlineKeyboardMarkup(inline_keyboard=[
                [dict(text='Indietro', callback_data=1),
                 dict(text='Avanti', callback_data=3)]
            ])

            bot.editMessageText(edited, feedback_def(json_data, query_data),
                                reply_markup=keyboard)

        if (query_data == str(3)):
            keyboard = InlineKeyboardMarkup(inline_keyboard=[
                [dict(text='Indietro', callback_data=2),
                 dict(text='Avanti', callback_data=4)]
            ])

            bot.editMessageText(edited, feedback_def(json_data, query_data),
                                reply_markup=keyboard)

        if (query_data == str(4)):
            keyboard = InlineKeyboardMarkup(inline_keyboard=[
                [dict(text='Indietro', callback_data=3),
                 dict(text='Avanti', callback_data=5)]
            ])

            bot.editMessageText(edited, feedback_def(json_data, query_data),
                                reply_markup=keyboard)

        if (query_data == str(5)):
            keyboard = InlineKeyboardMarkup(inline_keyboard=[
                [dict(text='Indietro', callback_data=4),
                 dict(text='Fine', callback_data=6)]
            ])

            bot.editMessageText(edited, feedback_def(json_data, query_data),
                                reply_markup=keyboard)

        if (query_data == str(6)):
            bot.editMessageText(edited, 'Fine')
            keyboard = InlineKeyboardMarkup(inline_keyboard=[
                [dict(text='Feedback di Google', callback_data=1),
                 dict(text='Feedback di FindEAT', callback_data=7)]
            ])
            bot.editMessageText(
                edited, "Hai bisogno di aiuto?", reply_markup=keyboard)

        if (query_data == str(7)):
            result = db.child(
                '/restaurants/'+place[from_id] + '/'+restaurant[from_id] + '/').get()
            if result.val() == None:
                keyboard = InlineKeyboardMarkup(inline_keyboard=[
                    [dict(text='Scrivi una recensione', callback_data=100)]
                ])
                bot.editMessageText(
                    edited, 'Nessuna recensione!', reply_markup=keyboard)
            else:
                keyboard = InlineKeyboardMarkup(inline_keyboard=[
                    [dict(text='Visualizza recensioni', callback_data=8),
                     dict(text='Scrivi una recensione', callback_data=100)]
                ])
                i = len(result.val())
                if i == 1:
                    bot.editMessageText(
                        edited, str(i) + ' recensione trovata', reply_markup=keyboard)
                else:
                    bot.editMessageText(
                        edited, str(i) + ' recensioni trovate', reply_markup=keyboard)

        if (query_data == str(8)):
            result = db.child(
                '/restaurants/'+place[from_id] + '/'+restaurant[from_id] + '/').get()

            j = len(result.val())

            feedback = ''
            for i in range(int(j)):
                unix_timestamp = result.val()[i]['time']
                utc_time = time.gmtime(unix_timestamp)
                local_time = time.localtime(unix_timestamp)
                feedback = feedback +\
                    str(i+1)+') Autore: '+result.val()[i]['author_name'] + \
                    '\nData: ' + str(time.strftime("%d/%m/%Y", local_time)) + \
                    '\nFeedback: ' + result.val()[i]['text'] + '\n\n'

            keyboard = InlineKeyboardMarkup(inline_keyboard=[
                [dict(text='Feedback di Google', callback_data=1),
                 dict(text='Feedback di FindEAT', callback_data=7)]
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
