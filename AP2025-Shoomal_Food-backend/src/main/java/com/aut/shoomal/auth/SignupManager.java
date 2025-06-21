package com.aut.shoomal.auth;
import com.aut.shoomal.entity.user.*;
import com.aut.shoomal.entity.user.access.Role;
import com.aut.shoomal.entity.user.access.RoleManager;
import com.aut.shoomal.exceptions.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class SignupManager
{
    private final UserManager userManager;
    private final RoleManager roleManager;
    public SignupManager(UserManager userManager, RoleManager roleManager)
    {
        this.userManager = userManager;
        this.roleManager = roleManager;
    }

    public User handleSignup(UserTypes types, String name, String phoneNumber, String password, String email,
                             String bankName, String accountNumber, String address, String profileImageBase64)
    {
        StringBuilder errors = new StringBuilder();
        if (!this.checkName(name))
            errors.append(" 'full_name' is required.");

        if (!(types == UserTypes.ADMIN && "admin".equalsIgnoreCase(phoneNumber))) {
            if (phoneNumber == null || phoneNumber.trim().isEmpty())
                errors.append(" 'phone' is required.");
            else if (!isValidPhoneNumber(phoneNumber))
                errors.append(" Invalid 'phone' format.");
        }

        if (password == null || password.trim().isEmpty())
            errors.append(" 'password' is required.");
        if (types == null)
            errors.append(" 'role' is required.");
        if (address == null || address.trim().isEmpty())
            errors.append(" 'address' is required.");

        if (email != null && !email.trim().isEmpty())
            if (!checkEmailValidation(email))
                errors.append(" Invalid 'email' format.");

        if (bankName != null && !bankName.trim().isEmpty())
        {
            if (accountNumber == null || accountNumber.trim().isEmpty())
                errors.append(" 'account_number' is required if 'bank_name' is provided.");
        }
        else if (accountNumber != null && !accountNumber.trim().isEmpty())
            errors.append(" 'bank_name' is required if 'account_number' is provided.");

        if (!errors.isEmpty())
            throw new InvalidInputException("400 Invalid input:" + errors);

        try {
            userManager.getUserByPhoneNumber(phoneNumber);
            throw new DuplicateUserException("409 Phone Number Already Exists");
        } catch (NotFoundException ignored) {}

        if (email != null && !email.trim().isEmpty())
        {
            try {
                userManager.getUserByEmail(email);
                throw new DuplicateUserException("409 Email Already Exists");
            } catch (NotFoundException ignored) {}
        }

        try {
            userManager.getUserByName(name);
            throw new DuplicateUserException("409 Name Already Exists");
        } catch (NotFoundException ignored) {}

        Role userRole = this.getRole(types);
        if (userRole == null)
            throw new NotFoundException("500 Internal Server Error: Role not found");
        String salt = PasswordHasher.generateSalt();
        if (salt == null)
            throw new ServiceUnavailableException("500 Internal Server Error: failed to generate salt");
        String hashedPassword = PasswordHasher.hashPassword(password, salt);
        if (hashedPassword == null)
            throw new ServiceUnavailableException("500 Internal Server Error: failed to hash password");

        User user = UserCreator.createUser(types, name, phoneNumber, hashedPassword, email, userRole);
        if (bankName != null && !bankName.trim().isEmpty() &&
                accountNumber != null && !accountNumber.trim().isEmpty())
        {
            BankInfo bank = new BankInfo(bankName, accountNumber);
            user.setBank(bank);
        }
        user.setSalt(salt);
        user.setAddress(address);
        user.setProfileImageBase64(profileImageBase64);

        try {
            userManager.addUser(user);
            return user;
        } catch (Exception e) {
            throw new ServiceUnavailableException("500 Internal Server Error: Failed to save user to database.");
        }
    }

    private Role getRole(UserTypes type)
    {
        return switch (type)
        {
            case ADMIN -> roleManager.findRoleByName("Admin");
            case BUYER -> roleManager.findRoleByName("Buyer");
            case SELLER -> roleManager.findRoleByName("Seller");
            case COURIER -> roleManager.findRoleByName("Courier");
        };
    }

    public static boolean checkEmailValidation(String email)
    {
        return email.matches("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$");
    }

    public static boolean isValidPhoneNumber(String phoneNumber)
    {
        return phoneNumber.matches("^09\\d{9}$");
    }

    private boolean checkName(String name)
    {
        return name != null && !name.trim().isEmpty();
    }

    public static String hashPassword(String password, String salt)
    {
        return PasswordHasher.hashPassword(password, salt);
    }

    public static boolean verifyPassword(String password, String hashedPassword, String salt)
    {
        return PasswordHasher.verifyPassword(password, hashedPassword, salt);
    }

    private static class PasswordHasher
    {
        private static final String SALT_GENERATION_ALGORITHM = "SHA1PRNG";
        private static final int SALT_LENGTH_BYTES = 16; // 128 bits
        private static final String HASHING_ALGORITHM = "SHA-256";

        /**
         * Generates a random salt.
         *
         * @return A Base64 encoded random salt.
         */
        public static String generateSalt()
        {
            try {
                SecureRandom random = SecureRandom.getInstance(SALT_GENERATION_ALGORITHM);
                byte[] salt = new byte[SALT_LENGTH_BYTES];
                random.nextBytes(salt);
                return Base64.getEncoder().encodeToString(salt);
            } catch (NoSuchAlgorithmException e) {
                System.err.println("Error generating Salt: " + e.getMessage());
                e.printStackTrace();
                return null;
            }
        }

        /**
         * Hashes a password using a provided salt.
         *
         * @param password The password to hash.
         * @param salt The Base64 encoded salt to use.
         * @return A Base64 encoded hash of the password combined with the salt,
         * or null if an error occurs.
         */
        public static String hashPassword(String password, String salt)
        {
            try {
                MessageDigest digest = MessageDigest.getInstance(HASHING_ALGORITHM);
                byte[] saltBytes = Base64.getDecoder().decode(salt);
                digest.update(saltBytes);
                byte[] passwordBytes = password.getBytes(StandardCharsets.UTF_8);
                byte[] hashedBytes = digest.digest(passwordBytes);
                return Base64.getEncoder().encodeToString(hashedBytes);
            } catch (NoSuchAlgorithmException e) {
                System.err.println("Error hashing password: " + e.getMessage());
                e.printStackTrace();
                return null;
            }
        }

        /**
         * Verifies if a given password matches the stored hash and salt.
         *
         * @param password The password to verify.
         * @param storedHash The Base64 encoded stored hash.
         * @param storedSalt The Base64 encoded stored salt.
         * @return {@code true} if the password matches, {@code false} otherwise.
         */
        public static boolean verifyPassword(String password, String storedHash, String storedSalt)
        {
            String hashedPassword = hashPassword(password, storedSalt);
            return hashedPassword != null && hashedPassword.equals(storedHash);
        }
    }

    public void ensureAdminUserExists() {
        try {
            userManager.getUserByName("admin");
            throw new ConflictException("Admin user already exists.");
        } catch (NotFoundException e) {
            System.out.println("Admin user not found. Creating default admin user...");
            try {
                handleSignup(
                        UserTypes.ADMIN,
                        "admin",
                        "admin",
                        "admin",
                        "admin@example.com",
                        null,
                        null,
                        "Admin HQ",
                        null
                );
                System.out.println("Default admin user created successfully.");
            } catch (Exception ex) {
                System.err.println("Failed to create default admin user: " + ex.getMessage());
                ex.printStackTrace();
            }
        } catch (ConflictException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            System.err.println("Error checking for admin user existence: " + e.getMessage());
            e.printStackTrace();
        }
    }
}