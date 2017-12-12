package nederhof.ocr.admin;

import java.io.*;
import java.net.*;
import java.util.*;

import nederhof.ocr.images.*;

// Prototype of sign. Consists of BinaryImage and File.
public class Prototype {
	public BinaryImage im;
	public File file;

	public Prototype(BinaryImage im, File file) {
		this.im = im;
		this.file = file;
	}
}
