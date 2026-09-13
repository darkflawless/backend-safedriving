package com.safedriving.service;

import com.safedriving.dto.request.RouteStopRequest;
import com.safedriving.dto.response.RouteStopResponse;

import java.util.List;

public interface RouteStopService {

    List<RouteStopResponse> getAllRouteStops(String routeId);

    RouteStopResponse getRouteStopById(String id);

    RouteStopResponse createRouteStop(RouteStopRequest request);

    RouteStopResponse updateRouteStop(String id, RouteStopRequest request);

    void deleteRouteStop(String id);
}
