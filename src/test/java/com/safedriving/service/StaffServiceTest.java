package com.safedriving.service;

import com.safedriving.dto.request.IdReference;
import com.safedriving.dto.request.StaffRequest;
import com.safedriving.dto.response.StaffResponse;
import com.safedriving.entity.Account;
import com.safedriving.entity.Staff;
import com.safedriving.entity.enums.AccountRole;
import com.safedriving.entity.enums.AccountStatus;
import com.safedriving.entity.enums.Gender;
import com.safedriving.exception.BadRequestException;
import com.safedriving.exception.ResourceNotFoundException;
import com.safedriving.repository.AccountRepository;
import com.safedriving.repository.DriverRepository;
import com.safedriving.repository.StaffRepository;
import com.safedriving.service.impl.StaffServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StaffServiceTest {

    @Mock
    private StaffRepository staffRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private DriverRepository driverRepository;

    @InjectMocks
    private StaffServiceImpl staffService;

    private Staff testStaff;
    private Account testAccount;

    @BeforeEach
    void setUp() {
        testAccount = Account.builder()
                .id("acc-1")
                .username("vana")
                .role(AccountRole.ADMIN)
                .status(AccountStatus.ACTIVE)
                .build();

        testStaff = Staff.builder()
                .id("staff-1")
                .firstName("Van A")
                .lastName("Nguyen")
                .dateOfBirth(LocalDate.of(1995, 5, 20))
                .gender(Gender.MALE)
                .email("vana@example.com")
                .phone("0901234567")
                .exactAddress("123 Phố Huế")
                .commune("Hàng Bài")
                .province("Hà Nội")
                .account(testAccount)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("getAllStaff - Lấy toàn bộ danh sách nhân viên thành công")
    void getAllStaff_Success() {
        when(staffRepository.findAll()).thenReturn(List.of(testStaff));

        List<StaffResponse> responses = staffService.getAllStaff();

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("Van A", responses.get(0).getFirstName());
        assertEquals("Van A Nguyen", responses.get(0).getFullName());
        assertEquals("vana@example.com", responses.get(0).getEmail());
        assertEquals("123 Phố Huế", responses.get(0).getExactAddress());
        assertEquals("Hàng Bài", responses.get(0).getCommune());
        assertEquals("Hà Nội", responses.get(0).getProvince());
        assertNotNull(responses.get(0).getAccount());
        assertEquals("vana", responses.get(0).getAccount().getUsername());
    }

    @Test
    @DisplayName("getStaffById - Thành công khi ID tồn tại")
    void getStaffById_Success() {
        when(staffRepository.findById("staff-1")).thenReturn(Optional.of(testStaff));

        StaffResponse response = staffService.getStaffById("staff-1");

        assertNotNull(response);
        assertEquals("staff-1", response.getId());
        assertEquals("Nguyen", response.getLastName());
    }

    @Test
    @DisplayName("getStaffById - Thất bại ném ResourceNotFoundException khi ID không tồn tại")
    void getStaffById_NotFound() {
        when(staffRepository.findById("staff-non-existent")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> staffService.getStaffById("staff-non-existent"));
    }

    @Test
    @DisplayName("createStaff - Tạo nhân sự thành công với địa chỉ và tài khoản")
    void createStaff_Success() {
        StaffRequest request = StaffRequest.builder()
                .firstName("Thi B")
                .lastName("Tran")
                .dateOfBirth(LocalDate.of(1998, 8, 15))
                .gender(Gender.FEMALE)
                .email("thib@example.com")
                .phone("0987654321")
                .exactAddress("456 Cầu Giấy")
                .commune("Dịch Vọng")
                .province("Hà Nội")
                .account(IdReference.builder().id("acc-1").build())
                .build();

        when(accountRepository.findById("acc-1")).thenReturn(Optional.of(testAccount));
        when(staffRepository.existsByAccountId("acc-1")).thenReturn(false);
        when(staffRepository.save(any(Staff.class))).thenAnswer(invocation -> {
            Staff s = invocation.getArgument(0);
            s.setId("staff-2");
            return s;
        });

        StaffResponse response = staffService.createStaff(request);

        assertNotNull(response);
        assertEquals("staff-2", response.getId());
        assertEquals("Thi B Tran", response.getFullName());
        assertEquals(Gender.FEMALE, response.getGender());
        assertEquals("456 Cầu Giấy", response.getExactAddress());
        assertEquals("Dịch Vọng", response.getCommune());
        assertEquals("Hà Nội", response.getProvince());
        verify(staffRepository).save(any(Staff.class));
    }

    @Test
    @DisplayName("createStaff - Thất bại khi accountId đã được gán cho nhân sự khác")
    void createStaff_AccountAlreadyLinked() {
        StaffRequest request = StaffRequest.builder()
                .firstName("Thi B")
                .lastName("Tran")
                .dateOfBirth(LocalDate.of(1998, 8, 15))
                .gender(Gender.FEMALE)
                .phone("0987654321")
                .account(IdReference.builder().id("acc-1").build())
                .build();

        when(accountRepository.findById("acc-1")).thenReturn(Optional.of(testAccount));
        when(staffRepository.existsByAccountId("acc-1")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> staffService.createStaff(request));
    }

    @Test
    @DisplayName("createStaff - Tạo nhân sự thành công khi không truyền address và account")
    void createStaff_WithoutAddressAndAccount() {
        StaffRequest request = StaffRequest.builder()
                .firstName("Van C")
                .lastName("Le")
                .dateOfBirth(LocalDate.of(2000, 1, 1))
                .gender(Gender.OTHER)
                .phone("0912345678")
                .build();

        when(staffRepository.save(any(Staff.class))).thenAnswer(invocation -> {
            Staff s = invocation.getArgument(0);
            s.setId("staff-3");
            return s;
        });

        StaffResponse response = staffService.createStaff(request);

        assertNotNull(response);
        assertEquals("staff-3", response.getId());
        assertNull(response.getExactAddress());
        assertNull(response.getCommune());
        assertNull(response.getProvince());
        assertNull(response.getAccount());
    }

    @Test
    @DisplayName("updateStaff - Cập nhật thông tin nhân viên thành công")
    void updateStaff_Success() {
        StaffRequest updateRequest = StaffRequest.builder()
                .firstName("Van A Mới")
                .lastName("Nguyen")
                .dateOfBirth(LocalDate.of(1995, 5, 20))
                .gender(Gender.MALE)
                .email("vana_new@example.com")
                .phone("0909999999")
                .exactAddress("789 Kim Mã")
                .commune("Ngọc Khánh")
                .province("Hà Nội")
                .account(IdReference.builder().id("acc-1").build())
                .build();

        when(staffRepository.findById("staff-1")).thenReturn(Optional.of(testStaff));
        when(accountRepository.findById("acc-1")).thenReturn(Optional.of(testAccount));
        when(staffRepository.existsByAccountIdAndIdNot("acc-1", "staff-1")).thenReturn(false);
        when(staffRepository.save(any(Staff.class))).thenReturn(testStaff);

        StaffResponse response = staffService.updateStaff("staff-1", updateRequest);

        assertNotNull(response);
        assertEquals("Van A Mới", response.getFirstName());
        assertEquals("0909999999", response.getPhone());
        assertEquals("vana_new@example.com", response.getEmail());
    }

    @Test
    @DisplayName("deleteStaff - Xóa thành công khi không vướng quan hệ Driver")
    void deleteStaff_Success() {
        when(staffRepository.findById("staff-1")).thenReturn(Optional.of(testStaff));
        when(driverRepository.existsByStaffId("staff-1")).thenReturn(false);

        staffService.deleteStaff("staff-1");

        verify(staffRepository).delete(testStaff);
    }

    @Test
    @DisplayName("deleteStaff - Thất bại ném BadRequestException khi nhân sự đang làm tài xế")
    void deleteStaff_BoundToDriver_ThrowsException() {
        when(staffRepository.findById("staff-1")).thenReturn(Optional.of(testStaff));
        when(driverRepository.existsByStaffId("staff-1")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> staffService.deleteStaff("staff-1"));
    }
}
