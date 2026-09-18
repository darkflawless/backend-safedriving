package com.safedriving.service;

import com.safedriving.dto.request.DriverRequest;
import com.safedriving.dto.request.IdReference;
import com.safedriving.dto.request.LicenseClassReference;
import com.safedriving.dto.response.DriverResponse;
import com.safedriving.entity.Driver;
import com.safedriving.entity.LicenseClass;
import com.safedriving.entity.Staff;
import com.safedriving.entity.Vehicle;
import com.safedriving.entity.enums.Gender;
import com.safedriving.entity.enums.VehicleStatus;
import com.safedriving.exception.BadRequestException;
import com.safedriving.exception.ResourceNotFoundException;
import com.safedriving.repository.DriverRepository;
import com.safedriving.repository.LicenseClassRepository;
import com.safedriving.repository.StaffRepository;
import com.safedriving.repository.VehicleRepository;
import com.safedriving.service.impl.DriverServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DriverServiceTest {

    @Mock
    private DriverRepository driverRepository;

    @Mock
    private StaffRepository staffRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private LicenseClassRepository licenseClassRepository;

    @InjectMocks
    private DriverServiceImpl driverService;

    private Driver testDriver;
    private Staff testStaff;
    private Vehicle testVehicle;
    private LicenseClass testLicenseClass;

    @BeforeEach
    void setUp() {
        testStaff = Staff.builder()
                .id("staff-1")
                .firstName("Van A")
                .lastName("Nguyen")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .gender(Gender.MALE)
                .phone("0901234567")
                .build();

        testVehicle = Vehicle.builder()
                .id("veh-1")
                .plateNumber("29B-12345")
                .vin("VIN123456789")
                .capacity(45)
                .status(VehicleStatus.AVAILABLE)
                .build();

        testLicenseClass = LicenseClass.builder()
                .id((short) 1)
                .code("E")
                .name("Hạng E - Xe khách trên 30 chỗ")
                .capacity(45)
                .build();

        testDriver = Driver.builder()
                .id("driver-1")
                .hireDate(LocalDate.of(2022, 5, 10))
                .urlImage("https://example.com/avatar.jpg")
                .isActive(true)
                .staff(testStaff)
                .vehicle(testVehicle)
                .licenseClass(testLicenseClass)
                .isDeleted(false)
                .build();
    }

    @Test
    @DisplayName("getAllDrivers - Lấy danh sách tài xế chưa bị xóa thành công")
    void getAllDrivers_Success() {
        when(driverRepository.findByIsDeletedFalse()).thenReturn(List.of(testDriver));

        List<DriverResponse> responses = driverService.getAllDrivers();

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("driver-1", responses.get(0).getId());
        assertEquals("Van A", responses.get(0).getStaff().getFirstName());
        assertEquals("29B-12345", responses.get(0).getVehicle().getPlateNumber());
        assertEquals("E", responses.get(0).getLicenseClass().getCode());
    }

    @Test
    @DisplayName("getDriverById - Thành công khi ID tồn tại")
    void getDriverById_Success() {
        when(driverRepository.findByIdAndIsDeletedFalse("driver-1")).thenReturn(Optional.of(testDriver));

        DriverResponse response = driverService.getDriverById("driver-1");

        assertNotNull(response);
        assertEquals("driver-1", response.getId());
        assertEquals(LocalDate.of(2022, 5, 10), response.getHireDate());
    }

    @Test
    @DisplayName("getDriverById - Thất bại ném ResourceNotFoundException khi không tìm thấy")
    void getDriverById_NotFound() {
        when(driverRepository.findByIdAndIsDeletedFalse("driver-999")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> driverService.getDriverById("driver-999"));
    }

    @Test
    @DisplayName("createDriver - Thành công với đầy đủ quan hệ staff, vehicle, licenseClass")
    void createDriver_Success() {
        DriverRequest request = DriverRequest.builder()
                .hireDate(LocalDate.of(2023, 1, 15))
                .urlImage("https://example.com/driver2.jpg")
                .isActive(true)
                .staff(IdReference.builder().id("staff-1").build())
                .vehicle(IdReference.builder().id("veh-1").build())
                .licenseClass(LicenseClassReference.builder().id(1).build())
                .build();

        when(staffRepository.findById("staff-1")).thenReturn(Optional.of(testStaff));
        when(driverRepository.existsByStaffId("staff-1")).thenReturn(false);
        when(vehicleRepository.findByIdAndIsDeletedFalse("veh-1")).thenReturn(Optional.of(testVehicle));
        when(licenseClassRepository.findById((short) 1)).thenReturn(Optional.of(testLicenseClass));
        when(driverRepository.save(any(Driver.class))).thenAnswer(invocation -> {
            Driver d = invocation.getArgument(0);
            d.setId("driver-2");
            return d;
        });

        DriverResponse response = driverService.createDriver(request);

        assertNotNull(response);
        assertEquals("driver-2", response.getId());
        assertEquals("Van A", response.getStaff().getFirstName());
        assertEquals("29B-12345", response.getVehicle().getPlateNumber());
        assertEquals("E", response.getLicenseClass().getCode());
        verify(driverRepository).save(any(Driver.class));
    }

    @Test
    @DisplayName("createDriver - Thất bại khi thiếu thông tin nhân sự (staff)")
    void createDriver_MissingStaff_ThrowsBadRequest() {
        DriverRequest request = DriverRequest.builder()
                .hireDate(LocalDate.of(2023, 1, 15))
                .build();

        assertThrows(BadRequestException.class, () -> driverService.createDriver(request));
    }

    @Test
    @DisplayName("createDriver - Thất bại khi nhân sự đã được tạo tài xế trước đó")
    void createDriver_StaffAlreadyAssigned_ThrowsBadRequest() {
        DriverRequest request = DriverRequest.builder()
                .hireDate(LocalDate.of(2023, 1, 15))
                .staff(IdReference.builder().id("staff-1").build())
                .build();

        when(staffRepository.findById("staff-1")).thenReturn(Optional.of(testStaff));
        when(driverRepository.existsByStaffId("staff-1")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> driverService.createDriver(request));
    }

    @Test
    @DisplayName("updateDriver - Cập nhật hồ sơ tài xế thành công")
    void updateDriver_Success() {
        DriverRequest updateRequest = DriverRequest.builder()
                .hireDate(LocalDate.of(2021, 1, 1))
                .urlImage("https://example.com/avatar-updated.jpg")
                .isActive(false)
                .staff(IdReference.builder().id("staff-1").build())
                .build();

        when(driverRepository.findByIdAndIsDeletedFalse("driver-1")).thenReturn(Optional.of(testDriver));
        when(driverRepository.save(any(Driver.class))).thenReturn(testDriver);

        DriverResponse response = driverService.updateDriver("driver-1", updateRequest);

        assertNotNull(response);
        assertEquals(LocalDate.of(2021, 1, 1), response.getHireDate());
        assertEquals("https://example.com/avatar-updated.jpg", response.getUrlImage());
        assertEquals(false, response.getIsActive());
    }

    @Test
    @DisplayName("deleteDriver - Xóa mềm tài xế thành công")
    void deleteDriver_Success() {
        when(driverRepository.findByIdAndIsDeletedFalse("driver-1")).thenReturn(Optional.of(testDriver));

        driverService.deleteDriver("driver-1");

        assertTrue(testDriver.getIsDeleted());
        verify(driverRepository).save(testDriver);
    }
}
