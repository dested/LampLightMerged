package drawBig;

public class DColor {
	public int A;
	public int R;
	public int G;
	public int B;

	public DColor(int r, int g, int b) {

		if (r < 0)
			r = 0;
		R = r;
		if (g < 0)
			g = 0;
		G = g;
		if (b < 0)
			b = 0;
		B = b;
		A = 255;
	}

	public DColor(int a, int r, int g, int b) {
		if (a < 0)
			a = 0;
		A = a;
		if (r < 0)
			r = 0;
		R = r;
		if (g < 0)
			g = 0;
		G = g;
		if (b < 0)
			b = 0;
		B = b;
	}

	public static DColor Parse(String string) {
		String[] st = string.split("c");

		DColor a = new DColor();
		a.A = Integer.parseInt(st[0]);
		a.R = Integer.parseInt(st[1]);
		a.G = Integer.parseInt(st[2]);
		a.B = Integer.parseInt(st[3]);

		return a;
	}

	public DColor() {
	}

	@Override
	public String toString() {
		return A + "c" + R + "c" + G + "c" + B;
	}

	static java.util.Random r = new java.util.Random();

	public static DColor Random() {
		return new DColor(255, r.nextInt(255), r.nextInt(255), r.nextInt(255));

	}
}
