from firebase import firebase
from settings import token, start_msg, client_file, url_api
from telepot.loop import MessageLoop
from telepot.namedtuple import ReplyKeyboardMarkup, ReplyKeyboardRemove, InlineKeyboardMarkup, InlineKeyboardButton
from time import sleep
import time
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
            coordinates = (str(msg['location']['latitude']),
                           str(msg['location']['longitude'])),
            a = reverse_geocode.search(coordinates)[0]['city']

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
        richiesta(url_api + "?tipo=diretto&lista=" +
                  msg['text'] + ' ' + place[chat_id], msg)

    elif user_state[chat_id] == 6:

        try:
            author = msg['from']['first_name'] + ' ' + msg['from']['last_name']
        except:
            author = msg['from']['first_name']

        result = firebase.get(
            '/restaurants/'+place[chat_id] + '/'+restaurant[chat_id] + '/', None)
        try:
            datetime = int(time.time())
            i = len(result)
            result = firebase.patch('/restaurants/'+place[chat_id] + '/'+restaurant[chat_id] + '/'+str(i)+'/',
                                    {'author_name': author, 'text': msg['text'], 'time': datetime })
            bot.sendMessage(
                chat_id, 'Grazie per aver recensito ' + restaurant[chat_id] + ', questa è la ' +
                str(i+1) + ' recensione fatta 😄')

            markup = ReplyKeyboardMarkup(keyboard=[["Si", "No"]])

            bot.sendMessage(
                chat_id, 'Si può pagare con carte di credito?', reply_markup=markup)

            user_state[chat_id] = 7

        except:
            datetime = int(time.time())
            i = 0
            result = firebase.patch('/restaurants/'+place[chat_id] + '/'+restaurant[chat_id] + '/'+str(i)+'/',
                                    {'author_name': author, 'text': msg['text'], 'time': datetime})
            bot.sendMessage(
                chat_id, 'Complimenti! Hai creato la prima recensione per ' + restaurant[chat_id])

            markup = ReplyKeyboardMarkup(keyboard=[["Si", "No"]])

            bot.sendMessage(
                chat_id, 'Abbiamo altre domande per te!!!👍 \nSi può pagare con carte di credito?', reply_markup=markup)
            user_state[chat_id] = 7

    elif user_state[chat_id] == 7:
        if msg['text'] == 'Si':
            filtri('/filters/'+place[chat_id] + '/'+restaurant[chat_id] + '/cartadicredito/votisi',
                   '/filters/'+place[chat_id] + '/'+restaurant[chat_id] + '/cartadicredito/', 'votisi', {'votisi': 0, 'votino': 0})

        if msg['text'] == 'No':

            filtri('/filters/'+place[chat_id] + '/'+restaurant[chat_id] + '/cartadicredito/votino',
                   '/filters/'+place[chat_id] + '/'+restaurant[chat_id] + '/cartadicredito/', 'votino', {'votisi': 0, 'votino': 0})

        markup = ReplyKeyboardMarkup(keyboard=[["Si", "No"]])

        bot.sendMessage(
            chat_id, 'Il ristorante è costoso?', reply_markup=markup)

        user_state[chat_id] = 8

    elif user_state[chat_id] == 8:
        if msg['text'] == 'Si':
            filtri('/filters/'+place[chat_id] + '/'+restaurant[chat_id] + '/costo/alto',
                   '/filters/'+place[chat_id] + '/'+restaurant[chat_id] + '/costo/', 'alto', {'alto': 0, 'basso': 0})

        if msg['text'] == 'No':

            filtri('/filters/'+place[chat_id] + '/'+restaurant[chat_id] + '/costo/basso',
                   '/filters/'+place[chat_id] + '/'+restaurant[chat_id] + '/costo/', 'basso', {'alto': 0, 'basso': 0})

        markup = ReplyKeyboardMarkup(keyboard=[["Si", "No"]])
        bot.sendMessage(
            chat_id, 'Il ristorante prevede un menu senza glutine?', reply_markup=markup)
        user_state[chat_id] = 9

    elif user_state[chat_id] == 9:
        if msg['text'] == 'Si':
            filtri('/filters/'+place[chat_id] + '/'+restaurant[chat_id] + '/menu/0/celiaci/votisi',
                   '/filters/'+place[chat_id] + '/'+restaurant[chat_id] + '/menu/0/celiaci', 'votisi', {'votisi': 0, 'votino': 0})

        if msg['text'] == 'No':
            filtri('/filters/'+place[chat_id] + '/'+restaurant[chat_id] + '/menu/0/celiaci/votino',
                   '/filters/'+place[chat_id] + '/'+restaurant[chat_id] + '/menu/0/celiaci', 'votino', {'votisi': 0, 'votino': 0})

        markup = ReplyKeyboardMarkup(keyboard=[["Si", "No"]])
        bot.sendMessage(
            chat_id, 'Il ristorante prevede un menu per bambini?', reply_markup=markup)
        user_state[chat_id] = 10

    elif user_state[chat_id] == 10:
        if msg['text'] == 'Si':
            filtri('/filters/'+place[chat_id] + '/'+restaurant[chat_id] + '/menu/1/bambino/votisi',
                   '/filters/'+place[chat_id] + '/'+restaurant[chat_id] + '/menu/1/bambino', 'votisi', {'votisi': 0, 'votino': 0})

        if msg['text'] == 'No':
            filtri('/filters/'+place[chat_id] + '/'+restaurant[chat_id] + '/menu/1/bambino/votino',
                   '/filters/'+place[chat_id] + '/'+restaurant[chat_id] + '/menu/1/bambino', 'votino', {'votisi': 0, 'votino': 0})

        bot.sendMessage(
            chat_id, 'Grazie per aver risposto alle domande!!!🤞🏻🤝😄 ', reply_markup=ReplyKeyboardRemove(
                remove_keyboard=True))
        user_state[chat_id]=0


