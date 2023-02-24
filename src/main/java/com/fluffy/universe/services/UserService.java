package com.fluffy.universe.services;

import com.fluffy.universe.exceptions.HttpException;
import com.fluffy.universe.models.User;
import com.fluffy.universe.utils.Configuration;
import com.fluffy.universe.utils.DataSource;
import io.javalin.http.HttpCode;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.sql2o.Connection;

import java.sql.SQLException;

public final class UserService {
    private static final int BCRYPT_STRENGTH = Configuration.getAsClass("application.bcryptStrength", Integer.class);
    private static final String INSERT_USER_SQL = "INSERT INTO User "
            + "(RoleID, FirstName, LastName, Email, Password, Gender, Birthday, Address, Website, ResetPasswordToken) VALUES "
            + "(:roleId, :firstName, :lastName, :email, :password, :gender, :birthday, :address, :website, :resetPasswordToken)";
    private static final String UPDATE_USER_SQL = "UPDATE User SET "
            + "RoleID = :roleId,"
            + "FirstName = :firstName, "
            + "LastName = :lastName, "
            + "Password = :password, "
            + "Birthday = :birthday, "
            + "Address = :address, "
            + "Website = :website, "
            + "ResetPasswordToken = :resetPasswordToken";
    private static final String USER_BY_EMAIL_SQL = "SELECT * FROM User WHERE Email = :email";
    private static final String USER_BY_ID_SQL = "SELECT * FROM User WHERE ID = :id";
    private static final String USER_BY_RESET_PASSWORD_TOKEN = "SELECT * FROM User WHERE ResetPasswordToken = :resetPasswordToken";
    private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(BCRYPT_STRENGTH);

    public static User getUserByEmail(String email) {
        try (Connection connection = DataSource.getConnection()) {
            return connection.createQuery(USER_BY_EMAIL_SQL)
                    .addParameter("email", email)
                    .executeAndFetchFirst(User.class);
        } catch (SQLException throwables) {
            throw new HttpException(HttpCode.INTERNAL_SERVER_ERROR.getStatus(), "Problem with database connection: " + throwables);
        }
    }

    public static User getUserById(int id) {
        try (Connection connection = DataSource.getConnection()) {
            return connection.createQuery(USER_BY_ID_SQL)
                    .addParameter("id", id)
                    .executeAndFetchFirst(User.class);
        } catch (SQLException throwables) {
            throw new HttpException(HttpCode.INTERNAL_SERVER_ERROR.getStatus(), "Problem with database connection: " + throwables);
        }
    }

    public static User getUserByResetPasswordToken(String resetPasswordToken) {
        try (Connection connection = DataSource.getConnection()) {
            return connection.createQuery(USER_BY_RESET_PASSWORD_TOKEN)
                    .addParameter("resetPasswordToken", resetPasswordToken)
                    .executeAndFetchFirst(User.class);
        } catch (SQLException throwables) {
            throw new HttpException(HttpCode.INTERNAL_SERVER_ERROR.getStatus(), "Problem with database connection: " + throwables);
        }
    }

    public static void saveUser(User user) {
        try (Connection connection = DataSource.getConnection()) {
            String query = user.getId() == null ? INSERT_USER_SQL : UPDATE_USER_SQL;
            int id = connection.createQuery(query).bind(user).executeUpdate().getKey(Integer.class);
            user.setId(id);
        } catch (SQLException throwables) {
            throw new HttpException(HttpCode.INTERNAL_SERVER_ERROR.getStatus(), "Problem with database connection: " + throwables);
        }
    }

    public static String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }

    public static boolean isCorrectPassword(String password, String encodedPassword) {
        return passwordEncoder.matches(password, encodedPassword);
    }
}
