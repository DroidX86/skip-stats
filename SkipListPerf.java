import java.util.TreeMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.Random;
import java.util.UUID;


class PerfThread extends Thread {
	private TreeMap<Integer, String> tmap;
	private ConcurrentSkipListMap<Integer, String> slmap;
	int size, times;

	public PerfThread(TreeMap<Integer, String> tmap, ConcurrentSkipListMap<Integer, String> slmap, int times) {
		this.tmap = tmap;
		this.slmap = slmap;
		this.size = this.tmap.size();
		if (this.slmap.size() == this.tmap.size())
			this.times = times/1000;
	}

	public void run() {
		Random rn = new Random();
		long slATime = 0, tATime = 0, slMTime = 0, tMTime = 0;
		int run1 = 0, run2 = 0;
		//System.out.println(Thread.currentThread().getId() + " entering access test phase......");
		int correct = 0;
		while (run1 < this.times) {
			int key = Math.abs(rn.nextInt()) % this.size;
			long startTime, endTime;

			startTime = System.nanoTime();
			String v1 = null;
			synchronized(tmap) {
				v1 = tmap.get(key);
			}
			endTime = System.nanoTime();
			tATime += (endTime - startTime);

			startTime = System.nanoTime();
			String v2 = slmap.get(key);
			endTime = System.nanoTime();
			slATime += (endTime - startTime);

			//System.out.print(".");

			if (v1.equals(v2))
				correct++;
			run1++;
		}
		//System.out.println("");
		//System.out.println(Thread.currentThread().getId() + " done with access test. Took: SL=" + slATime + ", T=" + tATime);
		//System.out.println(Thread.currentThread().getId() + " access correct? " + (correct == this.times));

		//System.out.println(Thread.currentThread().getId() + " entering modify test phase....");
		rn = new Random();
		correct = 0;
		while (run2 < this.times) {
			int key = Math.abs(rn.nextInt()) % this.size;
			long startTime, endTime;

			startTime = System.nanoTime();
			String v1 = null;
			synchronized(tmap) {
				v1 = tmap.put(key, "replaced key");
			}
			endTime = System.nanoTime();
			tMTime += (endTime - startTime);

			startTime = System.nanoTime();
			String v2 = slmap.replace(key, "replaced key");
			endTime = System.nanoTime();
			slMTime += (endTime - startTime);

			//System.out.print(".");

			if (v1.equals(v2))
				correct++;
			run2++;
		}
		//System.out.println("");
		//System.out.println(Thread.currentThread().getId() + " done with modify test. Took: SL=" + slMTime + ", T=" + tMTime);
		//System.out.println(Thread.currentThread().getId() + " modify correct? " + (correct == this.times));
		System.out.println(tATime/times + "," + tMTime/times + "," + slATime/times + "," + slMTime/times);
	}
}

public class SkipListPerf {
	private static TreeMap<Integer, String> tmap;
	private static ConcurrentSkipListMap<Integer, String> slmap;

	public static void populateMaps(int size) {
		long tTime = 0, slTime = 0;
		for (int i = 0; i < size; i++) {
			String val = UUID.randomUUID().toString().replaceAll("-", "");
			long startTime, endTime;

			//System.out.println("Entering " + i + ", " + val);

			startTime = System.nanoTime();
			tmap.put(i, val);
			endTime = System.nanoTime();
			tTime += (endTime - startTime);

			startTime = System.nanoTime();
			slmap.put(i, val);
			endTime = System.nanoTime();
			slTime += (endTime - startTime);
		}
		//System.out.println("Average insertion time taken by TreeMap: " + tTime/size);
		//System.out.println("Average insertion time taken by SkipListMap: " + slTime/size);
		//System.out.println(tTime/size + "," + slTime/size);
	}

	public static void main(String args[]) {
		slmap = new ConcurrentSkipListMap<Integer, String>();
		tmap = new TreeMap<Integer, String>();
		int size = Integer.parseInt(args[0]);
		int numThreads = Integer.parseInt(args[1]);
		//System.out.println(size + "," + numThreads);
		populateMaps(size);
		for (int i = 0; i < numThreads; i++) {
			PerfThread pt = new PerfThread(tmap, slmap, size);
			pt.start();
		}
	}

}