def richiesta(url, msg):
    content_type, chat_type, chat_id=telepot.glance(msg)

    try:
        r=requests.get(
            url = url)
        json_data=r.json()
        url_salvato[chat_id]=url

        nome=json_data['lista'][0]['nome']
        orari=json_data['orari'][0]
        posizione=json_data['lista'][0]['posizione'].split(',')
        apertura=json_data['lista'][0]['apertura']
        numtell=json_data['lista'][0]['numtell']
        valutazione=json_data['lista'][0]['valutazione']

        if orari == None:
            orari='Orari non disponibili'
        else:
            orari='\n'.join(orari)

        restaurant[chat_id]=nome

        filtri('/filters/'+place[chat_id] + '/'+restaurant[chat_id] + '/contatore/0',
               '/filters/'+place[chat_id] + '/'+restaurant[chat_id] + '/contatore/', '0', {'0': 0})

        keyboard=InlineKeyboardMarkup(inline_keyboard = [
            [dict(text='Feedback di Google', callback_data=1),
             dict(text='Feedback di FindEAT', callback_data=7)]
        ])

        contatore=firebase.get('/filters/'+place[chat_id] + '/' +
                                 restaurant[chat_id] + '/contatore/0', None)
        cartadicredito=firebase.get(
            '/filters/'+place[chat_id] + '/' + restaurant[chat_id] + '/cartadicredito/votisi', None)
        cartadicredito1=firebase.get(
            '/filters/'+place[chat_id] + '/' + restaurant[chat_id] + '/cartadicredito/votino', None)

        costo=firebase.get(
            '/filters/'+place[chat_id] + '/' + restaurant[chat_id] + '/costo/basso', None)
        costo1=firebase.get(
            '/filters/'+place[chat_id] + '/' + restaurant[chat_id] + '/costo/alto', None)

        celiaci=firebase.get(
            '/filters/'+place[chat_id] + '/' + restaurant[chat_id] + '/menu/0/celiaci/votisi', None)
        celiaci1=firebase.get(
            '/filters/'+place[chat_id] + '/' + restaurant[chat_id] + '/menu/0/celiaci/votino', None)

        bambino=firebase.get(
            '/filters/'+place[chat_id] + '/' + restaurant[chat_id] + '/menu/1/bambino/votisi', None)
        bambino1=firebase.get(
            '/filters/'+place[chat_id] + '/' + restaurant[chat_id] + '/menu/1/bambino/votino', None)

        if apertura == 'Aperto' or apertura == None:
            bot.sendMessage(chat_id, '🍽 ' + nome + '\n🕐 ' + str(apertura).replace('None', 'Apertura non disponibile') +
                            '\n📱 ' + str(numtell) + '\n⭐️ ' + str(valutazione) + "\n\nFeedback by FindEAT\nCercato " +
                            str(contatore).replace("None", "Non recensito")+" volte" + "\n💳 Carta di credito: Voti si: " + str(cartadicredito).replace("None", "Non recensito") + ", Voti no: " + str(cartadicredito1).replace("None", "Non recensito") +
                            "\n💸 Costoso: Basso: " + str(costo) + ", Alto: " + str(costo1) +
                            "\n📒 Menù glutenfree: Voti si: " + str(celiaci).replace("None", "Non recensito") + ", Voti no: " + str(celiaci1).replace("None", "Non recensito") +
                            "\n👶🏼 Menù per bambini: Voti si: " +
                            str(bambino).replace("None", "Non recensito") + ", Voti no: " + str(bambino1).replace("None", "Non recensito"))

        else:
            bot.sendMessage(chat_id, '🍽 ' + nome + '\n🕐 ' + str(apertura).replace('None', 'Apertura non disponibile') +
                            '\n📱 ' + str(numtell) + '\n⭐️ ' + str(valutazione) + '\n------\n' + orari + "\n\nFeedback by FindEAT\nCercato " +
                            str(contatore)+" volte" + "\n💳 Carta di credito: Voti si: " + str(cartadicredito).replace("None", "Non recensito") + ", Voti no: " + str(cartadicredito1).replace("None", "Non recensito") +
                            "\n💸 Costoso: Basso: " + str(costo).replace("None", "Non recensito") + ", Alto: " + str(costo1).replace("None", "Non recensito") +
                            "\n📒 Menù glutenfree: Voti si: " + str(celiaci).replace("None", "Non recensito") + ", Voti no: " + str(celiaci1).replace("None", "Non recensito") +
                            "\n👶🏼 Menù per bambini: Voti si: " +
                            str(bambino).replace("None", "Non recensito") + ", Voti no: " + str(bambino1).replace("None", "Non recensito")

                            )

        bot.sendLocation(chat_id, posizione[0], posizione[1])
        bot.sendMessage(
            chat_id, "Hai bisogno di aiuto?", reply_markup = keyboard)
        user_state[chat_id]=0

    except:
        print("Errore API")


