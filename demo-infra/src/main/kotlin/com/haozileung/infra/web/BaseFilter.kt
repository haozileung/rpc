package com.haozileung.infra.web;

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.servlet.Filter
import javax.servlet.FilterConfig

abstract class BaseFilter : Filter {
    val logger: Logger = LoggerFactory.getLogger(javaClass)
    override fun init(filterConfig: FilterConfig) {
        Initializer.injector?.injectMembers(this);
    }
}