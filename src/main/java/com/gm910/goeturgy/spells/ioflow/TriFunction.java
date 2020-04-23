package com.gm910.goeturgy.spells.ioflow;

@FunctionalInterface
public interface TriFunction<S1, S2, S3, R> {

	public R apply(S1 o, S2 p, S3 q);
}
