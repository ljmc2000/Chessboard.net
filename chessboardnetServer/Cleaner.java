//cleanup when the application ends or crashes
package ie.delilahsthings.chessboardnetServer;
import org.bson.types.ObjectId;

public class Cleaner extends Thread
{
	ObjectId serialno;
	MongoDataManager db;

	public Cleaner(ObjectId serialno,MongoDataManager db)
	{
		this.serialno=serialno;
		this.db=db;
	}

	@Override
	public void run()
	{
		db.deregister(serialno);
	}
}
