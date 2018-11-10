from flask import Flask,jsonify,request
from pymongo import MongoClient
from bson.json_util import dumps
import bcrypt,secrets

app=Flask(__name__)
db=MongoClient().ChessboardNet


@app.route("/")
def hello_world():
	returnme=db.test.find_one()
	return app.response_class(response=dumps(returnme),status=200,mimetype="application/json")

@app.route("/signup",methods=["POST"])
def create_user():
	try:
		username=request.form.get("username")
		password=request.form.get("password")
		passhash=bcrypt.hashpw(password.encode(),bcrypt.gensalt())
		db.users.insert({"username":username,"passhash":passhash})
		return "success"
	except:
		return "failure"

@app.route("/signin",methods=["POST"])
def get_login_token():
	try:
		username=request.form.get("username")
		password=request.form.get("password")
		userdetails=db.users.find_one({"username":username})
		if bcrypt.checkpw(password.encode(),userdetails["passhash"]):
			login_token=secrets.token_urlsafe(16)
			db.user_tokens.insert({"_id":login_token,"user_id":userdetails["_id"]})
			return jsonify({"status":"success","token":login_token})
		else:
			return jsonify({"status":"fail"})
	except:
		return jsonify({"status":"exception"})
