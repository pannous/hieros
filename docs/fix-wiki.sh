git fetch --all
git pull
for f in `ls *.md`; do
 sed -i 's/\s*$/  /g' $f
 sed -i 's/<>/⇔/g' $f
 sed -i 's/<-/⇦/g' $f
 sed -i 's/->/⇨/g' $f
 sed -i 's/~/⋍/g' $f
 sed -i 's/^  $//' $f
 # sed -i 's/\[\[\(.*?\)\]\]/[\1](\1)/g' $f gehtnet => use js
done
# ./repair-links.js not neccessary?
git commit -a --allow-empty-message -m '⇔'
git push

# broken-link-checker "http://pannous.github.io/hieros/Home" -ro --get |gv 400|gv "─OK─"