# !/usr/bin/env python
# -*- coding: utf-8 -*-
import copy
import mysql.connector
import sys
import web
import json
import datetime

HOST = "127.0.0.1"
USER = "root"
PASSWORD = "root"
DATABASE = "bd_simulator"

urls = (
    '/get_camions/', 'list_camions',
    '/get_incendie/', 'list_incendie',
    '/get_intervention/', 'list_intervention',
    '/new_incendie/', 'insert_incendie',
    '/update_incendie/', 'update_incendie',
    '/deploie_intervention_to_simulator/', 'deploie_intervention_to_simulator',
    '/incendieLinkIntervention_for_simulator/', 'incendieLinkIntervention_for_simulator',
    '/deplacement_camion_for_simulator/', 'deplacement_camion_for_simulator',
    '/get_camion_intervenant_from_emergency/', 'get_camion_intervenant_from_emergency',
    '/get_incendie_camion_intervention_from_emergency/', 'get_incendie_camion_intervention_from_emergency',
    '/list_new_incendie_for_emergency/', 'list_new_incendie_for_emergency',
    '/camionsNoIntervention/', 'camionsNoIntervention',
    '/newIntervention/', 'newIntervention',
    '/camionsIntervenant/', 'camionsIntervenant',
    '/get_list_camion_intervenant/', 'get_list_camion_intervenant',
    '/get_list_incendie_with_intervention/', 'get_list_incendie_with_intervention',
    '/edit_intervention/', 'edit_intervention',
    '/get_coordcaserne/?(.*)', 'get_coordcaserne',
    '/deplacement_camion/', 'deplacement_camion',
    '/retour_camions/', 'retour_camions',
    '/incendieLinkIntervention/', 'incendieLinkIntervention',
    '/get_current_intervention/', 'get_current_intervention',
    '/edit_incendie/', 'edit_incendie',
    '/get_interventions_from_emergency/', 'get_interventions_from_emergency',
    '/get_incendie_from_emergency/', 'get_incendie_from_emergency',
    '/get_camions_intervenant_from_emergency/', 'get_camions_intervenant_from_emergency',

)

app = web.application(urls, globals())


def query_db(cursor, query, args=(), one=False):
    cursor.execute(query, args)
    dicts = {}
    dicts2 = {}
    j = 0
    for row in cursor.fetchall():
        # print(row)
        for i, value in enumerate(row):
            # print(i)
            # print(value)
            dicts[cursor.description[i][0]] = value.isoformat() if (
                    isinstance(value, datetime.datetime) and value is not None) else value
        # print(dicts)
        dicts2[j] = copy.copy(dicts)
        j += 1
    return dicts2


def query_select(ma_requete):
    try:
        conn = mysql.connector.connect(host=HOST, user=USER, password=PASSWORD, database=DATABASE)
        cursor = conn.cursor()
        my_query = query_db(cursor, ma_requete)
        json_output = json.dumps(my_query)
        # print(json_output)
        return json_output

    except mysql.connector.errors.InterfaceError as e:
        print("Error %d: %s" % (e.args[0], e.args[1]))
        sys.exit(1)

    finally:
        # On ferme la connexion
        if conn:
            conn.close()


def query_other(ma_requete, multiValue=False):
    try:
        conn = mysql.connector.connect(host=HOST, user=USER, password=PASSWORD, database=DATABASE)
        cursor = conn.cursor()
        try:
            cursor.execute(ma_requete, multi=multiValue)
            conn.commit()
            return "Requete effectuee avec succes"
        except (mysql.connector.Error, mysql.connector.Warning) as e:
            # En cas d'erreur on annule les modifications
            conn.rollback()
            return e

    except mysql.connector.errors.InterfaceError as e:
        print("Error %d: %s" % (e.args[0], e.args[1]))
        sys.exit(1)

    finally:
        # On ferme la connexion
        if conn:
            conn.close()


# -----------------------------


