package com.Helper;
import mazeBig.Point;


	public class Rational {
		public int numerator;
		public int denominator;

		public Rational(int a, int b) {
			numerator = a;
			denominator = b;
		}

		// more here...
		private int abs(int x) {
			return x < 0 ? -x : x;
		}

		private int gcd(int a, int b) {
			while (b != 0) {
				int t = b;
				b = a % b;
				a = t;
			}
			return a;
		}

		public Point reduce() {
			if (denominator < 0) {
				denominator = -denominator;
				numerator = -numerator;
			}
			int d = gcd(abs(numerator), denominator);
			numerator /= d;
			denominator /= d;

			return new Point(numerator, denominator);
		}
	}
