package nederhof.lexicon.egyptian;


// Key and use of one lexical annotation.
public class LexRecord {
    public String cite = "";

    public String keyhi = "";
    public String keyal = "";
    public String keytr = "";
    public String keyfo = "";
    public String keyco = "";

    public String hi = "";
    public String al = "";
    public String tr = "";
    public String fo = "";
    public String co = "";

    public LexRecord(String keyhi, String keyal, String keytr, String keyfo, String keyco) {
	this.keyhi = keyhi;
	this.keyal = keyal;
	this.keytr = keytr;
	this.keyfo = keyfo;
	this.keyco = keyco;
    }

    public void appendHi(String hi) {
	if (this.hi.equals(""))
	    this.hi = hi;
	else
	    this.hi += "-" + hi;
    }
    public void appendAl(String al) {
	if (this.al.equals(""))
	    this.al = al;
	else
	    this.al += " " + al;
    }
    public void appendTr(String tr) {
	if (this.tr.equals(""))
	    this.tr = tr;
	else
	    this.tr += " " + tr;
    }
    public void appendFo(String fo) {
	if (this.fo.equals(""))
	    this.fo = fo;
	else
	    this.fo += " " + fo;
    }
    public void appendCo(String co) {
	if (this.co.equals(""))
	    this.co = co;
	else
	    this.co += " " + co;
    }
}
