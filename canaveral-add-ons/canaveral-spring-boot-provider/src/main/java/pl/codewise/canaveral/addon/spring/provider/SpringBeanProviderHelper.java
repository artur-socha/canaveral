package pl.codewise.canaveral.addon.spring.provider;

import com.google.common.base.Preconditions;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

class SpringBeanProviderHelper {

    static Object getBean(Class<?> beanClass, Set<Annotation> knownAnnotations, ApplicationContext context) {
        List<String> qualifiers = knownAnnotations.stream()
                .filter(annotation -> annotation instanceof Qualifier)
                .map(qualifier -> ((Qualifier) qualifier).value())
                .collect(Collectors.toList());

        Preconditions.checkArgument(qualifiers.size() < 2, "Too many qualifiers " + qualifiers);

        if (qualifiers.size() == 1) {
            return context.getBean(qualifiers.get(0), qualifiers);
        } else {
            return context.getBean(beanClass);
        }
    }
}
    
