/**
 * 
 */
package jp.gr.java_conf.turner.util;

import java.io.File;
import java.io.IOException;

/**
 * @author nanakoso
 * 
 */
public class Sample {

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		Generator<String> g = new Generator<String>() {

			@Override
			public void run() throws InterruptedException {
				yield("ほげ");
				yield("ふが");
				yield("もげ");
			}
		};

		for (String s : g) {
			System.out.println(s);
		}

		MyGenerator t2 = new MyGenerator(new File(args[0]));


		for (File f : t2) {
			System.out.println(f.getCanonicalPath());
		}
	}

}

class MyGenerator extends Generator<File> {
	/**
	 * 
	 */
	private File root;

	public MyGenerator(File root) {
		super(Integer.MAX_VALUE);
		this.root = root;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jp.gr.java_conf.turner.util.Generator.Task#run()
	 */
	@Override
	public void run() throws InterruptedException {
		listPath(root);
	}

	private void listPath(File dir) throws InterruptedException {
		File[] fs = dir.listFiles();
		for (File f : fs) {
			if (f.isDirectory()) {
				listPath(f);
			} else if (f.isFile()) {
				yield(f);
			}
		}
	}

}
