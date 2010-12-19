package problem;

import java.util.Random;

/**
 * String portion taken from http://stackoverflow.com/questions/41107/how-to-generate-a-random-alpha-numeric-string-in-java
 *
 * Used to do really basic fuzzing on names the user might type
 */
public class RandomValues
{

	private static final char[] symbols = new char[36];

	static
	{
		for(int idx = 0; idx < 10; ++idx)
		{
			symbols[idx] = (char) ('0' + idx);
		}
		for(int idx = 10; idx < 36; ++idx)
		{
			symbols[idx] = (char) ('a' + idx - 10);
		}
	}
	private final Random random = new Random();
	private char[] buf;

	/**
	 * Returns a new random string 1-1000 characters long
	 * @return Random string using the established character set
	 */
	public String nextString()
	{
		return nextString(random.nextInt(999) + 1);
	}

	/**
	 * Returns a new random string of the specified length
	 * @param length Length of string (1 or more) to produce
	 * @return String composed of the established character set
	 */
	public String nextString(int length)
	{
		if(length < 1)
			throw new IllegalArgumentException("length < 1: " + length);

		buf = new char[length];
		for(int idx = 0; idx < buf.length; ++idx)
		{
			buf[idx] = symbols[random.nextInt(symbols.length)];
		}
		return new String(buf);
	}

	/**
	 * Returns a random double value in the full range of Doubles
	 * @return Pseudo-random double value
	 */
	public Double nextDouble()
	{
		return random.nextDouble();
	}
}
