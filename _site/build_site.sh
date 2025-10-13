cd ~/uruk_egypt/_site/

echo cp ~/uruk_egypt/docs/*.md .
cp ~/uruk_egypt/docs/*.md .

echo markdown to html takes some minutes:
echo kramdown -i markdown -o html files:

for file in *.md; do
  if ! grep -q "^---" "$file"; then
    echo -e "---\n---\n$(cat "$file")" > "$file"
  fi
  # Replace [[PageName]] with [PageName](PageName.html) in the current file
  sed -i '' 's/\[\[\([^]]*\)\]\]/[\1](\1.html)/g' "$file"

  echo $file
  kramdown -i markdown -o html "$file" > "${file/.md/.html}"

done
echo cleanup
echo rm ~/uruk_egypt/_site/*.md 