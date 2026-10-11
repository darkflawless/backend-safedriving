package com.safedriving.service.impl;

import com.safedriving.dto.request.IncidentRequest;
import com.safedriving.dto.response.IncidentResponse;
import com.safedriving.entity.Incident;
import com.safedriving.entity.Trip;
import com.safedriving.exception.ResourceNotFoundException;
import com.safedriving.repository.IncidentRepository;
import com.safedriving.repository.TripRepository;
import com.safedriving.service.IncidentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IncidentServiceImpl implements IncidentService {

    private final IncidentRepository incidentRepository;
    private final TripRepository tripRepository;

    @Override
    public List<IncidentResponse> getAll() {
        return incidentRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public IncidentResponse getById(String id) {
        Incident entity = incidentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sự cố ID: " + id));
        return mapToResponse(entity);
    }

    @Override
    public List<IncidentResponse> getByTripId(String tripId) {
        return incidentRepository.findByTripId(tripId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public IncidentResponse create(IncidentRequest request) {
        Trip trip = tripRepository.findByIdAndIsDeletedFalse(request.getTripId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chuyến đi ID: " + request.getTripId()));

        Incident incident = Incident.builder()
                .trip(trip)
                .timeIncident(request.getTimeIncident())
                .type(request.getType())
                .description(request.getDescription())
                .severity(request.getSeverity())
                .build();

        return mapToResponse(incidentRepository.save(incident));
    }

    @Override
    @Transactional
    public IncidentResponse update(String id, IncidentRequest request) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sự cố ID: " + id));

        Trip trip = tripRepository.findByIdAndIsDeletedFalse(request.getTripId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chuyến đi ID: " + request.getTripId()));

        incident.setTrip(trip);
        incident.setTimeIncident(request.getTimeIncident());
        incident.setType(request.getType());
        incident.setDescription(request.getDescription());
        incident.setSeverity(request.getSeverity());

        return mapToResponse(incidentRepository.save(incident));
    }

    @Override
    @Transactional
    public void delete(String id) {
        if (!incidentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Không tìm thấy sự cố ID: " + id);
        }
        incidentRepository.deleteById(id);
    }

    private IncidentResponse mapToResponse(Incident entity) {
        return IncidentResponse.builder()
                .id(entity.getId())
                .tripId(entity.getTrip().getId())
                .timeIncident(entity.getTimeIncident())
                .type(entity.getType())
                .description(entity.getDescription())
                .severity(entity.getSeverity())
                .build();
    }
}
