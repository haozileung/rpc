/*
 * HaoziLeung's Demo
 */

package com.haozileung.www.init

import com.haozileung.rpc.spring.RpcScanner
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor
import org.springframework.context.EnvironmentAware
import org.springframework.core.env.Environment

open class RpcClientConfig : BeanDefinitionRegistryPostProcessor, EnvironmentAware {
    override fun setEnvironment(environment: Environment?) {
    }

    override fun postProcessBeanFactory(beanFactory: ConfigurableListableBeanFactory) {
    }

    override fun postProcessBeanDefinitionRegistry(registry: BeanDefinitionRegistry) {
        val scanner = RpcScanner(registry)
        scanner.scan(*arrayOf("com.haozileung"))
    }
}