package nederhof.interlinear.egyptian;

import java.io.*;
import org.w3c.dom.*;
import org.xml.sax.*;

import nederhof.interlinear.*;

public class EgyptianOrthoGenerator extends ResourceGenerator {

    public TextResource generate(File file) throws IOException {
	if (!file.exists())
	    return EgyptianOrtho.make(file);
	else
	    return new EgyptianOrtho(file.getPath());
    }

    public TextResource interpret(String fileName, Object in) {
        if (in instanceof Document) {
            Document doc = (Document) in;
            try {
                return new EgyptianOrtho(fileName, doc);
            } catch (IOException e) {
                // file is not this type of resource
            }
        } 
        return null;
    }

    public String getName() {
        return "orthography";
    }

    public String getDescription() {
        return "tier for orthography";
    }

}