def query_select_for_emergency(ma_requete):
    try:
        conn = mysql.connector.connect(host=HOST, user=USER, password=PASSWORD, database='bd_emergency')
        # print("on est connecter")
        cursor = conn.cursor()
        my_query = query_db(cursor, ma_requete)
        # print(my_query)
        json_output = json.dumps(my_query)
        # print(json_output)
        return json_output

    except mysql.connector.errors.InterfaceError as e:
        print("Error %d: %s" % (e.args[0], e.args[1]))
        sys.exit(1)

    finally:
        # On ferme la connexion
        if conn:
            conn.close()


def query_other_for_emergency(ma_requete, multiValue=False):
    try:
        conn = mysql.connector.connect(host=HOST, user=USER, password=PASSWORD, database='bd_emergency')
        cursor = conn.cursor()
        try:
            cursor.execute(ma_requete, multi=multiValue)
            conn.commit()
            return "Requete effectuee avec succes"
        except (mysql.connector.Error, mysql.connector.Warning) as e:
            # En cas d'erreur on annule les modifications
            conn.rollback()
            return e

    except mysql.connector.errors.InterfaceError as e:
        print("Error %d: %s" % (e.args[0], e.args[1]))
        sys.exit(1)

    finally:
        # On ferme la connexion
        if conn:
            conn.close()


# ------------------------------

class list_camions:
    def GET(self):
        return query_select("""SELECT * FROM camion""")


class list_incendie:
    def GET(self):
        web.header('Access-Control-Allow-Origin', '*')
        return query_select("""SELECT * FROM incendie where intensite !=0""")


class list_intervention:
    def GET(self):
        return query_select("""SELECT * FROM intervention""")


class insert_incendie:
    def POST(self):
        sql_insert = "INSERT INTO `incendie`( `intensite`, `longitude`, `latitude`, `debut_incendie`)	VALUES"
        data = json.loads(web.data().decode("utf-8"))
        return_value = " \n "
        return_value2 = " \n "
        for incendie in data["incendie"]:
            values = ""
            if 'intensite' in incendie:
                values += "(" + str(incendie['intensite']) + ","
            else:
                values += "(null,"

            if 'longitude' in incendie:
                values += str(incendie['longitude']) + ","
            else:
                values += "null,"

            if 'latitude' in incendie:
                values += str(incendie['latitude']) + ","
            else:
                values += "null,"

            if 'debutIncendie' in incendie:
                values += "'" + incendie['debutIncendie'] + "'" + ")"
            else:
                values += "null)"
            print(sql_insert + values)
            return_value += str(query_other(str(sql_insert) + str(values), False)) + " \n "
            return_value2 += str(query_other_for_emergency(str(sql_insert) + str(values), False)) + " \n "
        print(return_value)
        return return_value


class deplacement_camion_for_simulator:
    def POST(self):
        data = json.loads(web.data().decode("utf-8"))
        sql = ""
        # return_value = " \n "
        return_value2 = " \n "
        for camion in data["camions"]:
            sql_update = " UPDATE camion SET "
            if 'longitude' in camion:
                sql_update += " longitude=" + str(camion['longitude'])

            if 'latitude' in camion:
                sql_update += " ,latitude=" + str(camion['latitude'])

            if 'id_intervention' in camion:
                sql_update += " , id_intervention=" + str(camion['id_intervention'])

            if 'id_camion' in camion:
                sql_update += " WHERE id_camion=" + str(camion['id_camion']) + "; "
            sql += sql_update
        # return_value += str(query_other(sql, True)) + " \n "
        print("simulator")
        # print(sql)
        return_value2 += str(query_other(sql, True)) + " \n "  # just for testing
        print(return_value2)
        return return_value2


