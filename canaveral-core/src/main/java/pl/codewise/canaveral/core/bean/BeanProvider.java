package pl.codewise.canaveral.core.bean;

import java.lang.annotation.Annotation;
import java.util.Set;

public interface BeanProvider {

    Object findBeanOrThrow(Class<?> beanClass, Set<Annotation> knownAnnotations);
}
