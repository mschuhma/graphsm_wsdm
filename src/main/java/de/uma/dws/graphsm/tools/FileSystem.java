package de.uma.dws.graphsm.tools;

import java.io.File;

public class FileSystem {

	public static File deleteFileOrDirectory(File path) {
		if (path.exists()) {
			if (path.isDirectory()) {
				for (File child : path.listFiles()) {
					deleteFileOrDirectory(child);
				}
			}
			path.delete();
		}
		return path;
	}

	public static File deleteFileOrDirectory(String path) {
		File f = new File(path);
		return deleteFileOrDirectory(f);
	}

}
