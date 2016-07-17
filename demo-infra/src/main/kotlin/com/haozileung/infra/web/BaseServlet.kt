package com.haozileung.infra.web;

import com.alibaba.fastjson.JSON
import com.google.common.base.Strings
import org.apache.commons.beanutils.BeanUtils
import org.apache.commons.beanutils.ConvertUtils
import org.apache.commons.beanutils.converters.DateConverter
import org.beetl.ext.servlet.ServletGroupTemplate
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException
import java.io.PrintWriter
import java.net.InetAddress
import java.net.UnknownHostException
import java.util.*
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

abstract class BaseServlet : HttpServlet() {
    val logger: Logger = LoggerFactory.getLogger(javaClass)
    override fun init() {
        Initializer.injector?.injectMembers(this);
    }

    fun isAjax(request: HttpServletRequest): Boolean {
        return "XMLHttpRequest".equals(request.getHeader("X-Requested-With"))
    }

    fun <T> getBean(t: T, request: HttpServletRequest): T? {
        try {
            val convert = DateConverter()
            val p = arrayOf("yyyyMMdd", "yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss")
            convert.patterns = p
            ConvertUtils.register(convert, Date::class.java)
            BeanUtils.populate(t, request.parameterMap);
        } catch(e: Exception) {
            logger.error(e.message)
        }
        return t;
    }

    fun getIpAddr(request: HttpServletRequest): String {
        var ipAddress: String? = request.getHeader("x-forwarded-for");
        if (ipAddress == null || ipAddress.length == 0 || "unknown".equals(ipAddress, true)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.length == 0 || "unknown".equals(ipAddress, true)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.length == 0 || "unknown".equals(ipAddress, true)) {
            ipAddress = request.remoteAddr;
            if (ipAddress.equals("127.0.0.1") || ipAddress.equals("0:0:0:0:0:0:0:1")) {
                //根据网卡取本机配置的IP
                var inet: InetAddress?;
                try {
                    inet = InetAddress.getLocalHost()
                    ipAddress = inet.hostAddress
                } catch (e: UnknownHostException) {
                    logger.error(e.message)
                }
            }
        }
        //对于通过多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
        if (ipAddress != null && ipAddress.indexOf(",") > 0) {
            ipAddress = ipAddress.substring(0, ipAddress.indexOf(","))
        }
        return ipAddress!!
    }

    fun renderView(code: Int, view: String, req: HttpServletRequest, resp: HttpServletResponse) {
        resp.contentType = "text/html;charset=utf-8";
        resp.status = code;
        if (!Strings.isNullOrEmpty(view)) {
            try {
                ServletGroupTemplate.instance().render(view, req, resp);
            } catch (e: Exception) {
                logger.error(e.message, e);
                try {
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                } catch (e1: IOException) {
                    logger.error(e1.message, e1);
                }
            }
        }
    }

    fun renderJSON(code: Int, data: Any?, resp: HttpServletResponse) {
        resp.contentType = "application/json;charset=utf-8";
        resp.status = code;
        try {
            var pr: PrintWriter = resp.writer;
            pr.print(JSON.toJSONString(data));
            pr.flush();
        } catch (e: IOException) {
            logger.error(e.message, e);
        }
    }

    fun render(result: Any?, req: HttpServletRequest, resp: HttpServletResponse) {
        if (result != null) {
            if (result is String) {
                if (result.startsWith("redirect:")) {
                    val v = result.replace("redirect:", "");
                    try {
                        resp.sendRedirect(v);
                        return
                    } catch (e: IOException) {
                        logger.error(e.message, e);
                    }
                }
                renderView(resp.status, result, req, resp);
            } else {
                renderJSON(resp.status, result, resp);
            }
        }
    }

    fun render(req: HttpServletRequest, resp: HttpServletResponse) {
        render(null, req, resp)
    }

    override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) {
        val obj: Any = get(req, resp);
        render(obj, req, resp);
    }


    override fun doPost(req: HttpServletRequest, resp: HttpServletResponse) {
        val obj: Any = post(req, resp);
        render(obj, req, resp);
    }

    override fun doPut(req: HttpServletRequest, resp: HttpServletResponse) {
        val obj: Any = put(req, resp);
        render(obj, req, resp);
    }

    override fun doDelete(req: HttpServletRequest, resp: HttpServletResponse) {
        val obj: Any = delete(req, resp);
        render(obj, req, resp);
    }

    open fun get(req: HttpServletRequest, resp: HttpServletResponse): Any {
        return Any();
    }

    open fun post(req: HttpServletRequest, resp: HttpServletResponse): Any {
        return Any();
    }

    open fun put(req: HttpServletRequest, resp: HttpServletResponse): Any {
        return Any();
    }

    open fun delete(req: HttpServletRequest, resp: HttpServletResponse): Any {
        return Any();
    }

}