def filtri(get, patch, request, check):

    try:
        result=firebase.get(get, None)

        n=int(result)
        result=firebase.patch(patch, {request: n+1})

    except:
        result=firebase.patch(patch, check)
        result=firebase.get(get, None)

        n=int(result)
        result=firebase.patch(patch, {request: n+1})


def cerca(luogo, msg):
    content_type, chat_type, chat_id=telepot.glance(msg)
    r=requests.get(
        url = url_api + '?tipo=luogo&lista=' + luogo.lower())
    json_data=r.json()
    place[chat_id]=luogo.lower()

    array=''
    array1=''
    for i in range(5):
        nome=json_data['lista'][i]['nome']
        placeid=json_data['lista'][i]['id']
        array += str(i + 1) + ': ' + nome + '\n'
        array1 += placeid + ','

    place_id[chat_id]=array1.split(',')

    markup=ReplyKeyboardMarkup(keyboard = [["Si", "No"]])
    bot.sendMessage(
        chat_id, array + "\nIl ristorante è nella lista?", reply_markup = markup)

    user_state[chat_id]=3


def on_callback_query(msg):
    query_id, from_id, query_data=telepot.glance(
        msg, flavor = 'callback_query')
    edited=(from_id, msg['message']['message_id'])
    try:
        r=requests.get(url = url_salvato[from_id])
        json_data=r.json()

        if (query_data == str(1)):

            lista=json_data['feedback'][int(query_data)-1]
            autore=lista['author_name']
            valurazione=str(lista['rating'])
            commento=lista['text']
            feedback='Autore: ' + autore + '\nValutazione: ' +\
                valurazione + '\nFeedback: ' + commento

            keyboard=InlineKeyboardMarkup(inline_keyboard = [
                [dict(text='Avanti', callback_data=2)]

            ])

            bot.editMessageText(edited, feedback,
                                reply_markup = keyboard)

        if (query_data == str(2)):
            keyboard=InlineKeyboardMarkup(inline_keyboard = [
                [dict(text='Indietro', callback_data=1),
                 dict(text='Avanti', callback_data=3)]
            ])

            lista=json_data['feedback'][int(query_data)-1]
            autore=lista['author_name']
            valurazione=str(lista['rating'])
            commento=lista['text']
            feedback='Autore: ' + autore + '\nValutazione: ' +\
                valurazione + '\nFeedback: ' + commento

            bot.editMessageText(edited, feedback,
                                reply_markup = keyboard)

        if (query_data == str(3)):
            keyboard=InlineKeyboardMarkup(inline_keyboard = [
                [dict(text='Indietro', callback_data=2),
                 dict(text='Avanti', callback_data=4)]
            ])

            lista=json_data['feedback'][int(query_data)-1]
            autore=lista['author_name']
            valurazione=str(lista['rating'])
            commento=lista['text']
            feedback='Autore: ' + autore + '\nValutazione: ' +\
                valurazione + '\nFeedback: ' + commento

            bot.editMessageText(edited, feedback,
                                reply_markup = keyboard)

        if (query_data == str(4)):
            keyboard=InlineKeyboardMarkup(inline_keyboard = [
                [dict(text='Indietro', callback_data=3),
                 dict(text='Avanti', callback_data=5)]
            ])

            lista=json_data['feedback'][int(query_data)-1]
            autore=lista['author_name']
            valurazione=str(lista['rating'])
            commento=lista['text']
            feedback='Autore: ' + autore + '\nValutazione: ' +\
                valurazione + '\nFeedback: ' + commento

            bot.editMessageText(edited, feedback,
                                reply_markup = keyboard)

        if (query_data == str(5)):
            keyboard=InlineKeyboardMarkup(inline_keyboard = [
                [dict(text='Indietro', callback_data=4),
                 dict(text='Fine', callback_data=6)]
            ])

            lista=json_data['feedback'][int(query_data)-1]
            autore=lista['author_name']
            valurazione=str(lista['rating'])
            commento=lista['text']
            feedback='Autore: ' + autore + '\nValutazione: ' +\
                valurazione + '\nFeedback: ' + commento

            bot.editMessageText(edited, feedback,
                                reply_markup = keyboard)

        if (query_data == str(6)):
            bot.editMessageText(edited, 'Fine')

            keyboard=InlineKeyboardMarkup(inline_keyboard = [
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
                unix_timestamp = result[i]['time']
                utc_time = time.gmtime(unix_timestamp)
                local_time = time.localtime(unix_timestamp)
                feedback = feedback +\
                    str(i+1)+') Autore: '+result[i]['author_name'] + \
                    '\nData: '+ str(time.strftime("%d/%m/%Y", local_time)) + '\nFeedback: ' + result[i]['text'] + '\n\n'

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
