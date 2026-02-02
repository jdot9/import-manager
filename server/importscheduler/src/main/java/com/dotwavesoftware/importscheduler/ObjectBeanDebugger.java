/*

package com.dotwavesoftware.importscheduler;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.boot.CommandLineRunner;


@Component
public class ObjectBeanDebugger implements CommandLineRunner {

    private final ApplicationContext ctx;

    public ObjectBeanDebugger(ApplicationContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public void run(String... args) throws Exception {
        String[] objectBeans = ctx.getBeanNamesForType(Object.class);
        System.out.println("=== Beans of type Object in context ===");
        for (String name : objectBeans) {
            System.out.println(name + " -> " + ctx.getBean(name).getClass());
        }
    }
}

*/