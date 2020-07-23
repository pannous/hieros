import csv
# file=open("Chinese_PIE.csv", "r")
file=open("chinese.freq.tsv", "r")
reader = csv.reader(file, delimiter='\t', quotechar='"')
bad=0
for row in reader:
  if(len(row)!=7):
    bad=bad+1
    print(row)
print(bad)
  # for en,de,zn,pi,see,note,*rest in row:
  #     print(en,de,zn,pi,see,note+" ".join(rest))
