from flask import Flask,jsonify,request
from pymongo import MongoClient,ASCENDING
from bson.json_util import dumps
from bson.objectid import ObjectId
import bcrypt,secrets,datetime,socket,versionInfo

TOKEN_LEN=32	#how many bytes (characters? citation needed) required for login token

app=Flask(__name__)
db=MongoClient(serverSelectionTimeoutMS=1)
db.server_info()
db=db.ChessboardNet
db.users.create_index([("username",ASCENDING)],unique=True)

@app.route("/")
def hello_world():
	returnme=db.test.find_one()
	return app.response_class(response=dumps(returnme),status=200,mimetype="application/json")

@app.route("/version")
def get_version():
	return jsonify({"version":versionInfo.version,"tag":versionInfo.tag})

@app.route("/download")
def download_redirect():
	return '<script>window.location.href = "http://%s/chessboard_net_%s.apk"</script>' % (versionInfo.repo,versionInfo.tag)

@app.route("/signup",methods=["POST"])
def create_user():
	try:
		username=request.json.get("username")
		password=request.json.get("password")
		passhash=bcrypt.hashpw(password.encode(),bcrypt.gensalt())
		userid=db.users.insert({"username":username,"passhash":passhash,"favourite_set":"white","secondary_set":"black"})
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
def getFreeServer():
	'''return the server with the lowest number of players currently using it'''
	lowest=99999999	#nice big number, intmax would be better
	for server in db.servers.distinct("_id"):
		players_on_server=db.ongoing_matches.count({"server":server})
		serverCapacity=db.servers.find_one({"_id":server})["capacity"]
		if players_on_server<lowest and serverCapacity>players_on_server:
			lowest=players_on_server
			freeServer=server

	return freeServer

@app.route("/lobby",methods=["POST"])
def joinLobby():
	'''add player to the user to the lobby for 60 seconds'''
	#remove players from queue if they stop sending join requests
	for person in lobby:
		if lobby[person]<datetime.datetime.now()-datetime.timedelta(seconds=60):
			lobby.pop(person)

	#add player to lobby
	token=request.json.get("token")
	user=db.user_tokens.find_one({"_id":token})
	userid=user["user_id"]
	if db.ongoing_matches.find_one({"players":{"$in":[userid]}}):	#check they are not already in match
		return jsonify({"status":1})

	#ensure a server is available
	if (db.servers.count()==0):
		return jsonify({"status":2})

	#if they have a preference on who to play
	opponent=request.json.get("opponent")
	if opponent != None:
		opponent=db.users.find_one({"username":opponent})
		if opponent==None:
			return jsonify({"status":4})
		else:
			opponent=opponent["_id"]
		if db.ongoing_matches.find_one({"players":{"$in":[opponent]}}) != None:
			return jsonify({"status":5})
		else:
			server=getFreeServer()
			db.ongoing_matches.insert({"players":[userid,opponent],"server":server})
			try:
				lobby.remove(userid)
			except:
				pass
			try:
				lobby.remove(opponent)
			except:
				pass
			return jsonify({"status":0})

	#Match players up
	lobby[userid]=datetime.datetime.now()
	while len(lobby)>1:
		p1=lobby.popitem()[0]
		p2=lobby.popitem()[0]

		server=getFreeServer()

		db.ongoing_matches.insert({"players":[p1,p2],"server":server})

	return jsonify({"status":0})

@app.route("/getmatch",methods=["POST"])
def getmatch():
	try:
		token=request.json.get("token")
		user=db.user_tokens.find_one({"_id":token})
		if user==None:
			return jsonify({"status":5})
		userid=user["user_id"]

		returnme=db.ongoing_matches.find_one({"players":{"$in":[userid]}})
		if returnme==None:
			return jsonify({"status":1})
		else:
			returnme["status"]=0

			server=db.servers.find_one(returnme.pop("server"))
			returnme["hostname"]=socket.gethostbyname(server["hostname"])
			returnme["port"]=server["port"]

			returnme["players"].remove(userid)
			returnme["opponentid"]=str(returnme["players"][0])
			returnme.pop("players")

			returnme.pop("_id")

			return app.response_class(response=dumps(returnme),status=200,mimetype="application/json")
	except:
		return jsonify({"status":-1})


