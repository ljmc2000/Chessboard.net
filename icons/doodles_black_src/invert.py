import sys
filename=sys.argv[1]

file=open(filename,"r+")

string=file.read()

string=string.replace("#000000","$^^&&^^$")
string=string.replace("#ffffff","#000000")
string=string.replace("$^^&&^^$","#ffffff")

file.seek(0)
file.write(string)
