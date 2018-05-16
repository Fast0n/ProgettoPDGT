import os
import sys
import telepot
import json
import requests
from settings import token, start_msg, client_file
from time import sleep

# State for user
user_state = {}

def on_chat_message(msg):
    content_type, chat_type, chat_id = telepot.glance(msg)
    command_input = msg['text']

    # Check user state
    try:
        user_state[chat_id] = user_state[chat_id]
    except:
        user_state[chat_id] = 0

    # start command
    if command_input == "/start":
        if register_user(chat_id):
            bot.sendMessage(chat_id, start_msg, parse_mode='Markdown')

    if command_input == '/cerca':
        bot.sendMessage(chat_id, 'Invia la zona del ristorante',
                        parse_mode='Markdown')
        user_state[chat_id] = 1

    elif user_state[chat_id] == 1:

        try:
            r = requests.get(
                url="http://android12.altervista.org/" + command_input.lower() + ".json")
            file = open("file.json", "w")
            file.write(r.text)
            file.close()

            with open("file.json") as json_file:
                json_data = json.load(json_file)

                for i in range(5):
                    nome = json_data['lista'][i]['nome']

                    bot.sendMessage(chat_id, str(i + 1) + ': ' + nome)

        except:
            print("Error URL check README.md")

        bot.sendMessage(chat_id, 'Scrivi il nome/numero del ristorante')
        user_state[chat_id] = 2

    elif user_state[chat_id] == 2:

        try:
            with open("file.json") as json_file:
                json_data = json.load(json_file)

            if command_input.isdigit():
                nome = json_data['lista'][int(command_input) - 1]['nome']
                posizione = json_data['lista'][int(
                    command_input) - 1]['posizione']
                valutazione = json_data['lista'][int(
                    command_input) - 1]['valutazione']
                bot.sendMessage(chat_id, nome)

            else:

                for i in range(5):
                    nome = json_data['lista'][i]['nome']
                    posizione = json_data['lista'][i]['posizione']
                    valutazione = json_data['lista'][i]['valutazione']
                    lista = nome + '\n' + \
                        str(posizione) + '\n' + str(valutazione)

                    s = nome.lower()
                    if s.find(command_input.lower()) != -1:
                        bot.sendMessage(chat_id, lista)
        except:
            print("Error URL check README.md")


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