@app.route("/userinfo",methods=["POST"])
def getUserInfo():
	try:
		token=request.json.get("token")
		if request.json.get("userid")==None:
			userid=db.user_tokens.find_one({"_id":token})["user_id"]
		else:
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

@app.route("/matchstats",methods=["POST"])
def matchstats():
	try:
		token=request.json.get("token")
	except AttributeError:
		token=request.json[0].get("token")
	userid=db.user_tokens.find_one({"_id":token})["user_id"]

	returnme=[]
	for player in db.match_results.distinct("players"):
		if player == userid:
			continue

		d={}
		d["total_matches"]=db.match_results.find({"players":{"$all":[userid,player]}}).count()
		if d["total_matches"]==0:
			continue

		d["surrenders"]=db.match_results.find({"players":{"$all":[userid,player]},"endstate":"surrender"}).count()
		d["wins"]=db.match_results.find({"players":{"$all":[userid,player]},"endstate":{'$regex':'^check'},"winner":userid}).count()
		d["losses"]=db.match_results.find({"players":{"$all":[userid,player]},"endstate":{'$regex':'^check'},"winner":player}).count()
		d["user_id"]=str(player)
		d["username"]=db.users.find_one({"_id":player})["username"]

		returnme.append(d)

	return jsonify(returnme)


class HasUnlocked:
	'''you may have noticed I love enums and switch statements. this is their python equivilant'''

	setlist=["white","black","goblins","teatime"]

	def white(self,userid):
		return True

	def black(self,userid):
		return True

	def goblins(self,userid):
		'''user must have won at least one match'''
		return db.match_results.find({"endstate": {'$regex':'^check'},"winner":userid}).count() >= 1

	def teatime(self,userid):
		'''must have won three'''
		return db.match_results.find({"endstate":{'$regex':'^check'},"winner":userid}).count() >= 3

	def _default_(self,userid):
		return False

	def __getitem__(self, name):
		try:
			return getattr(self, name)
		except AttributeError:
			return self._default_

@app.route("/setprefs",methods=["POST"])
def setprefs():
	token=request.json.get("token")
	userid=db.user_tokens.find_one({"_id":token})["user_id"]
	user=db.users.find_one(userid)

	hasUnlocked=HasUnlocked()

	a=request.json.get("favourite_set")
	if(a != None):
		if hasUnlocked[a](userid):
			if user["secondary_set"]==a:
				db.users.update({"_id":userid},{"$set":{"secondary_set":user["favourite_set"]}})
			db.users.update({"_id":userid},{"$set":{"favourite_set":a}})
			return jsonify({"status":0})
		else:
			return jsonify({"status":1,"reason":"you have not met the requirements for this chess set"})

	a=request.json.get("secondary_set")
	if(a != None):
		if hasUnlocked[a](userid):
			if user["favourite_set"]==a:
				 db.users.update({"_id":userid},{"$set":{"favourite_set":user["secondary_set"]}})
			db.users.update({"_id":userid},{"$set":{"secondary_set":a}})
			return jsonify({"status":0})
		else:
			return jsonify({"status":1,"reason":"you have not met the requirements for this chess set"})

	return jsonify({"status":1,"reason":"No valid setting found"})

@app.route("/getunlocked",methods=["POST"])
def get_unlocked():
	try:
		token=request.json.get("token")
		userid=db.user_tokens.find_one({"_id":token})["user_id"]
		unlocked=0
		hasUnlocked=HasUnlocked()

		num=1
		for set in hasUnlocked.setlist:
			if hasUnlocked[set](userid):
				unlocked+=num
			num*=2

		return jsonify({"status":0,"unlocked":unlocked,"userid":str(userid)})

	except:
		return jsonify({"status":1})
