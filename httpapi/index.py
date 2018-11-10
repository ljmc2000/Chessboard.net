from flask import Flask,jsonify,request
from pymongo import MongoClient
from bson.json_util import dumps
import bcrypt

app=Flask(__name__)
db=MongoClient().ChessboardNet

@app.route("/")
def hello_world():
	returnme=dumps(db.test.find())
	return jsonify(returnme)

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
