package net.ddns.gingerpi.chessboardnetServer;
import static net.ddns.gingerpi.chessboardnetCommon.ChessBoard.texturePack;

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

	public MongoDataManager()
	{
		client=new MongoClient();
		database=client.getDatabase("ChessboardNet");
		userTokens=database.getCollection("user_tokens");
		onGoingMatches=database.getCollection("ongoing_matches");
		matchResults=database.getCollection("match_results");
	}

	public ObjectId getUserId(String token)
	{
		BasicDBObject fields=new BasicDBObject("_id",token);
		Document result = userTokens.find(fields).first();
		return (ObjectId) result.get("user_id");
	}

	public void endGame(ObjectId opponent,String endstate)
	{
		ArrayList<ObjectId> player=new ArrayList();
		player.add(0,opponent);
		Document match=onGoingMatches.find(in("players",player)).first();

		//delete
		onGoingMatches.deleteOne(match);

		//save
		match.put("winner",opponent);
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

	public texturePack[] getUserFavourites(ObjectId userId)
	{
		texturePack[] returnme=new texturePack[]{texturePack.white,texturePack.black};
		return returnme;
	}

	public boolean getUserColor(ObjectId userId)
	{
		Document match=onGoingMatches.find(in("players",userId)).first();
		ArrayList<ObjectId> players=(ArrayList) match.get("players");
		ObjectId checkme=players.get(0);

		return checkme.toString().equals(userId.toString());
	}
}
