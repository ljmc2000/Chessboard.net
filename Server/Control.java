package net.ddns.gingerpi.chessboardnetServer;
import java.net.*;
import java.io.*;

class Control
{
	public static void main(String[] args)
	{
		try
		{
			ServerSocket myServerSocket=new ServerSocket(7000);
			int remainingConnections=100;

			//for the rest of runtime
			while(1==1)
			{
				if(remainingConnections>0)
				{
					Socket connection1 = myServerSocket.accept();
					Socket connection2 = myServerSocket.accept();
					ObjectOutputStream out1=new ObjectOutputStream(connection1.getOutputStream());
					ObjectInputStream in1=new ObjectInputStream(connection1.getInputStream());
					ObjectOutputStream out2=new ObjectOutputStream(connection2.getOutputStream());
					ObjectInputStream in2=new ObjectInputStream(connection2.getInputStream());

					ChessGameInstance p1Thread = new ChessGameInstance(out1,in2);
					ChessGameInstance p2Thread = new ChessGameInstance(out2,in1);
					p1Thread.start();
					p2Thread.start();
					remainingConnections--;
				}
			}
		}

		catch(Exception e)
		{
			System.out.println(e);
		}
	}
}
