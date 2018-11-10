from flask import Flask,jsonify
from pymongo import MongoClient
from bson.json_util import dumps

app=Flask(__name__)
db=MongoClient().ChessboardNet

@app.route("/")
def hello_world():
	returnme=dumps(db.test.find())
	return jsonify(returnme)
