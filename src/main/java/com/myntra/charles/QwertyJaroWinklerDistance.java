package com.myntra.charles;

import java.util.Arrays;

import org.apache.lucene.search.spell.StringDistance;

/**
 * QWERTY distance implementation of JaroWinkler distance.
 * 
 * @author 11169
 * 
 * @see <a href="http://en.wikipedia.org/wiki/Jaro%E2%80%93Winkler_distance">
 *      http://en.wikipedia.org/wiki/Jaro%E2%80%93Winkler_distance</a>
 */
public class QwertyJaroWinklerDistance implements StringDistance {

	private float threshold = 0.7f;
	private float[][] operationCost = new float[26][26];

	/**
	 * Creates a new distance metric with the default threshold for the Jaro
	 * Winkler bonus (0.7)
	 * 
	 * @see #setThreshold(float)
	 */
	public QwertyJaroWinklerDistance() {
	}

	private float[] qwertyMatches(String s1, String s2) {
		String max, min;
		if (s1.length() > s2.length()) {
			max = s1;
			min = s2;
		} else {
			max = s2;
			min = s1;
		}
		int range = Math.max(max.length() / 2 - 1, 0);
		int[] matchIndexes = new int[min.length()];
		Arrays.fill(matchIndexes, -1);
		boolean[] matchFlags = new boolean[max.length()];
		int matches = 0;
		float qwertyMatches = 0.f;
		for (int mi = 0; mi < min.length(); mi++) {
			char c1 = min.charAt(mi);
			float prevQwertyMatches = qwertyMatches;
			for (int xi = Math.max(mi - range, 0), xn = Math.min(mi + range + 1, max.length()); xi < xn; xi++) {
				if (!matchFlags[xi] && c1 == max.charAt(xi)) {
					matchIndexes[mi] = xi;
					matchFlags[xi] = true;
					matches++;
					qwertyMatches = Float.sum(qwertyMatches, 1.00f);
					break;
				}
			}
			if (prevQwertyMatches == qwertyMatches) {
				if (!matchFlags[mi]) {
					char targetAscii = Character.toUpperCase(c1);
					char otherAscii = Character.toUpperCase(max.charAt(mi));
					if (otherAscii >= 65 && otherAscii <= 90 && targetAscii >= 65 && targetAscii <= 90) {
						qwertyMatches = Float.sum(qwertyMatches, operationCost[targetAscii - 65][otherAscii - 65]);
					}
				}
			}
		}
		char[] ms1 = new char[matches];
		char[] ms2 = new char[matches];
		for (int i = 0, si = 0; i < min.length(); i++) {
			if (matchIndexes[i] != -1) {
				ms1[si] = min.charAt(i);
				si++;
			}
		}
		for (int i = 0, si = 0; i < max.length(); i++) {
			if (matchFlags[i]) {
				ms2[si] = max.charAt(i);
				si++;
			}
		}
		int transpositions = 0;
		for (int mi = 0; mi < ms1.length; mi++) {
			if (ms1[mi] != ms2[mi]) {
				transpositions++;
			}
		}
		int prefix = 0;
		for (int mi = 0; mi < min.length(); mi++) {
			if (s1.charAt(mi) == s2.charAt(mi)) {
				prefix++;
			} else {
				break;
			}
		}
		return new float[] { qwertyMatches, transpositions / 2, prefix, max.length() };
	}

	 @Override
	public float getDistance(String s1, String s2) {
		this.initializeCostMatrix();
		float[] mtp = qwertyMatches(s1.toUpperCase(), s2.toUpperCase());
		float m = mtp[0];
		if (m == 0) {
			return 0f;
		}
		float j = ((m / s1.length() + m / s2.length() + (m - mtp[1]) / m)) / 3;
		float jw = j < getThreshold() ? j : j + Math.min(0.1f, 1f / mtp[3]) * mtp[2] * (1 - j);
		return jw;
	}

	/**
	 * Sets the threshold used to determine when Winkler bonus should be used.
	 * Set to a negative value to get the Jaro distance.
	 * 
	 * @param threshold
	 *            the new value of the threshold
	 */
	public void setThreshold(float threshold) {
		this.threshold = threshold;
	}

