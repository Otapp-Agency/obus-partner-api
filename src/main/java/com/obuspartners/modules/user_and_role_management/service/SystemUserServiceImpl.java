package com.obuspartners.modules.user_and_role_management.service;

import com.obuspartners.modules.user_and_role_management.domain.entity.SystemUser;
import com.obuspartners.modules.user_and_role_management.domain.entity.User;
import com.obuspartners.modules.user_and_role_management.domain.dto.CreatePartnerUserRequestDto;
import com.obuspartners.modules.user_and_role_management.domain.dto.CreateAdminUserRequestDto;
import com.obuspartners.modules.user_and_role_management.domain.dto.UpdateSystemUserRequestDto;
import com.obuspartners.modules.user_and_role_management.domain.enums.UserStatus;
import com.obuspartners.modules.user_and_role_management.domain.enums.UserType;
import com.obuspartners.modules.user_and_role_management.repository.SystemUserRepository;
import com.obuspartners.modules.user_and_role_management.repository.UserRepository;
import com.obuspartners.modules.partner_management.repository.PartnerRepository;
import com.obuspartners.modules.partner_management.domain.entity.Partner;
import com.obuspartners.modules.common.util.PasswordHelperService;
import com.obuspartners.modules.common.service.EmailService;
import com.obuspartners.modules.common.exception.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of SystemUserService
 * Handles business logic for system users (ADMIN_USER and PARTNER_USER types)
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Service
@Transactional
public class SystemUserServiceImpl implements SystemUserService {

    @Autowired
    private SystemUserRepository systemUserRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PartnerRepository partnerRepository;

    @Override
    public SystemUser createPartnerUser(CreatePartnerUserRequestDto request) {
        // Validate username uniqueness
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ApiException("Username already exists: " + request.getUsername(), HttpStatus.CONFLICT);
        }

