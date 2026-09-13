package com.safedriving.service;

import com.safedriving.dto.request.RouteRequest;
import com.safedriving.dto.response.RouteResponse;

import java.util.List;

public interface RouteService {

    List<RouteResponse> getAllRoutes();

    RouteResponse getRouteById(String id);

    RouteResponse createRoute(RouteRequest request);

    RouteResponse updateRoute(String id, RouteRequest request);

    void deleteRoute(String id);
}
