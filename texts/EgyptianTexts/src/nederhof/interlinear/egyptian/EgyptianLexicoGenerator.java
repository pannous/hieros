package nederhof.interlinear.egyptian;

import java.io.*;
import org.w3c.dom.*;
import org.xml.sax.*;

import nederhof.interlinear.*;

public class EgyptianLexicoGenerator extends ResourceGenerator {

    public TextResource generate(File file) throws IOException {
	if (!file.exists())
	    return EgyptianLexico.make(file);
	else
	    return new EgyptianLexico(file.getPath());
    }

    public TextResource interpret(String fileName, Object in) {
        if (in instanceof Document) {
            Document doc = (Document) in;
            try {
                return new EgyptianLexico(fileName, doc);
            } catch (IOException e) {
                // file is not this type of resource
            }
        } 
        return null;
    }

    public String getName() {
        return "lexical";
    }

    public String getDescription() {
        return "tier for lexical annotation";
    }

}


