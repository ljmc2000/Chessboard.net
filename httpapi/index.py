from flask import Flask
from pymongo import MongoClient
from bson.json_util import dumps

app=Flask(__name__)
db=MongoClient().ChessboardNet

@app.route("/")
def hello_world():
	cur=db.test.find()
	return dumps(cur)
