package org.jeecg.modules.polymerize.drawflow.model;

import java.io.Serializable;
import java.util.function.Function;

@FunctionalInterface
public interface IRuleNode<T, R> extends Function<T, R>, Serializable {
}