	/**
	 * Returns the current value of the threshold used for adding the Winkler
	 * bonus. The default value is 0.7.
	 * 
	 * @return the current value of the threshold
	 */
	public float getThreshold() {
		return threshold;
	}

	@Override
	public int hashCode() {
		return 113 * Float.floatToIntBits(threshold) * getClass().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (null == obj || getClass() != obj.getClass())
			return false;

		QwertyJaroWinklerDistance o = (QwertyJaroWinklerDistance) obj;
		return (Float.floatToIntBits(o.threshold) == Float.floatToIntBits(this.threshold));
	}

	@Override
	public String toString() {
		return "jarowinkler(" + threshold + ")";
	}

	private void initializeCostMatrix() {
		float[][] temp = this.operationCost;

		for (int i = 0; i < temp.length; ++i) {
			float[] row = temp[i];
			Arrays.fill(row, 0.f);
		}

		addOperationCost('q', 'w', 0.5f);
		addOperationCost('q', 'a', 0.5f);
		addOperationCost('q', 's', 0.3f);

		addOperationCost('w', 'q', 0.5f);
		addOperationCost('w', 'e', 0.5f);
		addOperationCost('w', 's', 0.5f);
		addOperationCost('w', 'a', 0.3f);
		addOperationCost('w', 'd', 0.3f);

		addOperationCost('e', 'w', 0.5f);
		addOperationCost('e', 'r', 0.5f);
		addOperationCost('e', 'd', 0.5f);
		addOperationCost('e', 's', 0.3f);
		addOperationCost('e', 'f', 0.3f);

		addOperationCost('r', 'e', 0.5f);
		addOperationCost('r', 't', 0.5f);
		addOperationCost('r', 'f', 0.5f);
		addOperationCost('r', 'd', 0.3f);
		addOperationCost('r', 'g', 0.3f);

		addOperationCost('t', 'r', 0.5f);
		addOperationCost('t', 'y', 0.5f);
		addOperationCost('t', 'g', 0.5f);
		addOperationCost('t', 'f', 0.3f);
		addOperationCost('t', 'h', 0.3f);

		addOperationCost('y', 't', 0.5f);
		addOperationCost('y', 'u', 0.5f);
		addOperationCost('y', 'h', 0.5f);
		addOperationCost('y', 'g', 0.3f);
		addOperationCost('y', 'j', 0.3f);

		addOperationCost('u', 'y', 0.5f);
		addOperationCost('u', 'i', 0.5f);
		addOperationCost('u', 'j', 0.5f);
		addOperationCost('u', 'h', 0.3f);
		addOperationCost('u', 'k', 0.3f);

		addOperationCost('i', 'u', 0.5f);
		addOperationCost('i', 'o', 0.5f);
		addOperationCost('i', 'k', 0.5f);
		addOperationCost('i', 'j', 0.3f);
		addOperationCost('i', 'l', 0.3f);

		addOperationCost('o', 'i', 0.5f);
		addOperationCost('o', 'p', 0.5f);
		addOperationCost('o', 'l', 0.5f);
		addOperationCost('o', 'k', 0.3f);

		addOperationCost('p', 'o', 0.5f);
		addOperationCost('p', 'l', 0.3f);

		addOperationCost('a', 'q', 0.5f);
		addOperationCost('a', 's', 0.5f);
		addOperationCost('a', 'z', 0.3f);
		addOperationCost('a', 'w', 0.3f);
		addOperationCost('a', 'x', 0.1f);

		addOperationCost('s', 'a', 0.5f);
		addOperationCost('s', 'd', 0.5f);
		addOperationCost('s', 'w', 0.5f);
		addOperationCost('s', 'x', 0.3f);
		addOperationCost('s', 'q', 0.3f);
		addOperationCost('s', 'e', 0.3f);
		addOperationCost('s', 'z', 0.3f);
		addOperationCost('s', 'c', 0.1f);

		addOperationCost('d', 'e', 0.5f);
		addOperationCost('d', 's', 0.5f);
		addOperationCost('d', 'f', 0.5f);
		addOperationCost('d', 'c', 0.3f);
		addOperationCost('d', 'w', 0.3f);
		addOperationCost('d', 'r', 0.3f);
		addOperationCost('d', 'x', 0.3f);
		addOperationCost('d', 'v', 0.1f);

		addOperationCost('f', 'r', 0.5f);
		addOperationCost('f', 'd', 0.5f);
		addOperationCost('f', 'g', 0.5f);
		addOperationCost('f', 'v', 0.3f);
		addOperationCost('f', 'e', 0.3f);
		addOperationCost('f', 't', 0.3f);
		addOperationCost('f', 'c', 0.3f);
		addOperationCost('f', 'b', 0.1f);

		addOperationCost('g', 't', 0.5f);
		addOperationCost('g', 'f', 0.5f);
		addOperationCost('g', 'h', 0.5f);
		addOperationCost('g', 'b', 0.3f);
		addOperationCost('g', 'r', 0.3f);
		addOperationCost('g', 'y', 0.3f);
		addOperationCost('g', 'v', 0.3f);
		addOperationCost('g', 'n', 0.1f);

		addOperationCost('h', 'y', 0.5f);
		addOperationCost('h', 'g', 0.5f);
		addOperationCost('h', 'j', 0.5f);
		addOperationCost('h', 'n', 0.3f);
		addOperationCost('h', 't', 0.3f);
		addOperationCost('h', 'u', 0.3f);
		addOperationCost('h', 'b', 0.3f);
		addOperationCost('h', 'm', 0.1f);

		addOperationCost('j', 'u', 0.5f);
		addOperationCost('j', 'h', 0.5f);
		addOperationCost('j', 'k', 0.5f);
		addOperationCost('j', 'm', 0.3f);
		addOperationCost('j', 'n', 0.3f);
		addOperationCost('j', 'y', 0.3f);
		addOperationCost('j', 'i', 0.3f);

		addOperationCost('k', 'i', 0.5f);
		addOperationCost('k', 'j', 0.5f);
		addOperationCost('k', 'l', 0.5f);
		addOperationCost('k', 'm', 0.3f);
		addOperationCost('k', 'u', 0.3f);
		addOperationCost('k', 'o', 0.3f);

		addOperationCost('l', 'o', 0.5f);
		addOperationCost('l', 'k', 0.5f);
		addOperationCost('l', 'i', 0.3f);
		addOperationCost('l', 'p', 0.3f);

		addOperationCost('z', 'x', 0.5f);
		addOperationCost('z', 'a', 0.3f);
		addOperationCost('z', 's', 0.3f);

		addOperationCost('x', 'z', 0.5f);
		addOperationCost('x', 'c', 0.5f);
		addOperationCost('x', 's', 0.3f);
		addOperationCost('x', 'd', 0.3f);

		addOperationCost('c', 'x', 0.5f);
		addOperationCost('c', 'v', 0.5f);
		addOperationCost('c', 'd', 0.3f);
		addOperationCost('c', 'f', 0.3f);

		addOperationCost('v', 'c', 0.5f);
		addOperationCost('v', 'b', 0.5f);
		addOperationCost('v', 'f', 0.3f);
		addOperationCost('v', 'g', 0.3f);

		addOperationCost('b', 'v', 0.5f);
		addOperationCost('b', 'n', 0.5f);
		addOperationCost('b', 'g', 0.3f);
		addOperationCost('b', 'h', 0.3f);

		addOperationCost('n', 'b', 0.5f);
		addOperationCost('n', 'm', 0.5f);
		addOperationCost('n', 'h', 0.3f);
		addOperationCost('n', 'j', 0.3f);

		addOperationCost('m', 'n', 0.5f);
		addOperationCost('m', 'j', 0.3f);
		addOperationCost('m', 'k', 0.3f);

	}

	private void addOperationCost(char pivot, char c, float cost) {
		this.operationCost[toIndex(pivot)][toIndex(c)] = cost;
	}

	private int toIndex(char c) {
		c = Character.toUpperCase(c);
		return c - 65;
	}

}