package com.playwright.remote.engine.route

import com.playwright.remote.engine.route.api.IRoute

class Router {
    private var routes = arrayListOf<RouteInfo>()

    private class RouteInfo(
        val matcher: UrlMatcher,
        val handler: (IRoute) -> Unit
    )

    fun add(matcher: UrlMatcher, handler: (IRoute) -> Unit) = routes.add(0, RouteInfo(matcher, handler))

    fun remove(matcher: UrlMatcher, handler: ((IRoute) -> Unit)?) {
        routes = routes.filter {
            it.matcher != matcher || (handler != null && it.handler != handler)
        } as ArrayList<RouteInfo>
    }

    fun size(): Int = routes.size

    fun handle(_route: IRoute): Boolean {
        for (route in routes) {
            if (route.matcher.test(_route.request().url())) {
                route.handler(_route)
                return true
            }
        }
        return false
    }
}