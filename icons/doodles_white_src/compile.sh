#!/bin/bash
echo compiling textures
dir=/tmp/icons
mkdir $dir

mogrify -path /tmp/icons -format png -background none -density 1200 -scale 1080 *.svg
for a in $(ls /tmp/icons);
do mv $dir/$a $dir/doodle_white_$a;
done;
mv /tmp/icons/* ../../androidClient/app/src/main/res/drawable
mogrify -path /tmp/icons -alpha deactivate -negate -format png -background none -density 1200 -scale 1080 *.svg
for a in $(ls /tmp/icons);
do mv $dir/$a $dir/doodle_black_$a;
done;
mv /tmp/icons/* ../../androidClient/app/src/main/res/drawable
