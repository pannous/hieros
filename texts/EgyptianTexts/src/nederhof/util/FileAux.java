/***************************************************************************/
/*                                                                         */
/*  FileAux.java                                                           */
/*                                                                         */
/*  Copyright (c) 2009 Mark-Jan Nederhof                                   */
/*                                                                         */
/*  This file is part of the implementation of PhilologEG, and may only be */
/*  used, modified, and distributed under the terms of the                 */
/*  GNU General Public License (see doc/GPL.TXT).                          */
/*  By continuing to use, modify, or distribute this file you indicate     */
/*  that you have read the license and understand and accept it fully.     */
/*                                                                         */
/***************************************************************************/

// Manipulating files.

package nederhof.util;

import java.io.*;
import java.net.*;
import java.util.*;

public class FileAux {

	// Make absolute address, from address that is relative to code base.
	public static URL fromBase(String address) {
		if (address.startsWith("jar:") || address.startsWith("http:"))
			try {
				return new URL(address);
			} catch (MalformedURLException e) {
				LogAux.reportError(e.getMessage());
				return null;
			}
		else  {
			URL url = FileAux.class.getClassLoader().getResource(address);
			if (url == null)
				try {
					url = (new File(address)).toURI().toURL();
				} catch (MalformedURLException ex) {
					// ignore
				}
			return url;
		}
	}

	// Path of JAR that is called.
	public static File calledPath() {
		URL url = FileAux.class.getProtectionDomain().getCodeSource().getLocation();
		String urlString = url.toString();
		final String jar = "jar:";
		if (urlString.startsWith(jar)) {
			urlString = urlString.substring(jar.length());
		}
		urlString = urlString.replaceAll("\\\\", "/");
		File file = null;
		try {
			file = new File(new URI(urlString));
		} catch (URISyntaxException e) {
			// Fall back on simpler method. Fails for special characters.
			file = new File(url.getPath());
		}
		return file;
	}

	// Resolve one address to another. 
	public static String resolve(String str1, String str2) {
		str1 = str1.replaceAll("\\\\", "/");
		str2 = str2.replaceAll("\\\\", "/");
		if (str2.matches("[a-zA-Z]*:.*") || str2.matches("/.*"))
			return str2;
		if (str1.lastIndexOf('/') >= 0)
			str1 = str1.substring(0, str1.lastIndexOf('/') + 1);
		while (str2.startsWith("../")) {
			str2 = str2.substring("../".length());
			if (str1.lastIndexOf('/', str1.length()-2) >= 0)
				str1 = str1.substring(0, 
						str1.lastIndexOf('/', str1.length()-2) + 1);
		}
		return str1 + str2;
	}

	// Get path relative to base directory.
	public static File getRelativePath(File path, File base) {
		if (base == null) {
			base = new File(".");
		}
		try {
			File pathCanon = path.getCanonicalFile();
			File baseCanon = base.getCanonicalFile();
			path = pathCanon;
			base = baseCanon;
		} catch (IOException e) {
			System.err.println("FileAux.getRelativePath:\n" + e.getMessage());
			return path;
		} catch (SecurityException e) {
			System.err.println("FileAux.getRelativePath:\n" + e.getMessage());
			return path;
		}

		Vector pathParts = new Vector();
		Vector baseParts = new Vector();
		while (path.getParent() != null) {
			pathParts.add(0, path.getName());
			path = path.getParentFile();
		}
		pathParts.add(0, path.getName());
		while (base.getParent() != null) {
			baseParts.add(0, base.getName());
			base = base.getParentFile();
		}
		baseParts.add(0, base.getName());

		int common = 0;
		for (int i = 0; 
				i < pathParts.size() && i < baseParts.size(); 
				i++) {
			String pathPart = (String) pathParts.get(i);
			String basePart = (String) baseParts.get(i);
			if (pathPart.equals(basePart)) 
				common++;
		}

		String relative = "";
		for (int i = common; i < baseParts.size(); i++) 
			relative += ".." + File.separator;
		for (int i = common; i < pathParts.size(); i++) {
			if (i > common)
				relative += File.separator;
			relative += (String) pathParts.get(i);
		}

		return new File(relative);
	}

	// Get path relative to current directory.
	public static File getRelativePath(File path) {
		return getRelativePath(path, null);
	}