        // Validate email uniqueness
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ApiException("Email already exists: " + request.getEmail(), HttpStatus.CONFLICT);
        }

        // Validate and fetch partner
        Partner partner = partnerRepository.findById(request.getPartnerId())
                .orElseThrow(() -> new ApiException("Partner not found with ID: " + request.getPartnerId(), HttpStatus.NOT_FOUND));

        String generatedPassword = PasswordHelperService.generateStrongPassword();

        // Create User entity
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(generatedPassword));
        user.setDisplayName(request.getDisplayName());
        user.setUserType(UserType.PARTNER_USER);
        user.setPartner(partner); // Associate with partner
        user.setEnabled(true);
        user.setAccountNonExpired(true);
        user.setAccountNonLocked(true);
        user.setCredentialsNonExpired(true);
        user.setRequirePasswordChange(true);
        user.setCreatedAt(LocalDateTime.now());
        user = userRepository.save(user);

        // Create SystemUser entity
        SystemUser systemUser = new SystemUser();
        systemUser.setUser(user);
        systemUser.setFirstName(request.getFirstName());
        systemUser.setLastName(request.getLastName());
        systemUser.setDisplayName(request.getDisplayName());
        systemUser.setPhoneNumber(request.getPhoneNumber());
        systemUser.setPersonalEmail(request.getPersonalEmail());
        systemUser.setEmployeeId(request.getEmployeeId());
        systemUser.setDepartment(request.getDepartment());
        systemUser.setPosition(request.getPosition());
        systemUser.setOfficeLocation(request.getOfficeLocation());
        systemUser.setWorkPhone(request.getWorkPhone());
        systemUser.setWorkEmail(request.getWorkEmail());
        systemUser.setAddress(request.getAddress());
        systemUser.setCity(request.getCity());
        systemUser.setState(request.getState());
        systemUser.setCountry(request.getCountry());
        systemUser.setPostalCode(request.getPostalCode());
        systemUser.setNationalId(request.getNationalId());
        systemUser.setPassportNumber(request.getPassportNumber());
        systemUser.setGender(request.getGender());
        systemUser.setPreferredLanguage(request.getPreferredLanguage());
        systemUser.setTimezone(request.getTimezone());
        systemUser.setEmergencyContactName(request.getEmergencyContactName());
        systemUser.setEmergencyContactPhone(request.getEmergencyContactPhone());
        systemUser.setStatus(UserStatus.ACTIVE);
        systemUser.setEmailVerified(true);
        systemUser.setPhoneVerified(false);
        systemUser.setRegistrationDate(LocalDateTime.now());
        systemUser.setCreatedAt(LocalDateTime.now());

        // Link User and SystemUser
        user.setSystemUser(systemUser);
        systemUser.setUser(user);

        systemUser = systemUserRepository.save(systemUser);

        // Prepare and send user credentials email
        String subject = "Welcome to OTAPP PARTNERS – Your Account Credentials";
        String message = String.format(
                "Hello %s %s,\n\n" +
                        "Your OTAPP PARTNERS account has been successfully created.\n\n" + 
                        "Here are your login credentials:\n" +
                        "- Username: %s\n" +
                        "- Temporary Password: %s\n\n" +
                        "For security reasons, please log in and change your password immediately after your first login.\n\n" +
                        "Login URL: %s\n\n" +
                        "If you did not request this account, please contact our support team.\n\n" +
                        "Regards,\nOTAPP Support Team",
                request.getFirstName(),
                request.getLastName(),
                request.getUsername(),
                generatedPassword,
                "https://otapp.live/login");

        emailService.sendEmail(request.getEmail(), subject, message);

        return systemUser;
    }

    @Override
    public SystemUser createAdminUser(CreateAdminUserRequestDto request) {
        // Validate username uniqueness
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ApiException("Username already exists: " + request.getUsername(), HttpStatus.CONFLICT);
        }

        // Validate email uniqueness
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ApiException("Email already exists: " + request.getEmail(), HttpStatus.CONFLICT);
        }

        String generatedPassword = PasswordHelperService.generateStrongPassword();

        // Create User entity
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(generatedPassword));
        user.setDisplayName(request.getDisplayName());
        user.setUserType(UserType.ADMIN_USER);
        user.setEnabled(true);
        user.setAccountNonExpired(true);
        user.setAccountNonLocked(true);
        user.setCredentialsNonExpired(true);
        user.setRequirePasswordChange(true);
        user.setCreatedAt(LocalDateTime.now());
        user = userRepository.save(user);

        // Create SystemUser entity
        SystemUser systemUser = new SystemUser();
        systemUser.setUser(user);
        systemUser.setFirstName(request.getFirstName());
        systemUser.setLastName(request.getLastName());
        systemUser.setDisplayName(request.getDisplayName());
        systemUser.setPhoneNumber(request.getPhoneNumber());
        systemUser.setPersonalEmail(request.getPersonalEmail());
        systemUser.setEmployeeId(request.getEmployeeId());
        systemUser.setDepartment(request.getDepartment());
        systemUser.setPosition(request.getPosition());
        systemUser.setOfficeLocation(request.getOfficeLocation());
        systemUser.setWorkPhone(request.getWorkPhone());
        systemUser.setWorkEmail(request.getWorkEmail());
        systemUser.setAddress(request.getAddress());
        systemUser.setCity(request.getCity());
        systemUser.setState(request.getState());
        systemUser.setCountry(request.getCountry());
        systemUser.setPostalCode(request.getPostalCode());
        systemUser.setNationalId(request.getNationalId());
        systemUser.setPassportNumber(request.getPassportNumber());
        systemUser.setGender(request.getGender());
        systemUser.setPreferredLanguage(request.getPreferredLanguage());
        systemUser.setTimezone(request.getTimezone());
        systemUser.setEmergencyContactName(request.getEmergencyContactName());
        systemUser.setEmergencyContactPhone(request.getEmergencyContactPhone());
        systemUser.setStatus(UserStatus.ACTIVE);
        systemUser.setEmailVerified(true);
        systemUser.setPhoneVerified(false);
        systemUser.setRegistrationDate(LocalDateTime.now());
        systemUser.setCreatedAt(LocalDateTime.now());

        // Link User and SystemUser
        user.setSystemUser(systemUser);
        systemUser.setUser(user);

        systemUser = systemUserRepository.save(systemUser);

        // Prepare and send user credentials email
        String subject = "Welcome to OTAPP PARTNERS – Your Account Credentials";
        String message = String.format(
                "Hello %s %s,\n\n" +
                        "Your OTAPP PARTNERS account has been successfully created.\n\n" + 
                        "Here are your login credentials:\n" +
                        "- Username: %s\n" +
                        "- Temporary Password: %s\n\n" +
                        "For security reasons, please log in and change your password immediately after your first login.\n\n" +
                        "Login URL: %s\n\n" +
                        "If you did not request this account, please contact our support team.\n\n" +
                        "Regards,\nOTAPP Support Team",
                request.getFirstName(),
                request.getLastName(),
                request.getUsername(),
                generatedPassword,
                "https://otapp.live/login");

        emailService.sendEmail(request.getEmail(), subject, message);

        return systemUser;
    }

    @Override
    public Optional<SystemUser> findById(Long id) {
        return systemUserRepository.findById(id);
    }

    @Override
    public Optional<SystemUser> findByUsername(String username) {
        return systemUserRepository.findByUserUsername(username);
    }

    @Override
    public Optional<SystemUser> findByUid(String uid) {
        return systemUserRepository.findByUidWithUserAndPartner(uid);
    }

    @Override
    public Page<SystemUser> getAllSystemUsers(Pageable pageable) {
        return systemUserRepository.findAllWithUserAndPartner(pageable);
    }

    @Override
    public Optional<SystemUser> findByUserId(Long userId) {
        return systemUserRepository.findByUserId(userId);
    }

    @Override
    public List<SystemUser> findByUserType(UserType userType) {
        return systemUserRepository.findByUserUserType(userType);
    }

    @Override
    public List<SystemUser> findByStatus(UserStatus status) {
        return systemUserRepository.findByStatus(status);
    }

    @Override
    public List<SystemUser> findByDepartment(String department) {
        return systemUserRepository.findByDepartment(department);
    }

    @Override
    public SystemUser updateSystemUser(Long id, UpdateSystemUserRequestDto request) {
        SystemUser systemUser = systemUserRepository.findById(id)
                .orElseThrow(() -> new ApiException("System user not found with ID: " + id, HttpStatus.NOT_FOUND));

        // Prevent updating root admin user
        if ("ROOT001".equals(systemUser.getEmployeeId())) {
            throw new ApiException("Cannot update root admin user", HttpStatus.FORBIDDEN);
        }

        // Update SystemUser fields
        if (request.getFirstName() != null) {
            systemUser.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            systemUser.setLastName(request.getLastName());
        }
        if (request.getDisplayName() != null) {
            systemUser.setDisplayName(request.getDisplayName());
            // Also update the User entity displayName
            if (systemUser.getUser() != null) {
                systemUser.getUser().setDisplayName(request.getDisplayName());
            }
        }
        if (request.getPhoneNumber() != null) {
            systemUser.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getPersonalEmail() != null) {
            systemUser.setPersonalEmail(request.getPersonalEmail());
        }
        if (request.getEmployeeId() != null) {
            systemUser.setEmployeeId(request.getEmployeeId());
        }
        if (request.getDepartment() != null) {
            systemUser.setDepartment(request.getDepartment());
        }
        if (request.getPosition() != null) {
            systemUser.setPosition(request.getPosition());
        }
        if (request.getOfficeLocation() != null) {
            systemUser.setOfficeLocation(request.getOfficeLocation());
        }
        if (request.getWorkPhone() != null) {
            systemUser.setWorkPhone(request.getWorkPhone());
        }
        if (request.getWorkEmail() != null) {
            systemUser.setWorkEmail(request.getWorkEmail());
        }
        if (request.getAddress() != null) {
            systemUser.setAddress(request.getAddress());
        }
        if (request.getCity() != null) {
            systemUser.setCity(request.getCity());
        }
        if (request.getState() != null) {
            systemUser.setState(request.getState());
        }
        if (request.getCountry() != null) {
            systemUser.setCountry(request.getCountry());
        }
        if (request.getPostalCode() != null) {
            systemUser.setPostalCode(request.getPostalCode());
        }
        if (request.getNationalId() != null) {
            systemUser.setNationalId(request.getNationalId());
        }
        if (request.getPassportNumber() != null) {
            systemUser.setPassportNumber(request.getPassportNumber());
        }
        if (request.getGender() != null) {
            systemUser.setGender(request.getGender());
        }
        if (request.getPreferredLanguage() != null) {
            systemUser.setPreferredLanguage(request.getPreferredLanguage());
        }
        if (request.getTimezone() != null) {
            systemUser.setTimezone(request.getTimezone());
        }
        if (request.getEmergencyContactName() != null) {
            systemUser.setEmergencyContactName(request.getEmergencyContactName());
        }
        if (request.getEmergencyContactPhone() != null) {
            systemUser.setEmergencyContactPhone(request.getEmergencyContactPhone());
        }

        return systemUserRepository.save(systemUser);
    }

    @Override
    public SystemUser updateSystemUserByUid(String uid, UpdateSystemUserRequestDto request) {
        SystemUser systemUser = systemUserRepository.findByUid(uid)
                .orElseThrow(() -> new ApiException("System user not found with UID: " + uid, HttpStatus.NOT_FOUND));

        // Prevent updating root admin user
        if ("ROOT001".equals(systemUser.getEmployeeId())) {
            throw new ApiException("Cannot update root admin user", HttpStatus.FORBIDDEN);
        }

        // Update SystemUser fields
        if (request.getFirstName() != null) {
            systemUser.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            systemUser.setLastName(request.getLastName());
        }
        if (request.getDisplayName() != null) {
            systemUser.setDisplayName(request.getDisplayName());
            // Also update the User entity displayName
            if (systemUser.getUser() != null) {
                systemUser.getUser().setDisplayName(request.getDisplayName());
            }
        }
        if (request.getPhoneNumber() != null) {
            systemUser.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getPersonalEmail() != null) {
            systemUser.setPersonalEmail(request.getPersonalEmail());
        }
        if (request.getEmployeeId() != null) {
            systemUser.setEmployeeId(request.getEmployeeId());
        }
        if (request.getDepartment() != null) {
            systemUser.setDepartment(request.getDepartment());
        }
        if (request.getPosition() != null) {
            systemUser.setPosition(request.getPosition());
        }
        if (request.getOfficeLocation() != null) {
            systemUser.setOfficeLocation(request.getOfficeLocation());
        }
        if (request.getWorkPhone() != null) {
            systemUser.setWorkPhone(request.getWorkPhone());
        }
        if (request.getWorkEmail() != null) {
            systemUser.setWorkEmail(request.getWorkEmail());
        }
        if (request.getAddress() != null) {
            systemUser.setAddress(request.getAddress());
        }
        if (request.getCity() != null) {
            systemUser.setCity(request.getCity());
        }
        if (request.getState() != null) {
            systemUser.setState(request.getState());
        }
        if (request.getCountry() != null) {
            systemUser.setCountry(request.getCountry());
        }
        if (request.getPostalCode() != null) {
            systemUser.setPostalCode(request.getPostalCode());
        }
        if (request.getNationalId() != null) {
            systemUser.setNationalId(request.getNationalId());
        }
        if (request.getPassportNumber() != null) {
            systemUser.setPassportNumber(request.getPassportNumber());
        }
        if (request.getGender() != null) {
            systemUser.setGender(request.getGender());
        }
        if (request.getPreferredLanguage() != null) {
            systemUser.setPreferredLanguage(request.getPreferredLanguage());
        }
        if (request.getTimezone() != null) {
            systemUser.setTimezone(request.getTimezone());
        }
        if (request.getEmergencyContactName() != null) {
            systemUser.setEmergencyContactName(request.getEmergencyContactName());
        }
        if (request.getEmergencyContactPhone() != null) {
            systemUser.setEmergencyContactPhone(request.getEmergencyContactPhone());
        }

        return systemUserRepository.save(systemUser);
    }

    @Override
    public SystemUser activate(Long id) {
        SystemUser systemUser = systemUserRepository.findById(id)
                .orElseThrow(() -> new ApiException("System user not found with ID: " + id, HttpStatus.NOT_FOUND));
        systemUser.setStatus(UserStatus.ACTIVE);
        return systemUserRepository.save(systemUser);
    }

    @Override
    public SystemUser activateByUid(String uid) {
        SystemUser systemUser = systemUserRepository.findByUid(uid)
                .orElseThrow(() -> new ApiException("System user not found with UID: " + uid, HttpStatus.NOT_FOUND));
        systemUser.setStatus(UserStatus.ACTIVE);
        return systemUserRepository.save(systemUser);
    }

    @Override
    public SystemUser deactivate(Long id) {
        SystemUser systemUser = systemUserRepository.findById(id)
                .orElseThrow(() -> new ApiException("System user not found with ID: " + id, HttpStatus.NOT_FOUND));
        systemUser.setStatus(UserStatus.INACTIVE);
        return systemUserRepository.save(systemUser);
    }

    @Override
    public SystemUser deactivateByUid(String uid) {
        SystemUser systemUser = systemUserRepository.findByUid(uid)
                .orElseThrow(() -> new ApiException("System user not found with UID: " + uid, HttpStatus.NOT_FOUND));
        
        // Prevent deactivating root admin user
        if ("ROOT001".equals(systemUser.getEmployeeId())) {
            throw new ApiException("Cannot deactivate root admin user", HttpStatus.FORBIDDEN);
        }
        
        systemUser.setStatus(UserStatus.INACTIVE);
        return systemUserRepository.save(systemUser);
    }

    @Override
    public SystemUser suspend(Long id) {
        SystemUser systemUser = systemUserRepository.findById(id)
                .orElseThrow(() -> new ApiException("System user not found with ID: " + id, HttpStatus.NOT_FOUND));
        systemUser.setStatus(UserStatus.SUSPENDED);
        return systemUserRepository.save(systemUser);
    }

    @Override
    public SystemUser suspendByUid(String uid) {
        SystemUser systemUser = systemUserRepository.findByUid(uid)
                .orElseThrow(() -> new ApiException("System user not found with UID: " + uid, HttpStatus.NOT_FOUND));
        
        // Prevent suspending root admin user
        if ("ROOT001".equals(systemUser.getEmployeeId())) {
            throw new ApiException("Cannot suspend root admin user", HttpStatus.FORBIDDEN);
        }
        
        systemUser.setStatus(UserStatus.SUSPENDED);
        return systemUserRepository.save(systemUser);
    }

    @Override
    public SystemUser verifyEmail(Long id) {
        SystemUser systemUser = systemUserRepository.findById(id)
                .orElseThrow(() -> new ApiException("System user not found with ID: " + id, HttpStatus.NOT_FOUND));
        systemUser.setEmailVerified(true);
        return systemUserRepository.save(systemUser);
    }

    @Override
    public SystemUser verifyEmailByUid(String uid) {
        SystemUser systemUser = systemUserRepository.findByUid(uid)
                .orElseThrow(() -> new ApiException("System user not found with UID: " + uid, HttpStatus.NOT_FOUND));
        systemUser.setEmailVerified(true);
        return systemUserRepository.save(systemUser);
    }

    @Override
    public SystemUser verifyPhone(Long id) {
        SystemUser systemUser = systemUserRepository.findById(id)
                .orElseThrow(() -> new ApiException("System user not found with ID: " + id, HttpStatus.NOT_FOUND));
        systemUser.setPhoneVerified(true);
        return systemUserRepository.save(systemUser);
    }

    @Override
    public SystemUser verifyPhoneByUid(String uid) {
        SystemUser systemUser = systemUserRepository.findByUid(uid)
                .orElseThrow(() -> new ApiException("System user not found with UID: " + uid, HttpStatus.NOT_FOUND));
        systemUser.setPhoneVerified(true);
        return systemUserRepository.save(systemUser);
    }

    @Override
    public SystemUser updateLastLogin(Long id) {
        SystemUser systemUser = systemUserRepository.findById(id)
                .orElseThrow(() -> new ApiException("System user not found with ID: " + id, HttpStatus.NOT_FOUND));
        systemUser.setLastLoginDate(LocalDateTime.now());
        return systemUserRepository.save(systemUser);
    }

    @Override
    public SystemUser updateLastLoginByUid(String uid) {
        SystemUser systemUser = systemUserRepository.findByUid(uid)
                .orElseThrow(() -> new ApiException("System user not found with UID: " + uid, HttpStatus.NOT_FOUND));
        systemUser.setLastLoginDate(LocalDateTime.now());
        return systemUserRepository.save(systemUser);
    }


    @Override
    public void deleteByUid(String uid) {
        SystemUser systemUser = systemUserRepository.findByUid(uid)
                .orElseThrow(() -> new ApiException("System user not found with UID: " + uid, HttpStatus.NOT_FOUND));
        
        // Prevent deleting root admin user
        if ("ROOT001".equals(systemUser.getEmployeeId())) {
            throw new ApiException("Cannot delete root admin user", HttpStatus.FORBIDDEN);
        }
        
        systemUserRepository.deleteById(systemUser.getId());
    }


    @Override
    public boolean existsByUid(String uid) {
        return systemUserRepository.existsByUid(uid);
    }

    @Override
    public long count() {
        return systemUserRepository.count();
    }

    @Override
    public long countByUserType(UserType userType) {
        return systemUserRepository.countByUserUserType(userType);
    }

    @Override
    public long countByStatus(UserStatus status) {
        return systemUserRepository.countByStatus(status);
    }

    @Override
    public Optional<SystemUser> findByEmployeeId(String employeeId) {
        return systemUserRepository.findByEmployeeId(employeeId);
    }

    @Override
    public Optional<SystemUser> findByEmail(String email) {
        return systemUserRepository.findByPersonalEmailOrWorkEmail(email, email);
    }

    @Override
    public SystemUser updateStatus(Long id, UserStatus status) {
        SystemUser systemUser = systemUserRepository.findById(id)
                .orElseThrow(() -> new ApiException("System user not found with ID: " + id, HttpStatus.NOT_FOUND));
        systemUser.setStatus(status);
        return systemUserRepository.save(systemUser);
    }

    @Override
    public SystemUser updateStatusByUid(String uid, UserStatus status) {
        SystemUser systemUser = systemUserRepository.findByUid(uid)
                .orElseThrow(() -> new ApiException("System user not found with UID: " + uid, HttpStatus.NOT_FOUND));
        systemUser.setStatus(status);
        return systemUserRepository.save(systemUser);
    }

    @Override
    public SystemUser updatePasswordChangedDate(Long id) {
        SystemUser systemUser = systemUserRepository.findById(id)
                .orElseThrow(() -> new ApiException("System user not found with ID: " + id, HttpStatus.NOT_FOUND));
        systemUser.setPasswordChangedDate(LocalDateTime.now());
        return systemUserRepository.save(systemUser);
    }

    @Override
    public SystemUser updatePasswordChangedDateByUid(String uid) {
        SystemUser systemUser = systemUserRepository.findByUid(uid)
                .orElseThrow(() -> new ApiException("System user not found with UID: " + uid, HttpStatus.NOT_FOUND));
        systemUser.setPasswordChangedDate(LocalDateTime.now());
        return systemUserRepository.save(systemUser);
    }

    @Override
    public boolean existsByPersonalEmail(String personalEmail) {
        return systemUserRepository.existsByPersonalEmail(personalEmail);
    }

    @Override
    public boolean existsByWorkEmail(String workEmail) {
        return systemUserRepository.existsByWorkEmail(workEmail);
    }

    @Override
    public boolean existsByEmployeeId(String employeeId) {
        return systemUserRepository.existsByEmployeeId(employeeId);
    }

    @Override
    public long countByDepartment(String department) {
        return systemUserRepository.countByDepartment(department);
    }

    @Override
    public void deleteById(Long id) {
        if (!systemUserRepository.existsById(id)) {
            throw new ApiException("System user not found with ID: " + id, HttpStatus.NOT_FOUND);
        }
        systemUserRepository.deleteById(id);
    }

    @Override
    public void delete(SystemUser systemUser) {
        systemUserRepository.delete(systemUser);
    }
}