class edit_incendie:
    def POST(self):
        data = json.loads(web.data().decode("utf-8"))
        # print(data)
        sql = ""
        return_value = " \n "
        return_value2 = " \n "
        for incendie in data["incendies"]:
            sql_update = " UPDATE incendie SET "
            if 'intensite' in incendie:
                sql_update += " intensite=" + str(incendie['intensite'])

            if 'fin_incendie' in incendie and incendie['fin_incendie'] != 'null':
                sql_update += " ,fin_incendie='" + str(incendie['fin_incendie']) + "'"

            if 'id_incendie' in incendie:
                sql_update += " WHERE id_incendie=" + str(incendie['id_incendie'])
            sql += sql_update
            print(sql_update)
            return_value += str(query_other(sql_update, False)) + " \n "
            return_value2 += str(query_other_for_emergency(sql_update, False)) + " \n "  # just for testing

        return return_value2


class edit_intervention:
    def POST(self):
        data = json.loads(web.data().decode("utf-8"))
        # print(data)
        return_value = " \n "
        return_value2 = " \n "
        sql_update = " UPDATE intervention SET "
        if 'fin_intervention' in data:
            sql_update += " fin_intervention='" + str(data['fin_intervention']) + "'"
            sql_update += " where id_intervention=" + str(data['id_intervention'])
            # print(sql_update)
            return_value += str(query_other(sql_update, False)) + " \n "
            return_value2 += str(query_other_for_emergency(sql_update, False)) + " \n "  # just for testing
            return return_value2
        return "error"


class deploie_intervention_to_simulator:
    def POST(self):
        data = json.loads(web.data().decode("utf-8"))
        return_value = ""
        if 'id_intervention' in data and 'debut_intervention' in data:
            sql_insert = "INSERT INTO `intervention`(`id_intervention`,`debut_intervention`)VALUES "
            sql_insert += "(" + str(data["id_intervention"]) + ", '" + data["debut_intervention"] + "')"
            return_value += str(query_other(sql_insert, False))
            return return_value
        return "error"


class incendieLinkIntervention_for_simulator:
    def POST(self):
        data = json.loads(web.data().decode("utf-8"))
        if 'id_intervention' in data and 'id_incendie' in data:
            sql_update = "UPDATE incendie SET id_intervention = " + str(
                data["id_intervention"]) + " where id_incendie= " + \
                         str(data["id_incendie"])
            return_value = str(query_other(sql_update, False))
            return return_value
        return "error"


class get_camion_intervenant_from_emergency:
    def GET(self):
        return query_select_for_emergency("""SELECT * FROM camion where id_intervention is not null""")


class synchroniseCamionsForSimulator_from_emergency:
    def GET(self):
        return query_select_for_emergency("""SELECT * FROM camion where id_intervention is not null""")


'''
-----------------------------
for emergency 
-----------------------------
'''


# On recupere les données from simulator to emergency
def recup_data():
    try:
        conn = mysql.connector.connect(host=HOST, user=USER, password=PASSWORD, database=DATABASE)
        cursor = conn.cursor()
        cursor.execute("""INSERT INTO bd_emergency.intervention SELECT * FROM bd_simulator.intervention""")
        # print(cursor.fetchall)
        # json_output = json.dumps(my_query)
        # print(json_output)
        # return json_output
    except mysql.connector.errors.InterfaceError as e:
        print("Error %d: %s" % (e.args[0], e.args[1]))
        sys.exit(1)

    finally:
        # On ferme la connexion
        if conn:
            conn.close()


class get_incendie_camion_intervention_from_emergency:
    def GET(self):
        print('dans get')
        return query_select_for_emergency("""SELECT * FROM incendie where id_intervention=null""")


class list_new_incendie_for_emergency:
    def GET(self):
        return query_select_for_emergency("""SELECT * FROM incendie where intensite !=0 and id_intervention is null""")


class camionsNoIntervention:
    def GET(self):
        return query_select_for_emergency("""SELECT * FROM camion where id_intervention is null""")


class newIntervention:
    def POST(self):
        sql_insert = "INSERT INTO `intervention`( `debut_intervention`)	VALUES "
        data = json.loads(web.data().decode("utf-8"))
        return_value = ""
        if 'debut_intervention' in data:
            sql_insert += " ('" + data["debut_intervention"] + "')"
            return_value += str(query_other_for_emergency(sql_insert, False))
            return query_select_for_emergency("""SELECT MAX(id_intervention) FROM intervention""")

        return "error"


