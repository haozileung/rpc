package com.haozileung.rpc.spring;

import com.haozileung.rpc.common.annotation.RpcService;
import com.haozileung.rpc.client.netty.NettyClientFactory;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.util.Set;

public class RpcScanner extends ClassPathBeanDefinitionScanner {
    private final NettyClientFactory clientFactory;

    public RpcScanner(BeanDefinitionRegistry registry) {
        super(registry);
        clientFactory = new NettyClientFactory();
    }

    public void registerDefaultFilters() {
        this.addIncludeFilter(new AnnotationTypeFilter(RpcService.class));
    }

    public boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
        return beanDefinition.getMetadata()
                .hasAnnotation(RpcService.class.getName());
    }

    @Override
    protected Set<BeanDefinitionHolder> doScan(String... basePackages) {
        Set<BeanDefinitionHolder> beanDefinitions = super.doScan(basePackages);
        for (BeanDefinitionHolder holder : beanDefinitions) {
            GenericBeanDefinition definition = (GenericBeanDefinition) holder.getBeanDefinition();
            try {
                getClass().getClassLoader().loadClass(definition.getBeanClassName());
                definition.getPropertyValues().add("invokerInterface", definition.getBeanClassName());
                definition.getPropertyValues().add("clientFactory", clientFactory);
                definition.setDestroyMethodName("close");
                definition.setBeanClass(SpringProxyFactoryBean.class);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return beanDefinitions;
    }
}
