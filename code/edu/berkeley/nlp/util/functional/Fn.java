package edu.berkeley.nlp.util.functional;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
* User: aria42
* Date: Oct 9, 2008
* Time: 6:31:23 PM
*/
public interface Fn<I,O> extends Serializable {
	public O apply(I input);

  public static class ConstantFn<I,O> implements Fn<I,O>
  {

    private O c;

    public ConstantFn(O c) {
      this.c = c;  
    }

    public O apply(I input) {
      return  c;
    }
  }

	public static class IdentityFn<I> implements Fn<I, I>
	{

		public I apply(I input)
		{
			return input;
		}
	}
  
}