	// Copy a binary file.
	public static void copyBinaryFile(File in, File out) 
		throws FileNotFoundException, IOException {
			if (in.equals(out))
				return;
			FileInputStream inStream = new FileInputStream(in);
			FileOutputStream outStream = new FileOutputStream(out);

			byte[] buffer = new byte[1024];
			int n = 0;
			while ((n = inStream.read(buffer)) != -1) 
				outStream.write(buffer, 0, n);

			inStream.close();
			outStream.close();
		}

	// Copy text files.
	public static void copyFile(File in, File out)
		throws FileNotFoundException, IOException {
			if (in.equals(out))
				return;

			BufferedReader inStream =
				new BufferedReader(new FileReader(in));
			PrintWriter outStream =
				new PrintWriter(new FileOutputStream(out));
			String line = inStream.readLine();
			while (line != null) {
				outStream.println(line);
				line = inStream.readLine();
			}
			inStream.close();
			outStream.close();
		}

	// Copy two files, where source is file that can be URL,
	public static void copyFile(String in, File out) 
		throws FileNotFoundException, IOException {
			if (in.equals(out.toString()))
				return;

			BufferedReader inStream;
			if (in.startsWith("jar:") ||
					in.startsWith("http:") ||
					in.startsWith("file:")) {
				URL url = new URL(in);
				inStream = new BufferedReader(
						new InputStreamReader(url.openStream()));
			} else {
				inStream = new BufferedReader(
						new InputStreamReader(
							new FileInputStream(in), "UTF-8"));
			}
			PrintWriter outStream =
				new PrintWriter(new FileOutputStream(out));
			String line = inStream.readLine();
			while (line != null) {
				outStream.println(line);
				line = inStream.readLine();
			}
			inStream.close();
			outStream.close();
		}

	// Of file name, determine whether it ends on extension.
	public static boolean hasExtension(String name, String ext) {
		Vector fullExtensions = new Vector();
		fullExtensions.add("." + ext);
		fullExtensions.add("." + ext.toUpperCase());
		fullExtensions.add("." + ext.toLowerCase());
		for (int i = 0; i < fullExtensions.size(); i++) {
			String fullExt = (String) fullExtensions.get(i);
			if (name.endsWith(fullExt)) 
				return true;
		}
		return false;
	}

	// Of file, determine whether it ends on extension.
	public static boolean hasExtension(File name, String ext) {
		return hasExtension(name.getName(), ext);
	}

	// Of file name, remove extension.
	// Return null, if file name doesn't have that extension.
	// Extension in argument is without dot.
	public static String removeExtension(String name, String ext) {
		Vector fullExtensions = new Vector();
		fullExtensions.add("." + ext);
		fullExtensions.add("." + ext.toUpperCase());
		fullExtensions.add("." + ext.toLowerCase());
		for (int i = 0; i < fullExtensions.size(); i++) {
			String fullExt = (String) fullExtensions.get(i);
			if (name.endsWith(fullExt)) {
				return name.substring(0, 
						name.length() - fullExt.length());
			}
		}
		return null;
	}

	// Remove any extension.
	public static String removeExtension(String name) {
		if (name.matches(".*\\.[a-zA-Z]*")) 
			return name.replaceAll("\\.[a-zA-Z]*$", "");
		else
			return null;
	}

	// Get the extension, or "" if none.
	public static String getExtension(String name) {
		if (name.matches(".*\\.[a-zA-Z]*"))
			return name.replaceFirst("^.*\\.([a-zA-Z]*)$", "$1");
		else
			return "";
	}

	// Convert file to URI as string.
	public static String getUriString(File file) {
		try {
			URI uri = new URI("file", file.getAbsolutePath(), null);
			return uri.toASCIIString();
		} catch (Exception e) {
			// fall back on file path
			return file.getAbsolutePath();
		}
	}

	// Get stream from either file or URL.
	// Meant to take care of spaces in address.
	public static InputStream addressToStream(String address) 
		throws IOException {
			try {
				File file = new File(address);
				if (file.exists()) 
					return new FileInputStream(file);
				else {
					URL url = fromBase(address);
					return url.openStream();
				}
			} catch (Exception e) {
				URL url = fromBase(address);
				if (url == null)
					LogAux.reportError("null trying to open " + address +
							" in nederhof.util.FileAux.addressToStream");
				return url.openStream();
			}
		}

	// Testing
	public static void main(String[] args) {
		System.out.println(getRelativePath(
					new File("b/c"), new File("b")));
	}

}
