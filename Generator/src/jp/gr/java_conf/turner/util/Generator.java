/**
 * パイソン風ジェネレータクラス
 */
package jp.gr.java_conf.turner.util;

import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;

/**
 * パイソン風ジェネレータクラス
 * 
 * @author nanakoso
 */
public abstract class Generator<E> implements Iterable<E> {

	public static void setExecutor(Executor exec) {
		threadPool = exec;
	}
	
	/**
	 * 
	 */
	public Generator() {
		this(0);
	}

	public Generator(int bufsize) {
		switch (bufsize) {
		case 0:
			que = new SynchronousQueue<Maybe>();
			break;
		default:
			que = new LinkedBlockingQueue<Maybe>(bufsize);
		}
	}

	public abstract void run() throws InterruptedException;


	protected void yield(E e) throws InterruptedException {
		que.put(new Maybe(e));
	}

	private void execute() {
		threadPool.execute(new Runnable() {

			public void run() {
				try {
					Generator.this.run();
					que.put(END_OF_QUE);
				} catch (InterruptedException e) {
					// NOP
				}
			}
		});
	}



	private class Maybe {
		private final E e;

		Maybe(E e) {
			this.e = e;
		}

	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Iterable#iterator()
	 */
	public Iterator<E> iterator() {

		execute();

		return new Iterator<E>() {
			public boolean hasNext() {
				Maybe m = END_OF_QUE;
				try {
					m = waitNext();
				} catch (InterruptedException e) {
					setEnd();
				}
				return m != END_OF_QUE;
			}

			public E next() {
				Maybe m = END_OF_QUE;
				try {
					m = takeNext();
				} catch (InterruptedException e) {
					setEnd();
				}
				return m.e;
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}

			private Maybe nextOne = null;

			private void setEnd() {
				nextOne = END_OF_QUE;
			}

			private Maybe waitNext() throws InterruptedException {
				if (nextOne == null) {
					nextOne = que.take();
				}
				return nextOne;
			}

			private Maybe takeNext() throws InterruptedException {
				Maybe m = waitNext();
				if (nextOne != END_OF_QUE) {
					nextOne = null;
				}
				return m;
			}
		};
	}

	private static Executor threadPool = new Executor() {
		public void execute(Runnable command) {
			(new Thread(command)).start();
		}
	};

	final private Maybe END_OF_QUE = new Maybe(null);

	final private BlockingQueue<Maybe> que;
}
