package com.safedriving.service.impl;

import com.safedriving.dto.request.LoginRequest;
import com.safedriving.dto.request.RegisterRequest;
import com.safedriving.dto.response.AuthResponse;
import com.safedriving.dto.response.StaffAccountInfo;
import com.safedriving.dto.response.StaffResponse;
import com.safedriving.entity.Account;
import com.safedriving.entity.Staff;
import com.safedriving.entity.enums.AccountRole;
import com.safedriving.entity.enums.AccountStatus;
import com.safedriving.exception.BadRequestException;
import com.safedriving.repository.AccountRepository;
import com.safedriving.repository.StaffRepository;
import com.safedriving.security.JwtTokenProvider;
import com.safedriving.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AccountRepository accountRepository;
    private final StaffRepository staffRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        log.info("Xử lý đăng nhập cho người dùng: {}", request.getUsername());

        Account account = accountRepository.findByUsernameAndIsDeletedFalse(request.getUsername())
                .orElseThrow(() -> new BadRequestException("Tên đăng nhập hoặc mật khẩu không chính xác"));

        if (!passwordEncoder.matches(request.getPassword(), account.getPassword())) {
            throw new BadRequestException("Tên đăng nhập hoặc mật khẩu không chính xác");
        }

        if (account.getStatus() == AccountStatus.LOCKED) {
            throw new BadRequestException("Tài khoản đã bị khóa. Vui lòng liên hệ quản trị viên.");
        }

        if (account.getStatus() == AccountStatus.DISABLED) {
            throw new BadRequestException("Tài khoản đã bị vô hiệu hóa.");
        }

        LocalDateTime now = LocalDateTime.now();
        account.setLastLoginAt(now);
        accountRepository.save(account);

        String token = jwtTokenProvider.generateToken(
                account.getUsername(),
                account.getRole().name(),
                account.getId()
        );

        log.info("Người dùng {} đăng nhập thành công với vai trò {}", account.getUsername(), account.getRole());

        StaffResponse staffResponse = staffRepository.findByAccountId(account.getId())
                .map(this::toStaffResponse)
                .orElse(null);

        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getJwtExpirationMs())
                .accountId(account.getId())
                .username(account.getUsername())
                .role(account.getRole())
                .status(account.getStatus())
                .lastLoginAt(now)
                .staff(staffResponse)
                .build();
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Xử lý đăng ký tài khoản Quản lý mới: {}", request.getUsername());

        String username = request.getUsername().trim();
        if (accountRepository.existsByUsername(username)) {
            throw new BadRequestException("Tên đăng nhập đã tồn tại trong hệ thống");
        }

        String phone = request.getPhone().trim();
        if (staffRepository.existsByPhone(phone)) {
            throw new BadRequestException("Số điện thoại đã được sử dụng bởi một nhân sự khác");
        }

        String email = request.getEmail() != null && !request.getEmail().isBlank()
                ? request.getEmail().trim()
                : null;
        if (email != null && staffRepository.existsByEmail(email)) {
            throw new BadRequestException("Email đã được sử dụng bởi một nhân sự khác");
        }

        // Tạo tài khoản với vai trò mặc định là MANAGER
        Account account = Account.builder()
                .username(username)
                .password(passwordEncoder.encode(request.getPassword()))
                .role(AccountRole.MANAGER)
                .status(AccountStatus.ACTIVE)
                .isDeleted(false)
                .build();

        Account savedAccount = accountRepository.save(account);

        // Tạo hồ sơ nhân sự (Staff) liên kết 1-1 với tài khoản vừa tạo
        Staff staff = Staff.builder()
                .firstName(request.getFirstName().trim())
                .lastName(request.getLastName().trim())
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender())
                .phone(phone)
                .email(email)
                .exactAddress(request.getExactAddress() != null ? request.getExactAddress().trim() : null)
                .commune(request.getCommune() != null ? request.getCommune().trim() : null)
                .province(request.getProvince() != null ? request.getProvince().trim() : null)
                .account(savedAccount)
                .createdAt(LocalDateTime.now())
                .build();

        Staff savedStaff = staffRepository.save(staff);

        String token = jwtTokenProvider.generateToken(
                savedAccount.getUsername(),
                savedAccount.getRole().name(),
                savedAccount.getId()
        );

        log.info("Đăng ký thành công tài khoản Quản lý: {} (Account ID: {}, Staff ID: {}, Role: {})",
                savedAccount.getUsername(), savedAccount.getId(), savedStaff.getId(), savedAccount.getRole());

        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getJwtExpirationMs())
                .accountId(savedAccount.getId())
                .username(savedAccount.getUsername())
                .role(savedAccount.getRole())
                .status(savedAccount.getStatus())
                .lastLoginAt(savedAccount.getLastLoginAt())
                .staff(toStaffResponse(savedStaff))
                .build();
    }

    private StaffResponse toStaffResponse(Staff staff) {
        StaffAccountInfo accountInfo = null;
        if (staff.getAccount() != null) {
            Account acc = staff.getAccount();
            accountInfo = StaffAccountInfo.builder()
                    .id(acc.getId())
                    .username(acc.getUsername())
                    .role(acc.getRole())
                    .status(acc.getStatus())
                    .build();
        }

        return StaffResponse.builder()
                .id(staff.getId())
                .firstName(staff.getFirstName())
                .lastName(staff.getLastName())
                .fullName(staff.getFirstName() + " " + staff.getLastName())
                .dateOfBirth(staff.getDateOfBirth())
                .gender(staff.getGender())
                .email(staff.getEmail())
                .phone(staff.getPhone())
                .exactAddress(staff.getExactAddress())
                .commune(staff.getCommune())
                .province(staff.getProvince())
                .account(accountInfo)
                .createdAt(staff.getCreatedAt())
                .build();
    }
}
