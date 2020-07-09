# Chessboard.Net

At some point in third year of collage I was given a rather open ended assignment for android programming, and decided that what I wanted to make was a network multiplayer game. 

Chess was chosen for a couple of reasons. The rules are common knowledge, which means no tutorial should be necesseary or if one is required, there are near limitless resources already available. The game has been public domain since before copyright even existed, so no fear of retribution from the game's original creator. The rules are complicated enough to be a challenge, but simple enough to complete within the project's deadline, which from the commit timeline seems to have been about a month and a half. Finally it comes with it's own premade audience, as chess is played around the world by millions of people. 

# Successes

The project was delivered on time. 

The reason for the network game thing is because network programming was being covered in another module at the time and I could not resist the opertunity to put everything I was learning into one cohesive project. I also developed a HTTPAPI using flask like in cloud computing with a MongoDB backend which is what we were doing at the time in databases. Four of the six modules I completed that semester have some representation in this project, making it a testament to everything I was learning, not just android programming. The work I put into this project earned me a final year project supervisor who took an active interest in my FYP before he even knew what I was going to make. Looking back it's probably done more for me than anything I've ever made. And I don't even like chess.

I had a lot of fun designing the chess sets. The style I was going for was the imps seen on zero punctuation, and I think that left me with some pretty pieces indeed. 

# Quirks

Well of course not everything went perfectly. There were some bugs, which I'll here address.

The submission version does not in fact work because shortly beyond it's release google amended android's security policy, that all http requests in an application must use HTTPS, which I knew how to do, but did not do because of the additional effort required.

There are three rules of chess that are so fringe that an outsider like myself should be forgiven for knowing them not. The first is promotion. When a pawn reaches the far side of the board, it may be exchanged for a rook, bishop, knight or queen. (guess which one the player will choose 99% of the time. Bonus points for guessing which one covers the other 1%) I was aware of this mechanic and had coded accordingly. The second of these rules is a move called castling or alternatively rooking. If the king and rook are on their first turn, and there are no pieces betwixt them, the king and rook may both be moved to specific spaces. Of this mechanic I was also aware. What I was not aware of is that if either piece would be in danger on any space during the manouver or if the king begins in check, the manouver is illegal. So that works that way. The final mechanic is known as en passant. This move is so circumstantial that I was surprised the chess enthusiast who informed me of it's existance knew of it. This was never implemented. The project had already been demonstrated before that conversation took place (I lost). This is a perfect example of the dunning kruger effect in action.

# Special Thanks

Special thanks to David Leonard, Jaime Walsh and Jack, all of whom are better chess players than I am.

# Building Instructions

Good question. Run version.py and open android studio. The project should build from there. You will need a server and httpapi running. The server requires a java virtual machine to run. The httpapi was built for a version of python 3. I can't remember the subversion and if you get it wrong bcrypt will throw a tantrum. This is why you don't write the documentation having not touched the program for over a year.
