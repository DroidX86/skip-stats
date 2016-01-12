import java.util.TreeMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.Random;
import java.util.UUID;


class PerfThread extends Thread {
	private TreeMap<Integer, String> tmap;
	private ConcurrentSkipListMap<Integer, String> slmap;
	int size, times;

	public PerfThread(TreeMap<Integer, String> tmap, ConcurrentSkipListMap<Integer, String> slmap, int percent) {
		this.tmap = tmap;
		this.slmap = slmap;
		this.size = this.tmap.size();
		if (this.slmap.size() == this.tmap.size())
			this.times = this.tmap.size() * (percent / 100);
	}

	public void run() {
		Random rn = new Random();
		long slATime = 0, tATime = 0, slMTime = 0, tMTime = 0;
		int run1 = this.times, run2 = this.times;
		System.out.println(Thread.currentThread().getId() + " entering access test phase: ");
		while (run1 >= 0) {
			int key = rn.nextInt() % this.size;
			long startTime, endTime;

			startTime = System.nanoTime();
			synchronized(tmap) {
				tmap.get(key);
			}
			endTime = System.nanoTime();
			tATime += (endTime - startTime);

			startTime = System.nanoTime();
			slmap.get(key);
			endTime = System.nanoTime();
			slATime += (endTime - startTime);

			run1--;
		}
		System.out.println(Thread.currentThread().getId() + " done with access test. Took: SL=" + slATime + ", T=" + tATime);

		System.out.println(Thread.currentThread().getId() + " entering modify test phase: ");
		while (run2 >= 0) {
			int key = rn.nextInt() % this.size;
			long startTime, endTime;

			startTime = System.nanoTime();
			synchronized(tmap) {
				tmap.put(key, "replaced key");
			}
			endTime = System.nanoTime();
			tMTime += (endTime - startTime);

			startTime = System.nanoTime();
			slmap.replace(key, "replaced key");
			endTime = System.nanoTime();
			slMTime += (endTime - startTime);

			run2--;
		}
		System.out.println(Thread.currentThread().getId() + " done with modify test. Took: SL=" + slMTime + ", T=" + tMTime);

	}
}

public class SkipListPerf {
	private static TreeMap<Integer, String> tmap;
	private static ConcurrentSkipListMap<Integer, String> slmap;

	public static void populateMaps(int size) {
		System.out.println("<Single-Threaded insertion phase>");
		long tTime = 0, slTime = 0;
		for (int i = 0; i < size; i++) {
			String val = UUID.randomUUID().toString().replaceAll("-", "");
			long startTime, endTime;

			startTime = System.nanoTime();
			tmap.put(size, val);
			endTime = System.nanoTime();
			tTime += (endTime - startTime);

			startTime = System.nanoTime();
			slmap.put(size, val);
			endTime = System.nanoTime();
			slTime += (endTime - startTime);
		}
		System.out.println("Total insertion time taken by TreeMap: " + tTime);
		System.out.println("Total insertion time taken by SkipListMap: " + slTime);
	}

	public static void main(String args[]) {
		slmap = new ConcurrentSkipListMap<Integer, String>();
		tmap = new TreeMap<Integer, String>();
		int size = Integer.parseInt(args[0]);
		int numThreads = Integer.parseInt(args[1]);
		populateMaps(size);
		for (int i = 0; i < numThreads; i++) {
			PerfThread pt = new PerfThread(tmap, slmap, size/numThreads);
			pt.start();
		}
	}

}
