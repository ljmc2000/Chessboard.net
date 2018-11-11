from flask import Flask,jsonify,request
from pymongo import MongoClient
from bson.json_util import dumps
import bcrypt,secrets,datetime

app=Flask(__name__)
db=MongoClient().ChessboardNet


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
		db.users.insert({"username":username,"passhash":passhash})
		return "0"
	except:
		return "-1"

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
			login_token=secrets.token_urlsafe(32)
			expires=datetime.datetime.now()+datetime.timedelta(days=365)
			db.user_tokens.insert({"_id":login_token,"user_id":userdetails["_id"],"expires":expires})
			return jsonify({"status":"0","id":str(userdetails["_id"]),"token":login_token})
		else:
			return jsonify({"status":"1"})
	except:
		return jsonify({"status":"-1"})
