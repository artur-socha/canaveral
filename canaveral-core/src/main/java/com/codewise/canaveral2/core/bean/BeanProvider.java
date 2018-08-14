package com.codewise.canaveral2.core.bean;

import java.lang.annotation.Annotation;
import java.util.Set;

public interface BeanProvider {

    Object findBeanOrThrow(Class<?> beanClass, Set<Annotation> knownAnnotations);
}