class incendieLinkIntervention:
    def POST(self):
        data = json.loads(web.data().decode("utf-8"))
        if 'id_intervention' in data and 'id_incendie' in data:
            sql_update = "UPDATE incendie SET id_intervention = " + str(
                data["id_intervention"]) + " where id_incendie= " + \
                         str(data["id_incendie"])
            return_value = str(query_other_for_emergency(sql_update, False))
            return return_value
        return "error"


class camionsIntervenant:
    def POST(self):
        data = json.loads(web.data().decode("utf-8"))
        return_value = ""
        for camion in data["camion"]:
            if 'id_camion' in camion:
                id_camion = camion["id_camion"]
                if 'id_intervention' in camion:
                    id_intervention = camion["id_intervention"]
                    sql_insert = "UPDATE camion  SET id_intervention =" + str(
                        id_intervention) + " WHERE id_camion=" + str(id_camion)
                    return_value += str(query_other_for_emergency(sql_insert, False))
        return return_value


class get_list_camion_intervenant:
    def GET(self):
        return query_select_for_emergency("""SELECT * FROM camion where id_intervention is not null""")


class get_list_incendie_with_intervention:
    def GET(self):
        return query_select_for_emergency(
            """SELECT * FROM incendie where id_intervention is not null and fin_incendie is null""")


class get_coordcaserne:
    def GET(self, args):
        user_data = web.input()
        id = user_data.id
        return query_select_for_emergency("SELECT *  FROM caserne where id_caserne= " + str(id))


class get_current_intervention:
    def GET(self):
        return query_select_for_emergency("""SELECT * FROM intervention where fin_intervention is null""")


class get_interventions_from_emergency:
    def GET(self):
        # web.header('Access-Control-Allow-Origin', '*')
        return query_select_for_emergency(
            """SELECT * FROM intervention, incendie WHERE fin_intervention is null and intervention.id_intervention=incendie.id_intervention and fin_incendie is null """)


class deplacement_camion:
    def POST(self):
        print("emergency")
        data = json.loads(web.data().decode("utf-8"))
        sql = ""
        # return_value = " \n "
        return_value2 = " \n "
        for camion in data["camions"]:
            sql_update = " UPDATE camion SET "
            if 'longitude' in camion:
                sql_update += " longitude=" + str(camion['longitude']) + ","

            if 'id_intervention' in camion:
                print(camion['id_intervention'])
                sql_update += " id_intervention=" + str(camion['id_intervention']) + ","

            if 'latitude' in camion:
                sql_update += " latitude=" + str(camion['latitude'])

            if 'id_camion' in camion:
                sql_update += " WHERE id_camion=" + str(camion['id_camion'])
            print(sql_update)
            sql += sql_update
            return_value2 += str(query_other_for_emergency(sql, False)) + " \n "
        # return_value += str(query_other(sql, True)) + " \n "
        # print(sql)
        # just for testing
        print(return_value2)
        return return_value2


class retour_camions:
    def GET(self):
        # web.header('Access-Control-Allow-Origin', '*')
        resut_emergency = query_other_for_emergency(
            """ UPDATE camion SET id_intervention= null WHERE id_intervention in (SELECT id_intervention FROM intervention where fin_intervention is not null) """)
        resut_simulator = query_other(
            """ UPDATE camion SET id_intervention= null WHERE id_intervention in (SELECT id_intervention FROM intervention where fin_intervention is not null) """)
        return resut_emergency


class get_incendie_from_emergency:
    def GET(self):
        web.header('Access-Control-Allow-Origin', 'http://localhost:8082')
        return query_select_for_emergency("""SELECT * FROM incendie where intensite !=0""")


class get_camions_intervenant_from_emergency:
    def GET(self):
        web.header('Access-Control-Allow-Origin','http://localhost:8082')
        return query_select_for_emergency("""SELECT * FROM camion where id_intervention is not null""")


if __name__ == '__main__':
    print("Démarrage du service simulator")
    app.run()
