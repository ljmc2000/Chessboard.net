package net.ddns.gingerpi.chessboardnetServer;

import org.bson.types.ObjectId;
import org.bson.*;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import com.mongodb.MongoClient;
import com.mongodb.BasicDBObject;
import static com.mongodb.client.model.Filters.*;

import java.util.ArrayList;

class MongoDataManager
{
	MongoClient client;
	MongoDatabase database;
	MongoCollection<Document> userTokens;
	MongoCollection<Document> onGoingMatches;
	MongoCollection<Document> matchResults;
	MongoCollection<Document> serverList;

	public MongoDataManager()
	{
		client=new MongoClient();
		database=client.getDatabase("ChessboardNet");
		userTokens=database.getCollection("user_tokens");
		onGoingMatches=database.getCollection("ongoing_matches");
		matchResults=database.getCollection("match_results");
		serverList=database.getCollection("servers");
	}

	public ObjectId register(String hostname,int port,int capacity)
	{
		Document fields=new Document("hostname",hostname);
		fields.put("port",port);
		fields.put("capacity",capacity);

		serverList.insertOne(fields);
		return (ObjectId) fields.get("_id");
	}

	public void deregister(ObjectId serverid)
	{
		Document fields=new Document("_id",serverid);
		serverList.deleteOne(fields);
	}

	public ObjectId getUserId(String token)
	{
		BasicDBObject fields=new BasicDBObject("_id",token);
		Document result = userTokens.find(fields).first();
		return (ObjectId) result.get("user_id");
	}

	public void endGame(ObjectId winner,String endstate)
	{
		ArrayList<ObjectId> player=new ArrayList();
		player.add(0,winner);
		Document match=onGoingMatches.find(in("players",player)).first();

		//delete
		onGoingMatches.deleteOne(match);

		//save
		match.put("winner",winner);
		match.put("endstate",endstate);
		matchResults.insertOne(match);
	}

	public ObjectId getOpponentId(ObjectId userid)
	{
		Document match=onGoingMatches.find(in("players",userid)).first();
		ArrayList<ObjectId> players=(ArrayList) match.get("players");
		ObjectId returnme=players.get(0);

		if(returnme.toString().equals(userid.toString()))
			returnme=players.get(1);

		return returnme;
	}

	public ObjectId getGameId(ObjectId userid)
	{
		Document match=onGoingMatches.find(in("players",userid)).first();
		ObjectId gameId=(ObjectId) match.get("_id");
		return gameId;
	}

	public boolean getUserColor(ObjectId userId)
	{
		Document match=onGoingMatches.find(in("players",userId)).first();
		ArrayList<ObjectId> players=(ArrayList) match.get("players");
		ObjectId checkme=players.get(0);

		return checkme.toString().equals(userId.toString());
	}
}
