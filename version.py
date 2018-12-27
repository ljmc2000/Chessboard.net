#generate the version information files for all parts of the program
version=2
tag="v1.1.1"
repo="gingerpi.ddns.net/chessrepo"

java=open("androidClient/app/src/main/java/net/ddns/gingerpi/chessboardnetCommon/VersionInfo.java","w+")
java.write('''package net.ddns.gingerpi.chessboardnetCommon;

public class VersionInfo
{
	public final static int version=%d;
	public final static String tag="%s";
}
''' % (version,tag))
java.close()

python=open("httpapi/versionInfo.py","w+")
python.write('''version=%d
tag="%s"
repo="%s"
''' % (version,tag,repo))
