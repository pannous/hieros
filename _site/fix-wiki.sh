git fetch --all
git pull
for f in `ls *.md`; do
 sed -i 's/\s*$/  /g' $f
 sed -i 's/⇔/⇔/g' $f
 sed -i 's/^  $//' $f
 sed -i 's/\[\[\(.*?\)\]\]/[\1](\1)/g' $f
done
git commit -a --allow-empty-message -m ''
git push