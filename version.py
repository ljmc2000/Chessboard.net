#generate the version information files for all parts of the program
import re
applicationIdPattern=re.compile(r'applicationId "(.+)"')
versionCodePattern=re.compile(r'versionCode (\d+)')
versionNamePattern=re.compile(r'versionName "(.+)"')

with open("androidClient/app/build.gradle") as buildfile:
	data=buildfile.read()
	version=int(next(versionCodePattern.finditer(data)).group(1))
	tag=next(versionNamePattern.finditer(data)).group(1)
	repo=next(applicationIdPattern.finditer(data)).group(1)

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
