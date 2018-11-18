from flask import Flask,jsonify,request
from pymongo import MongoClient
from bson.json_util import dumps
from bson.objectid import ObjectId
import bcrypt,secrets,datetime

TOKEN_LEN=32	#how many bytes (characters? citation needed) required for login token

app=Flask(__name__)
db=MongoClient(serverSelectionTimeoutMS=1)
db.server_info()
db=db.ChessboardNet

@app.route("/")
def hello_world():
	returnme=db.test.find_one()
	return app.response_class(response=dumps(returnme),status=200,mimetype="application/json")

@app.route("/signup",methods=["POST"])
def create_user():
	try:
		username=request.json.get("username")
		password=request.json.get("password")
		passhash=bcrypt.hashpw(password.encode(),bcrypt.gensalt())
		userid=db.users.insert({"username":username,"passhash":passhash})
		login_token=secrets.token_urlsafe(TOKEN_LEN)
		expires=datetime.datetime.now()+datetime.timedelta(days=365)
		db.user_tokens.insert({"_id":login_token,"user_id":userid,"expires":expires})
		return jsonify({"status":"0","token":login_token,"id":str(userid)})
	except:
		return jsonify({"status":"-1"})

@app.route("/signin",methods=["POST"])
def get_login_token():
	#0 success
	#1 fail
	#-1 exception

	try:
		username=request.json.get("username")
		password=request.json.get("password")
		userdetails=db.users.find_one({"username":username})
		if bcrypt.checkpw(password.encode(),userdetails["passhash"]):
			login_token=secrets.token_urlsafe(TOKEN_LEN)
			expires=datetime.datetime.now()+datetime.timedelta(days=365)
			db.user_tokens.insert({"_id":login_token,"user_id":userdetails["_id"],"expires":expires})
			return jsonify({"status":"0","id":str(userdetails["_id"]),"token":login_token})
		else:
			return jsonify({"status":"1"})
	except:
		return jsonify({"status":"-1"})

@app.route("/signout",methods=["POST"])
def destroy_login_token():
	token=request.json.get("token")
	db.user_tokens.remove({"_id":token})
	return jsonify({"status":0})


lobby={}	#in the from {"uid","last time they sent a request"}
@app.route("/lobby",methods=["POST"])
def lobby():
	'''add player to the user to the lobby for 60 seconds'''
	#remove players from queue if they stop sending join requests
	for person in lobby:
		if lobby[person]<datetime.datetime.now()-datetime.timedelta(seconds=60):
			lobby.pop(person)

	#add player to lobby
	token=request.json.get("token")
	user=db.user_tokens.find_one({"_id":token})
	userid=user["user_id"]
	if db.ongoing_matches.find_one({"players":{"$in":userid}}):	#check they are not already in match
		return jsonify({"status":1})
	lobby[userid]=datetime.datetime.now()

	#Match players up
	while len(lobby)>1:
		p1=lobby.popitem()[0]
		p2=lobby.popitem()[0]

		lowest=99999999	#nice big number, intmax would be better
		for server in db.servers.distinct("hostname"):
			players_on_server=db.ongoing_matches.count({"server":server})
			if players_on_server<lowest:
				lowest=players_on_server
				freeServer=server
		db.ongoing_matches.insert({"players":[p1,p2],"server":server})

	return jsonify({"status":0})

@app.route("/getmatch",methods=["POST"])
def getmatch():
	try:
		token=request.json.get("token")
		user=db.user_tokens.find_one({"_id":token})
		userid=user["user_id"]

		returnme=db.ongoing_matches.find_one({"players":{"$in":[userid]}})
		if returnme==None:
			return jsonify({"status":1})
		else:
			returnme["status"]=0

			returnme["players"].remove(userid)
			returnme["opponent"]=str(returnme["players"][0])
			returnme.pop("players")

			returnme.pop("_id")

			return app.response_class(response=dumps(returnme),status=200,mimetype="application/json")
	except:
		return jsonify({"status":-1})


@app.route("/userinfo",methods=["POST"])
def getUserInfo():
	try:
		token=request.json.get("token")
		userid=ObjectId(request.json.get("userid"))

		if not db.user_tokens.find_one({"_id":token}):
			return jsonify({"status":1,"message":"Access Prohibited"})

		else:
			user=db.users.find_one({"_id":userid})
			user.pop("passhash")
			user["userid"]=str(user.pop("_id"))
			user["status"]=0
			return app.response_class(response=dumps(user),status=200,mimetype="application/json")
	except:
		return jsonify({"status":-1})
